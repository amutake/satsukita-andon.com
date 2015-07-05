package andon.utils

import play.api.Play
import play.api.Play.current

object Notifier {

  private[this] lazy val base = Play.application.configuration.getString("notifier.base")
  private[this] lazy val appname = Play.application.configuration.getString("notifier.appname")

  def notify(tweet: Boolean, body: String, url: Option[String] = None): Unit = {
    val fullurl = for {
      b <- base
      u <- url
    } yield (b + u)
    if (tweet) {
      Twitter.tweet(body, fullurl)
    }
    val app = appname.getOrElse("")
    Slack.notify(app, body, fullurl)
  }
}
