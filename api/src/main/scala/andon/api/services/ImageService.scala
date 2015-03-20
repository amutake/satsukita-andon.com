package andon.api.services

import java.io.File
import scala.collection.JavaConversions
import com.typesafe.config.ConfigFactory

import andon.api.util.OrdInt

object ImageObjects {
  case class Image(thumbnail: String, fullsize: String)
  case class Thumbnail(times: OrdInt, grade: Int, `class`: Int, path: String)
}

object ImageService {

  import ImageObjects._

  def classAll(times: OrdInt, grade: Int, `class`: Int, offset: Option[Int] = None, limit: Option[Int] = None): Seq[Image] = {
    val o = offset.filter(0 <= _).getOrElse(0)
    val l = limit.filter(0 <= _).getOrElse(50)
    Images.all(times, grade, `class`, o, l)
  }

  private object Images {

    lazy val base = {
      val conf = ConfigFactory.load()
      conf.getString("images.base") // e.g., /home/amutake/sites/satsukita-andon.com/files
    }

    def fullsizeDirPath(t: OrdInt, g: Int, c: Int): String =
      "/gallery/fullsize/" + t + "/" + g + "/" + c

    def thumbnailDirPath(t: OrdInt, g: Int, c: Int): String =
      "/gallery/thumbnail/" + t + "/" + g + "/" + c

    def fullsizeDir(t: OrdInt, g: Int, c: Int): Option[File] = {
      val dir = new File(base + fullsizeDirPath(t, g, c))
      if (dir.isDirectory) {
        Some(dir)
      } else {
        None
      }
    }

    def thumbnailDir(t: OrdInt, g: Int, c: Int): Option[File] = {
      val dir = new File(base + thumbnailDirPath(t, g, c))
      if (dir.isDirectory) {
        Some(dir)
      } else {
        None
      }
    }

    def all(t: OrdInt, g: Int, c: Int, o: Int, l: Int): Seq[Image] = {
      val res = for {
        fulldir <- fullsizeDir(t, g, c)
        thumbdir <- thumbnailDir(t, g, c)
      } yield {
        // XXX: Handle if fulls != thumbs
        val fulls = fulldir.listFiles.map(_.getName).sorted.drop(o).take(l)
        val thumbs = thumbdir.listFiles.map(_.getName).sorted.drop(o).take(l)
        fulls.zip(thumbs).map { case (full, thumb) =>
          Image(fullsizeDirPath(t, g, c) + full, thumbnailDirPath(t, g, c) + thumb)
        }
      }
      res.toSeq.flatten
    }
  }
}
