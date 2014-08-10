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

object Artisan extends Controller with Authentication {

  val notEmpty = pattern(".+".r, error = "値を入力してください。")

  val notSpace = pattern("\\S+".r, error = "空白は含めません。")

  val loginForm = Form(
    tuple(
      "username" -> text.verifying(notEmpty).verifying(pattern("""(\w|-)+""".r, error = "半角英数字・ハイフン・アンダースコアのみです。")),
      "password" -> text.verifying(notEmpty)
    ) verifying ("ユーザー名かパスワードが間違っています。", result => result match {
      case (username, password) => Accounts.authenticate(username, password).isDefined
    })
  )

  def login = Action { implicit request =>
    Ok(views.html.artisan.login(loginForm))
  }

  def authenticate = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.artisan.login(formWithErrors)),
      { account =>
        Accounts.findByUsername(account._1).map { account_ =>
          Redirect(routes.Artisan.home).withSession("userid" -> account_.id.toString)
        }.getOrElse(Forbidden(views.html.errors.forbidden()))
      }
    )
  }

  def logout = Action {
    Redirect(routes.Artisan.login).withNewSession.flashing(
      "success" -> "ログアウトしました。"
    )
  }

  def home = IsValidAccount { account => implicit request =>
    Ok(views.html.artisan.home(account))
  }

  def myAccount = IsValidAccount { acc => _ =>
    Ok(views.html.artisan.myAccount(acc))
  }

  def articles = IsValidAccount { account => implicit request =>
    Ok(views.html.artisan.articles(account))
  }

  def article(id: Long) = IsEditableArticle(id) { _ => article => implicit request =>
    Ok(views.html.artisan.article(article))
  }

  val articleForm = Form(
    tuple(
      "title" -> text.verifying(notEmpty),
      "text" -> text.verifying(pattern("""[\s\S]+""".r, error = "本文を入力してください")),
      "type" -> text.verifying(notEmpty).verifying(pattern(ArticleType.all.mkString("|").r, error = "不正な入力です。")),
      "genre" -> text,
      "optAuthor" -> optional(text),
      "optDate" -> optional(text),
      "editable" -> boolean
    )
  )

  def creatable(level: AccountLevel) = level match {
    case Admin | Master => List(Info, Howto)
    case Writer => List(Howto)
  }

  def createArticle = IsValidAccount { acc => _ =>
    Ok(views.html.artisan.createArticle(acc.level, creatable(acc.level), articleForm))
  }

  def postCreateArticle = IsValidAccount { acc => implicit request =>
    articleForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.artisan.createArticle(acc.level, creatable(acc.level), formWithErrors)),
      { article =>
        val id = Articles.create(acc.id, article._1, article._2, ArticleType.fromString(article._3), article._4, article._5, article._6, article._7)
        Redirect(routes.Artisan.article(id)).flashing(
          "success" -> "記事を作成しました。"
        )
      }
    )
  }

  def editArticle(id: Long) = IsEditableArticle(id) { acc => art => _ =>
    val data = (art.title, art.text, art.articleType.toString, art.genre, art.optAuthor, art.optDate, art.editable)
    Ok(views.html.artisan.editArticle(acc, id, art.createAccountId, articleForm.fill(data)))
  }

  def postEditArticle(id: Long) = IsEditableArticle(id) { acc => art => implicit request =>
    articleForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.artisan.editArticle(acc, id, art.createAccountId, formWithErrors)),
      article => {
        Articles.update(id, acc.id, article._1, article._2, article._4, article._5, article._6, article._7)
        Redirect(routes.Artisan.article(id)).flashing(
          "success" -> "記事を編集しました。"
        )
      }
    )
  }

  def deleteArticle(id: Long) = IsEditableArticle(id) { acc => art => _ =>
    if (acc.level == Writer && acc.id != art.createAccountId) {
      Forbidden(views.html.errors.forbidden())
    } else {
      Articles.delete(id)
      Redirect(routes.Artisan.articles).flashing(
        "success" -> "記事を削除しました"
      )
    }
  }

  def accounts = HasAuthority(Master) { account => implicit request =>
    Ok(views.html.artisan.accounts(account, Accounts.all))
  }

  def account(id: Int) = HasAuthority(Master) { me => _ =>
    Accounts.findById(id).map { acc =>
      Ok(views.html.artisan.account(me, acc))
    }.getOrElse(Results.NotFound(views.html.errors.notFound("/artisan/account?id=" + id.toString)))
  }

  val accountForm = Form(
    tuple(
      "name" -> text.verifying(notEmpty).verifying(notSpace),
      "username" -> text.verifying(notEmpty).verifying(pattern("""(\w|-)+""".r, error = "半角英数字・ハイフン・アンダースコアのみです。")),
      "times" -> number,
      "level" -> text.verifying(notEmpty).verifying(pattern("admin|master|writer".r, error = "不正な入力です。")),
      "class1" -> optional(number),
      "class2" -> optional(number),
      "class3" -> optional(number)
    )
  )

  val createAccountForm = Form(
    accountForm.mapping.verifying("そのユーザー名は既に使われています。", result => result match {
      case (_, u, _, _, _, _, _) => !Accounts.all.map(_.username).contains(u)
    })
  )

  def createAccount = HasAuthority(Master) { acc => _ =>
    Ok(views.html.artisan.createAccount(acc, createAccountForm))
  }

  def postCreateAccount = HasAuthority(Master) { me => implicit request =>
    createAccountForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.artisan.createAccount(me, formWithErrors)),
      { newacc =>
        val times = OrdInt(newacc._3.toInt)
        def mkClassId(grade: Int) = { classn: Int =>
          val t = grade match {
            case 1 => OrdInt(times.n - 2)
            case 2 => OrdInt(times.n - 1)
            case 3 => times
          }
          ClassId(t, grade, classn)
        }
        val pass = Random.alphanumeric.take(9).mkString
        val id = Accounts.create(newacc._1, newacc._2, pass, times, AccountLevel.fromString(newacc._4),
          newacc._5.map(mkClassId(1)), newacc._6.map(mkClassId(2)), newacc._7.map(mkClassId(3)))
        Accounts.findById(id).map { acc =>
          Twitter.tweet(me.name + "により新しいアカウント『" + acc.name + "』が作られました", "")
          Ok(views.html.artisan.confirmAccount(acc, pass))
        }.getOrElse(InternalServerError)
      }
    )
  }

  def editAccount(id: Int) = IsEditableAccount(id) { _ => acc => _ =>
    val data = (acc.name, acc.username, acc.times.n, acc.level.toString,
      acc.class1.map(_.classn), acc.class2.map(_.classn), acc.class3.map(_.classn))
    Ok(views.html.artisan.editAccount(acc, accountForm.fill(data)))
  }

  def postEditAccount(id: Int) = HasAuthority(Admin) { me => implicit request =>
    accountForm.bindFromRequest.fold(
      { formWithErrors =>
        Accounts.findById(id).map { acc =>
          BadRequest(views.html.artisan.editAccount(acc, formWithErrors))
        }.getOrElse(Redirect(routes.Artisan.home).flashing(
          "error" -> "不正な操作"
        ))
      },
      { case (name, username, times, level, class1, class2, class3) =>
        val l = AccountLevel.fromString(level)
        def mkClassId(grade: Int) = { classn: Int =>
          val t = grade match {
            case 1 => OrdInt(times.toInt - 2)
            case 2 => OrdInt(times.toInt - 1)
            case 3 => OrdInt(times.toInt)
          }
          ClassId(t, grade, classn)
        }
        Accounts.update(id, name, username, OrdInt(times.toInt), l,
          class1.map(mkClassId(1)), class2.map(mkClassId(2)), class3.map(mkClassId(3)))
        if (me.id == id) {
          Twitter.tweet(me.name + "が自分のアカウント情報を編集しました", "")
        } else {
          Twitter.tweet(me.name + "によりアカウント『" + name + "』の情報が編集されました", "")
        }
        Redirect(routes.Artisan.home).flashing(
          "success" -> "アカウントを編集しました。"
        )
      }
    )
  }

  def deleteAccount(id: Int) = GreaterThan(id) { me => acc => _ =>
    Accounts.delete(id)
    Twitter.tweet(me.name + "によりアカウント『" + acc.name + "』が削除されました", "")
    Redirect(routes.Artisan.accounts()).flashing(
      "success" -> "アカウントを削除しました。"
    )
  }

  def deleteMyAccount = IsValidAccount { me => _ =>
    Accounts.delete(me.id)
    Twitter.tweet(me.name + "が自分のアカウントを削除しました", "")
    Redirect(routes.Artisan.login()).withNewSession.flashing(
      "success" -> "アカウントを削除しました。"
    )
  }

  val passwordForm = Form(
    tuple(
      "yourpass" -> text.verifying(notEmpty),
      "password" -> text.verifying(notEmpty),
      "confirm" -> text.verifying(notEmpty)
    ).verifying("パスワードが一致しません", result => result match {
      case (_, c, p) => c == p
    })
  )

  def editMyPassword = IsValidAccount { acc => _ =>
    Ok(views.html.artisan.editMyPassword(passwordForm))
  }

  def postEditMyPassword = IsValidAccount { acc => implicit request =>
    passwordForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.artisan.editMyPassword(formWithErrors)),
      pass => if (acc.validPassword(pass._1)) {
        Accounts.updatePassword(acc.id, pass._2)
        Redirect(routes.Artisan.home).flashing(
          "success" -> "パスワードを変更しました。"
        )
      } else {
        BadRequest(views.html.artisan.editMyPassword(passwordForm.withGlobalError("パスワードが間違っています。")))
      }
    )
  }

  def editPassword(id: Int) = GreaterThan(id) { me => acc => request =>
    Ok(views.html.artisan.editPassword(acc, passwordForm))
  }

  def postEditPassword(id: Int) = GreaterThan(id) { me => acc => implicit request =>
    passwordForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.artisan.editPassword(acc, formWithErrors)),
      pass => if (me.validPassword(pass._1)) {
        Accounts.updatePassword(acc.id, pass._2)
        Redirect(routes.Artisan.home).flashing(
          "success" -> "パスワードを変更しました。"
        )
      } else {
        BadRequest(views.html.artisan.editPassword(acc, passwordForm.withGlobalError("パスワードが間違っています。")))
      }
    )
  }

  def data = IsValidAccount { acc => _ =>
    acc.level match {
      case Admin | Master => Ok(views.html.artisan.data(Data.dateSorted))
      case Writer => Ok(views.html.artisan.data(Data.findByAccountId(acc.id)))
    }
  }

  val datumForm = Form(
    tuple(
      "name" -> text.verifying(notEmpty),
      "genre" -> text.verifying(notEmpty),
      "optAuthor" -> optional(text),
      "optDate" -> optional(text)
    )
  )

  def uploadDatum = IsValidAccount { acc => _ =>
    Ok(views.html.artisan.uploadDatum(acc.level, datumForm))
  }

  def postUploadDatum = IsValidAccountWithParser(parse.multipartFormData) { acc => implicit request =>
    request.body.file("file").map { file =>
      datumForm.bindFromRequest().fold(
        formWithErrors => BadRequest(views.html.artisan.uploadDatum(acc.level, formWithErrors)),
        result => {
          val now = new Date()
          val path = "/files/data/" + now.getTime().toString + "-" + file.filename.filter(_ != ' ')
          file.ref.moveTo(new File("." + path), true)
          Data.create(result._1, acc.id, path, result._2, result._3, result._4)
          Redirect(routes.Artisan.home).flashing(
            "success" -> "資料をアップロードしました。"
          )
        }
      )
    }.getOrElse {
      BadRequest(views.html.artisan.uploadDatum(acc.level, datumForm.withGlobalError("ファイルのアップロードに失敗しました。")))
    }
  }

  def editDatum(id: Int) = IsEditableDatum(id) { acc => datum => request =>
    val data = (datum.name, datum.genre, datum.optAuthor, datum.optDate)
    Ok(views.html.artisan.editDatum(id, acc.level, datumForm.fill(data)))
  }

  def postEditDatum(id: Int) = IsEditableDatum(id) { acc => _ => implicit request =>
    datumForm.bindFromRequest().fold(
      formWithErrors => BadRequest(views.html.artisan.editDatum(id, acc.level, formWithErrors)),
      result => {
        Data.update(id, result._1, result._2, result._3, result._4)
        request.body.asMultipartFormData.flatMap { fd =>
          fd.file("file").map { file =>
            val now = new Date()
            val path = "/files/data/" + now.getTime().toString + "-" + file.filename.filter(_ != ' ')
            file.ref.moveTo(new File("." + path), true)
            Data.fileUpdate(id, path)
            Redirect(routes.Artisan.home).flashing(
              "success" -> "資料を更新しました。"
            )
          }
        }.getOrElse(Redirect(routes.Artisan.home).flashing(
          "success" -> "資料情報を編集しました。"
        ))
      }
    )
  }

  def deleteDatum(id: Int) = IsEditableDatum(id) { acc => datum => _ =>
    Data.delete(id)
    new File("." + datum.path).delete()
    Redirect(routes.Artisan.home).flashing(
      "success" -> "資料を削除しました。"
    )
  }

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
          Twitter.tweet(
            acc.name + "により" + classId + "が作成されました",
            "/gallery/" + Seq(classId.times, classId.grade, classId.classn).mkString("/")
          )
          Redirect(routes.Artisan.classData(Some(classId.times.n))).flashing(
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

        Twitter.tweet(
          acc.name + "により" + data.id + "の情報が編集されました",
          "/gallery/" + Seq(data.id.times, data.id.grade, data.id.classn).mkString("/")
        )

        Redirect(routes.Artisan.classData(Some(new ClassId(id).times.n))).flashing(
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
      formWithErrors => Redirect(routes.Artisan.classData(Some(new ClassId(id).times.n))).flashing(
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
        Twitter.tweet(
          acc.name + "により" + data.id + "のタグが編集されました",
          "/gallery/" + Seq(data.id.times, data.id.grade, data.id.classn).mkString("/")
        )
        Redirect(routes.Artisan.classData(Some(new ClassId(id).times.n))).flashing(
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
        def redirect(str: String) = Redirect(routes.Artisan.classData(Some(data.id.times.n))).flashing(
          "success" -> str
        )
        def bad(err: FormError) = BadRequest(views.html.artisan.editReview(data.id, reviewForm.fill(result).withError(err)))

        result._2.map { text =>
          if (result._3) {
            bad(FormError("delete", "不正な入力です。"))
          } else {
            result._1.map { n =>
              Reviews.update(n, text)
              redirect("講評を更新しました。")
            }.getOrElse {
              Reviews.create(data.id, acc.id, text)
              redirect("講評を作成しました。")
            }
          }
        }.getOrElse {
          if (result._3) {
            result._1.map { n =>
              Reviews.delete(n)
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
        Redirect(routes.Artisan.classData(Some(new ClassId(id).times.n))).flashing(
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

        Twitter.tweet(
          acc.name + "により" + classId + "の画像が" + files.length + "枚追加されました",
          "/gallery/" + Seq(c.id.times, c.id.grade, c.id.classn).mkString("/")
        )

        Redirect(routes.Artisan.classData(Some(new ClassId(id).times.n))).flashing(
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
        Twitter.tweet(
          acc.name + "により" + data.id + "のトップ画像が変更されました",
          "/gallery/" + Seq(data.id.times, data.id.grade, data.id.classn).mkString("/")
        )
        Redirect(routes.Artisan.classData(Some(new ClassId(id).times.n))).flashing(
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

        Twitter.tweet(
          acc.name + "により" + data.id + "の画像が" + filenames.length + "枚削除されました",
          "/gallery/" + Seq(data.id.times, data.id.grade, data.id.classn).mkString("/")
        )

        Redirect(routes.Artisan.classData(Some(new ClassId(id).times.n))).flashing(
          "success" -> "画像を削除しました。"
        )
      }
    )
  }

  def timesData = HasAuthority(Master) { _ => implicit request =>
    Ok(views.html.artisan.timesData())
  }

  val timesForm = Form(single("title" -> text))

  def editTimesData(id: Int) = AboutTimes(id) { _ => data => _ =>
    Ok(views.html.artisan.editTimesData(data.times, timesForm.fill(data.title)))
  }

  def postEditTimesData(id: Int) = AboutTimes(id) { acc => data => implicit request =>
    timesForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.artisan.editTimesData(data.times, formWithErrors)),
      title => {
        TimesData.update(data.times, title)
        Twitter.tweet(
          acc.name + "により" + data.times + "の情報が編集されました",
          "/gallery"
        )
        request.body.asMultipartFormData.flatMap { fd =>
          fd.file("top").map { file =>
            // TODO: check file extension
            val path = "./files/grands/" + data.times + ".jpg"
            file.ref.moveTo(new File(path), true)

            Process("mogrify -resize 320x -unsharp 2x1.2+0.5+0.5 -quality 75 " + path).!
            Redirect(routes.Artisan.timesData).flashing(
              "success" -> "編集しました"
            )
          }
        }.getOrElse(Redirect(routes.Artisan.timesData).flashing(
          "success" -> "編集しました"
        ))
      }
    )
  }

  val timesBaseForm = Form(single(
    "times" -> number(min = 1)
  ))

  def createTimes = HasAuthority(Master) { _ => _ =>
    Ok(views.html.artisan.createTimes(timesBaseForm))
  }

  def postCreateTimes = HasAuthority(Master) { acc => implicit request =>
    timesBaseForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.artisan.createTimes(formWithErrors)),
      n => {
        val times = OrdInt(n)
        TimesData.findByTimes(times).map { _ =>
          BadRequest(views.html.artisan.createTimes(
            timesBaseForm.fill(n).withGlobalError("その回は存在しています。")
          ))
        }.getOrElse {
          TimesData.createByTimes(times)
          Twitter.tweet(
            acc.name + "により" + times + "が作成されました",
            "/gallery"
          )
          Redirect(routes.Artisan.timesData).flashing(
            "success" -> "回を作成しました。"
          )
        }
      }
    )
  }


  def uploadOtherImages = IsValidAccount { _ => _ =>
    Ok(views.html.artisan.uploadOtherImages())
  }

  def postUploadOtherImages = IsValidAccountWithParser(parse.multipartFormData) { acc => request =>
    val files = request.body.files.filter { file =>
      file.contentType.map(_.take(5)) == Some("image")
    }
    if (files.length != request.body.files.length) {
      Redirect(routes.Artisan.home).flashing(
        "error" -> "画像ではないファイルが含まれています。"
      )
    } else {
      files.foreach { file =>
        val fullsize = "/files/gallery/fullsize/others/"
        val thumbnail = "/files/gallery/thumbnail/others/"
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

      Twitter.tweet(
        acc.name + "によりその他の画像が" + files.length + "枚追加されました",
        "/gallery/others"
      )

      Redirect(routes.Artisan.home).flashing(
        "success" -> "画像をアップロードしました。"
      )
    }
  }
}
