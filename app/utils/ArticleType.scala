package andon.utils

import scala.slick.lifted.MappedTypeMapper

sealed trait ArticleType {
  override def toString: String
}

case object InfoTop extends ArticleType {
  override def toString = "InfoTop"
}

case object Info extends ArticleType {
  override def toString = "Info"
}

case object Howto extends ArticleType {
  override def toString = "Howto"
}

case object About extends ArticleType {
  override def toString = "About"
}

case object Contact extends ArticleType {
  override def toString = "Contact"
}

object ArticleType {
  def fromString(str: String) = str match {
    case "InfoTop" => InfoTop
    case "Info" => Info
    case "Howto" => Howto
    case "About" => About
    case "Contact" => Contact
  }

  val all = Seq(InfoTop, Info, Howto, About, Contact)

  implicit val articleTypeTypeMapper = MappedTypeMapper.base[ArticleType, String](
    { a => a.toString },
    { s => fromString(s) }
  )
}
