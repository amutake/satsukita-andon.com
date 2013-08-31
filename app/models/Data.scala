package models

import scala.slick.driver.H2Driver.simple._

import java.util.Date

import andon.utils._
import andon.utils.DateUtil.dateTypeMapper

case class Datum(id: Int, name: String, accountId: Int, date: Date, path: String, genre: String, optAuthor: Option[String], optDate: Option[String])

object Data extends Table[Datum]("DATA") {

  val db = DB.db

  def id = column[Int]("ID", O.NotNull, O.PrimaryKey, O.AutoInc)
  def name = column[String]("NAME", O.NotNull)
  def accountId = column[Int]("ACCOUNT_ID", O.NotNull)
  def date = column[Date]("DATE", O.NotNull)
  def path = column[String]("PATH", O.NotNull)
  def genre = column[String]("GENRE", O.NotNull)
  def optAuthor = column[Option[String]]("OPT_AUTHOR")
  def optDate = column[Option[String]]("OPT_DATE")

  def * = id ~ name ~ accountId ~ date ~ path ~ genre ~ optAuthor ~ optDate <> (
    Datum.apply _,
    Datum.unapply _
  )

  def ins = name ~ accountId ~ date ~ path ~ genre ~ optAuthor ~ optDate returning id

  val query = Query(Data)

  def dateSort(q: Query[Data.type, Datum]) =
    q.sortBy(_.date.desc).sortBy(_.optDate.desc.nullsFirst)

  def findById(id: Int): Option[Datum] = db.withSession { implicit session: Session =>
    query.where(_.id === id).firstOption
  }

  def all = db.withSession { implicit session: Session =>
    query.list
  }

  def dateSorted = db.withSession { implicit session: Session =>
    dateSort(query).list
  }

  def findByGenre(g: String) = db.withSession { implicit session: Session =>
    dateSort(query.where(_.genre === g)).list
  }

  def findByAccountId(a: Int) = db.withSession { implicit session: Session =>
    dateSort(query.where(_.accountId === a)).list
  }

  def create(name: String, accountId: Int, path: String, genre: String, optAuthor: Option[String], optDate: Option[String]) = db.withSession { implicit session: Session =>
    val date = new Date()
    Data.ins.insert(name, accountId, date, path, genre, optAuthor, optDate)
  }

  def update(id: Int, name: String, genre: String, optAuthor: Option[String], optDate: Option[String]) = db.withSession { implicit session: Session =>
    query.where(_.id === id).map(d => d.name ~ d.genre ~ d.optAuthor ~ d.optDate).update((name, genre, optAuthor, optDate))
  }

  def fileUpdate(id: Int, path: String) = db.withSession { implicit session: Session =>
    val date = new Date()
    query.where(_.id === id).map(d => d.date ~ d.path).update((date, path))
  }

  def delete(id: Int) = db.withSession { implicit session: Session =>
    query.where(_.id === id).delete
  }
}
