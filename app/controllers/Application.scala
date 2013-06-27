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

  def timesGallery(t: OrdInt) = Action {
    val ps = Images.getTimesImages(t)
    Ok(views.html.timesGallery(t.toString(), ps))
  }

  def classGallery(t: OrdInt, g: Int, c: Int) = Action {
    val ps = Images.getClassImages(t, g, c)
    Ok(views.html.classGallery(t.toString, g.toString, c.toString, ps))
  }

  def howto = Action {
    Ok(views.html.howto())
  }
}
