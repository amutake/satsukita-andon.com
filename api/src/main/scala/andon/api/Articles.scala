package andon.api

case class Article(
  id: Option[Long],
  title: String,
  body: String,
  userId: Long
)

object Articles {

  private var articles = Seq(
    Article(
      id = Some(1),
      title = "行灯の記事",
      body = "# 行灯の記事\n\nほげぽよー",
      userId = 1
    ),
    Article(
      id = Some(2),
      title = "ほげ",
      body = "# akka-http desu\n\nほげぽよー",
      userId = 1
    )
  )

  def all(params: Map[String, String]) = {
    println(params)
    articles
  }

  def get(id: Long): Either[Errors.Error, Article] = {
    articles.filter(_.id == Some(id)).headOption.toRight {
      Errors.ResourceNotFound
    }
  }

  def add(article: Article) = {
    articles = articles :+ article
    article
  }
}
