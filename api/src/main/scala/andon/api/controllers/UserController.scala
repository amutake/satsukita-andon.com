package andon.api.controllers

import andon.api.models.User

object UserJsons {
  case class Simple(
    id: Long,
    login: String,
    name: String,
    icon: Option[String]
  )

  object Simple {
    def apply(user: User): Simple =
      Simple(
        user.id,
        user.login,
        user.name,
        user.icon)
  }
}
