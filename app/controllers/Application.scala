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
    val times = TimesData.findById(t)
    if (times.isDefined) {
      val ps = Images.getTimesImages(t)
      Ok(views.html.gallery.times(t.toString(), ps))
    } else {
      NotFound(views.html.errors.notFound("/gallery/" + t))
    }
  }

  def classGallery(t: OrdInt, g: Int, c: Int) = Action {
    val data = ClassData.findById(t, g, c)
    // TODO
    if (data.isDefined) {
      val ps = Images.getClassImages(t, g, c).map { p =>
        (p, Images.toThumbnail(p))
      }
      Ok(views.html.gallery.classg(data.get, ps))
    } else {
      NotFound(views.html.errors.notFound("/gallery/" + t + "/" + g + "/" + c))
    }
  }

  def howto = Action {
    Ok(views.html.howto.top())
  }

  def eachHowto(page: String) = Action {
    page match {
      case "intro" => Ok(views.html.howto.intro())
      case "offseason" => Ok(views.html.howto.offseason())
      case "organize" => Ok(views.html.howto.organize())
      case "scheduling" => Ok(views.html.howto.scheduling())
      case "face" => Ok(views.html.howto.face())
      case "harigane" => Ok(views.html.howto.harigane())
      case "manual_d_choki" => Ok(views.html.howto.manual_d_choki())
      case "kamihari" => Ok(views.html.howto.kamihari())
      case "daizai" => Ok(views.html.howto.daizai())
      case "susume" => Ok(views.html.howto.susume())
      case "howtomake" => Ok(views.html.howto.howtomake())
      case "tools" => Ok(views.html.howto.tools())
      case "lumber" => Ok(views.html.howto.lumber())
      case "sketchup" => Ok(views.html.howto.sketchup())
      case _ => NotFound(views.html.errors.notFound("/howto/" + page))
    }
  }

  def about = Action {
    Ok(views.html.about())
  }

  def contact = Action {
    Ok(views.html.contact())
  }
}
