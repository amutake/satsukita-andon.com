package controllers

object Util {

  def toTimesStr(times: Int) = (times % 10) match {
    case 1 => times + "st"
    case 2 => times + "nd"
    case 3 => times + "rd"
    case _ => times + "th"
  }
}
