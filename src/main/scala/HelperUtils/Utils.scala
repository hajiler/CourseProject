package HelperUtils

import org.apache.spark.sql.functions.{col, collect_list, lit}

import scala.io.Source
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import com.amazonaws.services.simpleemail.{AmazonSimpleEmailService, AmazonSimpleEmailServiceClientBuilder}
import com.amazonaws.services.simpleemail.model.{Body, Message, Content, Destination, SendEmailRequest}
import scala.collection.mutable

object Utils {
  def getLogsFromFileEvent(fileEvent: List[String]): List[String] = {
    val logs = fileEvent.flatMap(fileName => {
      try {
        val fileReader = Source.fromFile(fileName)
        val lines = fileReader.getLines
          .toList
        fileReader.close()
        lines
      } catch {
        case exception: Exception => List()
      }
    })
//      .reduce((files, file1) => files + file1)
    logs
  }

  // Given the a log timestamp, return the 10 second time interval of where the log occurred.
  def hashTimeString(logTime: String): String = {
    val times = logTime.split(":")
    val second = (times(2).toDouble / 10).toInt.toString
    val interval = s"${times(0)}:${times(1)}:${second}-${second}9"
    interval
  }

  def extractErrorLogs(data: Dataset[String], spark: SparkSession) : Dataset[(String, String)] = {
    import spark.implicits._
    data.map(_.split(" "))
      .filter(_(2).equals("ERROR"))
      .map(parsedErrorLog => {
        val timeBucket = hashTimeString(parsedErrorLog(0))
        val log = parsedErrorLog.reduce((tokens, token) => tokens.concat(" " + token))
        (timeBucket, log)
      })
  }

  def summarizeErrorLogs(data: Dataset[(String, String)], spark: SparkSession) : Dataset[String] = {
    val a : mutable.Map[String, String] = mutable.Map()
    import spark.implicits._
    data.printSchema()
    data
      .collect()
      .foreach(value => {
        val timeBucket = value._1
        val log = value._2
        val newValue = a.getOrElse(timeBucket, "") + " ENDLOG " + log
        a += (timeBucket -> newValue)
      })
    val values = a.values.toSeq

    spark.createDataset(values)
  }

  def summarizeErrorLogs2(data: Dataset[(String, String)], spark: SparkSession): Dataset[String]  = {
    import spark.implicits._
    data.groupBy(col("_1"))
      .agg(collect_list("_2"))
      .map(row => {
        val bucket = row.getString(0)
        val logs = row.getList(1).toArray()
          .reduce((acc, log) => acc  + " ENDLOG " + log)
        bucket +  " ENDTIMEBUCKET " + logs
      })
  }

  def createEmailRequest(data: String): SendEmailRequest = {
    val fromEmail = "erodri90@uic.edu"
    val toEmail = "erodri90@uic.edu"
    val subject = s"ERROR LOG UPDATE"
    val htmlBody = getEmailBodyFromLogs(data)
    new SendEmailRequest()
      .withDestination(
        new Destination()
          .withToAddresses(toEmail))
      .withMessage(new Message()
        .withBody(new Body()
          .withHtml(new Content()
            .withCharset("UTF-8").withData(htmlBody)))
        .withSubject(new Content()
          .withCharset("UTF-8").withData(subject)))
      .withSource(fromEmail)
  }

  def getEmailBodyFromLogs(data: String): String = {
    val parsedData = data.split(" ENDTIMEBUCKET ")
    val bucket = parsedData(0)
    val logs = parsedData(1).split(" ENDLOG ").toList
    val header = s"<h3>${logs.length} ERROR logs at $bucket</h3>"
    val body = logs.foldLeft("")((acc,log) => acc + "<p>" + log + "</p>")
    header + body
  }
}
