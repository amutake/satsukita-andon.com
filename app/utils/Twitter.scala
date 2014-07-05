package andon.utils

import java.util.Date

import play.api.Logger

import twitter4j._
import twitter4j.auth.AccessToken

object Twitter {

  val twitter = TwitterFactory.getSingleton()

  def tweet(body: String, url: String) = {
    val now = new Date()
    val header = "[" + DateUtil.detail(now) + "]\n"
    val link = if (url == "") "" else "\n" + "http://satsukita-andon.com" + url
    try {
      twitter.updateStatus(header + body + link)
    } catch {
      case e: Exception => Logger.error(e.toString())
    }
  }
}
