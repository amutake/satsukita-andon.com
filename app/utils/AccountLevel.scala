package andon.utils

import scala.slick.lifted.MappedTypeMapper

sealed trait AccountLevel {
  override def toString: String
}

case object Admin extends AccountLevel {
  override def toString = "admin"
}

case object Master extends AccountLevel {
  override def toString = "master"
}

case object Writer extends AccountLevel {
  override def toString = "writer"
}

object AccountLevel {
  def fromString(s: String) = s match {
    case "admin" => Admin
    case "master" => Master
    case "writer" => Writer
  }

  implicit val accountLevelTypeMapper = MappedTypeMapper.base[AccountLevel, String](
    { a => a.toString },
    { s => fromString(s) }
  )
}
