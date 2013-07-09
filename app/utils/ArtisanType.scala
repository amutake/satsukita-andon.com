package andon.utils

import scala.slick.lifted.MappedTypeMapper

sealed trait ArtisanType {
  override def toString: String
}

case object Admin extends ArtisanType {
  override def toString = "admin"
}

case object Master extends ArtisanType {
  override def toString = "master"
}

case object Writer extends ArtisanType {
  override def toString = "writer"
}

object ArtisanType {
  def fromString(s: String) = s match {
    case "admin" => Admin
    case "master" => Master
    case "writer" => Writer
  }

  implicit val artisanTypeTypeMapper = MappedTypeMapper.base[ArtisanType, String](
    { a => a.toString },
    { s => fromString(s) }
  )
}
