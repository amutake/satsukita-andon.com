package andon.api.controllers

import com.github.nscala_time.time.Imports.DateTime

import andon.api.models.{ Review, Reviews, ReviewObjects }
import andon.api.util.OrdInt

object ReviewJsons {
  final case class Simple(
    times: Int,
    times_ord: String,
    grade: Int,
    `class`: Int,
    user: UserJsons.Simple,
    text: String,
    created_at: DateTime,
    updated_at: Option[DateTime]
  )

  object Simple {
    def apply(base: ReviewObjects.Base): Simple = {
      val r = base.review
      Simple(
        r.times,
        OrdInt(r.times).toString,
        r.grade,
        r.`class`,
        UserJsons.Simple(base.user),
        r.text,
        r.createdAt,
        r.updatedAt
      )
    }
  }
}

object ReviewController {

  import ReviewJsons._

  def all(times: OrdInt, grade: Int, `class`: Int): Seq[Simple] = {
    Reviews.all(times.raw, grade, `class`).map(Simple.apply)
  }
}
