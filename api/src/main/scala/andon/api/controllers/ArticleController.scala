package andon.api.controllers

import andon.api.util.Errors
import andon.api.models.{ Article, Articles, ArticleObjects }
import com.github.nscala_time.time.Imports._

object ArticleJsons {

  final case class Create(
    title: String,
    body: String,
    user_id: Long // TODO: remove this field
  )

  final case class Detail(
    id: Long,
    title: String,
    body: String,
    create_user: UserJsons.Simple,
    update_user: Option[UserJsons.Simple],
    created_at: DateTime,
    updated_at: Option[DateTime]
  )

  final object Detail {
    def apply(base: ArticleObjects.Base): Detail =
      Detail(
        base.article.id,
        base.article.title,
        base.article.body,
        UserJsons.Simple(base.createUser),
        base.updateUser.map(UserJsons.Simple.apply),
        base.article.createdAt,
        base.article.updatedAt)
  }

  final case class Simple(
    id: Long,
    title: String,
    create_user: UserJsons.Simple,
    update_user: Option[UserJsons.Simple],
    created_at: DateTime,
    updated_at: Option[DateTime]
  )

  final object Simple {
    def apply(base: ArticleObjects.Base): Simple =
      Simple(
        base.article.id,
        base.article.title,
        UserJsons.Simple(base.createUser),
        base.updateUser.map(UserJsons.Simple.apply),
        base.article.createdAt,
        base.article.updatedAt)
  }
}

object ArticleController {

  import ArticleJsons._

  def all(offset: Option[Int], limit: Option[Int]): Seq[Simple] = {
    val o = offset.getOrElse(0)
    val l = limit.filter(l => 0 <= l && l <= 50).getOrElse(20)
    Articles.all(o, l).map(Simple.apply)
  }

  def get(id: Long): Either[Errors.Error, Detail] = {
    Articles.find(id).map(Detail.apply)
      .toRight(Errors.ResourceNotFound)
  }

  def add(article: ArticleJsons.Create): Either[Errors.Error, Detail] = {
    Articles.create(article.title, article.body, article.user_id)
      .right.map(Detail.apply)
  }
}
