package andon.utils

import play.api.mvc._

case class OrdInt(n: Int) {

  override def toString = (n % 10) match {
    case 1 => n + "st"
    case 2 => n + "nd"
    case 3 => n + "rd"
    case _ => n + "th"
  }
}

object OrdInt {
  implicit def pathBinder(implicit intBinder: PathBindable[Int]) = new PathBindable[OrdInt] {
    // TODO
    override def bind(key: String, value: String): Either[String, OrdInt] = {
      if (value.length >= 4) {
        val numstr = value.substring(0, 2)
        val ordstr = value.substring(2)
        val result = intBinder.bind(key, numstr)
        // result >>= match
        //   where
        //     match n
        //       | n `div` 10 == 1 && ordstr == "st" = Right n
        //       | n `div` 10 == 2 && ordstr == "nd" = Right n
        //       | n `div` 10 == 3 && ordstr == "rd" = Right n
        //       | ordstr == "th" = Right n
        //       | otherwise = Left "Ordinal number miss match"
        if (result.isRight) {
          val n = result.right.get
            (n % 10) match {
            case 1 if (ordstr == "st") => Right(OrdInt(n))
            case 2 if (ordstr == "nd") => Right(OrdInt(n))
            case 3 if (ordstr == "rd") => Right(OrdInt(n))
            case _ if (ordstr == "th") => Right(OrdInt(n))
            case _ => Left("Cannot parse parameter " + key + " as OrdInt: For input string: \"" + value + "\"")
          }
        } else {
          Left(result.left.get)
        }
      } else {
        Left("Cannot parse parameter " + key + " as OrdInt: For input string: \"" + value + "\"")
      }
    }
    override def unbind(key: String, ordInt: OrdInt): String = {
      ordInt.toString()
    }
  }
}
