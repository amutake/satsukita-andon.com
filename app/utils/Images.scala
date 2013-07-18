package andon.utils

import java.io._

import models._

object Images {

  def productsDir(t: OrdInt, g: Int, c: Int) =
    "./files/gallery/fullsize/" + t + "/" + g + "/" + c

  def thumbnailsDir(t: OrdInt, g: Int, c: Int) =
    "./files/gallery/thumbnail/" + t + "/" + g + "/" + c

  def fullsizePath(id: ClassId, filename: String) = {
    productsDir(id.times, id.grade, id.classn) + "/" + filename
  }

  def thumbnailPath(id: ClassId, filename: String) = toThumbnail(fullsizePath(id, filename))

  def toThumbnail(path: String) = """/fullsize/""".r.replaceFirstIn(path, "/thumbnail/")

  def dirOption(id: ClassId) = {
    val dir = new File(productsDir(id.times, id.grade, id.classn))
    if (dir.isDirectory) {
      Some(dir)
    } else {
      None
    }
  }

  def headOption(id: ClassId) = {
    dirOption(id).flatMap { d =>
      d.listFiles.headOption
    }
  }

  def list(id: ClassId) = {
    dirOption(id).map { d =>
      d.listFiles.toSeq
    }.getOrElse(Seq[File]())
  }

  def toPath(file: File) = file.getPath.substring(1)

  def getClassImages(id: ClassId) = list(id).map(toPath _)

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
    // 1. top some
    //   1. exists
    //   2. not exists
    // 2. top none
    //   1. head some
    //   2. head none
    def flatOptions[A](options: Seq[Option[A]]): Seq[A] = {
      options.flatMap(_ match {
        case Some(a) => Seq(a)
        case None => Seq()
      })
    }

    val options: Seq[Option[(ClassData, String)]] = cs.map { c =>
      c.top.flatMap { name => // String -> Option[(ClassData, String)]
        val path = thumbnailPath(c.id, name)
        val file = new File(path)
        if (file.exists) {
          Some(c, toPath(file))
        } else {
          None
        }
      }.orElse(headOption(c.id).map { file =>
        (c, toPath(file))
      })
    }

    flatOptions(options)
  }
}
