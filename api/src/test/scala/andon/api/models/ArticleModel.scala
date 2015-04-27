package andon.api.models

import org.scalatest.fixture.FunSpec
import org.scalatest.Matchers
import scalikejdbc._
import scalikejdbc.scalatest.AutoRollback
import com.github.nscala_time.time.Imports.DateTime

class ArticleModelSpec extends FunSpec with Matchers with AutoRollback {

  import ArticleObjects._

  val now = DateTime.now

  Tables.setup

  override def fixture(implicit session: DBSession) = {
    sql"insert into users values (1, 'user1', '****', 'user1', 60, null, null, null, null)".update.apply()
    sql"insert into articles values (1, 'title1', 'body1', 1, null, ${now}, null)".update.apply()
    sql"insert into articles values (2, 'title2', 'body2', 1, null, ${now}, null)".update.apply()
    ()
  }

  describe("#find") {
    it("should find correct article (1)") { implicit s =>
      ArticleModel.find(1).get should be(Base(
        Article(1, "title1", "body1", 1, None, now, None),
        User(1, "user1", "****", "user1", 60, None, None, None, None),
        None
      ))
    }

    it("should find correct article (2)") { implicit s =>
      ArticleModel.find(2).get should be(Base(
        Article(2, "title2", "body2", 1, None, now, None),
        User(1, "user1", "****", "user1", 60, None, None, None, None),
        None
      ))
    }
  }
}
