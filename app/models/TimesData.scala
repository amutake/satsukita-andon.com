package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

case class TimesData(times: Pk[Int], title: String)

object TimesData {

  val simple = {
    get[Pk[Int]]("TimesData.times") ~
    get[String]("TimesData.title") map {
      case times~title => TimesData(
        times, title
      )
    }
  }

  def findById(t: Int): Option[TimesData] = {
    DB.withConnection { implicit connection =>
      SQL("select * from TimesData where times = {t}").on(
        't -> t
      ).as(TimesData.simple.singleOpt)
    }
  }

  def findAll: Seq[TimesData] = {
    DB.withConnection { implicit connection =>
      SQL("select * from TimesData").as(TimesData.simple *)
    }
  }

  def create(data: TimesData): TimesData = {
    DB.withConnection { implicit connection =>
      SQL("insert into TimesData values ({times}, {title})").on(
        'times -> data.times,
        'title -> data.title
      ).executeUpdate()

      data.copy()
    }
  }
}
