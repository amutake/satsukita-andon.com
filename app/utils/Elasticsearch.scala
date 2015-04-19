package andon.utils

import com.sksamuel.elastic4s.ElasticClient
import com.sksamuel.elastic4s.ElasticDsl.{ search => esearch, _ }
import scala.collection.JavaConversions._

import models.{ Article, Articles }

case class SearchResult(total: Long, hits: Seq[SearchHit])
case class SearchHit(score: Float, highlight: String, article: Article)


object Elasticsearch {

  lazy val client = ElasticClient.remote("localhost", 9300)

  def search(q: String, offset: Int, limit: Int): SearchResult = {
    val res = client.execute {
      esearch in "andon-test" / "articles" rawQuery (
        s"""{"match": {"_all": {"query": "${q}", "operator": "and"}}}"""
      ) highlighting (
        highlight field "text"
          numberOfFragments 1
          fragmentSize 100
          preTag "∀"
          postTag "∃"
      ) start offset limit limit
    }.await
    val scoreIds = res.getHits.hits.map { h =>
      (h.score, h.highlightFields.mapValues(_.fragments.toSeq).values.toSeq.flatten.headOption.fold("")(_.toString), h.id)
    }
    val hits = scoreIds.toSeq.map { case (score, highlight, id) =>
      Articles.findById(id.toLong).map { article =>
        SearchHit(score, highlight, article)
      }
    }.flatten
    SearchResult(total = res.getHits.totalHits, hits = hits)
  }
}
