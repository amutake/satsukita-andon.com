package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

import andon.utils._

case class ClassData(times: OrdInt, grade: Int, classn: Int, title: String, prize: Option[Prize])

object ClassData {

  val simple = {
    get[Int]("ClassData.times") ~
    get[Int]("ClassData.grade") ~
    get[Int]("ClassData.classn") ~
    get[String]("ClassData.title") ~
    get[Option[String]]("ClassData.prize") map {
      case times~grade~classn~title~prize => ClassData(
        OrdInt(times), grade, classn, title, prize.flatMap(Prize.fromString)
      )
    }
  }

  def findById(t: OrdInt, g: Int, c: Int): Option[ClassData] = {
    DB.withConnection { implicit connection =>
      SQL("select * from ClassData where times = {t} and grade = {g} and classn = {c}").on(
        't -> t.n,
        'g -> g,
        'c -> c
      ).as(ClassData.simple.singleOpt)
    }
  }

  def findAll: Seq[ClassData] = {
    DB.withConnection { implicit connection =>
      SQL("select * from ClassData").as(ClassData.simple *)
    }
  }

  def findByTimes(t: OrdInt): Seq[ClassData] = {
    DB.withConnection { implicit connection =>
      SQL("select * from ClassData where times = {t}").on(
        't -> t.n
      ).as(ClassData.simple *)
    }
  }

  def create(data: ClassData): ClassData = {
    DB.withConnection { implicit connection =>
      SQL("insert into ClassData values ({t}, {g}, {c}, {title}, {prize})").on(
        't -> data.times.n,
        'g -> data.grade,
        'c -> data.classn,
        'title -> data.title,
        'prize -> data.prize.map(_.toString)
      ).executeUpdate()

      data.copy()
    }
  }
}
