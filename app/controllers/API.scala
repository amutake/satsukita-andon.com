package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json.Json

import models._
import andon.utils._

object API extends Controller {

  // TODO
  def search(times: String, prize: String, grade: String) = Action {
    val cs = Json.toJson(
      ClassData.search(times, prize, grade).map { c =>
        Json.toJson(
          Map(
            "times" -> Json.toJson(c.times.toString),
            "grade" -> Json.toJson(c.grade),
            "classn" -> Json.toJson(c.classn),
            "title" -> Json.toJson(c.title),
            "prize" -> Json.toJson(c.prize.map(_.toString).getOrElse(""))
          )
        )
      }
    )
    Ok(cs)
  }
}
