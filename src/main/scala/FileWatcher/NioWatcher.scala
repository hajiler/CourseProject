package FileWatcher

import java.nio.file.StandardWatchEventKinds.{ENTRY_CREATE, ENTRY_MODIFY}
import java.nio.file.{FileSystems, Path, Paths, WatchEvent, WatchKey, WatchService}
import HelperUtils.ObtainConfigReference

class NioWatcher {
  val config = ObtainConfigReference("akka") match {
    case Some(value) => value
    case None => throw new RuntimeException("Cannot obtain a reference to the config data.")
  }
  // Create watch service
  val watchService : WatchService = FileSystems.getDefault().newWatchService()
  val path : Path = Paths.get(config.getString("akka.kafka.dirToWatch"))
  // Watch for creations and modifications
  path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY)
  val watchKey : WatchKey = watchService.take()

  def startWatch() : List[String] = {
    println(s"Watching path ${path.getParent}")
    // While the watch key is valid (able to reset)
    while(watchKey.reset()) {
      // Poll for directory events
      val pollEvents = watchKey.pollEvents()
      // If there have been events
      if (!pollEvents.isEmpty) {
        // Map the evnets to file names and return them as a list
        return pollEvents.toArray()
          .map(event => event.asInstanceOf[WatchEvent[_]].context().toString)
          .toList
      }
    }
    List[String]()
  }
}