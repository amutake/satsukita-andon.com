package models

import scala.slick.driver.H2Driver.simple._

import andon.utils._

case class TimesData(times: OrdInt, title: String)

object TimesData extends Table[TimesData]("TIMESDATA") {

  val db = DB.db

  def times = column[Int]("TIMES", O.NotNull, O.PrimaryKey)
  def title = column[String]("TITLE", O.NotNull)

  def * = times ~ title <> (
    tt => TimesData(OrdInt(tt._1), tt._2),
    data => Some(data.times.n, data.title)
  )

  val query = Query(TimesData)

  def findByTimes(t: OrdInt) = db.withSession { implicit session: Session =>
    query.where(_.times === t.n).firstOption
  }

  def all = db.withSession { implicit session: Session =>
    query.sortBy(_.times.desc).list
  }

  def create(data: TimesData) = db.withSession { implicit session: Session =>
    TimesData.insert(data)
  }

  def createByTimes(t: OrdInt) = db.withSession { implicit session: Session =>
    TimesData.insert(TimesData(t, ""))
  }

  def update(t: OrdInt, title: String) = db.withSession { implicit session: Session =>
    query.where(_.times === t.n).map(_.title).update(title)
  }
}
