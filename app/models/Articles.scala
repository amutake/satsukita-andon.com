package models

import play.api.db._
import play.api.Play.current

import scala.slick.driver.H2Driver.simple._
import scala.slick.lifted.MappedTypeMapper

import java.util.Date

import andon.utils._

case class Article(id: Long, createAccountId: Int, updateAccountId: Int, title: String, text: String, createDate: Date, updateDate: Date, articleType: ArticleType, genre: String)

object Articles extends Table[Article]("ARTICLES") {

  val db = Database.forDataSource(DB.getDataSource("default"))

  implicit val dateTypeMapper = MappedTypeMapper.base[Date, Long](
    { d => d.getTime() },
    { l => new Date(l) }
  )

  def id = column[Long]("ID", O.NotNull, O.PrimaryKey, O.AutoInc)
  def createAccountId = column[Int]("CREATE_ACCOUNT_ID", O.NotNull)
  def updateAccountId = column[Int]("UPDATE_ACCOUNT_ID", O.NotNull)
  def title = column[String]("TITLE", O.NotNull)
  def text = column[String]("TEXT", O.NotNull)
  def createDate = column[Date]("CREATE_DATE", O.NotNull)
  def updateDate = column[Date]("UPDATE_DATE", O.NotNull)
  def articleType = column[ArticleType]("ARTICLE_TYPE", O.NotNull)
  def genre = column[String]("GENRE", O.NotNull)

  def * = id ~ createAccountId ~ updateAccountId ~ title ~ text ~ createDate ~ updateDate ~ articleType ~ genre <> (Article.apply _, Article.unapply _)

  def ins = createAccountId ~ updateAccountId ~ title ~ text ~ createDate ~ updateDate ~ articleType ~ genre returning id

  val query = Query(Articles)

  def findById(id: Long) = db.withSession { implicit session: Session =>
    query.filter(_.id === id).firstOption
  }

  def findByCreateAccountId(aId: Int) = db.withSession { implicit session: Session =>
    query.filter(_.createAccountId === aId).list
  }

  def findByType(t: ArticleType) = db.withSession { implicit session: Session =>
    query.filter(_.articleType === t).list
  }

  def findByGenre(g: String) = db.withSession { implicit session: Session =>
    query.filter(_.genre === g).list
  }

  def all = db.withSession { implicit session: Session =>
    query.list
  }

  def create(accountId: Int, title: String, text: String, articleType: ArticleType, genre: String) = db.withSession { implicit session: Session =>
    val date = new Date()
    Articles.ins.insert(accountId, accountId, title, text, date, date, articleType, genre)
  }

  def update(id: Long, accountId: Int, title: String, text: String, genre: String) = db.withSession { implicit session: Session =>
    findById(id).map { before =>
      val date = new Date()
      val after = before.copy(updateAccountId = accountId, title = title, text = text, updateDate = date, genre = genre)
      query.filter(_.id === id).update(after)
    }
  }

  def delete(id: Long) = db.withSession { implicit session: Session =>
    query.filter(_.id === id).delete
  }
}
