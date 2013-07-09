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

case class Howto(genre: String) extends ArticleType {
  override def toString = "howto_" + genre
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
    case s if s.startsWith("howto_") => Howto(s.drop("howto_".length))
    case "about" => About
    case "contact" => Contact
  }

  implicit val articleTypeTypeMapper = MappedTypeMapper.base[ArticleType, String](
    { a => a.toString },
    { s => fromString(s) }
  )
}
