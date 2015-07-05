package controllers

import java.io.File
import java.util.Date

import scala.sys.process._
import scala.util.Random

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.libs.Files
import play.api.libs.json._
import play.api.libs.functional.syntax._

import models._
import andon.utils._

object ArtisanArticle extends Controller with ControllerUtils with Authentication {

  def articles = IsValidAccount { account => implicit request =>
    Ok(views.html.artisan.articles(account))
  }

  def article(id: Long) = IsEditableArticle(id) { _ => article => implicit request =>
    Ok(views.html.artisan.article(article))
  }

  val articleForm = Form(
    tuple(
      "title" -> text.verifying(notEmpty),
      "text" -> text.verifying(pattern("""[\s\S]+""".r, error = "本文を入力してください")),
      "type" -> text.verifying(notEmpty).verifying(pattern(ArticleType.all.mkString("|").r, error = "不正な入力です。")),
      "genre" -> text,
      "optAuthor" -> optional(text),
      "optDate" -> optional(text),
      "editable" -> boolean
    )
  )

  def creatable(level: AccountLevel) = level match {
    case Admin | Master => List(Info, Howto)
    case Writer => List(Howto)
  }

  def createArticle = IsValidAccount { acc => _ =>
    Ok(views.html.artisan.createArticle(acc.level, creatable(acc.level), articleForm))
  }

  def postCreateArticle = IsValidAccount { acc => implicit request =>
    articleForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.artisan.createArticle(acc.level, creatable(acc.level), formWithErrors)),
      { article =>
        val id = Articles.create(acc.id, article._1, article._2, ArticleType.fromString(article._3), article._4, article._5, article._6, article._7)
        History.create(id, article._2, acc.id)
        Redirect(routes.ArtisanArticle.article(id)).flashing(
          "success" -> "記事を作成しました。"
        )
      }
    )
  }

  def editArticle(id: Long) = IsEditableArticle(id) { acc => art => _ =>
    val data = (art.title, art.text, art.articleType.toString, art.genre, art.optAuthor, art.optDate, art.editable)
    Ok(views.html.artisan.editArticle(acc, id, art.createAccountId, articleForm.fill(data)))
  }

  def postEditArticle(id: Long) = IsEditableArticle(id) { acc => art => implicit request =>
    articleForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.artisan.editArticle(acc, id, art.createAccountId, formWithErrors)),
      article => {
        Articles.update(id, acc.id, article._1, article._2, article._4, article._5, article._6, article._7)
        History.update(id, article._2, acc.id)
        Redirect(routes.ArtisanArticle.article(id)).flashing(
          "success" -> "記事を編集しました。"
        )
      }
    )
  }

  val previewForm = Form(
    "text" -> text
  )

  def preview = IsValidAccount { acc => implicit request =>

    previewForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.errors.error(new Exception("予期しないエラー"))),
      preview => {
        Ok(views.html.artisan.preview(preview)).withHeaders(
          // for Google Chrome
          // http://stackoverflow.com/questions/17016960/google-chromes-xss-auditor-causing-issues
          "X-XSS-Protection" -> "0"
        )
      }
    )
  }

  def deleteArticle(id: Long) = IsEditableArticle(id) { acc => art => _ =>
    if (acc.level == Writer && acc.id != art.createAccountId) {
      Forbidden(views.html.errors.forbidden())
    } else {
      Articles.delete(id)
      History.delete(id, acc.id)
      Redirect(routes.ArtisanArticle.articles).flashing(
        "success" -> "記事を削除しました"
      )
    }
  }

  def history(id: Long) = IsEditableArticle(id) { acc => art => _ =>
    val hists = History.histories(id)
    Ok(views.html.artisan.history(hists, art))
  }

  def historyContent(id: Long, commitId: String) = IsEditableArticle(id) { acc => art => implicit request =>
    History.history(id, commitId).map { hist =>
      Ok(views.html.artisan.historyContent(hist, art))
    }.getOrElse(NotFound(views.html.errors.notFound(request.path)))
  }

  def historySource(id: Long, commitId: String) = IsEditableArticle(id) { acc => art => implicit request =>
    History.history(id, commitId).map { hist =>
      Ok(views.html.artisan.historySource(hist, art))
    }.getOrElse(NotFound(views.html.errors.notFound(request.path)))
  }

  def diff(id: Long, commitId: String) = IsEditableArticle(id) { acc => art => implicit request =>
    val result = for {
      article <- Articles.findById(id)
      newHistory <- History.history(id, commitId)
      oldHistory = History.previousHistory(id, commitId)
    } yield Ok(views.html.artisan.diff(newHistory, oldHistory, article))
    result.getOrElse(NotFound(views.html.errors.notFound(request.path)))
  }
}
