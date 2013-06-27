package controllers

import play.api._
import play.api.mvc._

import models._

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

  def timesGallery(times: Int) = Action {
    val cs = ClassData.findByTimes(times)
    val ps = cs.map { c =>
      Images.getClassImages(c.times, c.grade, c.classn).head
    }
    Ok(views.html.timesGallery(Util.toTimesStr(times), ps))
  }


  def howto = Action {
    Ok(views.html.howto())
  }
}
