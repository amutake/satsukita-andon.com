package andon.api.controllers

import org.scalatest.{ FunSpec, Matchers }
import scalikejdbc._
import com.github.nscala_time.time.Imports.DateTime

import andon.api.models._
import andon.api.services._
import andon.api.util.Errors

object ArticleModelStub extends ArticleModel {

  import ArticleObjects._

  val now = DateTime.now

  val articles = Array(
    Article(1, "title1", "body1", 1, None, now, None),
    Article(2, "title2", "body2", 2, Some(1), now.minusDays(1), Some(now))
  )

  val users = Seq(
    User(1, "user1", "password1", "user1", 60, None, None, None, None),
    User(2, "user2", "password2", "user2", 60, None, None, None, None)
  )

  override def find(id: Long)(implicit s: DBSession = Article.autoSession): Option[Base] = {
    articles.filter(_.id == id).headOption.flatMap(toBase)
  }

  override def create(title: String, body: String, userId: Long)
    (implicit s: DBSession = Article.autoSession): Either[Errors.Error, Base] = {
    val maxId = articles.map(_.id).max
    val article = Article(maxId + 1, title, body, userId, None, now, None)
    toBase(article).toRight(Errors.ResourceNotFound)
  }

  override def all(offset: Int, limit: Int)
    (implicit s: DBSession = Article.autoSession): Seq[Base] = {
    articles.toSeq.map(toBase).flatten.drop(offset).take(limit)
  }

  private def toBase(article: Article): Option[Base] = {
    for {
      createUser <- users.filter(_.id == article.createUserId).headOption
    } yield {
      val updateUser = article.updateUserId.flatMap { updateUserId =>
        users.filter(_.id == updateUserId).headOption
      }
      Base(article, createUser, updateUser)
    }
  }
}

object HistoryServiceStub extends HistoryService {
}

object ArticleControllerForSpec extends ArticleController {
  val Articles = ArticleModelStub
  val HistoryService = HistoryServiceStub
}

class ArticleControllerSpec extends FunSpec with Matchers {

  val A = ArticleJsons
  val U = UserJsons

  val controller = ArticleControllerForSpec

  val user1 = U.Simple(1, "user1", "user1", None)
  val user2 = U.Simple(2, "user2", "user2", None)

  val article1 = A.Simple(1, "title1", user1, None, ArticleModelStub.now, None)
  val article2 = A.Simple(2, "title2", user2, Some(user1), ArticleModelStub.now.minusDays(1), Some(ArticleModelStub.now))

  describe("#all") {
    describe("when no limit and offset") {
      it("should return all articles") {
        controller.all(None, None) should be(Seq(article1, article2))
      }
    }

    describe("when offset = 1") {
      it("should return articles except first article") {
        controller.all(Some(1), None) should be(Seq(article2))
      }
    }

    describe("when limit = 1") {
      it("should return an article") {
        controller.all(None, Some(1)) should be(Seq(article1))
      }
    }

    describe("when offset = 1, limit = 1") {
      it("should return second article") {
        controller.all(Some(1), None) should be(Seq(article2))
      }
    }
  }
}
