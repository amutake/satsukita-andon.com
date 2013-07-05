package andon.utils

case class ClassId(times: OrdInt, grade: Int, classn: Int) {

  // ClassId.toId(ClassId(OrdInt(60), 3, 9)) => 60000 + 300 + 19 = 60319
  // ClassId.toId(ClassId(OrdInt(48), 3, -9)) => 48000 + 300 + 1 = 48301
  def toId: Int = (times.n * 1000) + (grade * 100) + (classn + 10)
}

object ClassId {

  // toId . fromId = fromId . toId = id
  def fromId(id: Int): ClassId = {
    val times = id / 1000
    val grade = (id % 1000) / 100
    val classn = (id % 100) - 10

    ClassId(OrdInt(times), grade, classn)
  }
}
