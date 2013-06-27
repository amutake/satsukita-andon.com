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
    val ts = Images.getGrandImages
    Ok(views.html.gallery.top(ts))
  }

  def timesGallery(t: OrdInt) = Action {
    val ps = Images.getTimesImages(t)
    Ok(views.html.gallery.times(t.toString(), ps))
  }

  def classGallery(t: OrdInt, g: Int, c: Int) = Action {
    val data = ClassData.findById(t, g, c)
    // TODO
    if (data.isDefined) {
      val ps = Images.getClassImages(t, g, c)
      Ok(views.html.gallery.classg(data.get, ps))
    } else {
      NotFound
    }
  }

  def howto = Action {
    Ok(views.html.howto.top())
  }

  def eachHowto(page: String) = Action {
    page match {
      case "intro" => Ok(views.html.howto.intro())
      case _ => NotFound
    }
  }
}
