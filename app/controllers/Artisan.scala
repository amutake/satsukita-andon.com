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
    ) verifying ("Invalid username or password", result => result match {
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

  def home = IsAuthenticated { username => _ =>
    Artisans.findByUsername(username).map { artisan =>
      Ok(views.html.artisan.home(artisan))
    }.getOrElse(Forbidden)
  }
}
