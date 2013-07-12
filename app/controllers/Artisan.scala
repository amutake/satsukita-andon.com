package controllers

import scala.util.Random

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._

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

  def articles = IsValidAccount { account => _ =>
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
      "genre" -> text
    )
  )

  def editable(level: AccountLevel) = level match {
    case Admin | Master => List(Info, Howto)
    case Writer => List(Howto)
  }

  def createArticle = IsValidAccount { acc => _ =>
    Ok(views.html.artisan.createArticle(editable(acc.level), articleForm))
  }

  def postCreateArticle = IsValidAccount { acc => implicit request =>
    articleForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.artisan.createArticle(editable(acc.level), formWithErrors)),
      { article =>
        val id = Articles.create(acc.id, article._1, article._2, ArticleType.fromString(article._3), article._4)
        Redirect(routes.Artisan.article(id)).flashing(
          "success" -> "記事を作成しました。"
        )
      }
    )
  }

  def editArticle(id: Long) = IsEditableArticle(id) { acc => art => _ =>
    val data = (art.title, art.text, art.articleType.toString, art.genre)
    Ok(views.html.artisan.editArticle(id, articleForm.fill(data)))
  }

  def postEditArticle(id: Long) = IsEditableArticle(id) { acc => art => implicit request =>
    articleForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.artisan.editArticle(id, formWithErrors)),
      article => {
        Articles.update(id, acc.id, article._1, article._2, article._4)
        Redirect(routes.Artisan.article(id)).flashing(
          "success" -> "記事を編集しました。"
        )
      }
    )
  }

  def deleteArticle(id: Long) = IsEditableArticle(id) { _ => _ => _ =>
    Articles.delete(id)
    Redirect(routes.Artisan.home).flashing(
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
}
