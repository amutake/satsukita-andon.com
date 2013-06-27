package controllers

import play.api._
import play.api.mvc._

import models._
import andon.utils._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def info = Action {
    Ok(views.html.info())
  }

  def gallery = Action {
    val ts = TimesData.findAll
    Ok(views.html.gallery(ts))
  }

  def timesGallery(times: OrdInt) = Action {
    val ps = Images.getTimesImages(times)
    Ok(views.html.timesGallery(times.toString(), ps))
  }

  def howto = Action {
    Ok(views.html.howto())
  }
}
