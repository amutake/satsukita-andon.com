package controllers

import play.api.data.validation.Constraints._

trait ControllerUtils {
  val notEmpty = pattern(".+".r, error = "値を入力してください。")
  val notSpace = pattern("\\S+".r, error = "空白は含めません。")
}
