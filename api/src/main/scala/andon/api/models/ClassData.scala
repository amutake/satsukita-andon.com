package andon.api.models

import scalikejdbc._

case class ClassData(
  times: Int,
  grade: Int,
  `class`: Int,
  title: String,
  description: Option[String],
  topUrl: Option[String]
)

object ClassData extends SQLSyntaxSupport[ClassData] {

  import ClassDataObjects._

  override val tableName = "class_data"
  def apply(c: SyntaxProvider[ClassData])(rs: WrappedResultSet): ClassData =
    apply(c.resultName)(rs)
  def apply(c: ResultName[ClassData])(rs: WrappedResultSet): ClassData =
    new ClassData(
      times = rs.get(c.times),
      grade = rs.get(c.grade),
      `class` = rs.get(c.`class`),
      title = rs.get(c.title),
      description = rs.get(c.description),
      topUrl = rs.get(c.topUrl)
    )
  def opt(c: SyntaxProvider[ClassData])(rs: WrappedResultSet): Option[ClassData] =
    rs.intOpt(c.resultName.times).map(_ => ClassData(c)(rs))

  val c = syntax("c")
  val p = Prize.syntax("p")

  def find(times: Int, grade: Int, `class`: Int)
    (implicit s: DBSession = autoSession): Option[Base] = {
    withSQL {
      select.from(ClassData as c)
        .leftJoin(Prize as p).on(sqls"".eq(c.times, p.times).and.eq(c.grade, p.grade).and.eq(c.`class`, p.`class`))
        .where.eq(c.times, times).and.eq(c.grade, grade).and.eq(c.`class`, `class`)
    }.one(ClassData(c)).toMany(Prize.opt(p))
      .map { (cd, prizes) => Base(cd, prizes.map(_.kind)) }
      .single.apply()
  }

  def gradeAll(times: Int, grade: Int)(implicit s: DBSession = autoSession): Seq[Base] = {
    withSQL {
      select.from(ClassData as c)
        .leftJoin(Prize as p).on(sqls"".eq(c.times, p.times).and.eq(c.grade, p.grade).and.eq(c.`class`, p.`class`))
        .where.eq(c.times, times).and.eq(c.grade, grade)
    }.one(ClassData(c)).toMany(Prize.opt(p))
      .map { (cd, prizes) => Base(cd, prizes.map(_.kind)) }
      .list.apply()
  }

  def timesAll(times: Int)(implicit s: DBSession = autoSession): Seq[Base] = {
    withSQL {
      select.from(ClassData as c)
        .leftJoin(Prize as p).on(sqls"".eq(c.times, p.times).and.eq(c.grade, p.grade).and.eq(c.`class`, p.`class`))
        .where.eq(c.times, times)
    }.one(ClassData(c)).toMany(Prize.opt(p))
      .map { (cd, prizes) => Base(cd, prizes.map(_.kind)) }
      .list.apply()
  }
}

object ClassDataObjects {
  final case class Base(cd: ClassData, prizes: Seq[String])
}
