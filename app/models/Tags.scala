package models

import play.api.db.DB
import play.api.Play.current

import scala.slick.driver.H2Driver.simple._

import andon.utils._

case class Tag(classId: Int, tag: String)

object Tags extends Table[Tag]("TAGS") {

  val db = Database.forDataSource(DB.getDataSource("default"))

  def classId = column[Int]("CLASS_ID", O.NotNull)
  def tag = column[String]("TAG", O.NotNull)

  def * = classId ~ tag <> (Tag.apply _, Tag.unapply _)

  def findByClassId(c: ClassId) = db.withSession { implicit session: Session =>
    (for {
      t <- Tags if t.classId === c.toId
    } yield t).list
  }

  def all = db.withSession { implicit session: Session =>
    Query(Tags).list
  }

  def findClassIdByTag(tag: String) = db.withSession { implicit session: Session =>
    (for {
      t <- Tags if t.tag === tag
    } yield t.classId).list.map(ClassId.fromId)
  }
}
