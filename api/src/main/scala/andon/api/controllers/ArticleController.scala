package andon.api.controllers

import andon.api.util.Errors
import andon.api.models.{ Article, Articles }

case class CreateArticle(
  title: String,
  body: String,
  user_id: Long
)

object ArticleController {

  def all(params: Map[String, String]) = {
    println(params)
    Articles.all
  }

  def get(id: Long): Either[Errors.Error, Article] = {
    Articles.find(id).toRight(Errors.ResourceNotFound)
  }

  def add(article: CreateArticle) = {
    Articles.create(article.title, article.body, article.user_id)
  }
}
