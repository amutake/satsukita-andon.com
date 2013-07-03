# ClassData and TimesData Schema

# --- !Ups

create table ClassData (
  times                         int not null,
  grade                         int not null,
  classn                        int not null,
  title                         varchar(255) not null,
  prize                         varchar(255)
);

create table TimesData (
  times                         int not null primary key,
  title                         varchar(255) not null
);

# --- !Downs

drop table if exists ClassData;
drop table if exists TimesData;
