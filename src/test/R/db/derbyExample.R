library("DBI")
library("se.alipsa:R2JDBC")
library("hamcrest")


drv <- JDBC("org.apache.derby.jdbc.EmbeddedDriver")
con <- dbConnect(drv, url="jdbc:derby:derbyDB;create=true")

tryCatch(dbSendUpdate(con, "DROP TABLE MyTable"), error = function(e) e)

# Creating table
dbSendUpdate(con, paste('CREATE TABLE MyTable (
	"id" INT NOT NULL,
	"title" VARCHAR(50) NOT NULL,
	"author" VARCHAR(20) NOT NULL,
	"submission_date" DATE,
	"insert_date" TIMESTAMP,
	"price" NUMERIC(20, 2)
	)
'))

# add some data
dbSendUpdate(con, paste("
insert into MyTable values 
(1, 'Answer to Job', 'C.G. Jung', CURRENT_DATE, CURRENT_TIMESTAMP, 22),
(2, 'Lord of the Rings', 'J.R.R. Tolkien', '2019-01-20', CURRENT_TIMESTAMP, 14.11),
(3, 'Siddharta', 'Herman Hesse', '2019-01-23', CURRENT_TIMESTAMP, 9.90)
"
))

# some simple tests

books <- dbGetQuery(con, "select * from MyTable")
assertThat(books[books$id == 3, "title"], identicalTo("Siddharta"))

books <- dbGetQuery(con, paste("select * from MyTable"))
assertThat(books[books$submission_date == '2019-01-23', "price"], identicalTo(9.90))

books <- dbGetQuery(con, paste("select * from MyTable"))
assertThat(books[books$price == 14.11, "author"], identicalTo('J.R.R. Tolkien'))

dbDisconnect(con)