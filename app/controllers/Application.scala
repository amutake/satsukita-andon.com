package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.data.validation.Constraints._

import models._
import andon.utils._

object Application extends Controller with Authentication {

  def index = Action {
    Ok(views.html.index())
  }

  def info(page: Int) = Action { implicit request =>
    val count = 10
    val infos = Articles.findInfoByPage(page, count)
    val max = Articles.countPageByType(Info, count)
    Ok(views.html.info(infos, page, max))
  }

  def gallery = Action {
    val ts = Images.getTimesImages
    Ok(views.html.gallery.top(ts))
  }

  def search = Action {
    val times = TimesData.all
    val tags = Tags.all.map { t =>
      t.tag
    }.distinct
    Ok(views.html.gallery.search(times, tags))
  }

  def tags(tag: String) = Action {
    val ids = Tags.findClassIdByTag(tag)
    val cs = Images.getTopImages(ids.sorted.map { id =>
      ClassData.findByClassId(id)
    }.flatten)
    Ok(views.html.gallery.tags(tag, cs))
  }

  def others(page: Int) = Action {
    val count: Int = 30
    val paths = Images.getOtherImages
    val max: Int = {
      val len = paths.length
      if (len % count == 0 && len != 0) {
        (len / count) - 1
      } else {
        len / count
      }
    }
    Ok(views.html.gallery.others(paths, page, count, max))
  }

  def timesGallery(t: OrdInt) = Action {
    val times = TimesData.findByTimes(t)
    if (times.isDefined) {
      val ps = Images.getClassTopImages(t)
      Ok(views.html.gallery.times(t.toString(), ps))
    } else {
      NotFound(views.html.errors.notFound("/gallery/" + t))
    }
  }

  def classGallery(t: OrdInt, g: Int, c: Int) = Action {
    val id = ClassId(t, g, c)
    ClassData.findByClassId(id).map { data =>
      val ft = Images.getClassImages(id).map { f =>
        (f, Images.toThumbnail(f))
      }
      Ok(views.html.gallery.classg(data, ft))
    }.getOrElse(NotFound)
  }

  def howto = Action {
    Ok(views.html.howto("", ""))
  }

  def allHowto = Action {
    Ok(views.html.howto("all", ""))
  }

  def category(htype: String, genre: String) = Action {
    Ok(views.html.howto(htype, genre))
  }

  def about = Action {
    Ok(views.html.about())
  }

  def contact = Action {
    Ok(views.html.contact())
  }

  val commentForm = Form(
    tuple(
      "accountId" -> optional(number),
      "name" -> text.verifying(nonEmpty),
      "text" -> text.verifying(nonEmpty),
      "password" -> optional(text)
    )
  )

  def article(id: Long) = Action { implicit request =>
    Articles.findById(id).map { article =>
      Ok(views.html.article(article, commentForm, myAccount))
    }.getOrElse(NotFound(views.html.errors.notFound(request.path)))
  }

  def history(id: Long) = Action { implicit request =>
    Articles.findById(id).map { article =>
      val histories = History.histories(id)
      Ok(views.html.history(histories, article))
    }.getOrElse(NotFound(views.html.errors.notFound(request.path)))
  }

  def historyContent(id: Long, commitId: String) = Action { implicit request =>
    Articles.findById(id).flatMap { article =>
      History.history(id, commitId).map { history =>
        Ok(views.html.historyContent(history, article))
      }
    }.getOrElse(NotFound(views.html.errors.notFound(request.path)))
  }

  def diff(id: Long, commitId: String) = Action { implicit request =>
    val result = for {
      article <- Articles.findById(id)
      newHistory <- History.history(id, commitId)
      oldHistory = History.previousHistory(id, commitId)
    } yield Ok(views.html.diff(newHistory, oldHistory, article))
    result.getOrElse(NotFound(views.html.errors.notFound(request.path)))
  }

  def articleSearch(q: String, p: Option[Int]) = Action { implicit request =>
    val page = p.fold(1) { n =>
      if (n < 1) 1 else n
    }
    val limit = 20
    val offset = (page - 1) * limit
    val res = Elasticsearch.search(q, offset, limit)
    Ok(views.html.articleSearch(q, page, res))
  }

  def postComment(id: Long) = Action { implicit request =>
    Articles.findById(id).map { article =>

      def success(commendId: Long, name: String, articleId: Long, title: String) = {
        Notifier.notify(
          tweet = true,
          body = "記事『" + title + "』に" + name + "さんのコメントが投稿されました",
          url = Some("/article/" + articleId + "#comment-" + commendId)
        )
        Redirect(routes.Application.article(id)).flashing(
          "success" -> "コメントを投稿しました。"
        )
      }

      def error(form: Form[(Option[Int], String, String, Option[String])]) = {
        val formWithError = form.withGlobalError("エラー。もう一度投稿してください。")
        BadRequest(views.html.article(article, formWithError, myAccount))
      }

      commentForm.bindFromRequest.fold(
        error,
        result => result match {
          case (account, name, text, password) => {
            myAccount.map { acc =>
              if (account == Some(acc.id)) {
                val id = Comments.create(article.id, account, acc.name, text, None)
                success(id, acc.name, article.id, article.title)
              } else {
                error(commentForm.fill(result).withGlobalError("送信されたアカウント情報が間違っています"))
              }
            }.getOrElse {
              account.map { _ =>
                error(commentForm.fill(result).withGlobalError("ログインしてください"))
              }.getOrElse {
                if ("""https?://([\w-]+\.)+[\w-]+(/[\w- ./?%&=]*)?""".r.findFirstIn(text).isEmpty) {
                  val id = Comments.create(article.id, None, name, text, password)
                  success(id, name, article.id, article.title)
                } else {
                  error(commentForm.fill(result).withGlobalError("URLは投稿できません"))
                }
              }
            }
          }
        }
      )
    }.getOrElse {
      BadRequest(views.html.errors.badRequest("その記事は存在しません"))
    }
  }

  def comments = Action {
    Ok(views.html.allComments())
  }

  def deleteComment(id: Long) = Action { implicit request =>

    Comments.findById(id).map { comment =>

      val form = Form(
        single("password" -> text)
      )
      val redirect = Redirect(routes.Application.article(comment.articleId))
      def del = {
        Comments.delete(id)
        Notifier.notify(
          tweet = false,
          body = "記事『" + Articles.findTitleById(comment.articleId) + "』への" + comment.name + "さんのコメントが削除されました"
        )
        redirect.flashing(
          "success" -> "コメントを削除しました。"
        )
      }

      form.bindFromRequest.fold(
        _ => redirect.flashing(
          "error" -> "入力エラー"
        ),
        password => {
          if (comment.accountId.isDefined && comment.accountId == myAccount.map(_.id)) {
            del
          } else if (comment.accountId.isEmpty && myAccount.map(_.level).getOrElse(Writer) != Writer) {
            del
          } else {
            if (Comments.authenticate(id, Some(password)).isDefined) {
              del
            } else {
              redirect.flashing(
                "error" -> "パスワードが違います。"
              )
            }
          }
        }
      )
    }.getOrElse {
      NotFound(views.html.errors.notFound("コメントが見つかりません"))
    }
  }

  def editComment(id: Long) = Action { implicit request =>

    Comments.findById(id).map { comment =>

      val form = Form(
        tuple(
          "text" -> text,
          "password" -> text
        )
      )
      val redirect = Redirect(routes.Application.article(comment.articleId))
      def edit(text: String) = {
        Comments.update(id, text)
        Notifier.notify(
          tweet = false,
          body = "記事『" + Articles.findTitleById(comment.articleId) + "』への" + comment.name + "さんのコメントが編集されました",
          url = Some("/article/" + comment.articleId + "#comment-" + id)
        )
        redirect.flashing(
          "success" -> "コメントを編集しました。"
        )
      }

      form.bindFromRequest.fold(
        _ => redirect.flashing(
          "error" -> "入力エラー"
        ),
        result => {
          if (comment.accountId.isDefined && comment.accountId == myAccount.map(_.id)) {
            edit(result._1)
          } else {
            if (Comments.authenticate(id, Some(result._2)).isDefined) {
              edit(result._1)
            } else {
              redirect.flashing(
                "error" -> "パスワードが違います。"
              )
            }
          }
        }
      )
    }.getOrElse {
      NotFound(views.html.errors.notFound("コメントが見つかりません"))
    }
  }
}
