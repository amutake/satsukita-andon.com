package andon.api.models

import scalikejdbc._

case class User(
  id: Long,
  login: String,
  password: String,
  name: String,
  times: Int, // times?
  icon: Option[String],
  firstId: Option[Int], // class id
  secondId: Option[Int],
  thirdId: Option[Int]
)

object User extends SQLSyntaxSupport[User] {
  override val tableName = "users"
  def apply(u: SyntaxProvider[User])(rs: WrappedResultSet): User =
    apply(u.resultName)(rs)
  def apply(u: ResultName[User])(rs: WrappedResultSet): User =
    new User(
      id = rs.get(u.id),
      login = rs.get(u.login),
      password = rs.get(u.password),
      name = rs.get(u.name),
      times = rs.get(u.times),
      icon = rs.get(u.icon),
      firstId = rs.get(u.firstId),
      secondId = rs.get(u.secondId),
      thirdId = rs.get(u.thirdId)
    )
}

object Users {
  val u = User.syntax
  val column = User.column
}
