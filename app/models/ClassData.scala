package models

import play.api.db._
import play.api.Play.current

import scala.slick.driver.H2Driver.simple._

import andon.utils._

case class ClassData(id: ClassId, title: String, prize: Option[Prize])

object ClassData extends Table[ClassData]("CLASSDATA") {

  val db = Database.forDataSource(DB.getDataSource("default"))

  def id = column[ClassId]("ID", O.NotNull, O.PrimaryKey)
  def title = column[String]("TITLE", O.NotNull)
  def prize = column[Option[Prize]]("PRIZE")

  def * = id ~ title ~ prize <> (ClassData.apply _, ClassData.unapply _)

  val query = Query(ClassData)

  def findByClassId(c: ClassId): Option[ClassData] = db.withSession { implicit session: Session =>
    query.filter(_.id === c).firstOption
  }

  def all = db.withSession { implicit session: Session =>
    query.list
  }

  def findByTimes(t: OrdInt) = db.withSession { implicit session: Session =>
    // TODO
    query.list.filter { data =>
      data.id.times == t
    }
  }

  def search(times: String, prize: String, grade: String, tag: String) = db.withSession { implicit session: Session =>

    val q = for {
      t <- Tags if t.tag === tag
      data <- ClassData if data.id === t.classId
    } yield data

    q.list
  }

  def create(data: ClassData) = db.withSession { implicit session: Session =>
    ClassData.insert(data)
  }
}
