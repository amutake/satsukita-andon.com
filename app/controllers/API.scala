package controllers

import play.api._
import play.api.mvc._
import play.api.libs.Files
import play.api.libs.json.Json

import java.io.File
import java.util.Date

import scala.sys.process._

import models._
import andon.utils._

object API extends Controller with Authentication {

  // TODO
  def search(times: String, prize: String, grade: String, tag: String) = Action {

    val sr = ClassData.search(times, prize, grade, tag)
    val cps = Images.getTopImages(sr)
    val json = Json.toJson(
      cps.map { cp =>
        Json.toJson(
          Map(
            "id" -> Json.toJson(cp._1.id.toInt),
            "times" -> Json.toJson(cp._1.id.times.toString),
            "grade" -> Json.toJson(cp._1.id.grade),
            "classn" -> Json.toJson(cp._1.id.classn),
            "title" -> Json.toJson(cp._1.title),
            "prize" -> Json.toJson(cp._1.prize.map(_.toJapanese).getOrElse("")),
            "thumbnail" -> Json.toJson(cp._2)
          )
        )
      }
    )
    Ok(json)
  }

  def searchByTimes(times: Int) = Action {
    val cs = ClassData.findByTimes(OrdInt(times))
    val cps = Images.getTopImagesOption(cs)
    val json = Json.toJson(
      cps.map { cp =>
        Json.toJson(
          Map(
            "id" -> Json.toJson(cp._1.id.toInt),
            "times" -> Json.toJson(cp._1.id.times.toString),
            "grade" -> Json.toJson(cp._1.id.grade),
            "classn" -> Json.toJson(cp._1.id.classn),
            "title" -> Json.toJson(cp._1.title),
            "prize" -> Json.toJson(cp._1.prize.map(_.toJapanese).getOrElse("")),
            "thumbnail" -> Json.toJson(cp._2.getOrElse("")),
            "fullsize" -> Json.toJson(cp._2.map(Images.toFullsize _).getOrElse(""))
          )
        )
      }
    )
    Ok(json)
  }

  def upload = IsValidAccountWithParser(parse.multipartFormData) { acc => request =>
    request.body.file("file").map { file =>
      if (file.contentType.map(_.take(5)) == Some("image")) {
        val fullsize = "/files/images/fullsize/"
        val thumbnail = "/files/images/thumbnail/"
        def valid(c: Char) = {
          val r = """[a-zA-Z0-9\.-]""".r
          c.toString match {
            case r() => true
            case _ => false
          }
        }
        val filename = new Date().getTime().toString + "-" + file.filename.filter(valid)

        file.ref.moveTo(new File("." + fullsize + filename), true)
        Files.copyFile(new File("." + fullsize + filename), new File("." + thumbnail + filename))

        Process("mogrify -quality 50 ." + fullsize + filename) !

        Process("mogrify -resize 320x -unsharp 2x1.2+0.5+0.5 -quality 75 ." + thumbnail + filename) !

        Ok(Json.toJson(Map(
          "status" -> "success",
          "path" -> (fullsize + filename),
          "thumbnail" -> (thumbnail + filename))))
      } else {
        BadRequest(Json.toJson(Map("status" -> "error", "message" -> "画像ではありません。")))
      }
    }.getOrElse {
      BadRequest(Json.toJson(Map("status" -> "error", "message" -> "ファイルを送信出来ませんでした。")))
    }
  }

  def deleteComment(id: Long, password: String) = Action {
    if (Comments.authenticate(id, Some(password)).isDefined) {
      Comments.delete(id)
      Ok("true")
    } else {
      Ok("false")
    }
  }
}
