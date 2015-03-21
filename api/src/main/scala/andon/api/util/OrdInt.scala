package andon.api.util

import scala.util.Try
import akka.http.server._
import akka.http.server.PathMatcher.{ Matching, Matched, Unmatched }
import akka.http.server.util.Tuple._
import akka.http.model.Uri.Path, Path.Segment

case class OrdInt(raw: Int) {
  override def toString: String = {
    raw % 10 match {
      case 1 => raw + "st"
      case 2 => raw + "nd"
      case 3 => raw + "rd"
      case _ => raw + "th"
    }
  }
}

object OrdInt {
  def fromString(str: String): Option[OrdInt] = {
    val r = """(\d+)(st|nd|rd|th)""".r
    str match {
      case r(nstr, ord) => {
        Try(nstr.toInt).toOption.flatMap { n =>
          n % 10 match {
            case 1 if ord == "st" => Some(OrdInt(n))
            case 2 if ord == "nd" => Some(OrdInt(n))
            case 3 if ord == "rd" => Some(OrdInt(n))
            case p if p != 1 && p != 2 && p != 3 && ord == "th" => Some(OrdInt(n))
            case _ => None
          }
        }
      }
      case _ => None
    }
  }
}

object OrdIntMatcher extends PathMatcher[Tuple1[OrdInt]] {
  def apply(path: Path): Matching[Tuple1[OrdInt]] = path match {
    case Segment(hd, tl) => {
      OrdInt.fromString(hd).map { ordint =>
        Matched(tl, Tuple1(ordint))
      }.getOrElse(Unmatched)
    }
    case _ => Unmatched
  }
}
