# Todo / Roadmap


### Minor fixes
Search 
    - should remember last search
    - should move to beginning after reaching the end
    - should alert that nothing matched if no results
 
### Git integration
Not all variations of authentication works
[This article](https://www.codeaffine.com/2014/12/09/jgit-authentication/) is old but useful 
to clarify how it works.  

For https when requiring a password the following error occurs:
org.eclipse.jgit.errors.TransportException: https://someuser@server/someproject.git: Authentication is required but no CredentialsProvider has been registered
        at org.eclipse.jgit.transport.TransportHttp.connect(TransportHttp.java:533) ~[org.eclipse.jgit-5.9.0.202009080501-r.jar:5.9.0.202009080501-r]
        at org.eclipse.jgit.transport.TransportHttp.openFetch(TransportHttp.java:361) ~[org.eclipse.jgit-5.9.0.202009080501-r.jar:5.9.0.202009080501-r]
        at org.eclipse.jgit.transport.FetchProcess.executeImp(FetchProcess.java:105) ~[org.eclipse.jgit-5.9.0.202009080501-r.jar:5.9.0.202009080501-r]
        at org.eclipse.jgit.transport.FetchProcess.execute(FetchProcess.java:91) ~[org.eclipse.jgit-5.9.0.202009080501-r.jar:5.9.0.202009080501-r]
        at org.eclipse.jgit.transport.Transport.fetch(Transport.java:1260) ~[org.eclipse.jgit-5.9.0.202009080501-r.jar:5.9.0.202009080501-r]
        at org.eclipse.jgit.api.FetchCommand.call(FetchCommand.java:211) ~[org.eclipse.jgit-5.9.0.202009080501-r.jar:5.9.0.202009080501-r]
        at org.eclipse.jgit.api.PullCommand.call(PullCommand.java:263) ~[org.eclipse.jgit-5.9.0.202009080501-r.jar:5.9.0.202009080501-r]
        at se.alipsa.ride.inout.DynamicContextMenu$1.call(DynamicContextMenu.java:581) ~[ride-1.2.2-SNAPSHOT.jar:?]
        at se.alipsa.ride.inout.DynamicContextMenu$1.call(DynamicContextMenu.java:574) ~[ride-1.2.2-SNAPSHOT.jar:?]  

### Project menu and project support
Add a project menu
Add support for project settings file which will override globals settings e.g.
    - classloading settings
Move the two generators from the code section (package and maven project)
Add support for "classic R project layout"
Add "clone from git repository" option

### Code menu improvements
- Format code, support https://bioconductor.org/developers/how-to/coding-style/
 and Hadley Wickham style guide http://adv-r.had.co.nz/Style.html#Assignment
 
### Tools -> Options menu
Make it possible to customize tab as \t or number of spaces

### Add suggestions when pressing . for SQL
use metadata to determine suggestion 

### Add object variables to code completion
For users own Reference and R6 classes the available methods and fields should be suggested e.g.

student <- setRefClass("student",
fields = list(name = "character", age = "numeric", GPA = "numeric"))

student$+ctrl+space should suggest name, age and GPA

### Implements graphics support (grDevices) for javafx

### Improve search
- Highlight found text
- Restart from beginning when end is reached
- Pop up "Not found" when there is no match
- Remember last search
- Add option for replace (shortcut <ctrl + r> or <ctrl + shift + j>)

### Enhance table browser
- add refresh option to right click menu

### Enhance FileViewer
- move file (maybe drag drop)

### Add import dataSet in File meny
Should generate code at current cursor

### Add more syntax highlighting support
NAMESPACE, SAS, SPSS

### Parse and report on issues in R code
- misspelled objects / vars
- syntax errors

### Enhance packages section
add checkbox and tick off is loaded into session, available packages should be listed from current classpath
(probably only useful for ClasspathPackageLoader, available packages would not relevant for Aether as everything is available)

### add Rmd support

### add Roxygen support

### Enhanced offline mode
- To allow explicit setting Aether to offline
- The maven Settings class created by AetherFactory must be publicly exposed,
ideally by AetherPackageLoader  

### Maybe Further out 
- Support fastR in addition to Renjin, maybe also GNU R.