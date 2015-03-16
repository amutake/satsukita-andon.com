create table articles (
  id bigint auto_increment primary key,
  title varchar(255) not null,
  body varchar(10000) not null,
  user_id bigint not null,
  created_at timestamp not null,
  updated_at timestamp
)
