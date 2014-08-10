package models

import play.api.libs.Codecs._

import scala.slick.driver.H2Driver.simple._

import andon.utils._

case class Account(id: Int, name: String, username: String, password: String, times: OrdInt, level: AccountLevel, class1: Option[ClassId], class2: Option[ClassId], class3: Option[ClassId]) {
  def validPassword(pass: String) = sha1(pass) == password
  def myClass(id: ClassId) = Some(id) == class1 || Some(id) == class2 || Some(id) == class3
}

object Accounts extends Table[Account]("ACCOUNTS") {

  val db = DB.db

  def id = column[Int]("ID", O.NotNull, O.PrimaryKey, O.AutoInc)
  def name = column[String]("NAME", O.NotNull)
  def username = column[String]("USERNAME", O.NotNull)
  def password = column[String]("PASSWORD", O.NotNull)
  def times = column[OrdInt]("TIMES", O.NotNull)
  def level = column[AccountLevel]("LEVEL", O.NotNull)
  def class1 = column[Option[ClassId]]("CLASS1")
  def class2 = column[Option[ClassId]]("CLASS2")
  def class3 = column[Option[ClassId]]("CLASS3")

  def * = id ~ name ~ username ~ password ~ times ~ level ~ class1 ~ class2 ~ class3 <> (
    Account.apply _,
    Account.unapply _
  )

  def ins = name ~ username ~ password ~ times ~ level ~ class1 ~ class2 ~ class3 returning id

  val query = Query(Accounts)

  def findById(id: Int): Option[Account] = db.withSession { implicit session: Session =>
    query.where(_.id === id).firstOption
  }

  def findByUsername(username: String): Option[Account] = db.withSession { implicit session: Session =>
    query.where(_.username === username).firstOption
  }

  def authenticate(username: String, password: String) = db.withSession { implicit session: Session =>
    query.where(_.username === username).where(_.password === sha1(password)).firstOption
  }

  def all = db.withSession { implicit session: Session =>
    query.list
  }

  def findByLevel(l: AccountLevel) = db.withSession { implicit session: Session =>
    query.where(_.level === l).list
  }

  def findByLevels(ls: Seq[AccountLevel]) = db.withSession { implicit session: Session =>
    val qs = ls.map { l =>
      query.where(_.level === l)
    }
    qs.reduce(_ union _).list
  }

  def findNameById(id: Int) = db.withSession { implicit session: Session =>
    query.where(_.id === id).firstOption.map { acc =>
      acc.name // + "(" + acc.times.n + "期)"
    }.getOrElse("?")
  }

  def create(name: String, username: String, password: String, times: OrdInt, level: AccountLevel,
    class1: Option[ClassId], class2: Option[ClassId], class3: Option[ClassId]) = db.withSession { implicit session: Session =>
    Accounts.ins.insert(name, username, sha1(password), times, level, class1, class2, class3)
  }

  def update(id: Int, name: String, username: String, times: OrdInt, level: AccountLevel,
    class1: Option[ClassId], class2: Option[ClassId], class3: Option[ClassId]) = db.withSession { implicit session: Session =>
    query.where(_.id === id).map(a => a.name ~ a.username ~ a.times ~ a.level ~ a.class1 ~ a.class2 ~ a.class3).update((name, username, times, level, class1, class2, class3))
  }

  def updatePassword(id: Int, password: String) = db.withSession { implicit session: Session =>
    query.where(_.id === id).map(_.password).update(sha1(password))
  }

  def delete(id: Int) = db.withSession { implicit session: Session =>
    query.where(_.id === id).delete
  }
}
