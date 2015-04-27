package andon.api.models

import scalikejdbc._
import scalikejdbc.config._

object Tables {

  def setup = {
    DBs.setupAll
    DB localTx { implicit s =>
      createArticles
      createUsers
    }
  }

  def createArticles(implicit session: DBSession) = {
    try {
      sql"""
create table articles (
  id bigint auto_increment primary key,
  title varchar(255) not null,
  body varchar(10000) not null,
  create_user_id bigint not null,
  update_user_id bigint,
  created_at timestamp not null,
  updated_at timestamp
);
""".execute.apply()
    } catch {
      case e: Throwable => {
        s"${e} is thrown, but continue"
      }
    }
  }

  def createUsers(implicit session: DBSession) = {
    try {
      sql"""
create table users (
  id bigint auto_increment primary key,
  login varchar(255) not null,
  password varchar(255) not null,
  name varchar(255) not null,
  times int not null,
  icon varchar(1023),
  first_id int,
  second_id int,
  third_id int
);
""".execute.apply()
    } catch {
      case e: Throwable => {
        s"${e} is thrown, but continue"
      }
    }
  }
}
