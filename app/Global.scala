import play.api._
import play.api.db._
import play.api.Play.current
import play.api.mvc.RequestHeader
import play.api.mvc.Result
import play.api.mvc.Results._

import java.util.Date

import scala.slick.driver.H2Driver.simple._
import scala.slick.jdbc.meta.MTable

import models._
import andon.utils._

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    InitialData.createTable
    InitialData.insert()
  }

  override def onBadRequest(request: RequestHeader, error: String) = {
    BadRequest(views.html.errors.badRequest(error))
  }

  override def onHandlerNotFound(request: RequestHeader): Result = {
    NotFound(views.html.errors.notFound(request.path))
  }

  override def onError(request: RequestHeader, throwable: Throwable) = {
    InternalServerError(views.html.errors.error(throwable))
  }
}

object InitialData {

  val db = Database.forDataSource(DB.getDataSource("default"))

  def makeTableMap(implicit session: Session): Map[String, MTable] = {
    val tableList = MTable.getTables.list()
    tableList.map {
      t => (t.name.name, t)
    }.toMap
  }

  def createTable = db.withSession { implicit session: Session =>
    val tableMap = makeTableMap
    if (!tableMap.contains("TIMESDATA")) {
      TimesData.ddl.create
    }
    if (!tableMap.contains("CLASSDATA")) {
      ClassData.ddl.create
    }
    if (!tableMap.contains("TAGS")) {
      Tags.ddl.create
    }
    if (!tableMap.contains("ACCOUNTS")) {
      Accounts.ddl.create
    }
    if (!tableMap.contains("ARTICLES")) {
      Articles.ddl.create
    }
  }

  def insert() = {

    // TODO
    if (Accounts.all.isEmpty) {
      db.withSession { implicit session: Session =>

        Accounts.ins.insertAll(
          ("甲乙人", "kohotsunin", "9d4e1e23bd5b727046a9e3b4b7db57bd8d6ee684", 60, Admin)
        )
      }
    }

    if (Articles.all.isEmpty) {
      db.withSession { implicit session: Session =>
        val now = new Date()
        Articles.ins.insertAll(
          (1, 1, "InfoTop", """
# 2013年度の行灯行列について

2013年度(64th)の行灯行列は7/5(金)に行われました。現役生および教職員、保護者の皆様、お疲れ様でした。


# 管理人募集

六代目管理人を募集しております。興味のある方は[Contactページ](http://satsukita-andon.com/contact)の連絡先までご連絡ください。

条件 : 行灯が好き ・ 62nd ~ 64th(64thの方は卒業後) ・ 浪人生でない

※Web制作に関する知識は必要ありません。


# 執筆者募集

記事の執筆者を募集しております。興味のある方は[Contactページ](http://satsukita-andon.com/contact)の連絡先までご連絡ください。
""", now, now, InfoTop, ""),
          (1, 1, "About", """
このサイトは札幌北高校の学校祭の行事である行灯行列の記録とその製作を支援するために、初代管理人の遊び人(51st)とその仲間たちによって作られたサイトです。

二代目管理人 仕掛人(53rd)、三代目管理人 御家人(56th)、四代目管理人 案内人(58th)と続き、現在は五代目管理人の甲乙人(60th)がサイトの運営をしております。

[2010年以前のサイト](http://old.satsukita-andon.com)


# 掲載画像について

掲載されている画像には、個人の顔が写っている写真もあります。不都合がある場合にはお知らせください。即刻削除致します。
""", now, now, About, ""),
          (1, 1, "Contact", """
このサイトに関する意見、要望、バグなど、また、甲乙人個人へのお問い合わせは下記の連絡先までご連絡ください。

- メールアドレス: andon.kohotsunin [at] gmail.com
- Twitter: [甲乙人](https://twitter.com/kohotsunin)
- GitHub: [行灯職人への道のGitHubリポジトリ](https://github.com/amkkun/satsukita-andon.com)へのPull RequestまたはIssue登録
""", now, now, Contact, "")
        )
      }
    }

    def mkt(times: Int, title: String) = {
      TimesData(OrdInt(times), title)
    }

    if (TimesData.all.isEmpty) {
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
      ).foreach(TimesData.create)
    }

    def mkc(times: Int, grade: Int, classn: Int, title: String, prize: Option[Prize]) = {
      ClassData(ClassId(OrdInt(times), grade, classn), title, prize)
    }

    if (ClassData.all.isEmpty) {
      Seq(
        mkc(64, 1, 1, "獅子奮迅", Some(Gold)),
        mkc(64, 1, 2, "武陵桃源", None),
        mkc(64, 1, 3, "咲翔志", None),
        mkc(64, 1, 4, "煌", None),
        mkc(64, 1, 5, "勢", None),
        mkc(64, 1, 6, "星羅", None),
        mkc(64, 1, 7, "〜想〜", None),
        mkc(64, 1, 8, "閻魔羅闍", Some(Silver)),

        mkc(64, 2, 1, "龍鬼雷伝", None),
        mkc(64, 2, 2, "IXA", None),
        mkc(64, 2, 3, "月下麗人", None),
        mkc(64, 2, 4, "龍喰迦楼羅王", Some(Gold)),
        mkc(64, 2, 5, "嵐猻疾撃牛魔乃王", None),
        mkc(64, 2, 6, "武者双龍共闘図", Some(Silver)),
        mkc(64, 2, 7, "四神", None),
        mkc(64, 2, 8, "衣蛸", None),

        mkc(64, 3, 1, "大翼麒麟像、与三頭大蛇闘", None),
        mkc(64, 3, 2, "弓取源頼政、鵺成敗", None),
        mkc(64, 3, 3, "悪鬼羅刹", Some(Gold)),
        mkc(64, 3, 4, "魚竜爵馬", None),
        mkc(64, 3, 5, "驚魂悸魄", Some(Silver)),
        mkc(64, 3, 6, "怨鬼闘龍伝", None),
        mkc(64, 3, 7, "夢幻ノ花", None),
        mkc(64, 3, 8, "末法末世", Some(Grand)),

        mkc(63, 1, 1, "南北朱武大図", Some(Gold)),
        mkc(63, 1, 2, "天孫降臨", Some(Bronze)),
        mkc(63, 1, 3, "風神雷神", None),
        mkc(63, 1, 4, "桃鬼戦", None),
        mkc(63, 1, 5, "燎原之観音像", None),
        mkc(63, 1, 6, "闘", None),
        mkc(63, 1, 7, "業火絢爛", Some(Silver)),
        mkc(63, 1, 8, "蒼淵蛇鬼", None),

        mkc(63, 2, 1, "安倍晴明伝", None),
        mkc(63, 2, 2, "金色孔雀王", Some(Bronze)),
        mkc(63, 2, 3, "炎槍必殺毘沙門天銀獅子水遁之陣", None),
        mkc(63, 2, 4, "龗鬼討猛虎", None),
        mkc(63, 2, 5, "捲土重来", Some(Silver)),
        mkc(63, 2, 6, "海神〜蛇討ち〜", None),
        mkc(63, 2, 7, "邁進", None),
        mkc(63, 2, 8, "素戔嗚尊等〜八岐大蛇〜", Some(Gold)),

        mkc(63, 3, 1, "龍虎退治", None),
        mkc(63, 3, 2, "撃〜清涼殿ニ鵺ハ哭ク", None),
        mkc(63, 3, 3, "永永無窮闇夜怨念絵図", Some(Bronze)),
        mkc(63, 3, 4, "陰陽師, 鬼封印伝", Some(Grand)),
        mkc(63, 3, 5, "女喰魚討伐", Some(Silver)),
        mkc(63, 3, 6, "不動釼来", Some(Grand)),
        mkc(63, 3, 7, "鳳凰無双", None),
        mkc(63, 3, 8, "怪力乱神", None),

        mkc(62, 1, 1, "パンダ愛", None),
        mkc(62, 1, 2, "闘", None),
        mkc(62, 1, 3, "神風雲強", None),
        mkc(62, 1, 4, "浦島太郎の泡沫", Some(Gold)),
        mkc(62, 1, 5, "華山龍舞", None),
        mkc(62, 1, 6, "矛盾", None),
        mkc(62, 1, 7, "兵", None),
        mkc(62, 1, 8, "極炎の武者", Some(Bronze)),

        mkc(62, 2, 1, "天照大御神", Some(Silver)),
        mkc(62, 2, 2, "大江戸炎上騒闘記", Some(Gold)),
        mkc(62, 2, 3, "豪傑淵辺義博、水龍ヲ討ツ", None),
        mkc(62, 2, 4, "五条大橋ノ怪僧", None),
        mkc(62, 2, 5, "独眼竜之戯", Some(Bronze)),
        mkc(62, 2, 6, "妖狐九尾退治之図", None),
        mkc(62, 2, 7, "蒼焔の舞", None),
        mkc(62, 2, 8, "独眼龍・正宗～咆哮竜退治～", None),

        mkc(62, 3, 1, "三国碧毛玉面九尾ノ襲来", None),
        mkc(62, 3, 2, "妖鬼乱行", None),
        mkc(62, 3, 3, "警醒", Some(Grand)),
        mkc(62, 3, 4, "魍魎妖怪鬼共互入交集夜街夜行～百鬼夜行の大行進～", None),
        mkc(62, 3, 5, "雷神巍巍乎", None),
        mkc(62, 3, 6, "麒麟飃爛図", Some(Bronze)),
        mkc(62, 3, 7, "天火明命放龍虎", Some(Gold)),
        mkc(62, 3, 8, "竜人不動明王ヲ討ツ", Some(Silver)),

        mkc(61, 1, 1, "四神獣", Some(Silver)),
        mkc(61, 1, 2, "鬼退治", None),
        mkc(61, 1, 3, "藤安権三の龍退治", None),
        mkc(61, 1, 4, "中村勘三郎", None),
        mkc(61, 1, 5, "光帝", None),
        mkc(61, 1, 6, "魔奇祇舞夢・彩・天狗", None),
        mkc(61, 1, 7, "連獅子演舞「炎」", Some(Gold)),
        mkc(61, 1, 8, "閻魔大王", Some(Bronze)),

        mkc(61, 2, 1, "春眠", None),
        mkc(61, 2, 2, "益荒男獅子と交手す", Some(Bronze)),
        mkc(61, 2, 3, "水龍乱舞", None),
        mkc(61, 2, 4, "灼飈乱舞～月下赤龍の陣", None),
        mkc(61, 2, 5, "氷帝討炎虎", Some(Gold)),
        mkc(61, 2, 6, "須佐之男命、龍牙に舞う", None),
        mkc(61, 2, 7, "緋爓白虎", Some(Silver)),
        mkc(61, 2, 8, "波瀾万丈", None),

        mkc(61, 3, 1, "宿怨～豪傑と猛虎", Some(Bronze)),
        mkc(61, 3, 2, "暴神須佐之男命、簸川之八岐大蛇ヲ討ツ", None),
        mkc(61, 3, 3, "雅轟龍魔伝", Some(Gold)),
        mkc(61, 3, 4, "義明九尾狐退治之図", Some(Grand)),
        mkc(61, 3, 5, "海上の死闘～死霊討伐～", None),
        mkc(61, 3, 6, "地靁也豪傑譚", Some(Silver)),
        mkc(61, 3, 7, "疾風怒濤～信玄の夢～", None),
        mkc(61, 3, 8, "風神雷神図屏風", None),

        mkc(60, 1, 1, "雷神", None),
        mkc(60, 1, 2, "輪廻転生", Some(Gold)),
        mkc(60, 1, 3, "胡蝶紫之女", None),
        mkc(60, 1, 4, "Peach, Man", None),
        mkc(60, 1, 5, "竜驤虎視", None),
        mkc(60, 1, 6, "滅龍人", Some(Bronze)),
        mkc(60, 1, 7, "氷之介と炎龍の戦", Some(Silver)),
        mkc(60, 1, 8, "月下無双", None),

        mkc(60, 2, 1, "CROSS, THE, STYX", None),
        mkc(60, 2, 2, "鮫人～さめんちゅ～", None),
        mkc(60, 2, 3, "The, Grapes, of, Wrath", None),
        mkc(60, 2, 4, "Triton～聖海の守護神～", None),
        mkc(60, 2, 5, "鬼ヶ島臨海之決闘図", None),
        mkc(60, 2, 6, "怒髪、天を衝く", Some(Gold)),
        mkc(60, 2, 7, "汪洋の麗姫～夜討ち朝駆け～", Some(Silver)),
        mkc(60, 2, 8, "死闘蒼閻王蠍図", Some(Bronze)),

        mkc(60, 3, 1, "終焉～大木斬伐・反撃襲人～", None),
        mkc(60, 3, 2, "蒙鮫洑滄", None),
        mkc(60, 3, 3, "神殲", Some(Silver)),
        mkc(60, 3, 4, "村雨斬火～八犬士之一閃～", Some(Bronze)),
        mkc(60, 3, 5, "風来葴画　黄泉の悷精", Some(Gold)),
        mkc(60, 3, 6, "釁～合成獣咆哮～", None),
        mkc(60, 3, 7, "天海之常～涙誓～", None),
        mkc(60, 3, 8, "狐鳥乱", None),
        mkc(60, 3, 9, "魄焰", Some(Grand)),

        mkc(59, 1, 1, "趙雲単騎駆け～長坂の戦い～", None),
        mkc(59, 1, 2, "地獄之番犬三頭獣之図", Some(Gold)),
        mkc(59, 1, 3, "獣王唐獅子", Some(Silver)),
        mkc(59, 1, 4, "西遊記～勝利は空に有", None),
        mkc(59, 1, 5, "悟空牛魔王之陣", None),
        mkc(59, 1, 6, "魔笛大蛇を呼ぶ", None),
        mkc(59, 1, 7, "毘沙門天", None),
        mkc(59, 1, 8, "八徳奪還", Some(Bronze)),

        mkc(59, 2, 1, "蛇級王者", Some(Gold)),
        mkc(59, 2, 2, "不倶戴天", None),
        mkc(59, 2, 3, "鵺之夜陣", None),
        mkc(59, 2, 4, "海王死闘ニ伏ス", None),
        mkc(59, 2, 5, "麗鹿霊鳥・死守神森", None),
        mkc(59, 2, 6, "百鬼夜航～船上之宴～", None),
        mkc(59, 2, 7, "Κένταυρος～詩われる神曲・断罪の一矢～", Some(Silver)),
        mkc(59, 2, 8, "海神討蛇～簸川死闘～", None),
        mkc(59, 2, 9, "獅子奮迅", Some(Bronze)),

        mkc(59, 3, 1, "桜咲く五条大橋", None),
        mkc(59, 3, 2, "舞伎獅吼", None),
        mkc(59, 3, 3, "蛇炎の恋～刹那の狂い道成寺～", Some(Grand)),
        mkc(59, 3, 4, "疾風迅雷", None),
        mkc(59, 3, 5, "胡蝶乱舞", Some(Silver)),
        mkc(59, 3, 6, "天翔龍煌", Some(Gold)),
        mkc(59, 3, 7, "鬼鬼麒麟", Some(Bronze)),
        mkc(59, 3, 8, "花鳥風月", None),
        mkc(59, 3, 9, "怒髪衝天、玄武を弑ス", None),

        mkc(58, 1, 1, "勧進帳", None),
        mkc(58, 1, 2, "蛟竜、雲雨を得", None),
        mkc(58, 1, 3, "炎々爛漫", None),
        mkc(58, 1, 4, "風神雷神天舞", None),
        mkc(58, 1, 5, "鬼桜丸君臨", Some(Silver)),
        mkc(58, 1, 6, "太鼓乃達人", Some(Bronze)),
        mkc(58, 1, 7, "画竜点睛", None),
        mkc(58, 1, 8, "鳳凰天駆", None),
        mkc(58, 1, 9, "娜鰄琉乃賜", Some(Gold)),

        mkc(58, 2, 1, "徐晃公明九尾ヲ討ツ", None),
        mkc(58, 2, 2, "神剣天叢雲剣", None),
        mkc(58, 2, 3, "～気焔～加藤清正猛虎ヲ伏ス", None),
        mkc(58, 2, 4, "源氏物語・葵", None),
        mkc(58, 2, 5, "陰陽五行清浄悪霊図", None),
        mkc(58, 2, 6, "図南之翼", Some(Silver)),
        mkc(58, 2, 7, "紫焔於龍出", Some(Gold)),
        mkc(58, 2, 8, "関雲長, 黒龍と対峙す", Some(Bronze)),
        mkc(58, 2, 9, "東海青龍王降臨", None),

        mkc(58, 3, 1, "舞双", None),
        mkc(58, 3, 2, "宇治の橋姫", Some(Gold)),
        mkc(58, 3, 3, "百鬼夜行", Some(Bronze)),
        mkc(58, 3, 4, "五音轟轟", None),
        mkc(58, 3, 5, "乾坤一擲", None),
        mkc(58, 3, 6, "罪輪の獄", Some(Grand)),
        mkc(58, 3, 7, "公孫勝龍ヲ喚ブ", Some(Silver)),
        mkc(58, 3, 8, "西遊記―天竺に行きましょう", None),
        mkc(58, 3, 9, "明王焔武", None),

        mkc(57, 1, 1, "天狗乱舞", None),
        mkc(57, 1, 2, "雷神～闇天響奏雷鼓", None),
        mkc(57, 1, 3, "天狗九尾狩", None),
        mkc(57, 1, 4, "奈落者登攀", None),
        mkc(57, 1, 5, "斉天大聖孫悟空", None),
        mkc(57, 1, 6, "阿修羅王", None),
        mkc(57, 1, 7, "九尾火炎伝", Some(Bronze)),
        mkc(57, 1, 8, "煙エン天に漲る", Some(Gold)),
        mkc(57, 1, 9, "山内一豊と白馬", Some(Silver)),

        mkc(57, 2, 1, "風雷飛宴図", Some(Gold)),
        mkc(57, 2, 2, "天空龍神闘武", None),
        mkc(57, 2, 3, "正義の麒麟, 悪党成敗", None),
        mkc(57, 2, 4, "猛虎襲来", None),
        mkc(57, 2, 5, "般若呪詛丑の刻参り", None),
        mkc(57, 2, 6, "秋風楽", None),
        mkc(57, 2, 7, "鬼神、狂瀾怒涛の戯", Some(Bronze)),
        mkc(57, 2, 8, "山岐大蛇, 上倶戴天", Some(Silver)),
        mkc(57, 2, 9, "猛雄虎ヲ討ツ", None),

        mkc(57, 3, 1, "三皇女カ～天地創造", None),
        mkc(57, 3, 2, "猿鬼成敗", None),
        mkc(57, 3, 3, "坂田金時大江山演義草子", Some(Gold)),
        mkc(57, 3, 4, "八犬士、雪辱晴ラス", None),
        mkc(57, 3, 5, "妖蛾艶耀", Some(Bronze)),
        mkc(57, 3, 6, "秋怨紅葉狩", None),
        mkc(57, 3, 7, "天神地祇　杯ヲ交ワス", None),
        mkc(57, 3, 8, "威風", Some(Silver)),
        mkc(57, 3, 9, "伊邪那岐之涙", Some(Grand)),

        mkc(56, 1, 1, "遠山金四郎百花繚乱桜舞", Some(Silver)),
        mkc(56, 1, 2, "スキ×４　一休さん", None),
        mkc(56, 1, 3, "水龍下の儀", None),
        mkc(56, 1, 4, "荒武者", None),
        mkc(56, 1, 5, "真大蛇伝説", None),
        mkc(56, 1, 6, "武蔵坊弁慶", Some(Bronze)),
        mkc(56, 1, 7, "弁慶乱舞", None),
        mkc(56, 1, 8, "義経初陣", None),
        mkc(56, 1, 9, "武勇伝～弐信死闘之図～", Some(Gold)),

        mkc(56, 2, 1, "蛇姫怨讐", None),
        mkc(56, 2, 2, "神成白虎猛威伝", Some(Silver)),
        mkc(56, 2, 3, "勇猛果敢～鵺を討つ", None),
        mkc(56, 2, 4, "須佐之男命大蛇退治", None),
        mkc(56, 2, 5, "日本武尊九頭竜ヲ滅す", None),
        mkc(56, 2, 6, "大伴御幸　五色龍歯ヲ奪る", Some(Gold)),
        mkc(56, 2, 7, "遼来来合肥攻防戦", Some(Bronze)),
        mkc(56, 2, 8, "陰陽師", None),
        mkc(56, 2, 9, "朱雀咆哮", None),

        mkc(56, 3, 1, "炎獅子襲来", None),
        mkc(56, 3, 2, "風神雷神", None),
        mkc(56, 3, 3, "笑門来福", Some(Gold)),
        mkc(56, 3, 4, "洛水の女神", Some(Grand)),
        mkc(56, 3, 5, "弐勇乱舞　川中島", None),
        mkc(56, 3, 6, "行深般若波羅蜜多", Some(Bronze)),
        mkc(56, 3, 7, "瓢箪送り", Some(Silver)),
        mkc(56, 3, 8, "琉球獅子外伝", None),
        mkc(56, 3, 9, "非天強襲", None),

        mkc(55, 1, 1, "鬼神乱舞", None),
        mkc(55, 1, 2, "炎中戦", Some(Bronze)),
        mkc(55, 1, 3, "天狗乱舞", None),
        mkc(55, 1, 4, "孫悟空", None),
        mkc(55, 1, 5, "龍飛鳳舞", None),
        mkc(55, 1, 6, "一休宗純知恵比べ", None),
        mkc(55, 1, 7, "オノ＝妖狐", None),
        mkc(55, 1, 8, "愛\"乱舞\"友", Some(Gold)),
        mkc(55, 1, 9, "不動鬼若丸", Some(Silver)),

        mkc(55, 2, 1, "許楮と典韋", Some(Silver)),
        mkc(55, 2, 2, "獅子奮迅～真田幸村吠えられる～", None),
        mkc(55, 2, 3, "鳳凰", None),
        mkc(55, 2, 4, "南柯太守伝－蟻ノ国－", None),
        mkc(55, 2, 5, "究極法師", None),
        mkc(55, 2, 6, "鬼獣逆乱", None),
        mkc(55, 2, 7, "素棧鳴尊大蛇退治絵巻", Some(Bronze)),
        mkc(55, 2, 8, "豪火赤壁", None),
        mkc(55, 2, 9, "妖狐九尾", Some(Gold)),

        mkc(55, 3, 1, "天舞龍鳳", Some(Silver)),
        mkc(55, 3, 2, "明王討鬼～beat, them!!～", None),
        mkc(55, 3, 3, "巫儀巡礼", Some(Bronze)),
        mkc(55, 3, 4, "鳥革き飛", None),
        mkc(55, 3, 5, "女神類を召して之来たる", None),
        mkc(55, 3, 6, "土蜘蛛死ス", Some(Grand)),
        mkc(55, 3, 7, "三國志演義張飛", Some(Gold)),
        mkc(55, 3, 8, "妖狐", None),
        mkc(55, 3, 9, "波斯王獅子狩之図", None),
        mkc(55, 3, 10, "修羅", None),

        mkc(54, 1, 1, "赤鬼、龍を討つ", None),
        mkc(54, 1, 2, "行灯楽", None),
        mkc(54, 1, 3, "咆虎睨龍", None),
        mkc(54, 1, 4, "異風胴堂", None),
        mkc(54, 1, 5, "魏之ﾎｳ徳～颯爽的馬上英姿", Some(Silver)),
        mkc(54, 1, 6, "決戦！風林火山", Some(Bronze)),
        mkc(54, 1, 7, "乱舞", None),
        mkc(54, 1, 8, "雨の五郎", None),
        mkc(54, 1, 9, "義経、流星が如く", Some(Gold)),

        mkc(54, 2, 1, "開眼流二刀流之巻", Some(Silver)),
        mkc(54, 2, 2, "妖術使児雷也蝦蟇ヲ呼バントス", Some(Bronze)),
        mkc(54, 2, 3, "大蛇―OROCHI―", None),
        mkc(54, 2, 4, "南蛮王　孟獲", None),
        mkc(54, 2, 5, "炎の最終決戦", None),
        mkc(54, 2, 6, "麒麟降臨", None),
        mkc(54, 2, 7, "奥内伝説『貝倉明神と龍』", None),
        mkc(54, 2, 8, "大蛇襲来", None),
        mkc(54, 2, 9, "魚跳龍門", Some(Gold)),
        mkc(54, 2, 10, "夢の終わるトキ～燃え尽きる本能寺～", None),

        mkc(54, 3, 1, "鳥～不朽之妖炎、災禍之化神～", None),
        mkc(54, 3, 2, "芦屋道満陰陽道式神ヲ喰ラフ", None),
        mkc(54, 3, 3, "鸞翔鳳炎", None),
        mkc(54, 3, 4, "海蛇の釜中に遊ぶが如し", None),
        mkc(54, 3, 5, "神謡アイヌラックル―凍える村の聖霊", None),
        mkc(54, 3, 6, "源頼光　土蜘蛛の夜襲", Some(Bronze)),
        mkc(54, 3, 7, "大百足退治", Some(Silver)),
        mkc(54, 3, 8, "孫悟空", None),
        mkc(54, 3, 9, "華炎", Some(Gold)),
        mkc(54, 3, 10, "降魔", Some(Grand)),

        mkc(53, 1, 1, "", None),
        mkc(53, 1, 2, "", Some(Gold)),
        mkc(53, 1, 3, "", None),
        mkc(53, 1, 4, "", None),
        mkc(53, 1, 5, "", None),
        mkc(53, 1, 6, "", None),
        mkc(53, 1, 7, "", None),
        mkc(53, 1, 8, "", None),
        mkc(53, 1, 9, "", None),
        mkc(53, 1, 10, "", None),

        mkc(53, 2, 1, "", None),
        mkc(53, 2, 2, "", None),
        mkc(53, 2, 3, "", None),
        mkc(53, 2, 4, "", None),
        mkc(53, 2, 5, "", None),
        mkc(53, 2, 6, "", None),
        mkc(53, 2, 7, "", None),
        mkc(53, 2, 8, "", None),
        mkc(53, 2, 9, "", None),
        mkc(53, 2, 10, "", None),
        mkc(53, 2, -1, "", Some(Gold)),
        mkc(53, 2, -2, "", None),

        mkc(53, 3, 1, "", None),
        mkc(53, 3, 2, "", None),
        mkc(53, 3, 3, "", None),
        mkc(53, 3, 4, "", None),
        mkc(53, 3, 5, "", Some(Gold)),
        mkc(53, 3, 6, "", None),
        mkc(53, 3, 7, "", Some(Grand)),
        mkc(53, 3, 8, "", None),
        mkc(53, 3, 9, "", Some(Bronze)),
        mkc(53, 3, 10, "", Some(Silver)),

        mkc(52, 1, 1, "", None),
        mkc(52, 1, 2, "", None),
        mkc(52, 1, 3, "", None),
        mkc(52, 1, 4, "", None),
        mkc(52, 1, 5, "", None),
        mkc(52, 1, 6, "", None),
        mkc(52, 1, 7, "", None),
        mkc(52, 1, 8, "", None),
        mkc(52, 1, 9, "", None),
        mkc(52, 1, 10, "", None),

        mkc(52, 2, 1, "", None),
        mkc(52, 2, 2, "", None),
        mkc(52, 2, 3, "", None),
        mkc(52, 2, 4, "", None),
        mkc(52, 2, 5, "", None),
        mkc(52, 2, 6, "", None),
        mkc(52, 2, 7, "", None),
        mkc(52, 2, 8, "", None),
        mkc(52, 2, 9, "", None),
        mkc(52, 2, 10, "", None),
        mkc(52, 2, -1, "", Some(Gold)),

        mkc(52, 3, 1, "", None),
        mkc(52, 3, 2, "", None),
        mkc(52, 3, 3, "", None),
        mkc(52, 3, 4, "", None),
        mkc(52, 3, 5, "", None),
        mkc(52, 3, 6, "", None),
        mkc(52, 3, 7, "", None),
        mkc(52, 3, 8, "", None),
        mkc(52, 3, 9, "", None),
        mkc(52, 3, 10, "", None),
        mkc(52, 3, -1, "", Some(Grand)),
        mkc(52, 3, -2, "", Some(Gold)),
        mkc(52, 3, -3, "", Some(Bronze)),

        mkc(51, 1, 1, "", None),
        mkc(51, 1, 2, "", None),
        mkc(51, 1, 3, "", None),
        mkc(51, 1, 4, "", None),
        mkc(51, 1, 5, "", None),
        mkc(51, 1, 6, "", None),
        mkc(51, 1, 7, "", None),
        mkc(51, 1, 8, "", None),
        mkc(51, 1, 9, "", None),
        mkc(51, 1, 10, "", None),
        mkc(51, 1, -1, "", Some(Gold)),
        mkc(51, 1, -2, "", None),

        mkc(51, 2, 1, "", None),
        mkc(51, 2, 2, "", None),
        mkc(51, 2, 3, "", None),
        mkc(51, 2, 4, "", None),
        mkc(51, 2, 5, "", None),
        mkc(51, 2, 6, "", None),
        mkc(51, 2, 7, "", None),
        mkc(51, 2, 8, "", None),
        mkc(51, 2, 9, "", None),
        mkc(51, 2, 10, "", None),
        mkc(51, 2, -1, "", Some(Gold)),

        mkc(51, 3, 1, "", Some(Grand)),
        mkc(51, 3, 2, "", None),
        mkc(51, 3, 3, "", None),
        mkc(51, 3, 4, "", None),
        mkc(51, 3, 5, "", None),
        mkc(51, 3, 6, "", Some(Gold)),
        mkc(51, 3, 7, "", Some(Bronze)),
        mkc(51, 3, 8, "", None),
        mkc(51, 3, 9, "", None),
        mkc(51, 3, 10, "", Some(Silver)),

        mkc(50, 1, 1, "", None),
        mkc(50, 1, 2, "", None),
        mkc(50, 1, 3, "", None),
        mkc(50, 1, 4, "", None),
        mkc(50, 1, 5, "", None),
        mkc(50, 1, 6, "", None),
        mkc(50, 1, 7, "", None),
        mkc(50, 1, 8, "", None),
        mkc(50, 1, 9, "", None),
        mkc(50, 1, 10, "", None),

        mkc(50, 2, 1, "", None),
        mkc(50, 2, 2, "", None),
        mkc(50, 2, 3, "", None),
        mkc(50, 2, 4, "", Some(Gold)),
        mkc(50, 2, 5, "", None),
        mkc(50, 2, 6, "", None),
        mkc(50, 2, 7, "", None),
        mkc(50, 2, 8, "", None),
        mkc(50, 2, 9, "", None),
        mkc(50, 2, 10, "", None),

        mkc(50, 3, 1, "", Some(Silver)),
        mkc(50, 3, 2, "", None),
        mkc(50, 3, 3, "", None),
        mkc(50, 3, 4, "", None),
        mkc(50, 3, 5, "", None),
        mkc(50, 3, 6, "", None),
        mkc(50, 3, 7, "", None),
        mkc(50, 3, 8, "", None),
        mkc(50, 3, 9, "", Some(Grand)),
        mkc(50, 3, 10, "", None),
        mkc(50, 3, 11, "", None),
        mkc(50, 3, -1, "", Some(Gold)),
        mkc(50, 3, -2, "", Some(Bronze)),
        mkc(50, 3, -3, "", None),

        mkc(49, 1, 1, "", None),
        mkc(49, 1, 2, "", None),
        mkc(49, 1, 3, "", None),
        mkc(49, 1, 4, "", None),
        mkc(49, 1, 5, "", None),
        mkc(49, 1, 6, "", None),
        mkc(49, 1, 7, "", None),
        mkc(49, 1, 8, "", None),
        mkc(49, 1, 9, "", None),
        mkc(49, 1, 10, "", None),

        mkc(49, 2, 1, "", None),
        mkc(49, 2, 2, "", None),
        mkc(49, 2, 3, "", None),
        mkc(49, 2, 4, "", None),
        mkc(49, 2, 5, "", None),
        mkc(49, 2, 6, "", None),
        mkc(49, 2, 7, "", None),
        mkc(49, 2, 8, "", None),
        mkc(49, 2, 9, "", None),
        mkc(49, 2, 10, "", None),

        mkc(49, 3, 1, "", None),
        mkc(49, 3, 2, "", None),
        mkc(49, 3, 3, "", None),
        mkc(49, 3, 4, "", None),
        mkc(49, 3, 5, "", None),
        mkc(49, 3, 6, "", None),
        mkc(49, 3, 7, "", None),
        mkc(49, 3, 8, "", None),
        mkc(49, 3, 9, "", None),
        mkc(49, 3, 10, "", None),
        mkc(49, 3, 11, "", None),
        mkc(49, 3, -1, "", Some(Grand)),
        mkc(49, 3, -2, "", Some(Gold)),
        mkc(49, 3, -3, "", Some(Silver)),
        mkc(49, 3, -4, "", Some(Bronze)),

        mkc(48, 1, 1, "", None),
        mkc(48, 1, 2, "", None),
        mkc(48, 1, 3, "", None),
        mkc(48, 1, 4, "", None),
        mkc(48, 1, 5, "", None),
        mkc(48, 1, 6, "", None),
        mkc(48, 1, 7, "", None),
        mkc(48, 1, 8, "", None),
        mkc(48, 1, 9, "", None),
        mkc(48, 1, 10, "", None),
        mkc(48, 1, 11, "", None),

        mkc(48, 2, 1, "", None),
        mkc(48, 2, 2, "", None),
        mkc(48, 2, 3, "", None),
        mkc(48, 2, 4, "", None),
        mkc(48, 2, 5, "", None),
        mkc(48, 2, 6, "", None),
        mkc(48, 2, 7, "", None),
        mkc(48, 2, 8, "", None),
        mkc(48, 2, 9, "", None),
        mkc(48, 2, 10, "", None),
        mkc(48, 2, 11, "", None),

        mkc(48, 3, 1, "", None),
        mkc(48, 3, 2, "", None),
        mkc(48, 3, 3, "", None),
        mkc(48, 3, 4, "", None),
        mkc(48, 3, 5, "", None),
        mkc(48, 3, 6, "", None),
        mkc(48, 3, 7, "", None),
        mkc(48, 3, 8, "", None),
        mkc(48, 3, 9, "", None),
        mkc(48, 3, 10, "", Some(Grand)),
        mkc(48, 3, -1, "", Some(Gold)),
        mkc(48, 3, -2, "", Some(Silver)),
        mkc(48, 3, -3, "", Some(Bronze)),
        mkc(48, 3, -4, "", None),
        mkc(48, 3, -5, "", None),
        mkc(48, 3, -6, "", None),
        mkc(48, 3, -7, "", None),
        mkc(48, 3, -8, "", None),
        mkc(48, 3, -9, "", None),

        mkc(47, 1, 1, "", None),
        mkc(47, 1, 2, "", None),
        mkc(47, 1, 3, "", None),
        mkc(47, 1, 4, "", None),
        mkc(47, 1, 5, "", None),
        mkc(47, 1, 6, "", None),
        mkc(47, 1, 7, "", Some(Gold)),
        mkc(47, 1, 8, "", None),
        mkc(47, 1, 9, "", None),
        mkc(47, 1, 10, "", None),
        mkc(47, 1, 11, "", None),

        mkc(47, 2, 1, "", Some(Gold)),
        mkc(47, 2, 2, "", None),
        mkc(47, 2, 3, "", None),
        mkc(47, 2, 4, "", None),
        mkc(47, 2, 5, "", None),
        mkc(47, 2, 6, "", None),
        mkc(47, 2, 7, "", None),
        mkc(47, 2, 8, "", None),
        mkc(47, 2, 9, "", None),
        mkc(47, 2, 10, "", None),
        mkc(47, 2, 11, "", None),

        mkc(47, 3, 1, "", Some(Grand)),
        mkc(47, 3, 2, "", Some(Bronze)),
        mkc(47, 3, 3, "", None),
        mkc(47, 3, 4, "", None),
        mkc(47, 3, 5, "", Some(Gold)),
        mkc(47, 3, 6, "", None),
        mkc(47, 3, 7, "", Some(Silver)),
        mkc(47, 3, 8, "", None),
        mkc(47, 3, 9, "", None),
        mkc(47, 3, 10, "", None),
        mkc(47, 3, 11, "", None),

        mkc(46, 1, 1, "", None),
        mkc(46, 1, 2, "", None),
        mkc(46, 1, 3, "", None),
        mkc(46, 1, 4, "", None),
        mkc(46, 1, 5, "", None),
        mkc(46, 1, 6, "", None),
        mkc(46, 1, 7, "", None),
        mkc(46, 1, 8, "", None),
        mkc(46, 1, 9, "", None),
        mkc(46, 1, 10, "", None),
        mkc(46, 1, 11, "", None),

        mkc(46, 2, 1, "", None),
        mkc(46, 2, 2, "", None),
        mkc(46, 2, 3, "", None),
        mkc(46, 2, 4, "", None),
        mkc(46, 2, 5, "", None),
        mkc(46, 2, 6, "", None),
        mkc(46, 2, 7, "", None),
        mkc(46, 2, 8, "", None),
        mkc(46, 2, 9, "", None),
        mkc(46, 2, 10, "", None),
        mkc(46, 2, 11, "", None),

        mkc(46, 3, 1, "", None),
        mkc(46, 3, 2, "", None),
        mkc(46, 3, 3, "", None),
        mkc(46, 3, 4, "", None),
        mkc(46, 3, 5, "", None),
        mkc(46, 3, 6, "", None),
        mkc(46, 3, 7, "", None),
        mkc(46, 3, 8, "", Some(Grand)),
        mkc(46, 3, 9, "", None),
        mkc(46, 3, 10, "", None),
        mkc(46, 3, 11, "", None),
        mkc(46, 3, -1, "", Some(Gold)),
        mkc(46, 3, -2, "", Some(Silver)),

        mkc(45, 1, 1, "", None),
        mkc(45, 1, 2, "", None),
        mkc(45, 1, 3, "", None),
        mkc(45, 1, 4, "", None),
        mkc(45, 1, 5, "", None),
        mkc(45, 1, 6, "", None),
        mkc(45, 1, 7, "", None),
        mkc(45, 1, 8, "", None),
        mkc(45, 1, 9, "", None),
        mkc(45, 1, 10, "", None),

        mkc(45, 2, 1, "", None),
        mkc(45, 2, 2, "", None),
        mkc(45, 2, 3, "", None),
        mkc(45, 2, 4, "", None),
        mkc(45, 2, 5, "", None),
        mkc(45, 2, 6, "", None),
        mkc(45, 2, 7, "", None),
        mkc(45, 2, 8, "", None),
        mkc(45, 2, 9, "", None),
        mkc(45, 2, 10, "", None),

        mkc(45, 3, 1, "", None),
        mkc(45, 3, 2, "", None),
        mkc(45, 3, 3, "", None),
        mkc(45, 3, 4, "", None),
        mkc(45, 3, 5, "", None),
        mkc(45, 3, 6, "", None),
        mkc(45, 3, 7, "", None),
        mkc(45, 3, 8, "", Some(Bronze)),
        mkc(45, 3, 9, "", None),
        mkc(45, 3, 10, "", None),
        mkc(45, 3, -1, "", Some(Grand)),
        mkc(45, 3, -2, "", Some(Gold)),
        mkc(45, 3, -3, "", Some(Silver)),

        mkc(44, 1, 1, "", None),
        mkc(44, 1, 2, "", None),
        mkc(44, 1, 3, "", None),
        mkc(44, 1, 4, "", None),
        mkc(44, 1, 5, "", None),
        mkc(44, 1, 6, "", None),
        mkc(44, 1, 7, "", None),
        mkc(44, 1, 8, "", None),
        mkc(44, 1, 9, "", None),
        mkc(44, 1, 10, "", None),

        mkc(44, 2, 1, "", None),
        mkc(44, 2, 2, "", None),
        mkc(44, 2, 3, "", None),
        mkc(44, 2, 4, "", None),
        mkc(44, 2, 5, "", None),
        mkc(44, 2, 6, "", None),
        mkc(44, 2, 7, "", None),
        mkc(44, 2, 8, "", None),
        mkc(44, 2, 9, "", None),
        mkc(44, 2, 10, "", None),

        mkc(44, 3, 1, "", Some(Gold)),
        mkc(44, 3, 2, "", None),
        mkc(44, 3, 3, "", None),
        mkc(44, 3, 4, "", Some(Bronze)),
        mkc(44, 3, 5, "", None),
        mkc(44, 3, 6, "", None),
        mkc(44, 3, 7, "", Some(Silver)),
        mkc(44, 3, 8, "", None),
        mkc(44, 3, 9, "", None),
        mkc(44, 3, 10, "", Some(Grand)),

        mkc(43, 1, 1, "", None),
        mkc(43, 1, 2, "", None),
        mkc(43, 1, 3, "", None),
        mkc(43, 1, 4, "", None),
        mkc(43, 1, 5, "", None),
        mkc(43, 1, 6, "", None),
        mkc(43, 1, 7, "", None),
        mkc(43, 1, 8, "", None),
        mkc(43, 1, 9, "", None),
        mkc(43, 1, 10, "", None),

        mkc(43, 2, 1, "", None),
        mkc(43, 2, 2, "", None),
        mkc(43, 2, 3, "", None),
        mkc(43, 2, 4, "", None),
        mkc(43, 2, 5, "", None),
        mkc(43, 2, 6, "", None),
        mkc(43, 2, 7, "", None),
        mkc(43, 2, 8, "", None),
        mkc(43, 2, 9, "", None),
        mkc(43, 2, 10, "", None),

        mkc(43, 3, 1, "", None),
        mkc(43, 3, 2, "", Some(Grand)),
        mkc(43, 3, 3, "", None),
        mkc(43, 3, 4, "", None),
        mkc(43, 3, 5, "", None),
        mkc(43, 3, 6, "", None),
        mkc(43, 3, 7, "", None),
        mkc(43, 3, 8, "", None),
        mkc(43, 3, 9, "", Some(Gold)),
        mkc(43, 3, 10, "", None)

      ).foreach(ClassData.create)
    }

    def tag(name: String, cs: Seq[(Int, Int, Int)]) = {
      cs.map { case (t, g, c) =>
        Tag(ClassId(OrdInt(t), g, c), name)
      }
    }

    if (Tags.all.isEmpty) {
      (tag("龍", Seq(
        (63, 2, 7),
        (63, 3, 1),
        (63, 3, 6),
        (63, 3, 7),
        (63, 3, 8),
        (62, 1, 5),
        (62, 2, 1),
        (62, 2, 2),
        (62, 2, 3),
        (62, 2, 5),
        (62, 2, 7),
        (62, 2, 8),
        (62, 3, 7),
        (62, 2, 3),
        (61, 1, 1),
        (61, 1, 3),
        (61, 2, 3),
        (61, 2, 4),
        (61, 2, 6),
        (61, 3, 3),
        (61, 3, 7),
        (61, 3, 3),
        (60, 1, 5),
        (60, 1, 6),
        (60, 2, 4),
        (60, 3, 3),
        (60, 3, 5),
        (60, 3, 6),
        (60, 3, 7),
        (60, 3, 9),
        (59, 3, 6),
        (58, 1, 7),
        (58, 2, 7),
        (58, 2, 8),
        (58, 3, 5),
        (58, 3, 7),
        (58, 3, 9),
        (57, 2, 2),
        (57, 2, 6),
        (57, 3, 8),
        (57, 3, 9),
        (56, 1, 3),
        (56, 2, 5),
        (56, 2, 6),
        (55, 3, 1),
        (55, 3, 7),
        (54, 1, 3),
        (54, 2, 7),
        (53, 3, 1),
        (53, 3, 6),
        (53, 3, 10),
        (51, 3, 1),
        (48, 3, -1)
      )) ++
      tag("虎", Seq(
        (63, 2, 4),
        (63, 3, 1),
        (63, 3, 2),
        (62, 3, 7),
        (61, 1, 1),
        (61, 2, 5),
        (61, 2, 7),
        (61, 3, 1),
        (61, 3, 7),
        (58, 2, 5),
        (57, 2, 4),
        (57, 2, 9),
        (56, 2, 2),
        (56, 2, 7),
        (55, 1, 6),
        (55, 3, 3),
        (54, 1, 3),
        (54, 2, 4),
        (53, 2, 1),
        (53, 2, 2),
        (53, 3, 9),
        (52, 3, 3),
        (51, 3, 2),
        (50, 3, 1)
      ))).foreach(Tags.create)
    }
  }
}
