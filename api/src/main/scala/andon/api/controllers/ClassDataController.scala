package andon.api.controllers

import andon.api.util.{ Errors, OrdInt }
import andon.api.models.{ ClassData, ClassDataObjects }

object ClassDataJsons {

  final case class Simple(
    times: Int,
    times_ord: String,
    grade: Int,
    `class`: Int, // TODO: class?
    title: String,
    description: Option[String], // TODO: option?
    prizes: Seq[String],
    top_url: String // TODO: option?
  )

  final object Simple {
    def apply(base: ClassDataObjects.Base): Simple =
      apply(
        base.cd.times,
        OrdInt(base.cd.times).toString,
        base.cd.grade,
        base.cd.`class`,
        base.cd.title,
        base.cd.description,
        base.prizes,
        base.cd.topUrl.getOrElse("http://files.satsukita-andon.com/util/no-image.png"))
  }
}
