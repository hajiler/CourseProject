package FileWatcher

import java.nio.file.StandardWatchEventKinds.{ENTRY_CREATE, ENTRY_MODIFY}
import java.nio.file.{FileSystems, Path, Paths, WatchEvent, WatchKey, WatchService}

class NioWatcher extends DirectoryWatcher {
  val watchService : WatchService = FileSystems.getDefault().newWatchService()
  val path : Path = Paths.get("/Users/hajiler/school/cs441/CourseProject")
  path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY)
  val watchKey : WatchKey = watchService.take()

  override def startWatch() : List[String] = {
    print(s"Watching path ${path.getParent}")
    while(watchKey.reset()) {
      val pollEvents = watchKey.pollEvents()
      if (!pollEvents.isEmpty) {
        return pollEvents.toArray()
          .map(event => event.asInstanceOf[WatchEvent[_]].context().toString)
          .toList
      }
    }
    List[String]()
  }
}