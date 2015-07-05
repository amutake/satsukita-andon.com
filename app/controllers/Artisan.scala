package controllers

import java.io.File
import java.util.Date

import scala.sys.process._
import scala.util.Random

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.libs.Files
import play.api.libs.json._
import play.api.libs.functional.syntax._

import models._
import andon.utils._

object Artisan extends Controller with ControllerUtils with Authentication {

  val loginForm = Form(
    tuple(
      "username" -> text.verifying(notEmpty).verifying(pattern("""(\w|-)+""".r, error = "半角英数字・ハイフン・アンダースコアのみです。")),
      "password" -> text.verifying(notEmpty)
    ) verifying ("ユーザー名かパスワードが間違っています。", result => result match {
      case (username, password) => Accounts.authenticate(username, password).isDefined
    })
  )

  def login = Action { implicit request =>
    Ok(views.html.artisan.login(loginForm))
  }

  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.artisan.login(formWithErrors)),
      { account =>
        Accounts.findByUsername(account._1).map { account_ =>
          Redirect(routes.Artisan.home).withSession("userid" -> account_.id.toString)
        }.getOrElse(Forbidden(views.html.errors.forbidden()))
      }
    )
  }

  def logout = Action {
    Redirect(routes.Artisan.login).withNewSession.flashing(
      "success" -> "ログアウトしました。"
    )
  }

  def home = IsValidAccount { account => implicit request =>
    Ok(views.html.artisan.home(account))
  }

  def myAccount = IsValidAccount { acc => _ =>
    Ok(views.html.artisan.myAccount(acc))
  }

  def uploadOtherImages = IsValidAccount { _ => _ =>
    Ok(views.html.artisan.uploadOtherImages())
  }

  def postUploadOtherImages = IsValidAccountWithParser(parse.multipartFormData) { acc => request =>
    val files = request.body.files.filter { file =>
      file.contentType.map(_.take(5)) == Some("image")
    }
    if (files.length != request.body.files.length) {
      Redirect(routes.Artisan.home).flashing(
        "error" -> "画像ではないファイルが含まれています。"
      )
    } else {
      files.foreach { file =>
        val fullsize = "/files/gallery/fullsize/others/"
        val thumbnail = "/files/gallery/thumbnail/others/"
        def valid(c: Char) = {
          val r = """[\w\.]""".r
          c.toString match {
            case r() => true
            case _ => false
          }
        }
        val filename = new Date().getTime().toString + "-" + file.filename.filter(valid)

        file.ref.moveTo(new File("." + fullsize + filename), true)
        Files.copyFile(new File("." + fullsize + filename), new File("." + thumbnail + filename))

        Process("mogrify -quality 50 ." + fullsize + filename).!
        Process("mogrify -resize 600x -unsharp 2x1.2+0.5+0.5 -quality 75 ." + thumbnail + filename).!
      }

      Twitter.tweet(
        acc.name + "によりその他の画像が" + files.length + "枚追加されました",
        "/gallery/others"
      )

      Redirect(routes.Artisan.home).flashing(
        "success" -> "画像をアップロードしました。"
      )
    }
  }
}
