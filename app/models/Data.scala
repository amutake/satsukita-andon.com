package models

import scala.slick.driver.H2Driver.simple._

import java.util.Date

import andon.utils._
import andon.utils.DateUtil.dateTypeMapper

case class Datum(id: Int, name: String, accountId: Int, date: Date, path: String, genre: String)

object Data extends Table[Datum]("DATA") {

  val db = DB.db

  def id = column[Int]("ID", O.NotNull, O.PrimaryKey, O.AutoInc)
  def name = column[String]("NAME", O.NotNull)
  def accountId = column[Int]("ACCOUNT_ID", O.NotNull)
  def date = column[Date]("DATE", O.NotNull)
  def path = column[String]("PATH", O.NotNull)
  def genre = column[String]("GENRE", O.NotNull)

  def * = id ~ name ~ accountId ~ date ~ path ~ genre <> (
    Datum.apply _,
    Datum.unapply _
  )

  def ins = name ~ accountId ~ date ~ path ~ genre returning id

  val query = Query(Data)

  def findById(id: Int): Option[Datum] = db.withSession { implicit session: Session =>
    query.filter(_.id === id).firstOption
  }

  def all = db.withSession { implicit session: Session =>
    query.list
  }

  def findByGenre(g: String) = db.withSession { implicit session: Session =>
    query.filter(_.genre === g).list
  }

  def create(name: String, accountId: Int, path: String, genre: String) = db.withSession { implicit session: Session =>
    val date = new Date()
    Data.ins.insert(name, accountId, date, path, genre)
  }

  def update(id: Int, name: String, genre: String) = db.withSession { implicit session: Session =>
    val target = query.filter(_.id === id)
    target.map(_.name).update(name)
    target.map(_.genre).update(genre)
  }

  def delete(id: Int) = db.withSession { implicit session: Session =>
    query.filter(_.id === id).delete
  }
}
