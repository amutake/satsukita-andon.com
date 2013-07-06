package models

import play.api.db.DB
import play.api.Play.current

import scala.slick.driver.H2Driver.simple._

import andon.utils._

case class TimesData(times: OrdInt, title: String)

object TimesData extends Table[TimesData]("TIMESDATA") {

  val db = Database.forDataSource(DB.getDataSource("default"))

  def times = column[Int]("TIMES", O.NotNull, O.PrimaryKey)
  def title = column[String]("TITLE", O.NotNull)

  def * = times ~ title <> (
    tt => TimesData(OrdInt(tt._1), tt._2),
    data => Some(data.times.n, data.title)
  )

  def findByTimes(t: OrdInt) = db.withSession { implicit session: Session =>
    Query(TimesData).filter(_.times === t.n).firstOption
  }

  def all = db.withSession { implicit session: Session =>
    Query(TimesData).list
  }

  def create(data: TimesData) = db.withSession { implicit session: Session =>
    TimesData.insert(data)
  }
}
