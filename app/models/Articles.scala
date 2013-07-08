package models

import play.api.db._
import play.api.Play.current

import scala.slick.driver.H2Driver.simple._
import scala.slick.lifted.MappedTypeMapper

import java.util.Date

import andon.utils._

case class Article(id: Long, authorId: Int, title: String, text: String, date: Date)

object Articles extends Table[Article]("ARTICLES") {

  val db = Database.forDataSource(DB.getDataSource("default"))

  implicit val dateTypeMapper = MappedTypeMapper.base[Date, Long](
    { d => d.getTime() },
    { l => new Date(l) }
  )

  def id = column[Long]("ID", O.NotNull, O.PrimaryKey, O.AutoInc)
  def authorId = column[Int]("AUTHOR_ID", O.NotNull)
  def title = column[String]("TITLE", O.NotNull)
  def text = column[String]("TEXT", O.NotNull)
  def date = column[Date]("DATE", O.NotNull)

  def * = id ~ authorId ~ title ~ text ~ date <> (Article.apply _, Article.unapply _)

  def ins = authorId ~ title ~ text ~ date returning id

  val query = Query(Articles)

  def findById(id: Long) = db.withSession { implicit session: Session =>
    query.filter(_.id === id).firstOption
  }

  def findByAuthorId(aId: Int) = db.withSession { implicit session: Session =>
    query.filter(_.authorId === aId).list
  }

  def create(authorId: Int, title: String, text: String) = db.withSession { implicit session: Session =>
    val date = new Date()
    Articles.ins.insert(authorId, title, text, date)
  }
}
