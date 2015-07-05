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

object ArtisanTimesData extends Controller with ControllerUtils with Authentication {

  def timesData = HasAuthority(Master) { _ => implicit request =>
    Ok(views.html.artisan.timesData())
  }

  val timesForm = Form(single("title" -> text))

  def editTimesData(id: Int) = AboutTimes(id) { _ => data => _ =>
    Ok(views.html.artisan.editTimesData(data.times, timesForm.fill(data.title)))
  }

  def postEditTimesData(id: Int) = AboutTimes(id) { acc => data => implicit request =>
    timesForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.artisan.editTimesData(data.times, formWithErrors)),
      title => {
        TimesData.update(data.times, title)
        Twitter.tweet(
          acc.name + "により" + data.times + "の情報が編集されました",
          "/gallery"
        )
        request.body.asMultipartFormData.flatMap { fd =>
          fd.file("top").map { file =>
            // TODO: check file extension
            val path = "./files/grands/" + data.times + ".jpg"
            file.ref.moveTo(new File(path), true)

            Process("mogrify -resize 320x -unsharp 2x1.2+0.5+0.5 -quality 75 " + path).!
            Redirect(routes.ArtisanTimesData.timesData).flashing(
              "success" -> "編集しました"
            )
          }
        }.getOrElse(Redirect(routes.ArtisanTimesData.timesData).flashing(
          "success" -> "編集しました"
        ))
      }
    )
  }

  val timesBaseForm = Form(single(
    "times" -> number(min = 1)
  ))

  def createTimes = HasAuthority(Master) { _ => _ =>
    Ok(views.html.artisan.createTimes(timesBaseForm))
  }

  def postCreateTimes = HasAuthority(Master) { acc => implicit request =>
    timesBaseForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.artisan.createTimes(formWithErrors)),
      n => {
        val times = OrdInt(n)
        TimesData.findByTimes(times).map { _ =>
          BadRequest(views.html.artisan.createTimes(
            timesBaseForm.fill(n).withGlobalError("その回は存在しています。")
          ))
        }.getOrElse {
          TimesData.createByTimes(times)
          Twitter.tweet(
            acc.name + "により" + times + "が作成されました",
            "/gallery"
          )
          Redirect(routes.ArtisanTimesData.timesData).flashing(
            "success" -> "回を作成しました。"
          )
        }
      }
    )
  }
}
