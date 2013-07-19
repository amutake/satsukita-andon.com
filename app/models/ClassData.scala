package models

import scala.slick.driver.H2Driver.simple._

import andon.utils._

case class ClassData(id: ClassId, title: String, prize: Option[Prize], top: Option[String])

object ClassData extends Table[ClassData]("CLASSDATA") {

  val db = DB.db

  def id = column[ClassId]("ID", O.NotNull, O.PrimaryKey)
  def times = column[Int]("TIMES", O.NotNull)
  def grade = column[Int]("GRADE", O.NotNull)
  def classn = column[Int]("CLASSN", O.NotNull)
  def title = column[String]("TITLE", O.NotNull)
  def prize = column[Option[String]]("PRIZE")
  def top = column[Option[String]]("TOP")

  def * = id ~ times ~ grade ~ classn ~ title ~ prize ~ top <> (
    (id, times, grade, classn, title, prize, top) => ClassData(id, title, prize.flatMap(Prize.fromString), top),
    data => Some((data.id, data.id.times.n, data.id.grade, data.id.classn, data.title, data.prize.map(_.toString), data.top))
  )

  val query = Query(ClassData)

  val desc = query.sortBy(_.times.desc)

  def findByClassId(c: ClassId): Option[ClassData] = db.withSession { implicit session: Session =>
    query.filter(_.id === c).firstOption
  }

  def all = db.withSession { implicit session: Session =>
    desc.list
  }

  def findByTimes(t: OrdInt) = db.withSession { implicit session: Session =>
    query.filter(_.times === t.n).list
  }

  def search(times: String, prize: String, grade: String, tag: String) = db.withSession { implicit session: Session =>

    val q = if (times == "all") {
      desc
    } else {
      desc.where(_.times === times.toInt)
    }

    val q1 = if (prize == "all") {
      q
    } else if (prize == "none") {
      q.where(_.prize.isNull)
    } else {
      q.where(_.prize === prize)
    }

    val q2 = if (grade == "all") {
      q1
    } else {
      q1.where(_.grade === grade.toInt)
    }

    val q3 = if (tag == "all") {
      q2
    } else {
      val tagId = for {
        t <- Tags if t.tag === tag
      } yield t.classId

      for {
        id <- tagId
        data <- q2
        if data.id === id
      } yield data
    }

    q3.list
  }

  def create(data: ClassData) = db.withSession { implicit session: Session =>
    ClassData.insert(data)
  }

  def updateTop(id: ClassId, top: Option[String]) = db.withSession { implicit session: Session =>
    query.where(_.id === id).map(_.top).update(top)
  }
}
