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

object ArtisanData extends Controller with ControllerUtils with Authentication {

  def data = IsValidAccount { acc => _ =>
    acc.level match {
      case Admin | Master => Ok(views.html.artisan.data(Data.dateSorted))
      case Writer => Ok(views.html.artisan.data(Data.findByAccountId(acc.id)))
    }
  }

  val datumForm = Form(
    tuple(
      "name" -> text.verifying(notEmpty),
      "genre" -> text.verifying(notEmpty),
      "optAuthor" -> optional(text),
      "optDate" -> optional(text)
    )
  )

  def uploadDatum = IsValidAccount { acc => _ =>
    Ok(views.html.artisan.uploadDatum(acc.level, datumForm))
  }

  def postUploadDatum = IsValidAccountWithParser(parse.multipartFormData) { acc => implicit request =>
    request.body.file("file").map { file =>
      datumForm.bindFromRequest().fold(
        formWithErrors => BadRequest(views.html.artisan.uploadDatum(acc.level, formWithErrors)),
        result => {
          val now = new Date()
          def valid(c: Char) = {
            val r = """[a-zA-Z0-9\.-]""".r
            c.toString match {
              case r() => true
              case _ => false
            }
          }
          val path = "/files/data/" + now.getTime().toString + "-" + file.filename.filter(valid)
          file.ref.moveTo(new File("." + path), true)
          Data.create(result._1, acc.id, path, result._2, result._3, result._4)
          Redirect(routes.Artisan.home).flashing(
            "success" -> "資料をアップロードしました。"
          )
        }
      )
    }.getOrElse {
      BadRequest(views.html.artisan.uploadDatum(acc.level, datumForm.withGlobalError("ファイルのアップロードに失敗しました。")))
    }
  }

  def editDatum(id: Int) = IsEditableDatum(id) { acc => datum => request =>
    val data = (datum.name, datum.genre, datum.optAuthor, datum.optDate)
    Ok(views.html.artisan.editDatum(id, acc.level, datumForm.fill(data)))
  }

  def postEditDatum(id: Int) = IsEditableDatum(id) { acc => _ => implicit request =>
    datumForm.bindFromRequest().fold(
      formWithErrors => BadRequest(views.html.artisan.editDatum(id, acc.level, formWithErrors)),
      result => {
        Data.update(id, result._1, result._2, result._3, result._4)
        request.body.asMultipartFormData.flatMap { fd =>
          fd.file("file").map { file =>
            val now = new Date()
            def valid(c: Char) = {
              val r = """[a-zA-Z0-9\.-]""".r
              c.toString match {
                case r() => true
                case _ => false
              }
            }
            val path = "/files/data/" + now.getTime().toString + "-" + file.filename.filter(valid)
            file.ref.moveTo(new File("." + path), true)
            Data.fileUpdate(id, path)
            Redirect(routes.Artisan.home).flashing(
              "success" -> "資料を更新しました。"
            )
          }
        }.getOrElse(Redirect(routes.Artisan.home).flashing(
          "success" -> "資料情報を編集しました。"
        ))
      }
    )
  }

  def deleteDatum(id: Int) = IsEditableDatum(id) { acc => datum => _ =>
    Data.delete(id)
    new File("." + datum.path).delete()
    Redirect(routes.Artisan.home).flashing(
      "success" -> "資料を削除しました。"
    )
  }
}
