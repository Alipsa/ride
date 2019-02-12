library("DBI")
library("RJDBC")
library("hamcrest")


drv <- JDBC("org.h2.Driver")
con <- dbConnect(drv, url="jdbc:h2:mem:test;DATABASE_TO_UPPER=FALSE")

dbSendUpdate(con, "DROP TABLE IF EXISTS MyTable")
# Creating table
dbSendUpdate(con, paste('CREATE TABLE IF NOT EXISTS MyTable (
	"id" INT NOT NULL,
	"title" VARCHAR(50) NOT NULL,
	"author" VARCHAR(20) NOT NULL,
	"submission_date" DATE,
	"insert_date" DATETIME,
	"price" NUMERIC(20, 2)
	);
'))

# add some data
dbSendUpdate(con, paste("
insert into MyTable values (1, 'Answer to Job', 'C.G. Jung', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP(), 22);
insert into MyTable values (2, 'Lord of the Rings', 'J.R.R. Tolkien', '2019-01-20', CURRENT_TIMESTAMP(), 14.11);
insert into MyTable values (3, 'Siddharta', 'Herman Hesse', '2019-01-23', CURRENT_TIMESTAMP(), 9.90);
"
))

# some simple tests
test.selectStrings <- function() {
    books <- dbGetQuery(con, "select * from MyTable")
    assertThat(books[books$id == 3, "title"], identicalTo("Siddharta"))
}

test.selectNumeric <- function() {
    books <- dbGetQuery(con, paste("select * from MyTable"))
    assertThat(books[books$submission_date == '2019-01-23', "price"], identicalTo(9.90))
}

test.selectOnNumeric <- function() {
    books <- dbGetQuery(con, paste("select * from MyTable"))
    assertThat(books[books$price == 14.11, "author"], identicalTo('J.R.R. Tolkien'))
}