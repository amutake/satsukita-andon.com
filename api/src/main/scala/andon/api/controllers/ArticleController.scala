package andon.api.controllers

import andon.api.util.Errors
import andon.api.models.{ Article, Articles }

case class CreateArticle(
  title: String,
  body: String,
  user_id: Long
)

object ArticleController {

  def all(offset: Option[Int], limit: Option[Int]) = {
    val o = offset.getOrElse(0)
    val l = limit.filter(l => 0 <= l && l <= 50).getOrElse(20)
    Articles.all(o, l)
  }

  def get(id: Long): Either[Errors.Error, Article] = {
    Articles.find(id).toRight(Errors.ResourceNotFound)
  }

  def add(article: CreateArticle) = {
    Articles.create(article.title, article.body, article.user_id)
  }
}
