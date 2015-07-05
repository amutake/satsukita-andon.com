package andon.utils

object Notifier {
  def notify(tweet: Boolean, body: String, url: Option[String] = None): Unit = {
    if (tweet) {
      Twitter.tweet(body, url)
    }
    Slack.notify(body, url)
  }
}
