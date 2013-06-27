package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import andon.utils._

case class TimesData(times: Pk[OrdInt], title: String)

object TimesData {

  val simple = {
    get[Pk[Int]]("TimesData.times") ~
    get[String]("TimesData.title") map {
      case times~title => TimesData(
        Id(OrdInt(times.get)), title
      )
    }
  }

  def findById(t: OrdInt): Option[TimesData] = {
    DB.withConnection { implicit connection =>
      SQL("select * from TimesData where times = {t}").on(
        't -> t.n
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
        'times -> data.times.get.n,
        'title -> data.title
      ).executeUpdate()

      data.copy()
    }
  }
}
