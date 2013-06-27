package andon.utils

import java.io._

import models._

object Images {

  def dirPath(t: OrdInt, g: Int, c: Int) =
    "./public/img/products/" + t + "/" + g + "/" + c

  def filePath(t: OrdInt, g: Int, c: Int, n: Int) =
    dirPath(t, g, c) + "/" + t + g + "-" + c + "_" + pictNumber(n) + ".jpg"

  def pictNumber(n: Int) = if (n < 10) ("0" + n) else (n.toString)

  def getClassImages(t: OrdInt, g: Int, c: Int): Seq[String] = {
    val path = dirPath(t, g, c)
    val dir = new File(path)
    if (dir.isDirectory) {
      dir.listFiles.map(_.getPath.substring("./public/".length))
    } else {
      Seq()
    }
  }

  def getTimesImages(t: OrdInt): Seq[(ClassData, String)] = {
    val cs = ClassData.findByTimes(t)
    cs.map { c =>
      val path = filePath(c.times, c.grade, c.classn, 1)
      val file = new File(path)
      if (file.exists) {
        (c, file.getPath.substring("./public/".length))
      } else {
        (c, "img/logo.png")
      }
    }
  }

  def getGrandImages: Seq[(TimesData, String)] = {
    val ts = TimesData.findAll
    ts.map { t =>
      (t, "img/grands/" + t.times + ".jpg")
    }
  }
}
