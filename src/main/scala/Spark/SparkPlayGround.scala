package Spark


import HelperUtils.ObtainConfigReference
import org.apache.spark.sql.{DataFrame, Dataset, ForeachWriter, Row, SparkSession}
import org.slf4j.LoggerFactory
import HelperUtils.Utils.{extractErrorLogs, summarizeErrorLogs, summarizeErrorLogs2}

object SparkPlayGround {
  val logger = LoggerFactory.getLogger(ObtainConfigReference.getClass)
  val config = ObtainConfigReference("akka") match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }

  def main(args: Array[String]) {
    val spark = SparkSession
      .builder
      .appName("StructuredNetworkWordCount")
      .config("spark.master", "local")
      .getOrCreate()
    spark.sparkContext.setLogLevel("ERROR")

    import spark.implicits._

    // Subscribe to Kafka source for logs, and load them into a DataFrame
    val df = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "b-2.demo-cluster-1.m021ne.c6.kafka.us-east-2.amazonaws.com:9092,b-3.demo-cluster-1.m021ne.c6.kafka.us-east-2.amazonaws.com:9092,b-1.demo-cluster-1.m021ne.c6.kafka.us-east-2.amazonaws.com:9092")
      .option("subscribe", "logs")
      .load()
    val logsFromSource = df
      // Process the data frame to filter for desired logs
      .map(row => row.get(1).asInstanceOf[Array[Byte]])
      .map(bytes=> new String(bytes))

    val errorLogs = extractErrorLogs(logsFromSource, spark)
    val query = summarizeErrorLogs2(errorLogs, spark)
    // Start query
      query.writeStream
      // Write processed data to new Kafka topic
      .format("kafka")
      .option("kafka.bootstrap.servers", "b-2.demo-cluster-1.m021ne.c6.kafka.us-east-2.amazonaws.com:9092,b-3.demo-cluster-1.m021ne.c6.kafka.us-east-2.amazonaws.com:9092,b-1.demo-cluster-1.m021ne.c6.kafka.us-east-2.amazonaws.com:9092")
      .option("topic", /*config.getString("akka.kafka.topic")*/ "results")
      .option("checkpointLocation", "/home/ec2-user/exec/kafka-checkpoints")
    //      .format("console")
      .outputMode("update")
      .start()
      // Wait for query to terminate
      .awaitTermination()
  }
}

