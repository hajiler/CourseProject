package Kafka

import HelperUtils.ObtainConfigReference
import akka.Done
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.Source
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object ProducerApp extends App {
  val logs = List("16:28:44.370 [scala-execution-context-global-123] WARN  HelperUtils.Parameters$ - x2oBSI0%CdfV2%ChSsnZ7vJo=2qJqZ%.kbc!0ne`y&m",
    "16:28:44.389 [scala-execution-context-global-123] DEBUG HelperUtils.Parameters$ - ihu}!A2]*07}|,lc",
    "16:28:44.389 [scala-execution-context-global-123] DEBUG HelperUtils.Parameters$ - ihu}!A2]*07}|,lc",
    "16:28:44.406 [scala-execution-context-global-123] DEBUG  HelperUtils.Parameters$ - CC]>~R#,^#0JWyESarZdETDcvk)Yk'I?")
  implicit val system: ActorSystem = ActorSystem("producer-sys")
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val config = ObtainConfigReference("akka") match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }
  val producerConfig = config.getConfig("akka.kafka.producer")
  val producerSettings = ProducerSettings(producerConfig, new StringSerializer, new StringSerializer)

  val produce: Future[Done] =
    Source(logs)
      .map(value => new ProducerRecord[String, String](config.getString("akka.kafka.topic"), value))
      .runWith(Producer.plainSink(producerSettings))

  produce onComplete  {
    case Success(_) => println("Done"); system.terminate()
    case Failure(err) => println(err.toString); system.terminate()
  }
}