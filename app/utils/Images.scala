package andon.utils

import java.io._

import models._

object Images {

  def productsDir(t: OrdInt, g: Int, c: Int) =
    "./files/gallery/fullsize/" + t + "/" + g + "/" + c

  def thumbnailsDir(t: OrdInt, g: Int, c: Int) =
    "./files/gallery/thumbnail/" + t + "/" + g + "/" + c

  def img(t: OrdInt, g: Int, c: Int, n: Int, f: (OrdInt, Int, Int) => String) =
    f(t, g, c) + "/" + t + g + "-" + c + "_" + pictNumber(n) + ".jpg"

  def pictNumber(n: Int) = if (n < 10) ("0" + n) else (n.toString)

  def toThumbnail(path: String) = """/fullsize/""".r.replaceFirstIn(path, "/thumbnail/")

  def getClassImages(t: OrdInt, g: Int, c: Int): Seq[String] = {
    val path = productsDir(t, g, c)
    val dir = new File(path)
    if (dir.isDirectory) {
      dir.listFiles.map(_.getPath.substring(1))
    } else {
      Seq()
    }
  }

  def getTimesImages(t: OrdInt): Seq[(ClassData, String)] = {
    val cs = ClassData.findByTimes(t)
    getTopImages(cs)
  }

  def getGrandImages: Seq[(TimesData, String)] = {
    val ts = TimesData.all
    ts.map { t =>
      (t, "img/grands/" + t.times + ".jpg")
    }
  }

  def getTopImages(cs: Seq[ClassData]): Seq[(ClassData, String)] = {
    cs.map { c =>
      val path = c.top.map { name =>
        thumbnailsDir(c.id.times, c.id.grade, c.id.classn) + "/" + name
      }.getOrElse(img(c.id.times, c.id.grade, c.id.classn, 1, thumbnailsDir))
      val file = new File(path)
      if (file.exists) {
        (c, file.getPath.substring(1))
      } else {
        (c, "img/logo.png")
      }
    }.filter { p =>
        p._2 != "img/logo.png"
    }
  }
}
