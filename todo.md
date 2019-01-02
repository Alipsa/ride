# TODO
## Version 1.0
### Track need to save
mark code tab with * in the title as soon as editing started
unmark as soon as code is saved
tabs that have changed should prompt a save dialog when closing

## version 1.1
### Implement History tab

### add terminal tab to ConsoleComponent
Maybe Beanshell since Jshell is only available for Java 9+
https://github.com/beanshell/beanshell
https://github.com/beanshell/beanshell/wiki

    <dependencies>
       <dependency>
         <groupId>org.apache-extras.beanshell</groupId>
         <artifactId>bsh</artifactId>
         <version>2.0b6</version>
       </dependency>
    </dependencies>
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
To allow explicit setting Aether to offline
The maven Settings class created by AetherFactory must be publicly exposed,
ideally by AetherPackageLoader  