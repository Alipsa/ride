print("Loading libraries")
library("DBI")
library("RJDBC")

#import(org.renjin.primitives.packaging.FqPackageName)
#package <- packageLoader$load(FqPackageName$new("org.postgresql","postgresql"))
#print(package$isPresent())
#print(package$get())
#library("org.postgresql:postgresql")
# jdbc:h2:~/test
print("Connect to db")
drv <- JDBC("org.h2.Driver") 
con <- dbConnect(drv, url="jdbc:h2:mem:test") 
#print(dbReadTable(con, "projectinfo")) 

printTables <- function() {
	print("Tables:")
	print("-------")
	print(dbListTables(con))
}

printColumns <- function(tableName) {
	print(paste("tableName:", tableName, "columns:"))
	print("------------------------------")
	print(dbListFields(con, tableName))
}

closeConnections <- function() {
    var <- as.list(.GlobalEnv)
    var_names <- names(var)
    for (i in seq_along(var_names)){
	varName <- var_names[i]
        if (class(var[[varName]]) == "JDBCConnection"){
	    print(paste("closing connection", varName))
            dbDisconnect(var[[varName]])
	    rm(list = as.character(substitute(varName)), envir = .GlobalEnv)
        } else if (class(var[[varName]]) == "JDBCDriver"){
	    print(paste("removing driver", varName))
	    rm(list = as.character(substitute(varName)), envir = .GlobalEnv)
	}
    }
}

print("Creating table")
dbSendUpdate(con, paste("CREATE TABLE IF NOT EXISTS MyTable (
	id INT NOT NULL, 
	title VARCHAR(50) NOT NULL, 
	author VARCHAR(20) NOT NULL, 
	submission_date DATE, 
	price DECIMAL(20, 2)
	);
"))

printTables()
printColumns("MyTable")

dbSendUpdate(con, paste("
insert into MyTable values (1, 'Answer to Job', 'C.G. Jung', CURRENT_TIMESTAMP(), 229.50);
insert into MyTable values (2, 'Sagan om Ringen', 'J.R.R. Tolkien', '2019-01-20', 149);
insert into MyTable values (3, 'Siddharta', 'Herman Hesse', '2019-01-23', 99.90);
"
))

books <- dbGetQuery(con, paste("select * from MyTable"));

print("Display books table")
inout$View(books)

print(books)

closeConnections()
