package FileWatcher

import java.nio.file.{FileSystems, Path, Paths, WatchKey, WatchService};
import java.nio.file.StandardWatchEventKinds._
import scala.jdk.CollectionConverters.CollectionHasAsScala

class DirectoryWatcher {
  val watchService : WatchService = FileSystems.getDefault().newWatchService()
  val path : Path = Paths.get("/Users/hajiler/school/cs441/CourseProject")
  path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY)
  val watchKey : WatchKey = watchService.take()

  def startWatch() : List[String] = {
    print(s"Watching path ${path.getParent}")
    while(watchKey.reset()) {
      val pollEvents = watchKey.pollEvents()
      if (!pollEvents.isEmpty) {
        return pollEvents.asScala
          .map(event => event.context().toString)
          .toList
      }
    }
    List[String]()
  }
}
