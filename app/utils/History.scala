package andon.utils

import org.eclipse.jgit.api._
import org.eclipse.jgit.lib._
import org.eclipse.jgit.revwalk._
import org.eclipse.jgit.treewalk._
import org.eclipse.jgit.treewalk.filter._
import org.eclipse.jgit.internal.storage.file._

import java.io.PrintWriter

import collection.JavaConversions._

case class History(id: String, articleId: Long, accountId: Int, time: Int) {
  def content = History.content(articleId, id)
}

object History {

  private val dir = "./history/"
  private val repository = new FileRepository(dir ++ ".git")
  private val git = new Git(repository)
  private val email = "satsukita.andon@gmail.com"

  private def filename(id: Long) = id + ".md"

  // create and update

  def create(id: Long, text: String, authorId: Int) = { // id is article id
    commonUpdate(id, text, authorId, "Create")
  }

  def update(id: Long, text: String, authorId: Int) = {
    commonUpdate(id, text, authorId, "Update")
  }

  def delete(id: Long, authorId: Int) = {
    val name = filename(id)
    git.rm.addFilepattern(name).call
    commit(id, authorId, "Delete")
  }

  private def commonUpdate(id: Long, text: String, authorId: Int, typ: String) = {
    val name = filename(id)
    val file = new PrintWriter(dir ++ name)
    file.println(text)
    file.close
    git.add.addFilepattern(name).call
    commit(id, authorId, typ)
  }

  private def commit(id: Long, authorId: Int, typ: String) = {
    val message = typ + ": article id = " + id + ", account id = " + authorId
    git.commit.setMessage(message)
      .setAuthor(authorId.toString, email)
      .setCommitter(authorId.toString, email)
      .call
  }

  // history

  def histories(id: Long) = {
    git.log.addPath(filename(id)).call.map { commit =>
      History(commit.getName, id, commit.getAuthorIdent.getName.toInt, commit.getCommitTime)
    }
  }

  def content(articleId: Long, commitId: String): Option[String] = {
    try {
      val commitObjectId = repository.resolve(commitId)
      val commit = new RevWalk(repository).parseCommit(commitObjectId)
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
    } catch {
      case _: Exception => None
    }
  }
}
