package controllers

import play.api._
import play.api.mvc._

import java.io.File

object FileService extends Controller {

  val dir = "./files/"

  def get(filename: String) = Action {
    val file = new File(dir + filename)
    Ok.sendFile(file)
  }
}
