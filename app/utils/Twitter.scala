package andon.utils

import java.util.Date

import play.api.Logger

import twitter4j._
import twitter4j.auth.AccessToken

object Twitter {

  val twitter = TwitterFactory.getSingleton()

  def tweet(body: String, url: Option[String]) = {
    val now = new Date()
    val header = "[" + DateUtil.detail(now) + "]\n"
    val link = url.fold("")(u => "\n" + u)
    try {
      twitter.updateStatus(header + body + link)
    } catch {
      case e: Exception => Logger.error(e.toString())
    }
  }
}
