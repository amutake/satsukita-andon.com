package controllers

import play.api._
import play.api.mvc._

import models._

object Artisan extends Controller with Authentication {

  val loginForm = Form(
    tuple(
      "username" -> text,
      "password" -> text
    ) verifying ("Invalid username or password", result => result match {
      case (username, password) => Artisan.authenticate(username, password).isDefined
    })
  )

  def login = Action {
    Ok(views.html.artisan.login(loginForm))
  }

  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.login(formWithErrors)),
      artisan => Redirect(routes.Artisan.home).withSession("username" -> artisan._1)
    )
  }
}
