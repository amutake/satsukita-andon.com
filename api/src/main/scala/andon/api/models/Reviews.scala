package andon.api.models

import scalikejdbc._
import com.github.nscala_time.time.Imports.DateTime

final case class Review(
  times: Int,
  grade: Int,
  `class`: Int,
  userId: Long,
  text: String,
  createdAt: DateTime,
  updatedAt: Option[DateTime]
)

object Review extends SQLSyntaxSupport[Review] {
  override val tableName = "reviews"
  def apply(r: SyntaxProvider[Review])(rs: WrappedResultSet): Review =
    apply(r.resultName)(rs)
  def apply(r: ResultName[Review])(rs: WrappedResultSet): Review =
    new Review(
      times = rs.get(r.times),
      grade = rs.get(r.grade),
      `class` = rs.get(r.`class`),
      userId = rs.get(r.userId),
      text = rs.get(r.text),
      createdAt = rs.get(r.createdAt),
      updatedAt = rs.get(r.updatedAt)
    )
}

object ReviewObjects {
  final case class Base(review: Review, user: User)
}

object Reviews {

  import ReviewObjects._

  val r = Review.syntax("r")
  val u = User.syntax("u")

  def all(times: Int, grade: Int, `class`: Int)(implicit s: DBSession = Review.autoSession): Seq[Base] = {
    withSQL {
      select.from(Review as r)
        .innerJoin(User as u).on(u.id, r.userId)
        .where.eq(r.times, times).and.eq(r.grade, grade).and.eq(r.`class`, `class`)
    }.one(Review(r)).toOne(User(u))
      .map { (r, u) => Base(r, u) }
      .list.apply()
  }
}
