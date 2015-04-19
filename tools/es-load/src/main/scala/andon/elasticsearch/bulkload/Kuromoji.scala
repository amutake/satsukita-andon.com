package andon.elasticsearch.bulkload

import com.sksamuel.elastic4s._

object Kuromoji {
  case object KuromojiTokenizer extends Tokenizer("kuromoji_tokenizer")
  case object KuromojiPartOfSpeech extends AnalyzerFilter {
    val name = "kuromoji_part_of_speech"
  }
  case object KuromojiBaseform extends AnalyzerFilter {
    val name = "kuromoji_baseform"
  }
  case object KuromojiStemmer extends AnalyzerFilter {
    val name = "kuromoji_stemmer"
  }
  case object CjkWidth extends AnalyzerFilter {
    val name = "cjk_width"
  }
  case object Stop extends AnalyzerFilter {
    val name = "stop"
  }
  case object Snowball extends AnalyzerFilter {
    val name = "snowball"
  }

  val analyzer = CustomAnalyzerDefinition(
    "my_analyzer",
    KuromojiTokenizer
    // KuromojiPartOfSpeech,
    // KuromojiBaseform,
    // KuromojiStemmer,
    // CjkWidth,
    // LowercaseTokenFilter,
    // Stop,
    // Snowball
  )
}
