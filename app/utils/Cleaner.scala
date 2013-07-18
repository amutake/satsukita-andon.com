package andon.utils

import java.io._

import models._

object Cleaner {

  def clean = {
    println("Cleaning!")
    cleanNotUsingImage
    println("Done.")
  }

  def cleanNotUsingImage = {
    val r = """/files/images/(fullsize|thumbnail)/\d{13,}-[\w\.]*\.\w+""".r
    val appeared = Articles.all.map(_.text).flatMap { text =>
      r.findAllIn(text).toList.map { path: String =>
        "." + path
      }
    }.toSet

    def recFiles(file: File): Set[File] = {
      if (file.isDirectory) {
        file.listFiles.toSet.flatMap(recFiles _)
      } else {
        Set(file)
      }
    }
    val paths = recFiles(new File("./files/images")).map(_.getPath)

    val notUsing = paths &~ appeared
    notUsing.map { path =>
      new File(path).delete()
    }
  }
}
