package andon.api

import akka.stream.ActorFlowMaterializer
import akka.http.server._
import Directives._
import akka.http.marshalling.ToResponseMarshallable
import akka.http.marshalling.Marshaller._
import akka.http.unmarshalling.Unmarshaller._
import scala.concurrent.ExecutionContext

import andon.api.util.Json4sJacksonSupport._
import andon.api.util.Errors
import andon.api.controllers._

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
            ArticleController.all(params)
          }
        }
      } ~
      post {
        entity(as[CreateArticle]) { article =>
          complete {
            ArticleController.add(article)
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
          ArticleController.get(id)
        }
      }
    }
  }
}
