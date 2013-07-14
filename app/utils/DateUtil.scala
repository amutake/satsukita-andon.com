package andon.utils

import java.util.Date
import java.text.SimpleDateFormat

import scala.slick.lifted.MappedTypeMapper

object DateUtil {

  def dateFormat(date: Date) = new SimpleDateFormat("yyyy/MM/dd").format(date)

  implicit val dateTypeMapper = MappedTypeMapper.base[Date, Long](
    { d => d.getTime() },
    { l => new Date(l) }
  )
}
