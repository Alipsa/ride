/* Works in 
H2, MySQL, PostgreSQL
*/

DROP TABLE if exists MYTABLE;
--if NOT exists (select * from INFORMATION_SCHEMA.TABLES where TABLE_NAME = 'MYTABLE')
CREATE TABLE IF NOT EXISTS MYTABLE (
	id INT NOT NULL, 
	title VARCHAR(50) NOT NULL, 
	author VARCHAR(20) NOT NULL, 
	submission_date DATE, 
	price DECIMAL(20, 2)
);
-- insert some values
/*
insert into MYTABLE values (1, 'Answer to Job', 'C.G. Jung', CURRENT_TIMESTAMP(), 229.50);
insert into MYTABLE values (2, 'Sagan om Ringen', 'J.R.R. Tolkien', '2019-01-20', 149);
insert into MYTABLE values (3, 'Siddharta', 'Herman Hesse', '2019-01-23', 99.90);
*/

insert into MYTABLE select * from (
select 1, 'Answer to Job', 'C.G. Jung', '2019-01-19', 229.50 union
select 2, 'Sagan om Ringen', 'J.R.R. Tolkien', '2019-01-20', 149 union
select 3, 'Siddharta', 'Herman Hesse', '2019-01-23', 99.90 
) x where not exists(select * from MYTABLE);
