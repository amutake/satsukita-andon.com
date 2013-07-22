package controllers

import play.api._
import play.api.mvc._

import models._
import andon.utils._

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def info(page: Int) = Action {
    val count = 6
    val infos = Articles.findInfoByPage(page, count)
    val max = Articles.countPageByType(Info, count)
    Ok(views.html.info(infos, page, max))
  }

  def gallery = Action {
    val ts = Images.getTimesImages
    Ok(views.html.gallery.top(ts))
  }

  def search = Action {
    val times = TimesData.all
    val tags = Tags.all.map { t =>
      t.tag
    }.distinct
    Ok(views.html.gallery.search(times, tags))
  }

  def tags(tag: String) = Action {
    val ids = Tags.findClassIdByTag(tag)
    val cs = Images.getTopImages(ids.map { id =>
      ClassData.findByClassId(id)
    }.flatten)
    Ok(views.html.gallery.tags(tag, cs))
  }

  def timesGallery(t: OrdInt) = Action {
    val times = TimesData.findByTimes(t)
    if (times.isDefined) {
      val ps = Images.getClassTopImages(t)
      Ok(views.html.gallery.times(t.toString(), ps))
    } else {
      NotFound(views.html.errors.notFound("/gallery/" + t))
    }
  }

  def classGallery(t: OrdInt, g: Int, c: Int) = Action {
    val id = ClassId(t, g, c)
    ClassData.findByClassId(id).map { data =>
      val ft = Images.getClassImages(id).map { f =>
        (f, Images.toThumbnail(f))
      }
      Ok(views.html.gallery.classg(data, ft))
    }.getOrElse(NotFound)
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

  def howtoArticle(id: Long) = Action { request =>
    Articles.findById(id).map { article =>
      Ok(views.html.howto.article(article))
    }.getOrElse(NotFound(views.html.errors.notFound(request.path)))
  }

  def about = Action {
    Ok(views.html.about())
  }

  def contact = Action {
    Ok(views.html.contact())
  }
}
