package andon.utils

import java.util.Date

import twitter4j._
import twitter4j.auth.AccessToken

object Twitter {

  val twitter = new TwitterFactory().getInstance()
  twitter.setOAuthConsumer("", "")
  twitter.setOAuthAccessToken(new AccessToken("", ""))

  def tweet(category: String, body: String, url: String) = {
    val now = new Date()
    val header = "[" + category + " - " + DateUtil.detail(now) + "]\n"
    val link = if (url == "") "" else "\n" + "http://satsukita-andon.com" + url
    try {
      twitter.updateStatus(header + body + link)
    } catch {
      case e: Exception => println(e)
    }
  }
}
