package Kafka

import HelperUtils.ObtainConfigReference
import HelperUtils.Utils.{createEmailRequest, getEmailBodyFromLogs}
import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.scaladsl.Sink
import org.apache.kafka.common.serialization.StringDeserializer
import com.amazonaws.services.simpleemail.{AmazonSimpleEmailService, AmazonSimpleEmailServiceClientBuilder}
import org.slf4j.LoggerFactory
import java.io.{PrintWriter, File, FileOutputStream}

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object ConsumerApp extends App {
  implicit val system: ActorSystem = ActorSystem("consumer-sys")
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  val config = ObtainConfigReference("akka") match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }
  
  val consumerConfig = config.getConfig("akka.kafka.consumer")
  val consumerSettings = ConsumerSettings(consumerConfig, new StringDeserializer, new StringDeserializer)
  val logger = LoggerFactory.getLogger(ConsumerApp.getClass)

  // Create kafka consumer from configurations and subscripe to data written by Spark
  val consume = Consumer
    .plainSource(consumerSettings, Subscriptions.topics(config.getString("akka.kafka.outputTopic")))
    .runWith(
      // For each record build and send email
      Sink.foreach(record => {
        // If running locally, just print the email body that would be sent
        if (config.getBoolean("akka.kafka.runLocal")) {
          logger.info(s"EMAIL THAT WOULD BE SENT:\n${getEmailBodyFromLogs(record.value())}")
        }
        else {
          AmazonSimpleEmailServiceClientBuilder
            .defaultClient()
            .sendEmail(createEmailRequest(record.value()))
        }
      })
    )

  consume onComplete {
    case Success(_) => println("Done"); system.terminate()
    case Failure(err) => println(err.toString); system.terminate()
  }
}
