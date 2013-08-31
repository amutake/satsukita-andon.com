import play.api._
import play.api.mvc.RequestHeader
import play.api.mvc.Result
import play.api.mvc.Results._

import scala.slick.driver.H2Driver.simple._
import scala.slick.jdbc.meta.MTable

import models._
import andon.utils._

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    InitialData.createTable
    Cleaner.clean
  }

  override def onBadRequest(request: RequestHeader, error: String) = {
    BadRequest(views.html.errors.badRequest(error))
  }

  override def onHandlerNotFound(request: RequestHeader): Result = {
    NotFound(views.html.errors.notFound(request.path))
  }

  override def onError(request: RequestHeader, throwable: Throwable) = {
    InternalServerError(views.html.errors.error(throwable))
  }
}

object InitialData {

  def makeTableMap(implicit session: Session): Map[String, MTable] = {
    val tableList = MTable.getTables.list()
    tableList.map {
      t => (t.name.name, t)
    }.toMap
  }

  def createTable = DB.db.withSession { implicit session: Session =>
    val tableMap = makeTableMap
    if (!tableMap.contains("TIMESDATA")) {
      TimesData.ddl.create
    }
    if (!tableMap.contains("CLASSDATA")) {
      ClassData.ddl.create
    }
    if (!tableMap.contains("TAGS")) {
      Tags.ddl.create
    }
    if (!tableMap.contains("ACCOUNTS")) {
      Accounts.ddl.create
    }
    if (!tableMap.contains("ARTICLES")) {
      Articles.ddl.create
    }
    if (!tableMap.contains("DATA")) {
      Data.ddl.create
    }
    if (!tableMap.contains("REVIEWS")) {
      Reviews.ddl.create
    }
    if (!tableMap.contains("COMMENTS")) {
      Comments.ddl.create
    }
  }
}
