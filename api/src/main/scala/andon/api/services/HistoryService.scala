package andon.api.services

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.revwalk.{ RevWalk, RevCommit }
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.treewalk.filter.PathFilter
import org.eclipse.jgit.internal.storage.file.FileRepository

import java.io.{ File, PrintWriter }

import scala.collection.JavaConversions._
import scala.util.Try
import com.typesafe.config.ConfigFactory
import com.github.nscala_time.time.Imports._

object HistoryObjects {
  case class History(id: String, articleId: Long, userId: Long, date: DateTime) {
    lazy val body = HistoryService.body(articleId, id)
  }
}

trait HistoryService {

  import HistoryObjects._

  private lazy val dir = {
    val conf = ConfigFactory.load()
    conf.getString("history.path")
  }
  private val repository = new FileRepository(dir ++ ".git")
  private val git = new Git(repository)
  private val email = "satsukita.andon@gmail.com"

  private def filename(id: Long) = id + ".md"

  // create and update

  def create(id: Long, body: String, authorId: Long) = { // id is article id
    commonUpdate(id, body, authorId, "Create")
  }

  def update(id: Long, body: String, authorId: Long) = {
    commonUpdate(id, body, authorId, "Update")
  }

  def delete(id: Long, authorId: Long) = {
    val name = filename(id)
    git.rm.addFilepattern(name).call
    commit(id, authorId, "Delete")
  }

  private def commonUpdate(id: Long, body: String, authorId: Long, typ: String) = {
    val name = filename(id)
    val file = new PrintWriter(dir ++ name)
    file.print(body)
    file.close
    git.add.addFilepattern(name).call
    commit(id, authorId, typ)
  }

  private def commit(id: Long, authorId: Long, typ: String) = {
    val message = s"AUTO COMMIT ver1: ${typ}: article id = ${id}, account id = ${authorId}"
    git.commit.setMessage(message)
      .setAuthor(authorId.toString, email)
      .setCommitter(authorId.toString, email)
      .call
  }

  // history

  private def commitToHistory(id: Long, commit: RevCommit) = {
    val date = new DateTime(commit.getCommitTime.toLong * 1000)
    History(commit.getName, id, commit.getAuthorIdent.getName.toLong, date)
  }

  def histories(id: Long): Option[Seq[History]] = {
    Try(git.log.addPath(filename(id)).call).toOption.map { cs =>
      cs.map(commitToHistory(id, _)).toSeq
    }
  }

  def history(articleId: Long, commitId: String): Option[History] = {
    getCommit(commitId).map(commitToHistory(articleId, _))
  }

  def previousHistory(articleId: Long, commitId: String): Option[History] = {
    histories(articleId).flatMap { hists =>
      val i = hists.indexWhere(h => h.id == commitId)
      if (i == -1) {
        None
      } else {
        hists.lift(i + 1)
      }
    }
  }

  private def getCommit(commitId: String): Option[RevCommit] = {
    Try(new RevWalk(repository).parseCommit(repository.resolve(commitId))).toOption
  }

  def body(articleId: Long, commitId: String): Option[String] = {
    try {
      getCommit(commitId).map { commit =>
        val treewalk = new TreeWalk(repository)
        treewalk.addTree(commit.getTree)
        treewalk.setRecursive(true)
        treewalk.setFilter(PathFilter.create(filename(articleId)))
        treewalk.next() match {
          case false => None
          case true => {
            val id = treewalk.getObjectId(0)
            Some(new String(repository.open(id).getBytes))
          }
        }
      }.getOrElse(None)
    } catch {
      case _: Throwable => None
    }
  }
}

object HistoryService extends HistoryService
