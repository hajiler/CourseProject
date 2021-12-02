package Kafka

import HelperUtils.ObtainConfigReference
import Kafka.ProducerApp.{config, logs}
import akka.Done
import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.Producer
import akka.stream.scaladsl.Source
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContextExecutor, Future}

class LogProducer(actorSystem: ActorSystem) {
  implicit val system: ActorSystem = actorSystem
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val config = ObtainConfigReference("akka") match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }

  private val logger = LoggerFactory.getLogger(ObtainConfigReference.getClass)

  private val producerConfig = config.getConfig("akka.kafka.producer")
  private val producerSettings = ProducerSettings(producerConfig, new StringSerializer, new StringSerializer)

  val produce: Future[Done] =
    Source(logs)
      .map(value => new ProducerRecord[String, String](config.getString("akka.kafka.topic"), value.toString))
      .runWith(Producer.plainSink(producerSettings))

}
