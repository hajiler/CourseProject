import akka.actor.typed.ActorSystem
import Akka._

object Main extends App {
//  val watch = new DirectoryWatcher()
//  watch.startWatch()
//  val logWatcher: ActorSystem[LogActors] = ActorSystem
  val logMain : ActorSystem[LogSystem.WatchDirectory] = ActorSystem(LogSystem(), "Testing")
  logMain ! LogSystem.WatchDirectory("fakePathFromMain")
}