package andon.api.util

final case class OrdInt(raw: Int) {
  override def toString: String = {
    raw % 10 match {
      case 1 => raw + "st"
      case 2 => raw + "nd"
      case 3 => raw + "rd"
      case _ => raw + "th"
    }
  }
}
