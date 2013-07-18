package inits

import scala.slick.driver.H2Driver.simple._

import andon.utils._

object Accounts {

  def initialize = {

    if (models.Accounts.all.isEmpty) {
      DB.db.withSession { implicit session: Session =>

        models.Accounts.ins.insertAll(
          ("甲乙人", "kohotsunin", "9d4e1e23bd5b727046a9e3b4b7db57bd8d6ee684", 60, Admin)
        )
      }
    }
  }
}
