drop table articles;

create table articles (
  id bigint auto_increment primary key,
  title varchar(255) not null,
  body varchar(10000) not null,
  create_user_id bigint not null,
  update_user_id bigint,
  created_at timestamp not null,
  updated_at timestamp
);

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

insert into users (login, password, name, times) values ('kohotsunin', '', '甲乙人', 60);

insert into articles (title, body, create_user_id, created_at) values ('test title', 'test body', 1, now());
