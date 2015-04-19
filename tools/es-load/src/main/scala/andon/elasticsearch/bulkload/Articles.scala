package andon.elasticsearch.bulkload

import scalikejdbc._

case class Article(
  id: Long,
  createAccountId: Int,
  updateAccountId: Int,
  title: String,
  text: String,
  createDate: Long,
  updateDate: Long,
  articleType: String,
  genre: String,
  optAuthor: Option[String],
  optDate: Option[String],
  editable: Boolean
)

object Article extends SQLSyntaxSupport[Article] {
  override val tableName = "articles"
  def apply(a: SyntaxProvider[Article])(rs: WrappedResultSet): Article =
    apply(a.resultName)(rs)
  def apply(a: ResultName[Article])(rs: WrappedResultSet): Article =
    new Article(
      id = rs.get(a.id),
      createAccountId = rs.get(a.createAccountId),
      updateAccountId = rs.get(a.updateAccountId),
      title = rs.get(a.title),
      text = rs.get(a.text),
      createDate = rs.get(a.createDate),
      updateDate = rs.get(a.updateDate),
      articleType = rs.get(a.articleType),
      genre = rs.get(a.genre),
      optAuthor = rs.get(a.optAuthor),
      optDate = rs.get(a.optDate),
      editable = rs.get(a.editable)
    )
}

object Articles {
  val a = Article.syntax("a")

  def all(implicit s: DBSession = Article.autoSession): Seq[Article] = {
    withSQL {
      select.from(Article as a)
    }.map(Article(a)).list.apply()
  }
}
