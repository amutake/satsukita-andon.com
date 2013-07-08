package andon.utils

import com.tristanhunt.knockoff.DefaultDiscounter._
import com.tristanhunt.knockoff._

import play.api.templates._

object Markdown {

  def escape(s: String) = HtmlFormat.raw(toXHTML(knockoff(HtmlFormat.escape(s).toString)).toString)
}
