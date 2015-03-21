create table reviews (
  times int not null,
  grade int not null,
  class int not null,
  user_id bigint not null,
  text varchar(10000) not null,
  created_at timestamp not null,
  updated_at timestamp,
  primary key(times, grade, class, user_id)
);

insert into reviews (times, grade, class, user_id, text, created_at) values (60, 3, 9, 1, '作りました', now());
