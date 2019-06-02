This is version 1.1 beta of Ride.

At this point Ride is usable and provides a nice IDE for developing and testing R scripts aimed to run in the Renjin ScriptEngine.
It also support SQL querying and updating.
  
To run Ride you need to have maven and Java 1.8 or later installed. 
If you use open JDK on Linux then you might need to do something like `sudo apt-get install openjfx` depending on your distro.

Ride is started using maven (see ride.sh/ride.cmd for details). 
This is because renjin is not included in the fat jar and needs to be wired in to the classpath upon startup. 
You can specify a different renjin version in the pom if you like but note that it requires renjin version 0.9.2716 or later to work. 

Since you probably want to run the ScriptEngine with the ClassPathPackageLoader when embedding your R app, 
you can set Ride to use this to resolve packages. You then need to include those dependencies to the maven.pom and hence
test that you got all dependencies right before attempting to run the R scripts from the java application (server). 

## How to install:
Unzip ride-1.1-beta-dist.zip to a directory of choice

## How to run:

`> ./ride.sh`

or on windows

`$ .\ride.cmd`

## If you need to run ride offline 

Unzip ride-fat-1.1-beta-dist.zip to a directory of choice

The `ride-offline.sh` / `ride-offline.cmd` starts ride with the Classpath package loader and includes all jars
in the lib folder. You need to manually add jars to the liv folder if you need a package or jdbc driver that is 
not included per default. Ant is used to run it (as opposed to maven for online use) so edit the ride.xml if you 
need to tweak it. 

## JDBC native components
For some jdbc drivers there are OS native files that is required for some connections features to work. 
This is the case for SQL Server where the sqljdbc_auth.dll is needed for integrated security to work.
In those cases just copy the native files to the lib dir, the startup script points out the lib dir as the java.library.path.   


# Version Descriptions

### 1.1
- Fixed links in the user manual when selecting "open link in new window"
- Removed empty menus (View, plots, Build, Profile) 

### 1.1 beta 2
- Theme support
    - There is now a dark theme in addition to the default theme (now called bright theme). Change theme in global options
    
- Add dependency on R2JDBC (forked from rjdbc)
    - Since there are several bugfixes to RJDBC (sql server datatypes, postgres datatypes, proper handling of datetime etc.) 
    that i submitted but have yet to be accepted into upstream i decided to bundle a forked version with ride. 
    If you want the old ones just edit the version in the pom file. 
    
- Improved hamcrest testing
    - now properly prints console output while executing tests
    - add elapsed test time at the end
    
- more copy support
    - select and copy content from View tab
    - select file/dir name from file tree   
    
- Improved connections tab
    - add separate username and password fields as putting username and password in the url does not always work for all databases
    - Add "view databases" right click menu option
    - Add "view R code" i.e. generate R code to set up the connection
    
- All code editors now have a button bar
    - this means a save button is always present
        
- Quite few minor bug fixes
    - Since Ride is now good enough for me to use as my primary data analysis tool at work, I use it for several hours daily. 
    Hence there were quite a few minor quirks that I could spot and fix.      
    
- User manual
    - Ride short cuts
    - Ride interaction
    - examples     
           
### 1.1 beta
- Create a new runbutton when hamcrest tests are detected
    - Will run hamcrest tests similar to the renjin-hamcrest-maven-plugin i.e. producing nice output, error messages and
    summary 
    
- Enable execution of SQL queries
    - Support for select and update queries in the SQL code tab 
    - multi row selects of query results are supported for easy copy paste to a spreadsheet app or similar.    
    
- Create a database browser
  - Tree structure based on INFORMATION_SCHEMA activated when right clicking on the connection and choosing view connection. 
  - ctrl+c will copy the name of the table or column   

- Edit menu
  - Now supports simple, one directional, find
  
- Code menu improvements
    - Comment/Uncomment lines + the Ctrl+Shift+C combo, supported for R and SQL files
    
- Enable offline mode
    - build fat zip with all dependencies in the lib folder
    - use ant to launch instead of maven
    - start with ClasspathPackageLoader instead of AetherPackageLoader regardless of user pref    

- Add suggestions when pressing ctrl+space (a.k.a autocomplete)
  - For R: base on R keywords + all base R methods (done)
  - For SQL: use SQL keywords
  
- indentation (Tabbing) support
    - use 2 spaces for tabs
    - shift tab reverses indentation  
    - tab anywhere on the line indents the line
    - handles selected text to indent/unindent whole area
    
### 1.0 Final
- Run scriptEngine in a separate thread but still make it possible to interact with the gui
    - Enables console output as the R script is executed rather than outputting content at the end as before.
- Syntax highlighting for SQL files as well
- Charset detection of files so at least UTF16LE files (and of course UTF 8 and ISO-8859-x) looks nice in the editor
- Opening unknown files in default associated program
- Various refactoring and minor bugfixes     

### 1.0 beta 4
- Add support for text, xml and java files
- Add context menu to file tree (add and remove dir/file)
- Track if files are modified and prompts to save when closing if unsaved
    - mark code tab with * in the title as soon as editing started
    - unmark as soon as code is saved
    - tabs that have changed should prompt a save dialog when closing
- Add "save as" menu option
- Add "new -> text file" menu option
 
### 1.0 beta3
- Add support for running on Windows.
- Add support for switching package loaders
- Various minor improvements.

### 1.0 beta2
- Will now honor ctrl + enter to execute current row or selection

- Run button will now execute selected text (if any is selected) or execute the whole script.

- Fixed setting working dir properly when changing dirs so that scripts can reference files relatively.

- Tested with Renjin 0.9.2717

### 1.0 beta
- Initial release