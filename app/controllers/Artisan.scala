package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import models._

object Artisan extends Controller with Authentication {

  val loginForm = Form(
    tuple(
      "username" -> text,
      "password" -> text
    ) verifying ("ユーザー名かパスワードが間違っています。", result => result match {
      case (username, password) => Artisans.authenticate(username, password).isDefined
    })
  )

  def login = Action { implicit request =>
    Ok(views.html.artisan.login(loginForm))
  }

  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.artisan.login(formWithErrors)),
      artisan => Redirect(routes.Artisan.home).withSession("username" -> artisan._1)
    )
  }

  def logout = Action {
    Redirect(routes.Artisan.login).withNewSession.flashing(
      "success" -> "ログアウトしました。"
    )
  }

  def home = IsAuthenticated { username => _ =>
    Artisans.findByUsername(username).map { artisan =>
      Ok(views.html.artisan.home(artisan))
    }.getOrElse(Forbidden)
  }

  def articles = IsAuthenticated { username => _ =>
    Artisans.findByUsername(username).map { artisan =>
      val articles = Articles.findByAuthorId(artisan.id)
      Ok(views.html.artisan.articles(artisan, articles))
    }.getOrElse(Forbidden)
  }

  val articleForm = Form(
    tuple(
      "title" -> text,
      "text" -> text
    ) verifying ("タイトルまたは本文が空です。", result => result match {
      case ("", _) => false
      case (_, "") => false
      case (_, _) => true
    })
  )

  def createArticle = IsAuthenticated { username => _ =>
    Ok(views.html.artisan.createArticle(articleForm))
  }

  def postCreateArticle = IsAuthenticated { username => implicit request =>
    Artisans.findByUsername(username).map { artisan =>
      articleForm.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.artisan.createArticle(formWithErrors)),
        { article =>
          Articles.create(artisan.id, article._1, article._2)
          Redirect(routes.Artisan.articles)
        }
      )
    }.getOrElse(Forbidden)
  }
}
