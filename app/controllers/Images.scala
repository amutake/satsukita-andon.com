package controllers

import java.io._

object Images {

  def dirName(t: Int, g: Int, c: Int) =
    "./public/img/products/" + Util.toTimesStr(t) + "/" + g + "/" + c

  def getImages(t: Int, g: Int, c: Int): Seq[String] = {
    val uri = dirName(t, g, c)
    val dir = new File(uri)
    if (dir.isDirectory) {
      dir.listFiles.map(_.getPath.substring("./public/".length))
    } else {
      Seq()
    }
  }
}
