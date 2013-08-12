package models

import scala.slick.driver.H2Driver.simple._

import andon.utils._

case class TimesData(times: OrdInt, title: String)

object TimesData extends Table[TimesData]("TIMESDATA") {

  val db = DB.db

  def times = column[OrdInt]("TIMES", O.NotNull, O.PrimaryKey)
  def title = column[String]("TITLE", O.NotNull)

  def * = times ~ title <> (
    TimesData.apply _,
    TimesData.unapply _
  )

  val query = Query(TimesData)

  def findByTimes(t: OrdInt) = db.withSession { implicit session: Session =>
    query.where(_.times === t).firstOption
  }

  def all = db.withSession { implicit session: Session =>
    query.sortBy(_.times.desc).list
  }

  def latest = db.withSession { implicit sessioin: Session =>
    query.sortBy(_.times.desc).firstOption
  }

  def create(data: TimesData) = db.withSession { implicit session: Session =>
    TimesData.insert(data)
  }

  def createByTimes(t: OrdInt) = db.withSession { implicit session: Session =>
    TimesData.insert(TimesData(t, ""))
  }

  def update(t: OrdInt, title: String) = db.withSession { implicit session: Session =>
    query.where(_.times === t).map(_.title).update(title)
  }
}
