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

object ArtisanAccount extends Controller with ControllerUtils with Authentication {

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
          Notifier.notify(
            tweet = false,
            body = me.name + "により新しいアカウント『" + acc.name + "』が作られました"
          )
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
        Notifier.notify(
          tweet = false,
          body = if (me.id == id) {
            me.name + "が自分のアカウント情報を編集しました"
          } else {
            me.name + "によりアカウント『" + name + "』の情報が編集されました"
          }
        )
        Redirect(routes.Artisan.home).flashing(
          "success" -> "アカウントを編集しました。"
        )
      }
    )
  }

  def deleteAccount(id: Int) = GreaterThan(id) { me => acc => _ =>
    Accounts.delete(id)
    Notifier.notify(
      tweet = false,
      body = me.name + "によりアカウント『" + acc.name + "』が削除されました"
    )
    Redirect(routes.ArtisanAccount.accounts()).flashing(
      "success" -> "アカウントを削除しました。"
    )
  }

  def deleteMyAccount = IsValidAccount { me => _ =>
    Accounts.delete(me.id)
    Notifier.notify(
      tweet = false,
      body = me.name + "が自分のアカウントを削除しました"
    )
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
        Notifier.notify(
          tweet = false,
          body = acc.name + "が自分のパスワードを変更しました"
        )
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
        Notifier.notify(
          tweet = false,
          body = me.name + "が" + acc.name + "のパスワードを変更しました"
        )
        Redirect(routes.Artisan.home).flashing(
          "success" -> "パスワードを変更しました。"
        )
      } else {
        BadRequest(views.html.artisan.editPassword(acc, passwordForm.withGlobalError("パスワードが間違っています。")))
      }
    )
  }
}
