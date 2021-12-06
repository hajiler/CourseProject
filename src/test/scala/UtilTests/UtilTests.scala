package UtilTests

import org.apache.hadoop.io.{IntWritable, Text}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import HelperUtils.Utils.{createEmailRequest, extractErrorLogs, getEmailBodyFromLogs, getLogsFromFileEvent, summarizeErrorLogs, summarizeErrorLogs}
import org.apache.hadoop.shaded.org.eclipse.jetty.websocket.common.frames.DataFrame

import scala.collection.JavaConverters._

class UtilTests extends AnyFlatSpec with Matchers {
  behavior of "Map Reduce functions"
  val spark = SparkSession.builder
    .appName("StructuredNetworkWordCount")
    .config("spark.master", "local")
    .getOrCreate()
  import spark.implicits._

  val logs = Seq("16:28:14.370 [scala-execution-context-global-123] WARN  HelperUtils.Parameters$ - x2oBSI0%CdfV2%ChSsnZ7vJo=2qJqZ%.kbc!0ne`y&m",
  "16:28:20.406 [scala-execution-context-global-123] ERROR  HelperUtils.Parameters$ - CC]>~R#,^#0JWyESarZdETDcvk)Yk'I?1",
  "16:28:44.389 [scala-execution-context-global-123] INFO HelperUtils.Parameters$ - ihu}!A2]*07}|,lc",
  "16:28:44.389 [scala-execution-context-global-123] DEBUG HelperUtils.Parameters$ - ihu}!A2]*07}|,lc",
  "16:28:34.406 [scala-execution-context-global-123] ERROR  HelperUtils.Parameters$ - CC]>~R#,^#0JWyESarZdETDcvk)Yk'I?2",
  "16:28:38.406 [scala-execution-context-global-123] ERROR  HelperUtils.Parameters$ - CC]>~R#,^#0JWyESarZdETDcvk)Yk'I?3",
  "16:29:44.406 [scala-execution-context-global-123] ERROR  HelperUtils.Parameters$ - CC]>~R#,^#0JWyESarZdETDcvk)Yk'I?4")

  it should "Read correct logs from a list of events" in {
    val actual = getLogsFromFileEvent(List("testLogFile.txt"))
    actual
      .zip(logs)
      .foreach(pair => pair._1 shouldBe pair._2)
  }

  it should "Filter out a set of Logs for only ERROR logs in a time bucket" in {
    val df = spark.createDataset(logs)
    val actual = extractErrorLogs(df, spark).collect().toList
    val expected = List(("16:28:2-29","16:28:20.406 [scala-execution-context-global-123] ERROR  HelperUtils.Parameters$ - CC]>~R#,^#0JWyESarZdETDcvk)Yk'I?1"),
                         ("16:28:3-39","16:28:34.406 [scala-execution-context-global-123] ERROR  HelperUtils.Parameters$ - CC]>~R#,^#0JWyESarZdETDcvk)Yk'I?2"),
                         ("16:28:3-39","16:28:38.406 [scala-execution-context-global-123] ERROR  HelperUtils.Parameters$ - CC]>~R#,^#0JWyESarZdETDcvk)Yk'I?3"),
                         ("16:29:4-49","16:29:44.406 [scala-execution-context-global-123] ERROR  HelperUtils.Parameters$ - CC]>~R#,^#0JWyESarZdETDcvk)Yk'I?4"))
    actual.zip(expected).foreach( pair => pair._1 shouldBe pair._2)
  }

  it should "Group logs in the same time bucket" in {
    val df = spark.createDataset(List(("16:28:2-29","16:28:20.406 [scala-execution-context-global-123] ERROR  HelperUtils.Parameters$ - CC]>~R#,^#0JWyESarZdETDcvk)Yk'I?1"),
      ("16:28:3-39","16:28:34.406 [scala-execution-context-global-123] ERROR  HelperUtils.Parameters$ - CC]>~R#,^#0JWyESarZdETDcvk)Yk'I?2"),
      ("16:28:3-39","16:28:38.406 [scala-execution-context-global-123] ERROR  HelperUtils.Parameters$ - CC]>~R#,^#0JWyESarZdETDcvk)Yk'I?3"),
      ("16:29:4-49","16:29:44.406 [scala-execution-context-global-123] ERROR  HelperUtils.Parameters$ - CC]>~R#,^#0JWyESarZdETDcvk)Yk'I?4")))
    val expected = List("16:29:4-49 ENDTIMEBUCKET 16:29:44.406 [scala-execution-context-global-123] ERROR  HelperUtils.Parameters$ - CC]>~R#,^#0JWyESarZdETDcvk)Yk'I?4",
      "16:28:2-29 ENDTIMEBUCKET 16:28:20.406 [scala-execution-context-global-123] ERROR  HelperUtils.Parameters$ - CC]>~R#,^#0JWyESarZdETDcvk)Yk'I?1",
      "16:28:3-39 ENDTIMEBUCKET 16:28:34.406 [scala-execution-context-global-123] ERROR  HelperUtils.Parameters$ - CC]>~R#,^#0JWyESarZdETDcvk)Yk'I?2 ENDLOG 16:28:38.406 [scala-execution-context-global-123] ERROR  HelperUtils.Parameters$ - CC]>~R#,^#0JWyESarZdETDcvk)Yk'I?3")
    summarizeErrorLogs(df, spark).collect()
      .toList
      .zip(expected)
      .foreach(pair => pair._1 shouldBe pair._2)
  }

  it should "Create a correct HTML email body from log data" in {
    val data = "16:28:3-39 ENDTIMEBUCKET 16:28:34.406 [scala-execution-context-global-123] ERROR  HelperUtils.Parameters$ - CC]>~R#,^#0JWyESarZdETDcvk)Yk'I?2 ENDLOG 16:28:38.406 [scala-execution-context-global-123] ERROR  HelperUtils.Parameters$ - CC]>~R#,^#0JWyESarZdETDcvk)Yk'I?3"
    val actual = getEmailBodyFromLogs(data)
    val expected = "<h3>2 ERROR logs at 16:28:3-39</h3><p>16:28:34.406 [scala-execution-context-global-123] ERROR  HelperUtils.Parameters$ - CC]>~R#,^#0JWyESarZdETDcvk)Yk'I?2</p><p>16:28:38.406 [scala-execution-context-global-123] ERROR  HelperUtils.Parameters$ - CC]>~R#,^#0JWyESarZdETDcvk)Yk'I?3</p>"
    actual shouldBe expected
  }

  it should "Create the correct email request from log data" in {
    val fromEmail = "erodri90@uic.edu"
    val toEmail = "erodri90@uic.edu"
    val subject = s"ERROR LOG UPDATE"
    val data = "16:28:3-39 ENDTIMEBUCKET 16:28:34.406 [scala-execution-context-global-123] ERROR  HelperUtils.Parameters$ - CC]>~R#,^#0JWyESarZdETDcvk)Yk'I?2 ENDLOG 16:28:38.406 [scala-execution-context-global-123] ERROR  HelperUtils.Parameters$ - CC]>~R#,^#0JWyESarZdETDcvk)Yk'I?3"
    val expectedMessage = "<h3>2 ERROR logs at 16:28:3-39</h3><p>16:28:34.406 [scala-execution-context-global-123] ERROR  HelperUtils.Parameters$ - CC]>~R#,^#0JWyESarZdETDcvk)Yk'I?2</p><p>16:28:38.406 [scala-execution-context-global-123] ERROR  HelperUtils.Parameters$ - CC]>~R#,^#0JWyESarZdETDcvk)Yk'I?3</p>"
    val actual = createEmailRequest(data)
    actual.getSource shouldBe fromEmail
    actual.getDestination.getToAddresses.get(0) shouldBe toEmail
    actual.getMessage.getSubject.getData shouldBe subject
    actual.getMessage.getBody.getHtml.getData shouldBe expectedMessage
  }
}