package Akka
import FileWatcher.NioWatcher
import Kafka.KafkaLogProducer
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import HelperUtils.Utils.getLogsFromFileEvent

object LogActors {
  final case class LogWatcher(directory: String, sendTo: ActorRef[LogHandler])
  final case class LogHandler(fileEvents: List[String], from: ActorRef[LogWatcher])

  // Behavior for LogWatcher actor. Watches directory for file events, and then sends them to
  // LogHandler Actor.
  def apply(): Behavior[LogWatcher] = Behaviors.receive { (context, message) =>
    context.log.info(s"Watching ${message.directory}")
    // Watch directory.
    val watcher = new NioWatcher()
    val changedFiles = watcher.startWatch()
    // Send file events to LogHandler actor.
    message.sendTo ! LogHandler(changedFiles, context.self)
    Behaviors.same
  }
}

// LogHandler actor behavior. Handles incoming file events by reading the logs from the specified file,
// and writing these logs to Kafka cluster.
object LogBot {
  def apply(): Behavior[LogActors.LogHandler] = {
    Behaviors.receive { (context, message) =>
      context.log.info(s"Reading logs from: ${message.fileEvents}")
      // Read files from file events/
      val logs = getLogsFromFileEvent(message.fileEvents)
      // Write logs to kafka topic
      val logProducer = new KafkaLogProducer(context.system.classicSystem)
      logProducer.writeLogsToKafka(logs)
      // Message original LogWatcher actor to continue directory watch
      message.from ! LogActors.LogWatcher("LogDirectory", context.self)
      Behaviors.same
    }
  }
}

// Default behavior of actor system. Spawn log watcher actor.
object LogSystem {
  final case class WatchDirectory(path: String)

  def apply(): Behavior[WatchDirectory] = {
    Behaviors.setup { context =>
      // Create actor for watching directory
      val watcher = context.spawn(LogActors(), name = "logwatcher")

      Behaviors.receiveMessage { message =>
        val sendTo = context.spawn(LogBot(), message.path)

        watcher ! LogActors.LogWatcher("LogDirectory", sendTo)
        Behaviors.same
      }
    }
  }
}

