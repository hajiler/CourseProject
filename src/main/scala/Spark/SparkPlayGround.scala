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

    val df = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", config.getString("akka.kafka.topic"))
      .load()
    df
      .map(row => row.get(1).asInstanceOf[Array[Byte]])
      .map(bytes=> new String(bytes))
      .writeStream
      .foreachBatch( (data:Dataset[String], batch:Long) => {
        logger.error("WHYTHAFUCK")
        println(s"BATCH NUMBER $batch")
        println(data.toString())
        data.foreach(println)
      })
      .format("console")
      .outputMode("append")
      .start()
      .awaitTermination()


    println("Spark executed some stuf")
  }
}

