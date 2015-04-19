package andon.elasticsearch.bulkload

import scalikejdbc.config._
import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.mappings.FieldType._
import com.sksamuel.elastic4s._
import org.elasticsearch.common.settings.ImmutableSettings
// import scala.concurrent.Await
// import scala.concurrent.duration.Duration

object Main extends App {

  DBs.setupAll()

  val settings = ImmutableSettings.settingsBuilder.put("cluster.name", "elasticsearch").build
  val client = ElasticClient.remote(settings, "127.0.0.1", 9300)

  // client.execute {
  //   deleteIndex("andon-test")
  // }.await

  // if (client.exists("andon-test").await.isExists) {
  //   println("index [andon-test] already exists")
  // } else {
  //   val res = client.execute {
  //     create index "andon-test" mappings (
  //       "articles" as (
  //         "title" typed StringType analyzer "my_analyzer",
  //         "text" typed StringType analyzer "my_analyzer",
  //         "genre" typed StringType analyzer "my_analyzer"
  //       )
  //     ) analysis (
  //       Kuromoji.analyzer
  //     )
  //   }.await
  //   if (res.isAcknowledged) {
  //     println("index [andon-test] have been created")
  //   } else {
  //     throw new java.lang.RuntimeException("can't create index")
  //   }
  // }

  val articles = Articles.all

  println(articles.length + " articles will be indexed")

  val futures = articles.map { article =>
    client.execute {
      index into "andon-test" / "articles" id article.id fields (
        "title" -> article.title,
        "text" -> article.text,
        "genre" -> article.genre
      )
    }
  }

  futures.foreach { f =>
    val res = f.await
    val typ = if (res.isCreated) "created" else "updated"
    println(res.getId + ": " + typ)
  }

  println("done")
}
