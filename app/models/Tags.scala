package models

import play.api.db.DB
import play.api.Play.current

import scala.slick.driver.H2Driver.simple._

import andon.utils._

case class Tag(classId: ClassId, tag: String)

object Tags extends Table[Tag]("TAGS") {

  val db = Database.forDataSource(DB.getDataSource("default"))

  def classId = column[Int]("CLASS_ID", O.NotNull)
  def tag = column[String]("TAG", O.NotNull)

  def * = classId ~ tag <> (
    (classId, tag) => Tag(ClassId.fromId(classId), tag),
    tag => Some(tag.classId.toId, tag.tag)
  )

  def findByClassId(c: ClassId) = db.withSession { implicit session: Session =>
    Query(Tags).filter(_.classId === c.toId).list
  }

  def all = db.withSession { implicit session: Session =>
    Query(Tags).list
  }

  def findClassIdByTag(tag: String) = db.withSession { implicit session: Session =>
    Query(Tags).filter(_.tag === tag).list
  }

  def create(tag: Tag) = db.withSession { implicit session: Session =>
    Tags.insert(tag)
  }
}
