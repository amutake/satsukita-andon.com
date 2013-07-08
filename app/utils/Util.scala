package andon.utils

import java.util.Date
import java.text.SimpleDateFormat

object Util {

  def showClassN(c: Int) = if (c > 0) c.toString() else "?"

  def dateFormat(date: Date) = new SimpleDateFormat("yyyy/MM/dd").format(date)
}
