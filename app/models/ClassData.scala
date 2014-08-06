package models

import scala.slick.driver.H2Driver.simple._

import andon.utils._

case class ClassData(id: ClassId, title: String, prize: Option[Prize], top: Option[String], intro: String)

object ClassData extends Table[ClassData]("CLASSDATA") {

  val db = DB.db

  def id = column[ClassId]("ID", O.NotNull, O.PrimaryKey)
  def times = column[OrdInt]("TIMES", O.NotNull)
  def grade = column[Int]("GRADE", O.NotNull)
  def classn = column[Int]("CLASSN", O.NotNull)
  def title = column[String]("TITLE", O.NotNull)
  def prize = column[Option[Prize]]("PRIZE")
  def top = column[Option[String]]("TOP")
  def intro = column[String]("INTRO")

  def * = id ~ times ~ grade ~ classn ~ title ~ prize ~ top ~ intro <> (
    (id, _, _, _, title, prize, top, intro) => ClassData(id, title, prize, top, intro),
    d => Some((d.id, d.id.times, d.id.grade, d.id.classn, d.title, d.prize, d.top, d.intro))
  )

  val query = Query(ClassData)

  val sorted = query.sortBy(_.classn.asc).sortBy(_.grade.asc).sortBy(_.times.desc)

  def findByClassId(c: ClassId): Option[ClassData] = db.withSession { implicit session: Session =>
    query.filter(_.id === c).firstOption
  }

  def all = db.withSession { implicit session: Session =>
    sorted.list
  }

  def findByTimes(t: OrdInt) = db.withSession { implicit session: Session =>
    query.filter(_.times === t).list
  }

  def search(times: String, prize: String, grade: String, tag: String) = db.withSession { implicit session: Session =>

    val q = if (times == "all") {
      sorted
    } else {
      sorted.where(_.times === OrdInt(times.toInt))
    }

    val q1 = if (prize == "all") {
      q
    } else if (prize == "none") {
      q.where(_.prize.isNull)
    } else {
      q.where(_.prize === Prize.fromString(prize))
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
        data <- q2
        id <- tagId
        if data.id === id
      } yield data
    }

    q3.list
  }

  def create(data: ClassData) = db.withSession { implicit session: Session =>
    ClassData.insert(data)
  }

  def createByClassId(id: ClassId) = db.withSession { implicit session: Session =>
    ClassData.insert(ClassData(id, "", None, None, ""))
  }

  def update(id: ClassId, title: String, prize: Option[Prize], intro: String) = db.withSession { implicit session: Session =>
    query.where(_.id === id).map(d => d.title ~ d.prize ~ d.intro).update((title, prize, intro))
  }

  def updateTop(id: ClassId, top: Option[String]) = db.withSession { implicit session: Session =>
    query.where(_.id === id).map(_.top).update(top)
  }
}
