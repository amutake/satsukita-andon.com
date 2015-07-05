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

object ArtisanOtherImages extends Controller with ControllerUtils with Authentication {

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

      Notifier.notify(
        tweet = true,
        body = acc.name + "によりその他の画像が" + files.length + "枚追加されました",
        url = Some("/gallery/others")
      )

      Redirect(routes.Artisan.home).flashing(
        "success" -> "画像をアップロードしました。"
      )
    }
  }
}
