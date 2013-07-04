package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json._

import models._
import andon.utils._

object API extends Controller {

  def search = Action {
    Ok(Json.toJson(ClassData.findAll.map { c =>
      ("times", c.times.toString)
    }.toMap))
  }
}
