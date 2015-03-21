package andon.api.controllers

import andon.api.models.{ Festival, Festivals, FestivalObjects, ClassDataObjects }
import andon.api.util.{ Errors, OrdInt }

object FestivalJsons {

  final case class Simple(
    times: Int,
    times_ord: String,
    theme: String,
    image_url: Option[String] // TODO: option?
  )

  object Simple {
    def apply(fes: Festival): Simple = Simple(
      times = fes.times,
      times_ord = OrdInt(fes.times).toString,
      theme = fes.theme,
      image_url = fes.imageUrl
    )
  }

  final case class Detail(
    times: Int,
    times_ord: String,
    theme: String,
    image_url: Option[String],
    prize_map: Map[String, Seq[ClassDataJsons.Simple]]
  )

  object Detail {
    def apply(d: FestivalObjects.Detail): Detail = {
      val map = {
        d.prizes.groupBy { case (p, _) =>
          p.kind
        } mapValues(_.map { case (p, cd) =>
          ClassDataJsons.Simple(ClassDataObjects.Base(cd, Seq(p.kind))) // FIXME: this causes always prizes.length = 1
        })
      }
      Detail(
        d.festival.times,
        OrdInt(d.festival.times).toString,
        d.festival.theme,
        d.festival.imageUrl,
        map
      )
    }
  }
}

object FestivalController {

  import FestivalJsons._

  def all: Seq[Simple] = {
    Festivals.all.map(Simple.apply)
  }

  def detail(times: OrdInt): Either[Errors.Error, Detail] = {
    Festivals.detail(times.raw).map(Detail.apply).toRight(Errors.ResourceNotFound)
  }
}
