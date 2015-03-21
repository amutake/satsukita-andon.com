package andon.api

import scala.concurrent.ExecutionContext
import scala.util.Try
import scala.util.control.NonFatal

import akka.stream.ActorFlowMaterializer
import akka.http.server._, Directives._
import akka.http.marshalling.ToResponseMarshallable
import akka.http.marshalling.Marshaller._
import akka.http.unmarshalling.Unmarshaller._

import andon.api.util.Json4sJacksonSupport._
import andon.api.util.{ Errors, OrdIntMatcher, SignedIntNumber }
import andon.api.controllers._

object Routes {

  def route(version: String)(implicit ec: ExecutionContext, fm: ActorFlowMaterializer): Route = {
    val exceptionHandler = ExceptionHandler {
      case NonFatal(e) => complete {
        Errors.Unexpected(e)
      }
    }
    handleExceptions(exceptionHandler) {
      pathPrefix(version) {
        articles ~ classData ~ gallery ~ festivals ~ reviews
      } ~
      complete {
        // catch-all
        Errors.ApiNotFound
      }
    }
  }

  private def articles(implicit ec: ExecutionContext, fm: ActorFlowMaterializer): Route = {
    pathPrefix("articles") {
      pathEnd {
        get {
          parameterMap { params =>
            complete {
              val offset = params.get("offset").map(s => Try(s.toInt).toOption).flatten
              val limit = params.get("limit").map(s => Try(s.toInt).toOption).flatten
              ArticleController.all(
                offset = offset,
                limit = limit
              )
            }
          }
        } ~
        post {
          entity(as[ArticleJsons.Create]) { article =>
            complete {
              ArticleController.add(article)
            }
          } ~
          complete {
            Errors.JsonError
          }
        }
      } ~
      pathPrefix(LongNumber) { id =>
        pathEnd {
          get {
            complete {
              ArticleController.get(id)
            }
          }
        } ~
        pathPrefix("commits") {
          pathEnd {
            get {
              complete {
                ArticleController.commits(id)
              }
            }
          } ~
          path(Rest) { commitId =>
            get {
              complete {
                ArticleController.commit(id, commitId)
              }
            }
          }
        }
      }
    }
  }

  private def classData(implicit ec: ExecutionContext, fm: ActorFlowMaterializer): Route = {
    pathPrefix("classes" / OrdIntMatcher) { t =>
      pathEnd {
        get {
          complete {
            ClassDataController.getTimes(t)
          }
        }
      } ~
      pathPrefix(IntNumber) { g =>
        pathEnd {
          get {
            complete {
              ClassDataController.getGrade(t, g)
            }
          }
        } ~
        path(SignedIntNumber) { c =>
          get {
            complete {
              ClassDataController.getClass(t, g, c)
            }
          }
        }
      }
    }
  }

  private def gallery(implicit ec: ExecutionContext, fm: ActorFlowMaterializer): Route = {
    path("gallery" / OrdIntMatcher / IntNumber / SignedIntNumber) { case (t, g, c) =>
      get {
        parameterMap { params =>
          complete {
            val offset = params.get("offset").map(s => Try(s.toInt).toOption).flatten
            val limit = params.get("limit").map(s => Try(s.toInt).toOption).flatten
            GalleryController.all(t, g, c, offset, limit)
          }
        }
      }
    }
  }

  private def festivals(implicit ec: ExecutionContext, fm: ActorFlowMaterializer): Route = {
    pathPrefix("festivals") {
      pathEnd {
        get {
          complete {
            FestivalController.all
          }
        }
      } ~
      path(OrdIntMatcher) { t =>
        get {
          complete {
            FestivalController.detail(t) // also return prize info
          }
        }
      }
    }
  }

  private def reviews(implicit ec: ExecutionContext, fm: ActorFlowMaterializer): Route = {
    path("reviews" / OrdIntMatcher / IntNumber / SignedIntNumber) { (t, g, c) =>
      get {
        complete {
          ReviewController.all(t, g, c)
        }
      }
    }
  }
}
