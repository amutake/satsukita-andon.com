package andon.utils

import play.api.{ Play, Logger }
import play.api.Play.current
import dispatch._, Defaults._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

object Slack {

  private[this] lazy val slackUrl = Play.application.configuration.getString("slack.incoming")
  private[this] val base = "http://satsukita-andon.com"

  def notify(body: String, path: Option[String]): Unit = {
    slackUrl.fold(()) { u =>
      val link = path.fold("")(p => "\n" + base + p)
      val json = ("text" -> (body + link))
      val rendered = compact(render(json))
      val slack = url(u).setContentType("application/json", "UTF-8") << rendered
      try {
        Http(slack OK as.String).apply() // TODO: Future
      } catch {
        case e: Exception => Logger.error(e.toString())
      }
    }
  }
}
