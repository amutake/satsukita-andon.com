package controllers

import play.api._
import play.api.mvc._

import java.io.File

object FileService extends Controller {

  def get(filename: String) = getByPath("/files/" + filename)

  def getByPath(path: String) = Action {
    val decoded = java.net.URLDecoder.decode(path, "UTF-8")
    val file = new File("." + decoded)
    if (file.exists()) {
      Ok.sendFile(file)
    } else {
      NotFound(views.html.errors.notFound(decoded))
    }
  }
}
