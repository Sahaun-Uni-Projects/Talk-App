drop table conversation_viewer
drop table messages_
drop table channels
drop table friendships
drop table users

create table users (
	username varchar(255) primary key,
	name varchar(255),
	pass varchar(255),
	image_name varchar(255),
	birthday varchar(255),
);

create table friendships (
	user1 varchar(255) foreign key references users(username),
	user2 varchar(255) foreign key references users(username),
	date_ varchar(255),
	constraint pk_friendships primary key(user1, user2)
);

create table channels (
	id int identity(0,1) primary key,
	user1 varchar(255) foreign key references users(username),
	user2 varchar(255) foreign key references users(username),
	date_ varchar(255)
);

create table messages_ (
	id int identity(0,1) primary key,
	username varchar(255) foreign key references users(username),
	channel int foreign key references channels(id),
	msg text,
	date_ varchar(255)
)

create table conversation_viewer (
	username varchar(255) foreign key references users(username),
	channel int foreign key references channels(id),
	date_ varchar(255)
	constraint pk_conv_viewer primary key(username, channel)
)

insert into users values('alice', 'Alice Alice', 'alice', 'person_red.png', '2022-01-01 00:00:00.0')
insert into users values('bob', 'Bob Bob', 'bob', 'person_blue.png', '2020-01-01 00:00:00.0')
insert into users values('chloe', 'Chloe Chloe', 'chloe', 'person_green.png', '2019-01-01 00:00:00.0')
insert into users values('dan', 'Dan Dan', 'dan', 'person_yellow.png', '2018-01-01 00:00:00.0')

insert into friendships values('alice', 'bob', '2022-01-01 00:00:00.0')
insert into friendships values('alice', 'chloe', '2020-11-03 00:00:00.0')
insert into friendships values('alice', 'dan', '2019-10-02 00:00:00.0')

insert into channels values('alice', 'bob', '2022-01-01 00:00:00.0')
insert into channels values('alice', 'chloe', '2022-01-01 00:00:00.0')
insert into channels values('alice', 'dan', '2022-01-01 00:00:00.0')

insert into messages_ values('alice', 0, 'Lorem ipsum dolor sit amet, consectetur adipiscing elit.
', '2022-01-01 00:00:00.0')
insert into messages_ values('bob', 0, 'Mauris sodales turpis ullamcorper mauris accumsan, sit amet sollicitudin lacus tristique.
', '2022-01-02 01:00:00.0')
insert into messages_ values('alice', 0, 'In non sapien non augue maximus egestas id vel enim.
', '2022-01-01 01:02:00.0')

insert into messages_ values('alice', 1, 'Sed laoreet nisl vitae lacus scelerisque rhoncus.
', '2022-02-02 12:03:00.0')
insert into messages_ values('chloe', 1, 'Maecenas ut quam non odio fermentum elementum.
', '2022-02-02 12:03:00.0')
insert into messages_ values('alice', 1, 'Ut tempus libero quis leo gravida malesuada.
', '2022-02-02 12:04:00.0')
insert into messages_ values('chloe', 1, 'Curabitur ut elit quis purus rhoncus lacinia at a lectus.
', '2022-01-01 12:12:00.0')

insert into messages_ values('alice', 2, 'Sed convallis enim vitae tortor consequat, in ultricies libero porta.
', '2010-09-12 05:05:00.0')
insert into messages_ values('alice', 2, 'Donec pretium mi nec tortor malesuada tempus.
', '2022-01-01 05:06:00.0')

insert into conversation_viewer values('alice', 0, '2019-01-01 00:00:00.0')
insert into conversation_viewer values('alice', 1, '2019-01-01 00:00:00.0')
insert into conversation_viewer values('alice', 2, '2019-01-01 00:00:00.0')
insert into conversation_viewer values('bob', 0, '2019-01-01 00:00:00.0')

select * from users
select * from friendships
select * from channels
select * from messages_
select * from conversation_viewer
