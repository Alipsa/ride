# Todo / Roadmap

## version 1.2

### Implement History tab (done)

### Code menu improvements
- Format code, support https://bioconductor.org/developers/how-to/coding-style/
 and Hadley Wickham style guide http://adv-r.had.co.nz/Style.html#Assignment
 
- Create package wizard to give a good starting point for creating packages (DONE)
### Add support for creating extensions (packages) (Done)
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

### enable users to use a pom file for defining the classpath to run
Currently to be able to run R code depending on other projects AetherpackageLoader often fails
and classpath package loader requires you to alter the ride pom.xml 

since we have maven support for building, it would make sense to provide an
option to use the classpath which is the result of the dependencies described in the pom
See se.alipsa.ride.utils.MavenUtils for a start

See here https://www.hascode.com/2017/09/downloading-maven-artifacts-from-a-pom-file-programmatically-with-eclipse-aether/ for some ideas...
Also https://mitre.github.io/mvndeps/ is doing basically this
https://github.com/nanosai/modrun classloading magic with maven
https://github.com/mguymon/naether dependency resolver
https://github.com/diet4j/ reads maven and exposes classloaders 

## Version 1.4
### Enable git integration (Done)
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