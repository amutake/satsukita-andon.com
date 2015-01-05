package andon.utils

import scala.slick.lifted.MappedTypeMapper

case class ClassId(times: OrdInt, grade: Int, classn: Int) {

  // toInt . this = this . toInt = id
  def this(i: Int) = this(OrdInt(i / 1000), (i % 1000) / 100, (i % 100) - 10)

  // ClassId(OrdInt(60), 3, 9).toId => 60000 + 300 + 19 = 60319
  // ClassId(OrdInt(48), 3, -9).toId => 48000 + 300 + 1 = 48301
  def toInt: Int = (times.n * 1000) + (grade * 100) + (classn + 10)

  override def toString = times.toString + grade + "-" + Util.showClassN(classn)

  def toJapanese = times.toString + grade + "年" + Util.showClassN(classn) + "組"
}

object ClassId {

  implicit val classIdTypeMapper = MappedTypeMapper.base[ClassId, Int](
    { c => c.toInt },
    { i => new ClassId(i) }
  )

  implicit val classIdOrdering = new Ordering[ClassId] {
    def compare(a: ClassId, b: ClassId) = {
      val classc = a.classn compare b.classn
      val gradec = if (a.grade == b.grade) {
        classc
      } else {
        a.grade compare b.grade
      }
      val timesc = if (a.times.n == b.times.n) {
        gradec
      } else {
        b.times.n compare a.times.n
      }

      timesc
    }
  }
}
