package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json.Json

import models._
import andon.utils._

object API extends Controller {

  // TODO
  def search(times: String, prize: String, grade: String, tag: String) = Action {

    val sr = ClassData.search(times, prize, grade)
    val cs = if (tag == "all") {
      sr
    } else {
      sr.filter { data =>
        Tags.findByTag(tag).exists { tag =>
          tag.times == data.times &&
          tag.grade == data.grade &&
          tag.classn == data.classn
        }
      }
    }
    val cps = Images.getTopImages(cs)
    val json = Json.toJson(
      cps.map { cp =>
        Json.toJson(
          Map(
            "times" -> Json.toJson(cp._1.times.toString),
            "grade" -> Json.toJson(cp._1.grade),
            "classn" -> Json.toJson(cp._1.classn),
            "title" -> Json.toJson(cp._1.title),
            "prize" -> Json.toJson(cp._1.prize.map(_.toString).getOrElse("")),
            "thumbnail" -> Json.toJson(cp._2)
          )
        )
      }
    )
    Ok(json)
  }
}
