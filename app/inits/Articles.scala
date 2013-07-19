package inits

import scala.slick.driver.H2Driver.simple._
import java.util.Date

import andon.utils._

object Articles {

  def initialize = {

    if (models.Articles.all.isEmpty) {
      DB.db.withSession { implicit session: Session =>
        val now = new Date()
        models.Articles.ins.insertAll(
          (1, 1, "InfoTop", """
# 2013年度の行灯行列について

2013年度(64th)の行灯行列は7/5(金)に行われました。現役生および教職員、保護者の皆様、お疲れ様でした。


# 管理人募集

六代目管理人を募集しております。興味のある方は[Contactページ](http://satsukita-andon.com/contact)の連絡先までご連絡ください。

条件 : 行灯が好き ・ 62nd ~ 64th(64thの方は卒業後) ・ 浪人生でない

※Web制作に関する知識は必要ありません。


# 執筆者募集

記事の執筆者を募集しております。興味のある方は[Contactページ](http://satsukita-andon.com/contact)の連絡先までご連絡ください。
""", now, now, InfoTop, "", None, None),
          (1, 1, "About", """
このサイトは札幌北高校の学校祭の行事である行灯行列の記録とその製作を支援するために、初代管理人の遊び人(51st)とその仲間たちによって作られたサイトです。

二代目管理人 仕掛人(53rd)、三代目管理人 御家人(56th)、四代目管理人 案内人(58th)と続き、現在は五代目管理人の甲乙人(60th)がサイトの運営をしております。

[2010年以前のサイト](http://old.satsukita-andon.com)


# 掲載画像について

掲載されている画像には、個人の顔が写っている写真もあります。不都合がある場合にはお知らせください。即刻削除致します。
""", now, now, About, "", None, None),
          (1, 1, "Contact", """
このサイトに関する意見、要望、バグなど、また、甲乙人個人へのお問い合わせは下記の連絡先までご連絡ください。

- メールアドレス: andon.kohotsunin [at] gmail.com
- Twitter: [甲乙人](https://twitter.com/kohotsunin)
- GitHub: [行灯職人への道のGitHubリポジトリ](https://github.com/amkkun/satsukita-andon.com)へのPull RequestまたはIssue登録
""", now, now, Contact, "", None, None)
        )
      }
    }
  }
}
