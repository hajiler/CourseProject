package Spark


import HelperUtils.ObtainConfigReference
import org.apache.spark.sql.{DataFrame, Dataset, ForeachWriter, Row, SparkSession}
import org.slf4j.LoggerFactory
import scala.jdk.CollectionConverters.CollectionHasAsScala

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
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", config.getString("akka.kafka.topic"))
      .load()
    df
      // Process the data frame to filter for desired logs
      .map(row => row.get(1).asInstanceOf[Array[Byte]])
      .map(bytes=> new String(bytes))
    // Start query
      .writeStream
      // Write processed data to new Kafka topic
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("topic", /*config.getString("akka.kafka.topic")*/ "results")
      .option("checkpointLocation", "/Users/hajiler/school/cs441/CourseProject/src/main/kafka/.checkpoint")
    //      .format("console")
      .outputMode("append")
      .start()
      // Wait for query to terminate
      .awaitTermination()
  }
}

