package andon.api.util

import akka.http.scaladsl.server.PathMatcher
import akka.http.scaladsl.server.PathMatcher.{ Matching, Matched, Unmatched }
import akka.http.scaladsl.model.Uri.Path, Path.Segment

object SignedIntNumber extends PathMatcher[Tuple1[Int]] {
  def apply(path: Path): Matching[Tuple1[Int]] = path match {
    case Segment(hd, tl) => {
      try {
        Matched(tl, Tuple1(hd.toInt))
      } catch {
        case _: NumberFormatException => Unmatched
      }
    }
    case _ => Unmatched
  }
}
