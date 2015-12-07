package andon.utils

import play.api.{ Play, Logger }
import play.api.Play.current
import play.api.libs.Codecs.sha1
import dispatch._, Defaults._
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._

object Slack {

  private[this] lazy val slackUrl = Play.application.configuration.getString("notifier.slack.incoming")

  def notify(appname: String, body: String, url: Option[String]): Unit = {
    slackUrl.fold(()) { u =>
      val link = url.fold("")(u => "\n" + u)
      val color = "#" + sha1(appname).take(6)
      // see: https://api.slack.com/docs/attachments
      val json =
        ("attachments" -> Seq(
          ("color" -> color) ~
          ("title" -> appname) ~
          ("text" -> (body + link))))
      val rendered = compact(render(json))
      val slack = dispatch.url(u).setContentType("application/json", "UTF-8") << rendered
      try {
        Http(slack OK as.String).apply() // TODO: Future
      } catch {
        case e: Exception => Logger.error(e.toString())
      }
    }
  }
}
