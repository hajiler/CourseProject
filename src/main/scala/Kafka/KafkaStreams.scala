package Kafka

import io.circe.generic.auto._
import io.circe.parser._
import io.circe.syntax._
import io.circe.{Decoder, Encoder}
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.streams.kstream.{GlobalKTable, JoinWindows, TimeWindows, Windowed}
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.scala.kstream.{KGroupedStream, KStream, KTable}
import org.apache.kafka.streams.scala.serialization.Serdes
import org.apache.kafka.streams.scala.serialization.Serdes._
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig, Topology}

import java.time.Duration
import java.time.temporal.ChronoUnit
import java.util.Properties
import scala.concurrent.duration._

object KafkaStreams {

  object Domain {
    type UserId = String
    type Profile = String
    type Product = String
    type OrderId = String

    case class Order(orderId: OrderId, user: UserId, products: List[Product], amount: Double)
    case class Discount(profile: Profile, amount: Double) // in percentage points
    case class Payment(orderId: OrderId, status: String)
  }

  object Topics {
    final val OrdersByUserTopic = "orders-by-user"
    final val DiscountProfilesByUserTopic = "discount-profiles-by-user"
    final val DiscountsTopic = "discounts"
    final val OrdersTopic = "orders"
    final val PaymentsTopic = "payments"
    final val PaidOrdersTopic = "paid-orders"
  }
  import Domain._
  implicit def serdeOrder[A >: Null : Decoder : Encoder]: Serde[A] = {
    val serializer = (a: A) => a.asJson.noSpaces.getBytes()
    val deserializer = (bytes: Array[Byte]) => {
      val string = new String(bytes)
      decode[A](string).toOption
    }

    Serdes.fromFn[A](serializer, deserializer)
    }

//  def main(args: Array[String]): Unit = {
////    // Topology
////    val builder = new StreamsBuilder()
////    import Topics._
////
////    // KStream
////    val usersOrderStream: KStream[UserId, Order] = builder.stream[UserId, Order](OrdersByUserTopic)
////
////    // KTable - distributed into partitions
////    val userProfilesTable: KTable[UserId, Profile] = builder.table[UserId, Profile](DiscountProfilesByUserTopic)
////
////    // GlobalKTable - copied to all nodes in kafka cluster
////    val discountProfilesGTable: GlobalKTable[Profile, Discount] = builder.globalTable[Profile, Discount](DiscountsTopic)
////
////    val expensiveOrders = usersOrderStream.filter {(userID, order) =>
////      order.amount > 1000
////    }
////
////    val listOfProducts =usersOrderStream.mapValues { order =>
////      order.products
////    }
////
////    val productsStream = usersOrderStream.flatMapValues(_.products)
////
////    // join
////
////    val ordersWithUserProfiles = usersOrderStream.join(userProfilesTable) { (order, profile) =>
////      (order, profile)
////    }
////
////    val topology = builder.build()
////
////    val props = new Properties()
////    props.put(StreamsConfig.APPLICATION_ID_CONFIG, "orders-application")
////    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
////    props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.stringSerde.getClass)
////
////    val application = new KafkaStreams(topology, props)
////    application.start()
//
//
//    List("orders-by-user",
//    "discount-profiles-by-user",
//    "discounts",
//    "orders",
//    "payments",
//    "paid-orders").foreach(topic => println(s"kafka-topics --bootstrap-server localhost:9092 --topic ${topic} --delete"))
//
//  }
}
