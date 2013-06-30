package andon.utils

import java.io._

import models._

object Images {

  def productsDir(t: OrdInt, g: Int, c: Int) =
    "./public/img/products/" + t + "/" + g + "/" + c

  def thumbnailsDir(t: OrdInt, g: Int, c: Int) =
    "./public/img/thumbnails/" + t + "/" + g + "/" + c

  def img(t: OrdInt, g: Int, c: Int, n: Int, f: (OrdInt, Int, Int) => String) =
    f(t, g, c) + "/" + t + g + "-" + c + "_" + pictNumber(n) + ".jpg"

  def pictNumber(n: Int) = if (n < 10) ("0" + n) else (n.toString)

  def toThumbnail(path: String) =
    "img/thumbnails" + path.substring("img/products".length)

  def getClassImages(t: OrdInt, g: Int, c: Int): Seq[String] = {
    val path = productsDir(t, g, c)
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
      val path = img(c.times, c.grade, c.classn, 1, thumbnailsDir)
      val file = new File(path)
      if (file.exists) {
        (c, file.getPath.substring("./public/".length))
      } else {
        (c, "img/logo.png")
      }
    }.filter { p =>
        p._2 != "img/logo.png"
    }
  }

  def getGrandImages: Seq[(TimesData, String)] = {
    val ts = TimesData.findAll
    ts.map { t =>
      (t, "img/grands/" + t.times + ".jpg")
    }
  }
}
