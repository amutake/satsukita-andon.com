package controllers

import scala.util.Random

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import models._
import andon.utils._

object Artisan extends Controller with Authentication {

  val loginForm = Form(
    tuple(
      "username" -> text,
      "password" -> text
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
        }.getOrElse(Forbidden)
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
      case Admin | Master => Ok(views.html.artisan.articles(account, Articles.all))
      case Writer => Ok(views.html.artisan.articles(account, Articles.findByCreateAccountId(account.id)))
    }
  }

  def article(id: Long) = IsEditableArticle(id) { account => article => _ =>
    Ok(views.html.artisan.article(account, article))
  }

  val articleForm = Form(
    tuple(
      "title" -> text,
      "text" -> text,
      "type" -> text
    ) verifying ("タイトルまたは本文が空です。", result => result match {
      case ("", _, _) => false
      case (_, "", _) => false
      case (_, _, _) => true
    })
  )

  def createArticle = IsAuthenticated { _ => _ =>
    Ok(views.html.artisan.createArticle(articleForm))
  }

  def postCreateArticle = IsAuthenticated { userid => implicit request =>
    articleForm.bindFromRequest.fold(
      formWithErrors => BadRequest(views.html.artisan.createArticle(formWithErrors)),
      { article =>
        Articles.create(userid, article._1, article._2, ArticleType.fromString(article._3))
        Redirect(routes.Artisan.articles)
      }
    )
  }

  def accounts = IsValidAccount { account => _ =>
    account.level match {
      case Admin => Ok(views.html.artisan.accounts(account, Accounts.findByLevels(Seq(Master, Writer))))
      case Master => Ok(views.html.artisan.accounts(account, Accounts.findByLevel(Writer)))
      case Writer => Redirect(routes.Artisan.home).flashing(
        "error" -> "その操作は許可されていません"
      )
    }
  }

  def account(id: Int) = IsEditableAccount(id) { me => acc => _ =>
    Ok(views.html.artisan.account(acc))
  }

  val accountForm = Form(
    tuple(
      "name" -> text,
      "username" -> text,
      "times" -> number,
      "level" -> text
    ) verifying ("空の項目があります。", result => result match {
      case (u, n, t, l) if u.trim.isEmpty || n.trim.isEmpty || l.trim.isEmpty => false
      case _ => true
    })
  )

  def createAccount = IsValidAccount { account => _ =>
    account.level match {
      case Admin | Master => Ok(views.html.artisan.createAccount(accountForm))
      case Writer => Redirect(routes.Artisan.home)
    }
  }

  def postCreateAccount = IsValidAccount { account => implicit request =>
    account.level match {
      case Admin | Master => accountForm.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.artisan.createAccount(formWithErrors)),
        { newAccount =>
          Accounts.create(newAccount._1, newAccount._2, Random.nextString(9), OrdInt(newAccount._3.toInt), AccountLevel.fromString(newAccount._4))
          Redirect(routes.Artisan.home).flashing(
            "success" -> "アカウントを作成しました。"
          )
        }
      )
      case Writer => Forbidden
    }
  }

  val editAccountForm = Form(
    tuple(
      "id" -> number,
      "name" -> text,
      "username" -> text,
      "password" -> text,
      "times" -> number,
      "level" -> text
    ) verifying ("空の項目があります。", result => result match {
      case (i, u, n, p, t, a) if u.trim.isEmpty || n.trim.isEmpty || p.trim.isEmpty || a.trim.isEmpty => false
      case _ => true
    })
  )

  def editAccount(id: Int) = IsEditableAccount(id) { me => acc => _ =>
    Ok(views.html.artisan.editAccount(editAccountForm, acc))
  }

  def postEditAccount = IsValidAccount { me => implicit request =>
    editAccountForm.bindFromRequest.fold(
      { formWithErrors =>
        Accounts.findById(formWithErrors.get._1).map { acc =>
          BadRequest(views.html.artisan.editAccount(formWithErrors, acc))
        }.getOrElse(Redirect(routes.Artisan.home).flashing(
          "error" -> "不正な操作"
        ))
      },
      { newAccount =>
        val acc = Account(newAccount._1.toInt, newAccount._2, newAccount._3, newAccount._4, OrdInt(newAccount._5.toInt), AccountLevel.fromString(newAccount._6))
        Accounts.update(acc)
        Redirect(routes.Artisan.home).flashing(
          "success" -> "アカウントを編集しました。"
        )
      }
    )
  }
}
