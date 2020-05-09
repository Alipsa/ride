# Todo / Roadmap

### Code menu improvements
- Format code, support https://bioconductor.org/developers/how-to/coding-style/
 and Hadley Wickham style guide http://adv-r.had.co.nz/Style.html#Assignment
 
### Tools -> Options menu
Make it possible to customize tab as \t or number of spaces

### Add suggestions when pressing . for SQL
use metadata to determine suggestion 

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

### Enhance code completion
- does not work well for . e.g. as.da+ctrl+space

### Add import dataSet in File meny
Should generate code at current cursor

### Add more syntax highlighting support
NAMESPACE, SAS, SPSS

### Parse and report on issues in R code
- misspelled objects / vars
- syntax errors

### Add current variables to code completion
e.g. when library("se.alipsa:R2JDBC") is loaded it should be possible to do 
dbGe+ctrl+space and get suggestion for dbGetQuery and dbGetException

Also for my own Reference and R6 classes the available methods and fields should be suggested e.g.

student <- setRefClass("student",
fields = list(name = "character", age = "numeric", GPA = "numeric"))

student$+ctrl+space should suggest name, age and GPA

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