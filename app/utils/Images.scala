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

  def toFullsize(path: String) = """/thumbnail/""".r.replaceFirstIn(path, "/fullsize/")

  def toThumbnail(path: String) = """/fullsize/""".r.replaceFirstIn(path, "/thumbnail/")

  def toFilename(path: String) =
    """\.?/files/gallery/(fullsize|thumbnail)/\d{2}(st|nd|rd|th)/\d/-?\d{1,2}/""".r.replaceFirstIn(path, "")

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

  def getClassTopImages(t: OrdInt): Seq[(ClassData, String)] = {
    val cs = ClassData.findByTimes(t)
    getTopImages(cs)
  }

  def getTimesImages: Seq[(TimesData, String)] = {
    val ts = TimesData.all
    ts.map { t =>
      (t, getTimesImage(t.times))
    }
  }

  def getTimesImage(t: OrdInt) = "img/grands/" + t + ".jpg"

  def findImage(c: ClassData): Option[File] = {
    // 1. top some
    //   1. exists
    //   2. not exists
    // 2. top none
    //   1. head some
    //   2. head none
    c.top.flatMap { name =>
      val path = thumbnailPath(c.id, name)
      val file = new File(path)
      if (file.exists) {
        Some(file)
      } else {
        None
      }
    }.orElse(headOption(c.id))
  }

  def findImagePath(c: ClassData) = findImage(c).map(toPath _)

  def getTopImages(cs: Seq[ClassData]): Seq[(ClassData, String)] = {

    def flatOptions[A](options: Seq[Option[A]]): Seq[A] = {
      options.flatMap(_ match {
        case Some(a) => Seq(a)
        case None => Seq()
      })
    }

    val options: Seq[Option[(ClassData, String)]] = cs.map { c =>
      findImagePath(c).map((c, _))
    }

    flatOptions(options)
  }

  def getTopImagesOption(cs: Seq[ClassData]): Seq[(ClassData, Option[String])] = {
    cs.map { c =>
      (c, findImagePath(c))
    }
  }
}
