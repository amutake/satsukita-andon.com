package models

import play.api.libs.Codecs._

import scala.slick.driver.H2Driver.simple._

import java.util.Date

import andon.utils._
import andon.utils.DateUtil.dateTypeMapper

case class Comment(id: Long, articleId: Long, accountId: Option[Int], name: String, text: String, password: Option[String], createDate: Date, updateDate: Date)

object Comments extends Table[Comment]("COMMENTS") {

  val db = DB.db

  def id = column[Long]("ID", O.NotNull, O.PrimaryKey, O.AutoInc)
  def articleId = column[Long]("ARTICLE_ID", O.NotNull)
  def accountId = column[Option[Int]]("ACCOUNT_ID")
  def name = column[String]("NAME", O.NotNull)
  def text = column[String]("TEXT", O.NotNull)
  def password = column[Option[String]]("PASSWORD")
  def createDate = column[Date]("CREATE_DATE", O.NotNull)
  def updateDate = column[Date]("UPDATE_DATE", O.NotNull)

  def * = id ~ articleId ~ accountId ~ name ~ text ~ password ~ createDate ~ updateDate <> (Comment.apply _, Comment.unapply _)

  def ins = articleId ~ accountId ~ name ~ text ~ password ~ createDate ~ updateDate returning id

  val query = Query(Comments)

  def findById(id: Long): Option[Comment] = db.withSession { implicit session: Session =>
    query.where(_.id === id).firstOption
  }

  def findByArticleId(a: Long) = db.withSession { implicit session: Session =>
    query.where(_.articleId === a).list
  }

  def all = db.withSession { implicit session: Session =>
    query.sortBy(_.id.desc).list
  }

  def take(n: Int) = db.withSession { implicit session: Session =>
    query.sortBy(_.id.desc).take(n).list
  }

  def authenticate(id: Long, password: Option[String]) = db.withSession { implicit session: Session =>
    query.where(_.id === id).where(_.password === password.map(sha1(_))).firstOption
  }

  def create(article: Long, account: Option[Int], name: String, text: String, password: Option[String]) = db.withSession { implicit session: Session =>
    val date = new Date()
    val id = Comments.ins.insert(article, account, name, text, password.map(sha1(_)), date, date)
    Twitter.tweet(
      "記事『" + Articles.findTitleById(article) + "』に" + name + "さんのコメントが投稿されました",
      "/article/" + article + "#comment-" + id
    )
  }

  def update(id: Long, text: String) = db.withSession { implicit session: Session =>
    val date = new Date()
    query.where(_.id === id).firstOption.map { comment =>
      query.where(_.id === id).map(c => c.text ~ c.updateDate).update((text, date))
      Twitter.tweet(
        "記事『" + Articles.findTitleById(comment.articleId) + "』への" + comment.name + "さんのコメントが編集されました",
        "/article/" + comment.articleId + "#comment-" + id
      )
    }
  }

  def delete(id: Long) = db.withSession { implicit session: Session =>
    findById(id).map { comment =>
      query.where(_.id === id).delete
      Twitter.tweet(
        "記事『" + Articles.findTitleById(comment.articleId) + "』への" + comment.name + "さんのコメントが削除されました",
        ""
      )
    }
  }
}
