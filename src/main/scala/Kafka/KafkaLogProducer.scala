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

import scala.concurrent.{ExecutionContextExecutor}

class KafkaLogProducer(actorSystem: ActorSystem) {
  // Create implicate objects for creating kafka source
  implicit val system: ActorSystem = actorSystem
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val config = ObtainConfigReference("akka") match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }

  private val logger = LoggerFactory.getLogger(this.getClass)

  // Create configs for Kafka source
  private val producerConfig = config.getConfig("akka.kafka.producer")
  private val producerSettings = ProducerSettings(producerConfig, new StringSerializer, new StringSerializer)

  // Write logs to Kafka topic
  def writeLogsToKafka(logs: List[String]) = {
    val topic = config.getString("akka.kafka.topic")
    Source(logs)
      .map(value => new ProducerRecord[String, String](topic, value))
      .runWith(Producer.plainSink(producerSettings))

    logger.info(s"Kafka Log Producer writing logs to kafka topic: $topic")
  }
}
