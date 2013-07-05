package andon.utils._

case class ClassId(times: OrdInt, grade: Int, classn: Int)

object ClassId {

  // ClassId.toId(ClassId(OrdInt(60), 3, 9)) => 60000 + 300 + 19 = 60319
  // ClassId.toId(ClassId(OrdInt(48), 3, -9)) => 48000 + 300 + 1 = 48301

  // toId . fromId = fromId . toId = id

  def toId(c: ClassId): Int = {
    (c.times.n * 1000) + (c.grade * 100) + (classn + 10)
  }

  def fromId(id: Int): ClassId = {
    val times = id / 1000
    val grade = (id % 1000) / 100
    val classn = (id % 100) - 10

    ClassId(OrdInt(times), grade, classn)
  }
}
