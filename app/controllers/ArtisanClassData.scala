package controllers

import java.io.File
import java.util.Date

import scala.sys.process._
import scala.util.Random

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.libs.Files
import play.api.libs.json._
import play.api.libs.functional.syntax._

import models._
import andon.utils._

object ArtisanClassData extends Controller with ControllerUtils with Authentication {

  def classData(times: Option[Int]) = IsValidAccount { acc => implicit request =>
    Ok(views.html.artisan.classData(times, acc))
  }

  val classIdForm = Form(
    tuple(
      "times" -> number(min = 1),
      "grade" -> number(min = 1, max = 3),
      "classn" -> number(min = -20, max = 20)
    )
  )

  def createClass = HasAuthority(Master) { _ => _ =>
    Ok(views.html.artisan.createClass(classIdForm))
  }

  def postCreateClass = HasAuthority(Master) { acc => implicit request =>
    classIdForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.artisan.createClass(formWithErrors)),
      result => {
        val classId = ClassId(OrdInt(result._1), result._2, result._3)
        ClassData.findByClassId(classId).map { _ =>
          BadRequest(views.html.artisan.createClass(
            classIdForm.fill(result).withGlobalError("そのクラスは存在しています。")
          ))
        }.getOrElse {
          ClassData.createByClassId(classId)
          Notifier.notify(
            tweet = false,
            body = acc.name + "により" + classId + "が作成されました",
            url = Some("/gallery/" + Seq(classId.times, classId.grade, classId.classn).mkString("/"))
          )
          Redirect(routes.ArtisanClassData.classData(Some(classId.times.n))).flashing(
            "success" -> "クラスを作成しました。"
          )
        }
      }
    )
  }

  val classForm = Form(
    tuple(
      "title" -> text,
      "prize" -> text,
      "intro" -> text
    )
  )

  def editClassData(id: Int) = AboutClass(id, Master) { _ => data => _ =>
    val d = (data.title, data.prize.map(_.toString).getOrElse("none"), data.intro)
    Ok(views.html.artisan.editClassData(data.id, classForm.fill(d)))
  }

  def postEditClassData(id: Int) = AboutClass(id, Master) { acc => data => implicit request =>
    classForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.artisan.editClassData(data.id, formWithErrors)),
      result => {
        val prize = Prize.fromString(result._2)
        ClassData.update(data.id, result._1, prize, result._3)
        Notifier.notify(
          tweet = false,
          body = acc.name + "により" + data.id + "の情報が編集されました",
          url = Some("/gallery/" + Seq(data.id.times, data.id.grade, data.id.classn).mkString("/"))
        )
        Redirect(routes.ArtisanClassData.classData(Some(new ClassId(id).times.n))).flashing(
          "success" -> "クラス情報を変更しました。"
        )
      }
    )
  }

  val tagsForm = Form(
    single("tags" -> list(tuple(
      "id" -> number,
      "type" -> text,
      "name" -> text
    )))
  )

  def editTags(id: Int) = AboutClass(id, Writer) { _ => data => implicit request =>
    val names = Tags.all.map(_.tag).distinct
    val tags = Tags.findByClassId(data.id)
    Ok(views.html.artisan.editTags(data, tags, names))
  }

  def postEditTags(id: Int) = AboutClass(id, Writer) { acc => data => implicit request =>
    tagsForm.bindFromRequest.fold(
      formWithErrors => Redirect(routes.ArtisanClassData.classData(Some(new ClassId(id).times.n))).flashing(
        "error" -> "エラー"
      ),
      tags => {
        tags.map { tag =>
          if (tag._2 == "add") {
            Tags.create(data.id, tag._3)
          } else if (tag._2 == "delete") {
            Tags.delete(tag._1)
          }
        }
        Notifier.notify(
          tweet = false,
          body = acc.name + "により" + data.id + "のタグが編集されました",
          url = Some("/gallery/" + Seq(data.id.times, data.id.grade, data.id.classn).mkString("/"))
        )
        Redirect(routes.ArtisanClassData.classData(Some(new ClassId(id).times.n))).flashing(
          "success" -> "タグを編集しました。"
        )
      }
    )
  }

  val reviewForm = Form(
    tuple(
      "rid" -> optional(longNumber), // "id" don't work. play's bug?
      "text" -> optional(text),
      "delete" -> boolean
    )
  )

  def editReview(id: Int) = AboutClass(id, Writer) { acc => data => _ =>
    val form = Reviews.findByClassIdAccountId(data.id, acc.id).map { review =>
      reviewForm.fill((Some(review.id), Some(review.text), false))
    }.getOrElse(reviewForm)
    Ok(views.html.artisan.editReview(data.id, form))
  }

  def postEditReview(id: Int) = AboutClass(id, Writer) { acc => data => implicit request =>
    reviewForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.artisan.editReview(data.id, formWithErrors)),
      result => {
        def redirect(str: String) = Redirect(routes.ArtisanClassData.classData(Some(data.id.times.n))).flashing(
          "success" -> str
        )
        def bad(err: FormError) = BadRequest(views.html.artisan.editReview(data.id, reviewForm.fill(result).withError(err)))
        def notify(typ: String) = Notifier.notify(
          tweet = true,
          body = data.id.toJapanese + "への" +
            Accounts.findNameById(acc.id) + "の講評が" +
            typ + "されました",
          url = Some("/gallery/" + data.id.times + "/" + data.id.grade + "/" + data.id.classn)
        )

        result._2.map { text =>
          if (result._3) {
            bad(FormError("delete", "不正な入力です。"))
          } else {
            result._1.map { n =>
              Reviews.update(n, text)
              notify("編集")
              redirect("講評を更新しました。")
            }.getOrElse {
              Reviews.create(data.id, acc.id, text)
              notify("作成")
              redirect("講評を作成しました。")
            }
          }
        }.getOrElse {
          if (result._3) {
            result._1.map { n =>
              Reviews.delete(n)
              notify("削除")
              redirect("講評を削除しました。")
            }.getOrElse(redirect("講評はありません。"))
          } else {
            bad(FormError("text", "値を入力してください。"))
          }
        }
      }
    )
  }

  def uploadImage(id: Int) = AboutClass(id, Writer) { _ => data => _ =>
    Ok(views.html.artisan.uploadImage(data.id))
  }

  def postUploadImage(id: Int) = IsValidAccountWithParser(parse.multipartFormData) { acc => request =>
    val classId = new ClassId(id)
    ClassData.findByClassId(classId).map { c =>
      val files = request.body.files.filter { file =>
        file.contentType.map(_.take(5)) == Some("image")
      }
      if (files.length != request.body.files.length) {
        Redirect(routes.ArtisanClassData.classData(Some(new ClassId(id).times.n))).flashing(
          "error" -> "画像ではないファイルが含まれています。"
        )
      } else {
        files.foreach { file =>
          val classDir = c.id.times + "/" + c.id.grade + "/" + c.id.classn + "/"
          val fullsize = "/files/gallery/fullsize/" + classDir
          val thumbnail = "/files/gallery/thumbnail/" + classDir
          def valid(c: Char) = {
            val r = """[\w\.]""".r
            c.toString match {
              case r() => true
              case _ => false
            }
          }
          val filename = new Date().getTime().toString + "-" + file.filename.filter(valid)

          file.ref.moveTo(new File("." + fullsize + filename), true)
          Files.copyFile(new File("." + fullsize + filename), new File("." + thumbnail + filename))

          Process("mogrify -quality 50 ." + fullsize + filename).!
          Process("mogrify -resize 600x -unsharp 2x1.2+0.5+0.5 -quality 75 ." + thumbnail + filename).!
        }

        Notifier.notify(
          tweet = true,
          body = acc.name + "により" + classId + "の画像が" + files.length + "枚追加されました",
          url = Some("/gallery/" + Seq(c.id.times, c.id.grade, c.id.classn).mkString("/"))
        )

        Redirect(routes.ArtisanClassData.classData(Some(new ClassId(id).times.n))).flashing(
          "success" -> "画像をアップロードしました。"
        )
      }
    }.getOrElse(BadRequest)
  }

  val topForm = Form(single("top" -> optional(text)))

  def selectTop(id: Int) = MyClassOrMaster(id) { acc => data => _ =>
    Ok(views.html.artisan.selectTop(data.id, topForm.fill(data.top)))
  }

  def postSelectTop(id: Int) = MyClassOrMaster(id) { acc => data => implicit request =>
    topForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.artisan.selectTop(data.id, formWithErrors)),
      top => {
        ClassData.updateTop(data.id, top)
        Notifier.notify(
          tweet = false,
          body = acc.name + "により" + data.id + "のトップ画像が変更されました",
          url = Some("/gallery/" + Seq(data.id.times, data.id.grade, data.id.classn).mkString("/"))
        )
        Redirect(routes.ArtisanClassData.classData(Some(new ClassId(id).times.n))).flashing(
          "success" -> "トップ画像を変更しました。"
        )
      }
    )
  }

  val deleteImageForm = Form(
    single("filename" -> list(text))
  )

  def deleteImage(id: Int) = AboutClass(id, Master) { acc => data => _ =>
    Ok(views.html.artisan.deleteImage(data.id, deleteImageForm))
  }

  def postDeleteImage(id: Int) = AboutClass(id, Master) { acc => data => implicit request =>
    deleteImageForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.artisan.deleteImage(data.id, formWithErrors)),
      filenames => {
        filenames.map { filename =>
          val fpath = Images.fullsizePath(data.id, filename)
          val tpath = Images.thumbnailPath(data.id, filename)
          new File(fpath).delete()
          new File(tpath).delete()
        }

        Notifier.notify(
          tweet = false,
          body = acc.name + "により" + data.id + "の画像が" + filenames.length + "枚削除されました",
          url = Some("/gallery/" + Seq(data.id.times, data.id.grade, data.id.classn).mkString("/"))
        )

        Redirect(routes.ArtisanClassData.classData(Some(new ClassId(id).times.n))).flashing(
          "success" -> "画像を削除しました。"
        )
      }
    )
  }
}
