package controllers

import play.api._
import play.api.mvc._

import java.io.File

object FileService extends Controller {

  val dir = "./files/"

  def get(filename: String) = Action {
    val decoded = java.net.URLDecoder.decode(filename)
    val file = new File(dir + decoded)
    if (file.exists()) {
      Ok.sendFile(file)
    } else {
      NotFound(views.html.errors.notFound("/files/" + decoded))
    }
  }
}
