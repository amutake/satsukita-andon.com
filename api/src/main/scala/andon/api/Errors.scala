package andon.api

import akka.http.model.{ StatusCode, StatusCodes }

object Errors {

  case class RawError(code: String, message: String)
  type Error = (StatusCode, RawError)

  val ApiNotFound = (StatusCodes.NotFound, RawError(code = "api_not_found", message = "requested API not found"))
  val ResourceNotFound = (StatusCodes.NotFound, RawError(code = "resource_not_found", message = "resource not found"))
  val JsonError = (StatusCodes.BadRequest, RawError(code = "json_error", message = "cannot extract entity to certain value"))
}
