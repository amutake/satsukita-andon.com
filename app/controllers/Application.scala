package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._

import models._
import andon.utils._

object Application extends Controller with Authentication {

  def index = Action {
    Ok(views.html.index())
  }

  def info(page: Int) = Action { implicit request =>
    val count = 10
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
    val cs = Images.getTopImages(ids.sorted.map { id =>
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

  def about = Action {
    Ok(views.html.about())
  }

  def contact = Action {
    Ok(views.html.contact())
  }

  val commentForm = Form(
    tuple(
      "accountId" -> optional(number),
      "name" -> text.verifying(nonEmpty),
      "text" -> text.verifying(nonEmpty),
      "password" -> optional(text)
    )
  )

  def article(id: Long) = Action { implicit request =>
    Articles.findById(id).map { article =>
      Ok(views.html.article(article, commentForm, myAccount))
    }.getOrElse(NotFound(views.html.errors.notFound(request.path)))
  }

  def postComment(id: Long) = Action { implicit request =>
    Articles.findById(id).map { article =>

      def success = Redirect(routes.Application.article(id))

      def error(form: Form[(Option[Int], String, String, Option[String])]) = {
        BadRequest(views.html.article(article, form, myAccount))
      }

      commentForm.bindFromRequest.fold(
        error,
        result => result match {
          case (account, name, text, password) => {
            myAccount.map { acc =>
              if (account == Some(acc.id)) {
                Comments.create(article.id, account, acc.name, text, None)
                success
              } else {
                error(commentForm.fill(result).withGlobalError("送信されたアカウント情報が間違っています"))
              }
            }.getOrElse {
              account.map { _ =>
                error(commentForm.fill(result).withGlobalError("ログインしてください"))
              }.getOrElse {
                Comments.create(article.id, None, name, text, password)
                success
              }
            }
          }
        }
      )
    }.getOrElse {
      BadRequest(views.html.errors.badRequest("その記事は存在しません"))
    }
  }
}
