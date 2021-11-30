package Akka
import FileWatcher.DirectoryWatcher
import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

object LogActors {
  final case class LogWatcher(directory: String, sendTo: ActorRef[LogHandler])
  final case class LogHandler(logs: List[String], from: ActorRef[LogWatcher])

  def apply(): Behavior[LogWatcher] = Behaviors.receive { (context, message) =>
    context.log.info(s"Watching ${message.directory}")
    val watcher = new DirectoryWatcher()
    val changedFiles = watcher.startWatch()
    message.sendTo ! LogHandler(changedFiles, context.self)
    Behaviors.same
  }
}

object LogBot {
  def apply(): Behavior[LogActors.LogHandler] = {
    Behaviors.receive { (context, message) =>
      context.log.info(s"Handling logs: ${message.logs}")
      message.from ! LogActors.LogWatcher("fakedirectory", context.self)
      Behaviors.same
    }
  }
}

object LogSystem {
  final case class WatchDirectory(path: String)

  def apply(): Behavior[WatchDirectory] = {
    Behaviors.setup { context =>
      // Create actor for watching directory
      val watcher = context.spawn(LogActors(), name = "logwatcher")

      Behaviors.receiveMessage { message =>
        val sendTo = context.spawn(LogBot(), message.path)

        watcher ! LogActors.LogWatcher("fakepath", sendTo)
        Behaviors.same
      }
    }
  }
}

