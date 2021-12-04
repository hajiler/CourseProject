package UtilTests

import org.apache.hadoop.io.{IntWritable, Text}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import HelperUtils.Utils.{extractErrorLogs, summarizeErrorLogs, summarizeErrorLogs2}
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
    val df = spark.createDataset(logs)
    val errorLogs = extractErrorLogs(df, spark)
    val actual = summarizeErrorLogs(errorLogs, spark)
    val expected = List("16:28:20.406 [scala-execution-context-global-123] ERROR  HelperUtils.Parameters$ - CC]>~R#,^#0JWyESarZdETDcvk)Yk'I?1",
      "16:28:34.406 [scala-execution-context-global-123] ERROR  HelperUtils.Parameters$ - CC]>~R#,^#0JWyESarZdETDcvk)Yk'I?2 || 16:28:38.406 [scala-execution-context-global-123] ERROR  HelperUtils.Parameters$ - CC]>~R#,^#0JWyESarZdETDcvk)Yk'I?3",
      "16:29:44.406 [scala-execution-context-global-123] ERROR  HelperUtils.Parameters$ - CC]>~R#,^#0JWyESarZdETDcvk)Yk'I?4")
    summarizeErrorLogs2(errorLogs, spark)
  }
}