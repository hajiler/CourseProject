import akka.actor.typed.ActorSystem
import Akka._
import Kafka.KafkaLogProducer
object Main extends App {
  // Create actor system
  val logMain : ActorSystem[LogSystem.WatchDirectory] = ActorSystem(LogSystem(), "LogSystem")

  // Start actor system behavior
  logMain ! LogSystem.WatchDirectory("Logs")
}