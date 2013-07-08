package controllers

import play.api.mvc._

trait Authentication {

  private def username(request: RequestHeader) = request.session.get("username")

  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Artisan.login)

  def IsAuthenticated(f: => String => Request[AnyContent] => Result) =
    Security.Authenticated(username, onUnauthorized) { artisan =>
      Action(request => f(artisan)(request))
    }
}
