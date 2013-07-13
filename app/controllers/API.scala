package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json.Json

import java.io.File
import java.util.Date

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
            "times" -> Json.toJson(cp._1.id.times.toString),
            "grade" -> Json.toJson(cp._1.id.grade),
            "classn" -> Json.toJson(cp._1.id.classn),
            "title" -> Json.toJson(cp._1.title),
            "prize" -> Json.toJson(cp._1.prize.map(_.toString).getOrElse("")),
            "thumbnail" -> Json.toJson(cp._2)
          )
        )
      }
    )
    Ok(json)
  }

  def upload = IsValidAccountWithParser(parse.multipartFormData) { acc => request =>
    request.body.file("file").map { file =>
      if (file.contentType.map(_.take(5)) == Some("image")) {
        val path = "./files/" + new Date().getTime().toString + "-" + file.filename
        file.ref.moveTo(new File(path), true)
        Ok(Json.toJson(Map("status" -> "success", "path" -> path.drop(1))))
      } else {
        Ok(Json.toJson(Map("status" -> "error", "message" -> "画像ではありません。")))
      }
    }.getOrElse {
      Ok(Json.toJson(Map("status" -> "error", "message" -> "ファイルを送信出来ませんでした。")))
    }
  }
}
