import play.api._

import models._

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    InitialData.insert()
  }
}

object InitialData {

  def insert() = {
    Seq(
      ClassData(60, 3, 9, "魄焰", "grand")
    ).foreach(ClassData.create)
  }
}
