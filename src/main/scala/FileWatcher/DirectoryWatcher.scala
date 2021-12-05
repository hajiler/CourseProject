package FileWatcher

import java.nio.file.{FileSystems, Path, Paths, WatchEvent, WatchKey, WatchService}
import java.nio.file.StandardWatchEventKinds._
import collection.JavaConverters

trait DirectoryWatcher {
  def startWatch(): List[String]

}
