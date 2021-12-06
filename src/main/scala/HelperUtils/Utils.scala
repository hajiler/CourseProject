package HelperUtils

import org.apache.spark.sql.functions.{col, collect_list}

import scala.io.Source
import org.apache.spark.sql.{Dataset, SparkSession}
import com.amazonaws.services.simpleemail.model.{Body, Message, Content, Destination, SendEmailRequest}

object Utils {
  def getLogsFromFileEvent(fileEvent: List[String]): List[String] = {
    val config = ObtainConfigReference("akka") match {
      case Some(value) => value
      case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
    }
    val logs = fileEvent
      // Map the event to a file path
      .map(config.getString("akka.kafka.dirToWatch") + "/" + _)
      // Flat map all paths to a list of logs from all files
      .flatMap(path => {
        println(s"Watching path ${path}")
        try {
          // Open the file
          val fileReader = Source.fromFile(path)
          // Read the file
          val lines = fileReader.getLines
            .toList
          // Close the file
          fileReader.close()
          lines
        } catch {
          case exception: Exception => List()
        }
    })

    logs
  }

  // Given the a log timestamp, return the 10 second time interval of where the log occurred.
  def hashTimeString(logTime: String): String = {
    val times = logTime.split(":")
    val second = (times(2).toDouble / 10).toInt.toString
    val interval = s"${times(0)}:${times(1)}:${second}-${second}9"
    interval
  }

  // Tranform incoming log data into a data frame containing the time interval for ERROR logs and the ERROR log itself
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


  // Transform incoming data containing (TimeInterval, Log) into (TimeInterval, List of logs in that time Interval)
  def summarizeErrorLogs(data: Dataset[(String, String)], spark: SparkSession): Dataset[String]  = {
    import spark.implicits._
    // Group logs by time interval
    data.groupBy(col("_1"))
      // Aggregate logs in each time interval into a list
      .agg(collect_list("_2"))
      // Map each row of (TimeInterval, Log) into a specifically formatted string for parsing
      .map(row => {
        val bucket = row.getString(0)
        val logs = row.getList(1).toArray()
          .reduce((acc, log) => acc  + " ENDLOG " + log)
        bucket +  " ENDTIMEBUCKET " + logs
      })
  }

  // Create and AWS email request from incoming data (in the format returned by the above). These are hardcoded
  // to my email because they should not be configured in any other way.
  def createEmailRequest(data: String): SendEmailRequest = {
    val fromEmail = "erodri90@uic.edu"
    val toEmail = "erodri90@uic.edu"
    val subject = "ERROR LOG UPDATE"
    // Create html body as a string (see below function)
    val htmlBody = getEmailBodyFromLogs(data)
    // Create email request
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

  // Format email from data
  def getEmailBodyFromLogs(data: String): String = {
    val parsedData = data.split(" ENDTIMEBUCKET ")
    val bucket = parsedData(0)
    val logs = parsedData(1).split(" ENDLOG ").toList
    val header = s"<h3>${logs.length} ERROR logs at $bucket</h3>"
    val body = logs.foldLeft("")((acc,log) => acc + "<p>" + log + "</p>")
    header + body
  }
}
