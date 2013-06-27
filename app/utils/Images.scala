package andon.utils

import java.io._

object Images {

  def dirName(t: OrdInt, g: Int, c: Int) =
    "./public/img/products/" + t + "/" + g + "/" + c

  def getClassImages(t: OrdInt, g: Int, c: Int): Seq[String] = {
    val uri = dirName(t, g, c)
    val dir = new File(uri)
    if (dir.isDirectory) {
      dir.listFiles.map(_.getPath.substring("./public/".length))
    } else {
      Seq()
    }
  }
}
