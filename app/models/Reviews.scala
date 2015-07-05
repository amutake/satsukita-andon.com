package models

import scala.slick.driver.H2Driver.simple._

import andon.utils._

case class Review(id: Long, classId: ClassId, accountId: Int, text: String)

object Reviews extends Table[Review]("REVIEWS") {

  val db = DB.db

  def id = column[Long]("ID", O.NotNull, O.PrimaryKey, O.AutoInc)
  def classId = column[ClassId]("CLASS_ID", O.NotNull)
  def accountId = column[Int]("ACCOUNT_ID", O.NotNull)
  def text = column[String]("TEXT", O.NotNull)

  def * = id ~ classId ~ accountId ~ text <> (Review.apply _, Review.unapply _)

  def ins = classId ~ accountId ~ text returning id

  val query = Query(Reviews)

  def findById(id: Long): Option[Review] = db.withSession { implicit session: Session =>
    query.where(_.id === id).firstOption
  }

  def findByClassId(c: ClassId) = db.withSession { implicit session: Session =>
    query.where(_.classId === c).list
  }

  def findByClassIdAccountId(c: ClassId, a: Int) = db.withSession { implicit session: Session =>
    query.where(_.classId === c).where(_.accountId === a).firstOption
  }

  def create(c: ClassId, a: Int, t: String) = db.withSession { implicit session: Session =>
    Reviews.ins.insert(c, a, t)
  }

  def update(id: Long, text: String) = db.withSession { implicit session: Session =>
    findById(id).map { review =>
      query.where(_.id === id).map(_.text).update(text)
    }
  }

  def delete(id: Long) = db.withSession { implicit session: Session =>
    findById(id).map { review =>
      query.where(_.id === id).delete
    }
  }
}
