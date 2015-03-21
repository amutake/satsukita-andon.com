package andon.api.controllers

import scalikejdbc.DB
import com.github.nscala_time.time.Imports._

import andon.api.util.Errors
import andon.api.models.{ Article, Articles, ArticleObjects, User, Users }
import andon.api.services.{ HistoryService, HistoryObjects }

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

object CommitJsons {

  final case class Simples(
    article: ArticleJsons.Simple,
    commits: Seq[Simple]
  )

  final case class Simple(
    id: String,
    user: UserJsons.Simple,
    date: DateTime
  )

  final case class Detail(
    id: String,
    article_id: Long,
    title: String,
    body: String,
    user: UserJsons.Simple,
    date: DateTime
  )
}

object ArticleController {

  import andon.api.controllers.{ ArticleJsons => A }
  import andon.api.controllers.{ CommitJsons => C }

  def all(offset: Option[Int], limit: Option[Int]): Seq[A.Simple] = {
    val o = offset.getOrElse(0)
    val l = limit.filter(l => 0 <= l && l <= 50).getOrElse(20)
    Articles.all(o, l).map(A.Simple.apply)
  }

  def get(id: Long): Either[Errors.Error, A.Detail] = {
    Articles.find(id).map(A.Detail.apply)
      .toRight(Errors.ResourceNotFound)
  }

  def add(article: ArticleJsons.Create): Either[Errors.Error, A.Detail] = {
    DB.localTx { implicit s =>
      Articles.create(article.title, article.body, article.user_id).right.map { a =>
        HistoryService.create(a.article.id, a.article.body, a.article.createUserId)
        A.Detail(a)
      }
    }
  }

  /**
    * If a deleted article id is requested, return ResourceNotFound
    * If the git file is not found, return ResourceNotFound
    */
  def commits(id: Long): Either[Errors.Error, C.Simples] = {
    (for {
      article <- Articles.find(id).map(A.Simple.apply)
      commits <- HistoryService.histories(id)
    } yield {
      val userIds = commits.map(_.userId).distinct
      val users = Users.allIn(userIds).map(UserJsons.Simple.apply)
      val simples = commits.map { c =>
        val u = users.filter(_.id == c.userId).headOption.getOrElse(UserJsons.Simple.Deleted)
        C.Simple(c.id, u, c.date)
      }
      C.Simples(article, simples)
    }).toRight(Errors.ResourceNotFound)
  }

  /**
    * If the user that have commited it is already deleted, return ResourceNotFound
    */
  def commit(articleId: Long, commitId: String): Either[Errors.Error, C.Detail] = {
    (for {
      article <- Articles.find(articleId)
      commit <- HistoryService.history(articleId, commitId)
      user <- Users.find(commit.userId).map(UserJsons.Simple.apply)
      body <- commit.body
    } yield {
      C.Detail(
        id = commit.id,
        article_id = article.article.id,
        title = article.article.title,
        body = body,
        user = user,
        date = commit.date
      )
    }).toRight(Errors.ResourceNotFound)
  }
}
