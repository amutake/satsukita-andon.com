package models

import play.api.db._
import play.api.Play.current

import scala.slick.driver.H2Driver.simple._

import andon.utils._

case class ClassData(id: ClassId, title: String, prize: Option[Prize])

object ClassData extends Table[ClassData]("CLASSDATA") {

  val db = Database.forDataSource(DB.getDataSource("default"))

  def id = column[Int]("ID", O.NotNull, O.PrimaryKey)
  def title = column[String]("TITLE", O.NotNull)
  def prize = column[Option[String]]("PRIZE")

  def * = id ~ title ~ prize <> (
    (id, title, prize) => ClassData(ClassId.fromId(id), title, prize.flatMap(Prize.fromString)),
    data => Some(data.id.toId, data.title, data.prize.map(_.toString))
  )

  def findByClassId(c: ClassId): Option[ClassData] = db.withSession { implicit session: Session =>
    Query(ClassData).filter(_.id === c.toId).firstOption
  }

  def all = db.withSession { implicit session: Session =>
    Query(ClassData).list
  }

  def findByTimes(t: OrdInt) = db.withSession { implicit session: Session =>
    // TODO
    Query(ClassData).list.filter { data =>
      data.id.times == t
    }
  }

  def search(times: String, prize: String, grade: String) = db.withSession { implicit session: Session =>
    // TODO
    Query(ClassData).list
  }

  def create(data: ClassData) = db.withSession { implicit session: Session =>
    ClassData.insert(data)
  }
}
