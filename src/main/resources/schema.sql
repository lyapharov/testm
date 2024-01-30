create table if not exists booking (
   user_id varchar(200) not null,
   device_id tinyint not null,
   booking_date timestamp,
   primary key (user_id, device_id, booking_date)
);

create table if not exists availability (
   device_id tinyint not null,
   quantity tinyint not null check (quantity >= 0 and quantity <= max_quantity),
   max_quantity tinyint not null check (max_quantity > 0),
   primary key (device_id)
);

insert into availability values(1, 1, 1);
insert into availability values(2, 2, 2);
insert into availability values(3, 1, 1);
insert into availability values(4, 1, 1);
insert into availability values(5, 1, 1);
insert into availability values(6, 1, 1);
insert into availability values(7, 1, 1);
insert into availability values(8, 1, 1);
insert into availability values(9, 1, 1);
