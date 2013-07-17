package inits

import andon.utils._

object TimesData {

  def initialize = {

    def mkt(times: Int, title: String) = {
      models.TimesData(OrdInt(times), title)
    }

    if (models.TimesData.all.isEmpty) {
      Seq(
        mkt(64, "絢"),
        mkt(63, "雅"),
        mkt(62, "蘭"),
        mkt(61, "暒"),
        mkt(60, "瞬"),
        mkt(59, "煌"),
        mkt(58, "爽"),
        mkt(57, "奏"),
        mkt(56, "咲"),
        mkt(55, "美"),
        mkt(54, "彩"),
        mkt(53, "凛"),
        mkt(52, "粋"),
        mkt(51, "宴"),
        mkt(50, "竹"),
        mkt(49, "熱っ!"),
        mkt(48, "煌"),
        mkt(47, "華"),
        mkt(46, "北高風味"),
        mkt(45, "雅"),
        mkt(44, "翔夢"),
        mkt(43, "")
      ).foreach(models.TimesData.create)
    }
  }
}
