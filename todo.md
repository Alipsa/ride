# Todo / Roadmap

## version 1.2

### Implement History tab (done)

### Code menu improvements
- Format code, support https://bioconductor.org/developers/how-to/coding-style/
 and Hadley Wickham style guide http://adv-r.had.co.nz/Style.html#Assignment
 
- Create package wizard to give a good starting point for creating packages (DONE)
### Add support for creating extensions (packages)
- create dir layout
- create pom
- run using maven-embedder: http://maven.apache.org/ref/3-LATEST/maven-embedder/summary.html


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

### Further out (post 1.4)
- Support fastR in addition to Renjin, maybe also GNU R.