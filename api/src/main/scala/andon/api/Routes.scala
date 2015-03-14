package andon.api

import akka.stream.ActorFlowMaterializer
import akka.http.server._
import Directives._
import akka.http.marshalling.ToResponseMarshallable
import akka.http.marshalling.Marshaller._
import akka.http.unmarshalling.Unmarshaller._
import scala.concurrent.ExecutionContext

import Json4sJacksonSupport._

object Routes {

  def route(version: String)(implicit ec: ExecutionContext, fm: ActorFlowMaterializer): Route = {
    pathPrefix(version) {
      articles
    } ~
    complete {
      // catch-all
      Errors.ApiNotFound
    }
  }

  private def articles(implicit ec: ExecutionContext, fm: ActorFlowMaterializer): Route = {
    path("articles") {
      get {
        parameterMap { params =>
          complete {
            Articles.all(params)
          }
        }
      } ~
      post {
        entity(as[Article]) { article =>
          complete {
            Articles.add(article)
          }
        } ~
        complete {
          Errors.JsonError
        }
      }
    } ~
    path("articles" / LongNumber) { id =>
      get {
        complete {
          Articles.get(id)
        }
      }
    }
  }
}
