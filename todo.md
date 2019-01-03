# Todo / Roadmap

## version 1.1
### Implement History tab
### implement edit menu
undo and find/replace seems most pertinent

### Code menu improvements
Comment/Uncomment lines
Format code

### add Rmd support

### add Roxygen support

### Tools -> Options menu
Make it possible to customize tab as \t or number of spaces

### Tools -> Generate menu
Create Maven pom (not in Rstudio). Create a Maven pom stub with dependencies for 
Renjin + whatever is needed for all library() commands to work.

### add terminal tab to ConsoleComponent
- Maybe Beanshell since Jshell is only available for Java 9+
    - https://github.com/beanshell/beanshell
    - https://github.com/beanshell/beanshell/wiki

````
    <dependencies>
       <dependency>
         <groupId>org.apache-extras.beanshell</groupId>
         <artifactId>bsh</artifactId>
         <version>2.0b6</version>
       </dependency>
    </dependencies>
````    

- An alternative would be Jython 

- ...or some kind of bash
    - either a homebrewed bridge to bash
    - or pick up on https://github.com/crashub/bash    

### Add import dataSet in File meny
Should generate code at current cursor

### Add more syntax highlighting support
NAMESPACE, SAS, SPSS

## Version 1.2
### Enable git integration
use jGit see 
- https://wiki.eclipse.org/JGit/User_Guide
- https://download.eclipse.org/jgit/site/5.2.0.201812061821-r/apidocs/index.html
- http://www.doublecloud.org/2013/01/how-to-read-git-repository-using-java-apis/
- https://git-scm.com/book/uz/v2/Appendix-B%3A-Embedding-Git-in-your-Applications-JGit
- https://github.com/centic9/jgit-cookbook
        
### Enable offline mode
- To allow explicit setting Aether to offline
- The maven Settings class created by AetherFactory must be publicly exposed,
ideally by AetherPackageLoader  