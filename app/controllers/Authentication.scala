package controllers

import play.api.mvc._

trait Authentication {

  private def userid(request: RequestHeader) = request.session.get("userid").map(_.toString)

  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Artisan.login)

  def IsAuthenticated(f: => Int => Request[AnyContent] => Result) =
    Security.Authenticated(userid, onUnauthorized) { artisan =>
      Action(request => f(artisan.toInt)(request))
    }
}
