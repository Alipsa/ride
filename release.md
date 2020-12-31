# This is version 1.2.3 of Ride.

Ride is a nice IDE for developing and testing R scripts aimed to run in the Renjin ScriptEngine but
it also supports SQL querying and updating as well as simple java (and Groovy) development so that complete Renjin projects and
packages can be developed, built and tested. Ride supports maven build files as well as git.
  
To run Ride you need to have maven and Java 1.8 or higher installed (preferably with bundled javafx). 
If you use open JDK on Linux then you might need to do something like `sudo apt-get install openjfx` depending on your distro.

Ride is started using the start script appropriate for your environment (see ride.sh/ride.cmd for details). 

Since you probably want to run the ScriptEngine with the ClassPathPackageLoader when embedding your R app, 
you can set Ride to use this to resolve packages. You then need to include those dependencies to the maven.pom 
of your project and hence test that you got all dependencies right before attempting to run the R scripts from the 
java application (server). 

## How to install:
Pre requisites:
- Java 8 or higher with java fx

Unzip ride-1.2.3-final-dist.zip to a directory of choice

## How to run:

`> ./ride.sh`

or on windows

`$ .\ride.cmd`

If you need to customize things, I suggest that you add those things in an env.sh/env.cmd file. 
This way you will not need to worry about upgrading to a later version of Ride later. 
The special variable JAVA_OPTS can be used to add system properties (-D key/values) to java. 
Some reasons why you want to do this are
- You do not have javafx in you jdk and need to wire it in
- You have a high DPI display and need to customize (scale) the screen
- You want to add more available memory to Ride than the default

If possible, create a project and use the pom file to manage dependencies. Ride has wizards for
projects (analysis projects using Renjin) and packages (renjin extensions).  
If that does not work, you might need to manually add / override jars in the lib folder but this runs the risk of
making ride unable to start. The only reason I can think of where the latter is needed is if there is
a Ride dependency that you must override. Older versions of Ride tried to include as many commonly used
dependencies as possible to make it simple to write R code without having to think of dependency management
but since Ride now has built in support for Maven (and soon also Gradle) we are moving away from this idea
and will instead remove any dependency that Ride is not using itself, so they can be managed by the build file 
in your project instead.

## JDBC native components
For some jdbc drivers there are OS native files that is required for some connections features to work. 
This is the case for SQL Server where the sqljdbc_auth.dll is needed for integrated security to work.
In those cases just copy the native files to the lib dir, the startup script points out the lib dir as the java.library.path.   


# Version Descriptions

### 1.2.3-SNAPSHOT
- treat rmd like a md file (should be improved in the future to support syntax highlighting for R sections)

### 1.2.2
- Autocomplete: Enhance autocomplete by taking in functions and objects from everything loaded (executed).
  Also fixed the shortcoming of . being regarded as a word boundary (. is just a character in R).
- Run Tests: Fixed bug in testRunner that did not capture errors properly in the summary
- File types: Recognize DESCRIPTION and NAMESPACE and properties as text files even though they are empty
  Recognize gradle files as groovy files (limited gradle support will be in the next version).
- Add a viewer to view html files (similar to RStudio) and viewHtml to view html content (strings)
- Add simple "check for updates" to help menu.
- Change structure for project pom to be more intuitive
- Add project dir to title
- Add ability to view the log file from the help menu
- Style dialogs such the search window, git popup dialogs, and file tree dialogs
- Remove jdbc driver from being included in Ride, so a specific driver version can be used for each project.
  Present a useful exception alert when the driver is missing.
- make TableMetaData more robust (able to handle more databases)  

### 1.2.1
- Add support for Groovy (see [Groovy.md](/Groovy.md) for details).
- Do Renjin initialization in a separate thread to reduce GUI freezes
- Add right click option to copy Viewer tables including headers, include headers if (ctrl + a) + (ctrl + c)
- Size the Exception alert, so it looks decent in windows on jfx 8
- Include meaningful part of SQL exception directly in the Alert so "expand" in not really needed
to understand what went wrong.  
- Search enhancements (only allow one search window, always on top, handle weird scenarios)
- Handle dependency issue a little better (throw and display exception), fallback to init renjin without maven cl
- Add styling to confirmation dialogs (on exit and on not saved)
- bump up versions plugin version and postgres jdbc driver version

### 1.2 Final
- add ability to create a basic pom.xml in "any" directory

### 1.2 Beta 4
- Add support for env customizations (env.sh/env.cmd called from start scripts if it exists)
- Test and make adjustments so it also works in Java 11 (with bundled javafx)
- Open a Browser if the artifact lookup comes back empty
- Bump dependency versions, notably rideutils which now 
provides additional gui interactive abilities (prompt, chooseFile and chooseDir)
- doc improvements, fixes to wait cursor, revert to an older (working) version of jgit

### 1.2 Beta 3
- Create a package browser to easily find the latest version of an artifact
- Removed the maven based execution to make things simpler
- Removed ant based startup and just rely on scripts
- Add windows executable
- Many small fixes e.g.
    - recognize global assignment operators (<<-, ->>)
    - add git "list remotes" context menu
    - adjust height of package Wizard
    - bump dependency versions
    - change central url to use https

### 1.2 Beta 2
- Enhanced connection functionality (auto save, jdbc url wizard)
- If the previous working dir has been removed we no longer move to parent as 
that might take forever to parse, instead we just do not initialize the file tree.
- Add styling to dialogs
- Add target/classes and target/test.classes to classpath to classic classloader (adjustable in options)
- Only insert right side of brackets if we are on the end of the line (feels more intuitive this way).
- Make git integration optional (configurable)

### 1.2 Beta 1
- Add maven build support
- Add a maven classloader that uses the dependencies in the pom
- Add git support
- Preserve indentation on next line
- Create package wizard to give a good starting point for creating packages
- Create project wizard to give a good starting point for creating projects
- Add support for markdown 
- Add support for java files 
- many small improvement, both cosmetic and functional

### 1.1 final
- Removed empty menus (View, plots, Build, Profile) 
- Add drag and drop support
- Connections: Handle urls containing username and password
- Add dark blue theme
- Dependency versions upgrades
- Expand on user manual
- Fixed links in the user manual when selecting "open link in new window"
- Fixed several minor quirks

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
        - Add view top 200 rows option when right clicking a table.
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