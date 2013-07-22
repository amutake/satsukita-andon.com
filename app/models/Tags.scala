package models

import scala.slick.driver.H2Driver.simple._

import andon.utils._

case class Tag(id: Long, classId: ClassId, tag: String)

object Tags extends Table[Tag]("TAGS") {

  val db = DB.db

  def id = column[Long]("ID", O.NotNull, O.PrimaryKey, O.AutoInc)
  def classId = column[ClassId]("CLASS_ID", O.NotNull)
  def tag = column[String]("TAG", O.NotNull)

  def * = id ~ classId ~ tag <> (Tag.apply _, Tag.unapply _)

  def ins = classId ~ tag returning id

  val query = Query(Tags)

  def findById(id: Long) = db.withSession { implicit session: Session =>
    query.where(_.id === id).firstOption
  }

  def findByClassId(c: ClassId) = db.withSession { implicit session: Session =>
    query.where(_.classId === c).list
  }

  def all = db.withSession { implicit session: Session =>
    query.list
  }

  def findClassIdByTag(tag: String) = db.withSession { implicit session: Session =>
    query.where(_.tag === tag).map(_.classId).list
  }

  def create(classId: ClassId, tag: String) = db.withSession { implicit session: Session =>
    Tags.ins.insert(classId, tag)
  }

  def delete(id: Long) = db.withSession { implicit session: Session =>
    query.where(_.id === id).delete
  }
}
