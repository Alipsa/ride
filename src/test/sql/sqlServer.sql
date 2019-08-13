
DROP TABLE IF EXISTS ##creditInformation;
create table #creditInformation (
  id int primary key,
  dateExtracted date,
  message varchar(20)
);

insert into #creditInformation values (1, '2019-01-20', 'first row');
insert into #creditInformation values (2, '2019-02-03', 'second row');
declare @extractDate date = (select max(dateExtracted) from #creditInformation);
declare @ciTable table (ciId numeric(19,0));

insert into @ciTable 
select id as ciId 
from #creditInformation 
where dateExtracted = @extractDate;

select * into testTable from @ciTable;

select * from testTable
