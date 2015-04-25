package andon.api.models

import scalikejdbc._
import com.github.nscala_time.time.Imports.DateTime

import andon.api.util.Errors

case class Article(
  id: Long,
  title: String,
  body: String,
  createUserId: Long,
  updateUserId: Option[Long],
  createdAt: DateTime,
  updatedAt: Option[DateTime]
)

object Article extends SQLSyntaxSupport[Article] {
  override val tableName = "articles"
  def apply(a: SyntaxProvider[Article])(rs: WrappedResultSet): Article =
    apply(a.resultName)(rs)
  def apply(a: ResultName[Article])(rs: WrappedResultSet): Article =
    new Article(
      id = rs.get(a.id),
      title = rs.get(a.title),
      body = rs.get(a.body),
      createUserId = rs.get(a.createUserId),
      updateUserId = rs.get(a.updateUserId),
      createdAt = rs.get(a.createdAt),
      updatedAt = rs.get(a.updatedAt)
    )
}

object ArticleObjects {
  case class Base(article: Article, createUser: User, updateUser: Option[User])
  object Base {
    def apply(a: SyntaxProvider[Article], cu: SyntaxProvider[User], uu: SyntaxProvider[User])(rs: WrappedResultSet): Base =
      apply(a.resultName, cu.resultName, uu.resultName)(rs)
    def apply(a: ResultName[Article], cu: ResultName[User], uu: ResultName[User])(rs: WrappedResultSet): Base =
      apply(Article(a)(rs), User(cu)(rs), rs.longOpt(uu.id).map(_ => User(uu)(rs)))
  }
}

trait ArticleModel {

  import ArticleObjects._

  private val a = Article.syntax("a")
  private val cu = User.syntax("cu")
  private val uu = User.syntax("uu")
  private val column = Article.column

  def find(id: Long)(implicit s: DBSession = Article.autoSession): Option[Base] = {
    withSQL {
      select.from(Article as a)
        .leftJoin(User as cu).on(a.createUserId, cu.id)
        .leftJoin(User as uu).on(a.updateUserId, uu.id)
        .where.eq(a.id, id)
    }.map(Base(a, cu, uu)).single.apply()
  }

  def create(title: String, body: String, userId: Long)
    (implicit s: DBSession = Article.autoSession): Either[Errors.Error, Base] = {
    val optUser = withSQL {
      select.from(User as cu).where.eq(cu.id, userId)
    }.map(User(cu)).single.apply()

    optUser.map { user =>
      val now = DateTime.now
      val id = withSQL {
        insert.into(Article).namedValues(
          column.title -> title,
          column.body -> body,
          column.createUserId -> userId,
          column.createdAt -> now)
      }.updateAndReturnGeneratedKey.apply()

      Right(Base(Article(
        id = id,
        title = title,
        body = body,
        createUserId = userId,
        updateUserId = None,
        createdAt = now,
        updatedAt = None),
        user,
        None))
    }.getOrElse(Left(Errors.ResourceNotFound))
  }

  def all(offset: Int, limit: Int)
    (implicit s: DBSession = Article.autoSession): Seq[Base] = {
    withSQL {
      select.from(Article as a)
        .leftJoin(User as cu).on(a.createUserId, cu.id)
        .leftJoin(User as uu).on(a.updateUserId, uu.id)
        .limit(limit)
        .offset(offset)
    }.map(Base(a, cu, uu)).list.apply()
  }
}

object ArticleModel extends ArticleModel
