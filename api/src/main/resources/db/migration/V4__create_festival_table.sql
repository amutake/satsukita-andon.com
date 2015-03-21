create table festivals (
  times int primary key,
  theme varchar(255) not null,
  image_url varchar(1023),
);

insert into festivals (times, theme) values (60, '瞬'), (61, '晴'), (62, '蘭'), (63, '雅'), (64, '絢'), (65, '漣');
