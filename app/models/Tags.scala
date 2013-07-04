package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import andon.utils._

case class Tag(times: OrdInt, grade: Int, classn: Int, tag: String)

object Tags {

  val simple = {
    get[Int]("Tags.times") ~
    get[Int]("Tags.grade") ~
    get[Int]("Tags.classn") ~
    get[String]("Tags.tag") map {
      case times~grade~classn~tag => Tag(
        OrdInt(times), grade, classn, tag
      )
    }
  }

  def findById(t: OrdInt, g: Int, c: Int): Option[Tag] = {
    DB.withConnection { implicit connection =>
      SQL("select * from Tags where times = {t} and grade = {g} and classn = {c}").on(
        't -> t.n,
        'g -> g,
        'c -> c
      ).as(Tags.simple.singleOpt)
    }
  }

  def findAll: Seq[Tag] = {
    DB.withConnection { implicit connection =>
      SQL("select * from Tags").as(Tags.simple *)
    }
  }

  def create(tag: Tag): Tag = {
    DB.withConnection { implicit connection =>
      SQL("insert into Tags values ({t}, {g}, {c}, {tag})").on(
        't -> tag.times.n,
        'g -> tag.grade,
        'c -> tag.classn,
        'tag -> tag.tag
      ).executeUpdate()

      tag.copy()
    }
  }
}
