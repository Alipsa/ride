# Todo / Roadmap

## version 1.1
### Run hamcrest tests
Create a new runbutton when hamcrest tests are detected
 
### enable execution of SQL queries 
- A run query button in the SQL code tab

### create a database browser
- Tree structure based on INFORMATION_SCHEMA
    
### Implement History tab
### implement edit menu
undo and find/replace seems most pertinent

### Code menu improvements
Comment/Uncomment lines + the Ctrl+Shift+C combo
Format code

### Tools -> Options menu
Make it possible to customize tab as \t or number of spaces

## version 1.2
### Implements graphics support (grDevices) for javafx

### add Rmd support

### add Roxygen support

## version 1.3

### Tools -> Generate menu
Create Maven pom (not in Rstudio). Create a Maven pom stub with dependencies for 
Renjin + whatever is needed for all library() commands to work.

### Add import dataSet in File meny
Should generate code at current cursor

### Add more syntax highlighting support
NAMESPACE, SAS, SPSS

## Version 1.4
### Enable git integration
use jGit see 
- https://wiki.eclipse.org/JGit/User_Guide
- https://download.eclipse.org/jgit/site/5.2.0.201812061821-r/apidocs/index.html
- http://www.doublecloud.org/2013/01/how-to-read-git-repository-using-java-apis/
- https://git-scm.com/book/uz/v2/Appendix-B%3A-Embedding-Git-in-your-Applications-JGit
- https://github.com/centic9/jgit-cookbook

## version 1.5        
### Enable offline mode
- To allow explicit setting Aether to offline
- The maven Settings class created by AetherFactory must be publicly exposed,
ideally by AetherPackageLoader  