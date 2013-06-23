package controllers

import play.api._
import play.api.mvc._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def info = Action {
    Ok(views.html.info())
  }

  def gallery = Action {
    Ok(views.html.gallery())
  }

  def howto = Action {
    Ok(views.html.howto())
  }
}
