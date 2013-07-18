package andon.utils

import org.pegdown.PegDownProcessor
import org.pegdown.Extensions

import play.api.templates._

object Conv {

  val pegdown = new PegDownProcessor(Extensions.ALL)

  def html(s: String) = HtmlFormat.raw(s)

  def both(s: String) = HtmlFormat.raw(pegdown.markdownToHtml(s))

  def markdown(s: String) = HtmlFormat.raw(pegdown.markdownToHtml(HtmlFormat.escape(s).toString))
}
