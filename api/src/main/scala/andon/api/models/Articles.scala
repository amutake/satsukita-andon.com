package andon.api.models

import scalikejdbc._
import com.github.nscala_time.time.Imports.DateTime

case class Article(
  id: Long,
  title: String,
  body: String,
  userId: Long,
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
      userId = rs.get(a.userId),
      createdAt = rs.get(a.createdAt),
      updatedAt = rs.get(a.updatedAt)
    )
}

object Articles {

  val a = Article.syntax
  val column = Article.column

  def find(id: Long)(implicit s: DBSession = Article.autoSession): Option[Article] = {
    withSQL {
      select.from(Article as a).where.eq(a.id, id)
    }.map(Article(a)).single.apply()
  }

  def create(title: String, body: String, userId: Long)
      (implicit s: DBSession = Article.autoSession): Article = {
    val now = DateTime.now
    val id = withSQL {
      insert.into(Article).namedValues(
        column.title -> title,
        column.body -> body,
        column.userId -> userId,
        column.createdAt -> now)
    }.updateAndReturnGeneratedKey.apply()
    Article(
      id = id,
      title = title,
      body = body,
      userId = userId,
      createdAt = now,
      updatedAt = None)
  }

  def all(implicit s: DBSession = Article.autoSession): Seq[Article] = {
    withSQL {
      select.from(Article as a)
    }.map(Article(a)).list.apply()
  }
}
