package models

import scala.slick.driver.H2Driver.simple._

import andon.utils._

case class Tag(classId: ClassId, tag: String)

object Tags extends Table[Tag]("TAGS") {

  val db = DB.db

  def classId = column[ClassId]("CLASS_ID", O.NotNull)
  def tag = column[String]("TAG", O.NotNull)

  def * = classId ~ tag <> (Tag.apply _, Tag.unapply _)

  val query = Query(Tags)

  def findByClassId(c: ClassId) = db.withSession { implicit session: Session =>
    query.where(_.classId === c).list
  }

  def all = db.withSession { implicit session: Session =>
    query.list
  }

  def findClassIdByTag(tag: String) = db.withSession { implicit session: Session =>
    query.where(_.tag === tag).list
  }

  def create(tag: Tag) = db.withSession { implicit session: Session =>
    Tags.insert(tag)
  }
}
