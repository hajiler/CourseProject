package HelperUtils

import scala.io.Source

object FileUtils {
  def getLogsFromFileEvent(fileEvent: List[String]): String = {
    val logs = fileEvent.flatMap(fileName => {
      try {
        val fileReader = Source.fromFile(fileName)
        val lines = fileReader.getLines
          .toList
        fileReader.close()
        lines
      } catch {
        case exception: Exception => List()
      }
    })
      .reduce((files, file1) => files + file1)
    logs
  }
}
