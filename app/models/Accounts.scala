package models

import play.api.db._
import play.api.Play.current
import play.api.libs.Codecs._

import scala.slick.driver.H2Driver.simple._

import andon.utils._

case class Account(id: Int, name: String, username: String, password: String, times: OrdInt, level: AccountLevel)

object Accounts extends Table[Account]("ACCOUNTS") {

  val db = Database.forDataSource(DB.getDataSource("default"))

  def id = column[Int]("ID", O.NotNull, O.PrimaryKey, O.AutoInc)
  def name = column[String]("NAME", O.NotNull)
  def username = column[String]("USERNAME", O.NotNull)
  def password = column[String]("PASSWORD", O.NotNull)
  def times = column[Int]("TIMES", O.NotNull)
  def level = column[AccountLevel]("LEVEL", O.NotNull)

  def * = id ~ name ~ username ~ password ~ times ~ level <> (
    (id, name, username, password, times, level) => Account(id, name, username, password, OrdInt(times), level),
    account => Some((account.id, account.name, account.username, account.password, account.times.n, account.level))
  )

  def ins = name ~ username ~ password ~ times ~ level returning id

  val query = Query(Accounts)

  def findById(id: Int): Option[Account] = db.withSession { implicit session: Session =>
    query.filter(_.id === id).firstOption
  }

  def findByUsername(username: String): Option[Account] = db.withSession { implicit session: Session =>
    query.filter(_.username === username).firstOption
  }

  def authenticate(username: String, password: String) = db.withSession { implicit session: Session =>
    query.filter(_.username === username).filter(_.password === sha1(password)).firstOption
  }

  def all = db.withSession { implicit session: Session =>
    query.list
  }

  def findByAccountLevel(a: AccountLevel) = db.withSession { implicit session: Session =>
    query.filter(_.level === a).list
  }

  def create(name: String, username: String, password: String, times: OrdInt, atype: AccountLevel) = db.withSession { implicit session: Session =>
    Accounts.ins.insert(name, username, sha1(password), times.n, atype)
  }
}
