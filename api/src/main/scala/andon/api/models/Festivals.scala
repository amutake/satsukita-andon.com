package andon.api.models

import scalikejdbc._

final case class Festival(
  times: Int,
  theme: String,
  imageUrl: Option[String]
)

object Festival extends SQLSyntaxSupport[Festival] {
  override val tableName = "festivals"
  def apply(f: SyntaxProvider[Festival])(rs: WrappedResultSet): Festival =
    apply(f.resultName)(rs)
  def apply(f: ResultName[Festival])(rs: WrappedResultSet): Festival =
    new Festival(
      times = rs.get(f.times),
      theme = rs.get(f.theme),
      imageUrl = rs.get(f.imageUrl)
    )
}

object FestivalObjects {
  final case class Detail(
    festival: Festival,
    prizes: Seq[(Prize, ClassData)]
  )
}

object Festivals {

  import FestivalObjects._

  val f = Festival.syntax("f")
  val p = Prize.syntax("p")
  val c = ClassData.syntax("c")

  def all(implicit s: DBSession = Festival.autoSession): Seq[Festival] = {
    withSQL {
      select.from(Festival as f)
    }.map(Festival(f)).list.apply()
  }

  def detail(times: Int)(implicit s: DBSession = Festival.autoSession): Option[Detail] = {
    withSQL {
      select.from(Festival as f)
        .leftJoin(Prize as p).on(p.times, f.times)
        .leftJoin(ClassData as c).on(sqls"".eq(c.times, p.times).and.eq(c.grade, p.grade).and.eq(c.`class`, p.`class`))
        .where.eq(f.times, times)
    }.one(Festival(f)).toManies(
      rs => Prize.opt(p)(rs),
      rs => ClassData.opt(c)(rs) // TODO: opt?
    ).map { (fes, ps, cds) =>
      Detail(fes, ps.zip(cds))
    }.single.apply()
  }
}
