CREATE TABLE customer_tmp (
   id INT  NOT NULL,
   name VARCHAR (50),
   PRIMARY KEY (id)
);

CREATE TABLE customer (
   id INT  NOT NULL,
   name VARCHAR (50),
   PRIMARY KEY (id)
);

insert into customer_tmp(id, name) values 
(1, 'a'),
(2, 'a2'),
(3, 'a3'),
(4, 'a4'),
(5, 'a5'),
(6, 'a6'),
(7, 'a7'),
(8, 'a8'),
(9, 'a9'),
(10, 'a10'),
(11, 'a11'),
(12, 'a12'),
(13, 'a13'),
(14, 'a14'),
(15, 'a15'),
(16, 'a16'),
(17, 'a17'),
(18, 'a18'),
(19, 'a19'),
(20, 'a20')
;