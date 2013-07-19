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

import models._
import andon.utils._

object Artisan extends Controller with Authentication {

  val notEmpty = pattern(".+".r, error = "値を入力してください。")

  val notSpace = pattern("\\S+".r, error = "空白は含めません。")

  val loginForm = Form(
    tuple(
      "username" -> text.verifying(notEmpty).verifying(pattern("\\w+".r, error = "半角英数字のみです。")),
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
    account.level match {
      case Admin | Master => Ok(views.html.artisan.articles(Articles.all))
      case Writer => Ok(views.html.artisan.articles(Articles.findByCreateAccountId(account.id)))
    }
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
      "optDate" -> optional(text)
    )
  )

  def editable(level: AccountLevel) = level match {
    case Admin | Master => List(Info, Howto)
    case Writer => List(Howto)
  }

  def createArticle = IsValidAccount { acc => _ =>
    Ok(views.html.artisan.createArticle(acc.level, editable(acc.level), articleForm))
  }

  def postCreateArticle = IsValidAccount { acc => implicit request =>
    articleForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.artisan.createArticle(acc.level, editable(acc.level), formWithErrors)),
      { article =>
        val id = Articles.create(acc.id, article._1, article._2, ArticleType.fromString(article._3), article._4, article._5, article._6)
        Redirect(routes.Artisan.article(id)).flashing(
          "success" -> "記事を作成しました。"
        )
      }
    )
  }

  def editArticle(id: Long) = IsEditableArticle(id) { acc => art => _ =>
    val data = (art.title, art.text, art.articleType.toString, art.genre, art.optAuthor, art.optDate)
    Ok(views.html.artisan.editArticle(acc.level, id, articleForm.fill(data)))
  }

  def postEditArticle(id: Long) = IsEditableArticle(id) { acc => art => implicit request =>
    articleForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.artisan.editArticle(acc.level, id, formWithErrors)),
      article => {
        Articles.update(id, acc.id, article._1, article._2, article._4, article._5, article._6)
        Redirect(routes.Artisan.article(id)).flashing(
          "success" -> "記事を編集しました。"
        )
      }
    )
  }

  def deleteArticle(id: Long) = IsEditableArticle(id) { _ => _ => _ =>
    Articles.delete(id)
    Redirect(routes.Artisan.articles).flashing(
      "success" -> "記事を削除しました"
    )
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
      "username" -> text.verifying(notEmpty).verifying(pattern("\\w+".r, error = "半角英数字のみです。")),
      "times" -> number,
      "level" -> text.verifying(notEmpty).verifying(pattern("admin|master|writer".r, error = "不正な入力です。"))
    )
  )

  val createAccountForm = Form(
    accountForm.mapping.verifying("そのユーザー名は既に使われています。", result => result match {
      case (_, u, _, _) => !Accounts.all.map(_.username).contains(u)
    })
  )

  def createAccount = HasAuthority(Master) { acc => _ =>
    Ok(views.html.artisan.createAccount(acc, createAccountForm))
  }

  def postCreateAccount = HasAuthority(Master) { acc => implicit request =>
    createAccountForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.artisan.createAccount(acc, formWithErrors)),
      { newacc =>
        val pass = Random.alphanumeric.take(9).mkString
        val id = Accounts.create(newacc._1, newacc._2, pass, OrdInt(newacc._3.toInt), AccountLevel.fromString(newacc._4))
        Accounts.findById(id).map { acc =>
          Ok(views.html.artisan.confirmAccount(acc, pass))
        }.getOrElse(InternalServerError)
      }
    )
  }

  def editAccount(id: Int) = IsEditableAccount(id) { _ => acc => _ =>
    val data = (acc.name, acc.username, acc.times.n, acc.level.toString)
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
      { case (name, username, times, level) =>
        val l = AccountLevel.fromString(level)
        Accounts.update(id, name, username, OrdInt(times.toInt), l)
        Redirect(routes.Artisan.home).flashing(
          "success" -> "アカウントを編集しました。"
        )
      }
    )
  }

  def deleteAccount(id: Int) = GreaterThan(id) { _ => _ => _ =>
    Accounts.delete(id)
    Redirect(routes.Artisan.accounts()).flashing(
      "success" -> "アカウントを削除しました。"
    )
  }

  def deleteMyAccount = IsValidAccount { me => _ =>
    Accounts.delete(me.id)
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

  def data = HasAuthority(Master) { acc => _ =>
    Ok(views.html.artisan.data())
  }

  val datumForm = Form(
    tuple(
      "name" -> text.verifying(notEmpty),
      "genre" -> text.verifying(notEmpty),
      "optAuthor" -> optional(text),
      "optDate" -> optional(text)
    )
  )

  def uploadDatum = HasAuthority(Master) { acc => _ =>
    Ok(views.html.artisan.uploadDatum(datumForm))
  }

  def postUploadDatum = IsValidAccountWithParser(parse.multipartFormData) { acc => implicit request =>
    acc.level match {
      case Admin | Master =>
        request.body.file("file").map { file =>
          datumForm.bindFromRequest().fold(
            formWithErrors => BadRequest(views.html.artisan.uploadDatum(formWithErrors)),
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
          BadRequest(views.html.artisan.uploadDatum(datumForm.withGlobalError("ファイルのアップロードに失敗しました。")))
        }
      case Writer => Forbidden(views.html.errors.forbidden())
    }
  }

  def editDatum(id: Int) = HasAuthority(Master) { acc => request =>
    Data.findById(id).map { datum =>
      val data = (datum.name, datum.genre, datum.optAuthor, datum.optDate)
      Ok(views.html.artisan.editDatum(id, datumForm.fill(data)))
    }.getOrElse(NotFound(views.html.errors.notFound(request.path)))
  }

  def postEditDatum(id: Int) = HasAuthority(Master) { acc => implicit request =>
    datumForm.bindFromRequest().fold(
      formWithErrors => BadRequest(views.html.artisan.editDatum(id, formWithErrors)),
      result => {
        Data.update(id, result._1, result._2, result._3, result._4)
        Redirect(routes.Artisan.home).flashing(
          "success" -> "資料情報を編集しました。"
        )
      }
    )
  }

  def deleteDatum(id: Int) = HasAuthority(Master) { acc => _ =>
    Data.findById(id).map { datum =>
      Data.delete(id)
      new File("." + datum.path).delete()
      Redirect(routes.Artisan.home).flashing(
        "success" -> "資料を削除しました。"
      )
    }.getOrElse(Redirect(routes.Artisan.home).flashing(
      "error" -> "その資料は既に削除されています。"
    ))
  }

  def classData = HasAuthority(Master) { _ => implicit request =>
    Ok(views.html.artisan.classData())
  }

  val classForm = Form(
    tuple(
      "title" -> text,
      "prize" -> text
    )
  )

  def editClassData(id: Int) = AboutClass(id) { _ => data => _ =>
    val d = (data.title, data.prize.map(_.toString).getOrElse("none"))
    Ok(views.html.artisan.editClassData(data.id, classForm.fill(d)))
  }

  def postEditClassData(id: Int) = AboutClass(id) { _ => data => implicit request =>
    classForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.artisan.editClassData(data.id, formWithErrors)),
      result => {
        val prize = Prize.fromString(result._2)
        ClassData.update(data.id, result._1, prize)
        Redirect(routes.Artisan.classData).flashing(
          "success" -> "クラス情報を変更しました。"
        )
      }
    )
  }

  def uploadImage(id: Int) = AboutClass(id) { _ => data => _ =>
    Ok(views.html.artisan.uploadImage(data.id))
  }

  def postUploadImage(id: Int) = IsValidAccountWithParser(parse.multipartFormData) { acc => request =>
    val classId = ClassId.fromId(id)
    ClassData.findByClassId(classId).map { c =>
      acc.level match {
        case Admin | Master => {
          request.body.files.foreach { file =>
            if (file.contentType.map(_.take(5)) == Some("image")) {
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
            } else {
              println("Not image. Abort.")
            }
          }
          Redirect(routes.Artisan.classData).flashing(
            "success" -> "画像をアップロードしました。"
          )
        }
        case Writer => Forbidden(views.html.errors.forbidden())
      }
    }.getOrElse(BadRequest)
  }

  val topForm = Form(single("top" -> optional(text)))

  def selectTop(id: Int) = AboutClass(id) { acc => data => _ =>
    Ok(views.html.artisan.selectTop(data.id, topForm.fill(data.top)))
  }

  def postSelectTop(id: Int) = AboutClass(id) { acc => data => implicit request =>
    topForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.artisan.selectTop(data.id, formWithErrors)),
      top => {
        ClassData.updateTop(data.id, top)
        Redirect(routes.Artisan.classData).flashing(
          "success" -> "トップ画像を変更しました。"
        )
      }
    )
  }

  val deleteImageForm = Form(
    single("filename" -> list(text))
  )

  def deleteImage(id: Int) = AboutClass(id) { acc => data => _ =>
    Ok(views.html.artisan.deleteImage(data.id, deleteImageForm))
  }

  def postDeleteImage(id: Int) = AboutClass(id) { acc => data => implicit request =>
    deleteImageForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.artisan.deleteImage(data.id, formWithErrors)),
      filenames => {
        filenames.map { filename =>
          val fpath = Images.fullsizePath(data.id, filename)
          val tpath = Images.thumbnailPath(data.id, filename)
          new File(fpath).delete()
          new File(tpath).delete()
        }

        Redirect(routes.Artisan.classData).flashing(
          "success" -> "画像を削除しました。"
        )
      }
    )
  }
}
