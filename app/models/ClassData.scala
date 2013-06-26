package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

case class ClassData(times: Int, grade: Int, classn: Int, title: String, prize: Option[String])

object ClassData {

  val simple = {
    get[Int]("ClassData.times") ~
    get[Int]("ClassData.grade") ~
    get[Int]("ClassData.classn") ~
    get[String]("ClassData.title") ~
    get[Option[String]]("ClassData.prize") map {
      case times~grade~classn~title~prize => ClassData(
        times, grade, classn, title, prize
      )
    }
  }

  def findById(t: Int, g: Int, c: Int): Option[ClassData] = {
    DB.withConnection { implicit connection =>
      SQL("select * from ClassData where times = {t}, grade = {g}, classn = {c}").on(
        't -> t,
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

  def findByTimes(t: Int): Seq[ClassData] = {
    DB.withConnection { implicit connection =>
      SQL("select * from ClassData where times = {t}").on(
        't -> t
      ).as(ClassData.simple *)
    }
  }

  def create(data: ClassData): ClassData = {
    DB.withConnection { implicit connection =>
      SQL("insert into ClassData values ({t}, {g}, {c}, {title}, {prize})").on(
        't -> data.times,
        'g -> data.grade,
        'c -> data.classn,
        'title -> data.title,
        'prize -> data.prize
      ).executeUpdate()

      data.copy()
    }
  }
}
