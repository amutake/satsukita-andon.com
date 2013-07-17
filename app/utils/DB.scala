package andon.utils

import scala.slick.driver.H2Driver.simple._
import play.api.Play.current

object DB {
  lazy val db = Database.forDataSource(play.api.db.DB.getDataSource("default"))
}
