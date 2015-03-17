package andon.api.models

import scalikejdbc._

case class Prize(times: Int, grade: Int, `class`: Int, kind: String)

object Prize extends SQLSyntaxSupport[Prize] {
  override val tableName = "prizes"
  def apply(p: SyntaxProvider[Prize])(rs: WrappedResultSet): Prize =
    apply(p.resultName)(rs)
  def apply(p: ResultName[Prize])(rs: WrappedResultSet): Prize =
    new Prize(
      times = rs.get(p.times),
      grade = rs.get(p.grade),
      `class` = rs.get(p.`class`),
      kind = rs.get(p.kind)
    )
  def opt(p: SyntaxProvider[Prize])(rs: WrappedResultSet): Option[Prize] =
    rs.intOpt(p.resultName.times).map(_ => Prize(p)(rs))
}
