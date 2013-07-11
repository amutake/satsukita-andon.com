package andon.utils

import scala.slick.lifted.MappedTypeMapper

sealed trait ArticleType {
  override def toString: String
}

case object InfoTop extends ArticleType {
  override def toString = "info_top"
}

case object Info extends ArticleType {
  override def toString = "info"
}

case object Howto extends ArticleType {
  override def toString = "howto"
}

case object About extends ArticleType {
  override def toString = "about"
}

case object Contact extends ArticleType {
  override def toString = "contact"
}

object ArticleType {
  def fromString(str: String) = str match {
    case "info_top" => InfoTop
    case "info" => Info
    case "howto" => Howto
    case "about" => About
    case "contact" => Contact
  }

  implicit val articleTypeTypeMapper = MappedTypeMapper.base[ArticleType, String](
    { a => a.toString },
    { s => fromString(s) }
  )
}
