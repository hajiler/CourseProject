package Spark


import HelperUtils.ObtainConfigReference
import org.apache.spark.sql.{SparkSession}
import org.slf4j.LoggerFactory
import HelperUtils.Utils.{extractErrorLogs, summarizeErrorLogs}

// Class to submit to spark
object SparkPlayGround {
  val logger = LoggerFactory.getLogger(ObtainConfigReference.getClass)
  val config = ObtainConfigReference("akka") match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }

  def main(args: Array[String]) {
    // Create spark session
    val spark = SparkSession
      .builder
      .appName("StructuredNetworkWordCount")
      .config("spark.master", "local")
      .getOrCreate()
    // Hide unnesscary errors
    spark.sparkContext.setLogLevel("ERROR")

    // Import implicits from spark session for transformations
    import spark.implicits._

    // Subscribe to Kafka source for logs, and load them into a DataFrame
    val df = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", config.getString("akka.kafka.kafka-servers"))
      .option("subscribe", config.getString("akka.kafka.topic"))
      .load()

    // Transform incoming data from bytes to strings
    val logsFromSource = df
      .map(row => row.get(1).asInstanceOf[Array[Byte]])
      .map(bytes=> new String(bytes))

    // Transform data to just just desired error logs
    val errorLogs = extractErrorLogs(logsFromSource, spark)
    // Group error logs by time bucket
    val query = summarizeErrorLogs(errorLogs, spark)
    // Start query
      query.writeStream
      // Write processed data to new Kafka topic, where the emails will be sent
      .format("kafka")
      .option("kafka.bootstrap.servers", config.getString("akka.kafka.kafka-servers"))
      .option("topic", config.getString("akka.kafka.outputTopic"))
      .option("checkpointLocation", config.getString("akka.kafka.checkPointPath"))
      .outputMode("update")
      .start()
      // Wait for query to terminate
      .awaitTermination()
  }
}

