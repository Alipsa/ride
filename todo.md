# Todo / Roadmap

## version 1.1
### Run hamcrest tests (done)
Create a new runbutton when hamcrest tests are detected
 
### enable execution of SQL queries  (done)
- A run query button in the SQL code tab

### create a database browser (done)
- Tree structure based on INFORMATION_SCHEMA
    
### implement edit menu (partially done)
undo, find and find/replace seems most pertinent

### Code menu improvements (done)
Comment/Uncomment lines + the Ctrl+Shift+C combo (done)

### Enable offline mode (done)
- build fat zip with all dependencies in the lib folder
- use ant to lauch instead of maven
- start with ClasspathPackageLoader instead of AetherPackageLoader regardless of user pref

### Add suggestions when pressing ctrl+space  (done)
- For R: Use R keywords + all base R methods
- For SQL: use SQL keywords

### indentation (Tabbing) support (done)
- use 2 spaces for tabs
- tab anywhere on the line indents the line
- handle selected text
- shift tab reverses indentation
 
## version 1.2

### Implement History tab

### Code menu improvements
Format code, use Hadley Wickham style guide http://adv-r.had.co.nz/Style.html#Assignment

### Tools -> Options menu
Make it possible to customize tab as \t or number of spaces

### Add suggestions when pressing . for SQL
use metadata to determine suggestion 

### Implements graphics support (grDevices) for javafx

## version 1.3

### Tools -> Generate menu
Create Maven pom (not in Rstudio). Create a Maven pom stub with dependencies for 
Renjin + whatever is needed for all library() commands to work.

### Add import dataSet in File meny
Should generate code at current cursor

### Add more syntax highlighting support
NAMESPACE, SAS, SPSS

### add Rmd support

### add Roxygen support

## Version 1.4
### Enable git integration
use jGit see 
- https://wiki.eclipse.org/JGit/User_Guide
- https://download.eclipse.org/jgit/site/5.2.0.201812061821-r/apidocs/index.html
- http://www.doublecloud.org/2013/01/how-to-read-git-repository-using-java-apis/
- https://git-scm.com/book/uz/v2/Appendix-B%3A-Embedding-Git-in-your-Applications-JGit
- https://github.com/centic9/jgit-cookbook

### Enhanced offline mode
- To allow explicit setting Aether to offline
- The maven Settings class created by AetherFactory must be publicly exposed,
ideally by AetherPackageLoader  