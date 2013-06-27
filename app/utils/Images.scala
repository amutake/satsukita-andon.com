package andon.utils

import java.io._

import models._

object Images {

  def dirName(t: OrdInt, g: Int, c: Int) =
    "./public/img/products/" + t + "/" + g + "/" + c

  def fileName(t: OrdInt, g: Int, c: Int, n: Int) =
    dirName(t, g, c) + "/" + t + g + "-" + c + "_" + pictNumber(n) + ".jpg"

  def pictNumber(n: Int) = if (n < 10) ("0" + n) else (n.toString)

  def getClassImages(t: OrdInt, g: Int, c: Int): Seq[String] = {
    val uri = dirName(t, g, c)
    val dir = new File(uri)
    if (dir.isDirectory) {
      dir.listFiles.map(_.getPath.substring("./public/".length))
    } else {
      Seq()
    }
  }

  def getTimesImages(t: OrdInt): Seq[String] = {
    val cs = ClassData.findByTimes(t)
    cs.map { c =>
      val uri = fileName(c.times, c.grade, c.classn, 1)
      val file = new File(uri)
      if (file.exists) {
        file.getPath.substring("./public/".length)
      } else {
        ""
      }
    }.filter { c => c != "" }
  }
}
