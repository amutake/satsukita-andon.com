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

  def home = IsAuthenticated { userid => implicit request =>
    Accounts.findById(userid).map { account =>
      Ok(views.html.artisan.home(account))
    }.getOrElse(Forbidden)
  }

  def articles = IsAuthenticated { userid => _ =>
    Accounts.findById(userid).map { account =>
      account.level match {
        case Admin | Master => Ok(views.html.artisan.articles(account, Articles.all))
        case Writer => Ok(views.html.artisan.articles(account, Articles.findByCreateAccountId(userid)))
      }
    }.getOrElse(Forbidden)
  }

  def article(id: Long) = IsAuthenticated { userid => _ =>
    Accounts.findById(userid).map { account =>
      Articles.findById(id).map { article =>
        Ok(views.html.artisan.article(account, article))
      }.getOrElse(NotFound(views.html.errors.notFound("/artisan/article?id=" + id.toString)))
    }.getOrElse(Forbidden)
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

  def accounts = IsAuthenticated { userid => _ =>
    Accounts.findById(userid).map { account =>
      account.level match {
        case Admin => Ok(views.html.artisan.accounts(account, Accounts.findByLevels(Seq(Master, Writer))))
        case Master => Ok(views.html.artisan.accounts(account, Accounts.findByLevel(Writer)))
        case Writer => Redirect(routes.Artisan.home).flashing(
          "error" -> "その操作は許可されていません"
        )
      }
    }.getOrElse(Forbidden)
  }

  def account(id: Int) = IsAuthenticated { userid => _ =>
    Accounts.findById(userid).map { myaccount =>
      Accounts.findById(id).map { account =>
        val mine = userid == id
        val l = account.level
        myaccount.level match {
          case Admin if mine || l == Master || l == Writer => Ok(views.html.artisan.account(account))
          case Master if mine || l == Writer => Ok(views.html.artisan.account(account))
          case Writer if mine => Ok(views.html.artisan.account(account))
          case _ => Redirect(routes.Artisan.home).flashing(
            "error" -> "その操作は許可されていません"
          )
        }
      }.getOrElse(NotFound(views.html.errors.notFound("/artisan/account?id=" + id.toString)))
    }.getOrElse(Forbidden)
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

  def createAccount = IsAuthenticated { userid => _ =>
    Accounts.findById(userid).map { account =>
      account.level match {
        case Admin | Master => Ok(views.html.artisan.createAccount(accountForm))
        case Writer => Redirect(routes.Artisan.home)
      }
    }.getOrElse(Forbidden)
  }

  def postCreateAccount = IsAuthenticated { userid => implicit request =>
    Accounts.findById(userid).map { account =>
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
      }
    }.getOrElse(Forbidden)
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

  def editAccount(id: Int) = IsAuthenticated { userid => _ =>
    Accounts.findById(userid).map { myaccount =>
      Accounts.findById(id).map { account =>
        val mine = userid == id
        val l = account.level
        val form = editAccountForm
        myaccount.level match {
          case Admin if mine || l == Master || l == Writer =>
            Ok(views.html.artisan.editAccount(form, account))
          case Master if mine || l == Writer =>
            Ok(views.html.artisan.editAccount(form, account))
          case Writer if mine => Ok(views.html.artisan.editAccount(form, account))
          case _ => Redirect(routes.Artisan.home).flashing(
            "error" -> "その操作は許可されていません"
          )
        }
      }.getOrElse(NotFound(views.html.errors.notFound("/artisan/account/edit?id=" + id.toString)))
    }.getOrElse(Forbidden)
  }

  def postEditAccount = IsAuthenticated { userid => implicit request =>
    Accounts.findById(userid).map { account =>
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
    }.getOrElse(Forbidden)
  }
}
