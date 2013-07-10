package controllers

import scala.util.Random

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import models._
import andon.utils._

object Artisan extends Controller with Authentication {

  val loginForm = Form(
    tuple(
      "username" -> text,
      "password" -> text
    ) verifying ("ユーザー名かパスワードが間違っています。", result => result match {
      case (username, password) => Accounts.authenticate(username, password).isDefined
    })
  )

  def login = Action { implicit request =>
    Ok(views.html.artisan.login(loginForm))
  }

  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.artisan.login(formWithErrors)),
      { account =>
        Accounts.findByUsername(account._1).map { account_ =>
          Redirect(routes.Artisan.home).withSession("userid" -> account_.id.toString)
        }.getOrElse(Forbidden)
      }
    )
  }

  def logout = Action {
    Redirect(routes.Artisan.login).withNewSession.flashing(
      "success" -> "ログアウトしました。"
    )
  }

  def home = IsAuthenticated { userid => implicit request =>
    Accounts.findById(userid).map { account =>
      Ok(views.html.artisan.home(account))
    }.getOrElse(Forbidden)
  }

  def articles = IsAuthenticated { userid => _ =>
    Accounts.findById(userid).map { account =>
      account.level match {
        case Admin | Master => Ok(views.html.artisan.articles(account, Articles.all))
        case Writer => Ok(views.html.artisan.articles(account, Articles.findByCreateAccountId(userid)))
      }
    }.getOrElse(Forbidden)
  }

  def article(id: Long) = IsAuthenticated { userid => _ =>
    Accounts.findById(userid).map { account =>
      Articles.findById(id).map { article =>
        Ok(views.html.artisan.article(account, article))
      }.getOrElse(NotFound(views.html.errors.notFound("/artisan/article?id=" + id.toString)))
    }.getOrElse(Forbidden)
  }

  val articleForm = Form(
    tuple(
      "title" -> text,
      "text" -> text,
      "type" -> text
    ) verifying ("タイトルまたは本文が空です。", result => result match {
      case ("", _, _) => false
      case (_, "", _) => false
      case (_, _, _) => true
    })
  )

  def createArticle = IsAuthenticated { _ => _ =>
    Ok(views.html.artisan.createArticle(articleForm))
  }

  def postCreateArticle = IsAuthenticated { userid => implicit request =>
    articleForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.artisan.createArticle(formWithErrors)),
      { article =>
        Articles.create(userid, article._1, article._2, ArticleType.fromString(article._3))
        Redirect(routes.Artisan.articles)
      }
    )
  }

  val accountForm = Form(
    tuple(
      "name" -> text,
      "username" -> text,
      "times" -> number,
      "type" -> text
    ) verifying ("空の項目があります。", result => result match {
      case (u, n, t, a) if u.trim.isEmpty || n.trim.isEmpty || a.trim.isEmpty => false
      case _ => true
    })
  )

  def createAccount = IsAuthenticated { userid => _ =>
    Accounts.findById(userid).map { account =>
      account.level match {
        case Admin | Master => Ok(views.html.artisan.createAccount(accountForm))
        case Writer => Redirect(routes.Artisan.home)
      }
    }.getOrElse(Forbidden)
  }

  def postCreateAccount = IsAuthenticated { userid => implicit request =>
    Accounts.findById(userid).map { account =>
      account.level match {
        case Admin | Master => accountForm.bindFromRequest.fold(
          formWithErrors => BadRequest(views.html.artisan.createAccount(formWithErrors)),
          { newAccount =>
            Accounts.create(newAccount._1, newAccount._2, Random.nextString(9), OrdInt(newAccount._3.toInt), AccountLevel.fromString(newAccount._4))
            Redirect(routes.Artisan.home).flashing(
              "success" -> "アカウントを作成しました。"
            )
          }
        )
      }
    }.getOrElse(Forbidden)
  }
}
