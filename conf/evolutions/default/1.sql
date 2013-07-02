# --- !Ups

create table "CLASSDATA" (
  "TIMES"                         int not null,
  "GRADE"                         int not null,
  "CLASSN"                        int not null,
  "TITLE"                         varchar(255) not null,
  "PRIZE"                         varchar(255)
);

create table "TIMESDATA" (
  "TIMES"                         int not null primary key,
  "TITLE"                         varchar(255) not null
);

# --- !Downs

drop table if exists ClassData;
drop table if exists TimesData;
