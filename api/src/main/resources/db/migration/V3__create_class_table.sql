create table class_data (
  times int not null,
  grade int not null,
  class int not null,
  title varchar(255) not null,
  description varchar(1023),
  top_url varchar(1023),
  primary key(times, grade, class)
);

create table prizes (
  times int not null,
  grade int not null,
  class int not null,
  kind varchar(31) not null,
  primary key(times, grade, class)
);

insert into class_data (times, grade, class, title) values (60, 3, 9, '魄焔');

insert into prizes (times, grade, class, kind) values (60, 3, 9, 'grand');
