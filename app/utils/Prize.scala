package andon.utils

import play.api.templates._

import scala.slick.lifted.MappedTypeMapper

sealed trait Prize {
  def toEnglish: String
  def toJapanese: String
  override def toString = toEnglish
  def span: Html =
    Html("<span class=\"" + this.toEnglish + "\">" + this.toJapanese + "</span>")
}

case object Grand extends Prize {
  def toEnglish = "grand"
  def toJapanese = "行灯大賞"
}
case object Gold extends Prize {
  def toEnglish = "gold"
  def toJapanese = "金賞"
}
case object Silver extends Prize {
  def toEnglish = "silver"
  def toJapanese = "銀賞"
}
case object Bronze extends Prize {
  def toEnglish = "bronze"
  def toJapanese = "銅賞"
}

object Prize {

  def fromEnglish(s: String): Option[Prize] = s match {
    case "grand" => Some(Grand)
    case "gold" => Some(Gold)
    case "silver" => Some(Silver)
    case "bronze" => Some(Bronze)
    case _ => None
  }
  def fromJapanese(s: String): Option[Prize] = s match {
    case "大賞" => Some(Grand)
    case "金賞" => Some(Gold)
    case "銀賞" => Some(Silver)
    case "銅賞" => Some(Bronze)
    case _ => None
  }
  def fromString(s: String) = fromEnglish(s)

  def partialFromString(s: String): Prize = fromEnglish(s).get

  implicit val prizeTypeMapper = MappedTypeMapper.base[Prize, String](
    { p => p.toString },
    { s => partialFromString(s) }
  )
}
