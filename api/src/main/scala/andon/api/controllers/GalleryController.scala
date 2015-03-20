package andon.api.controllers

import andon.api.services.{ ImageObjects, ImageService }
import andon.api.util.OrdInt

object GalleryController {
  def all(times: OrdInt, grade: Int, `class`: Int, offset: Option[Int], limit: Option[Int]): Seq[ImageObjects.Image] = {
    ImageService.classAll(times, grade, `class`, offset, limit)
  }
}
