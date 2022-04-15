# This is version 1.2.8-jdk11 of Ride.

Ride is a nice IDE for developing and testing R scripts aimed to run in the Renjin ScriptEngine, but
it also supports SQL querying and updating as well as simple java (and Groovy) development so that complete Renjin projects and
packages can be developed, built and tested. Ride supports maven build files as well as git.
  
To run Ride you need Java 11 or higher installed and preferably also maven. 

Ride is started using the start script appropriate for your environment (see ride.sh/ride.cmd for details). 

Since you probably want to run the ScriptEngine with the ClassPathPackageLoader when embedding your R app, 
you can set Ride to use this to resolve packages. You then need to include those dependencies to the maven pom file 
of your project and hence test that you got all dependencies right before attempting to run the R scripts from the 
java application (server). 

## How to install:
Pre requisites:
- Java JDK 11 or higher
Optional but recommended:
- Maven 3.3.9 or higher with either MAVEN_HOME set or mvn available in your path

Unzip ride-1.2.8-jdk11-dist.zip to a directory of choice, 
note that there is no directory "inside" the zip so create the destination
directory and unzip into that directory.

If java is not in your path and JAVA_HOME is not set you can create a file called env.cmd on windows and
env.sh on Linux and set those there e.g.
in env.cmd:
```shell
SET JAVA_HOME=C:\Programs\jdk-11.0.14
SET PATH=%JAVA_HOME%\bin;$PATH
```
in env.sh
```shell
export JAVA_HOME=/usr/local/jdk-11.0.14
export PATH=$JAVA_HOME/bin:$PATH
```

Windows: Create a shortcut to Ride on your desktop 
Right-click ride.cmd -> Send To -> Desktop (create shortcut)
Right-click the shortcut on your desktop and choose properties, change the icon to [ride installation dir]\ride-icon.ico

Linux: Create a launcher to Ride on your desktop
Run the `createLauncher.sh` script


## How to run:
Doubleclick your desktop icon if you created one or:

`> ./ride.sh`

or on Windows

`$ .\ride.cmd`

If you need to customize things, I suggest that you add those things in an env.sh/env.cmd file. 
This way you will not need to worry about upgrading to a later version of Ride later. 
The special variable JAVA_OPTS can be used to add system properties (-D key/values) to java. 
Some reasons why you want to do this are
- You do not have java in your path or JAVA_HOME set  
- You have a high DPI display and need to customize (scale) the screen 
- You want to add more available memory to Ride than the default

If possible, create a project and use the pom file to manage dependencies. Ride has wizards for
projects (analysis projects using Renjin) and packages (renjin extensions).  
If that does not work, you might need to manually add / override jars in the lib folder but this runs the risk of
making ride unable to start. The only reason I can think of where the latter is needed is if there is
a Ride dependency that you must override. Older versions of Ride tried to include as many commonly used
dependencies as possible to make it simple to write R code without having to think of dependency management
but since Ride now has built in support for Maven we are moving away from this idea
and will instead remove any dependency that Ride is not using itself, so they can be managed by the build file 
in your project instead.

## JDBC native components
For some jdbc drivers there are OS native files that is required for some connections features to work. 
This is the case for SQL Server where the sqljdbc_auth.dll is needed for integrated security to work.
In those cases just copy the native files to the lib dir, the startup script points out the lib dir as the java.library.path.   


# Version Descriptions

### 1.2.9
- Fix bug in JDBC url wizard: SQl server uses ; as delimiter, not the usual url standard (?, &)
- upgrade to groovy to use the org.apache versions (4.0.1)
- add version info in about dialog.
- Fix bug in url wizard for file based databases by converting `\` in windows paths to `/`
- Prompt to add jdbc dependency to pom if missing (and add to pom if yes)
- enable configurable run of autorun.R script in Ride install dir and/or project dir

### 1.2.8, 2022-04-01
- Change how we update connection combo in sql tabs when adding a connection, select if only one is available
- Add right click option to save an image in the plots tab to a file.
- Switch to java 11 as the main stream, merge java 11 branch into master. 1.2.8 will no longer support Java 8
- upgrade dependencies for jgit, richtextfx, spotbugs, classgraph, groovy, jackson, log4j
- Use openjdk standalone Nashorn instead of the deprecated, built-in, one.
- Export to csv now remembers last location and preselects that when exporting again, also the suggested
  file name is cleaned up a bit (* and spaces removed).

### 1.2.7, 2022-02-19, Last version supporting Java 8
- Add ride logo as icon on all dialogs (in Linux Mint, no icon on a dialog changes the logo of the main application)
- Make it possible to run R code parts in a mdr document separately (evaluated as standard R, although with the r2md library loaded)
- Change PackagesTab to contain a TableView of available packages with additional info and the currently loaded ones checked
- Make "loaded" in Packages tab editable and load or unload a package based on the selection.
- Upgrade rideutils to support View of table directly
- SQL: Enable execution of selected content (or current row) through ctrl+enter (same as for R code)
  - Add alert if a connection is not selected
- add "new" and "delete" buttons to connections, improve navigation
- Add View and transpose functions to javascript code
- Use Groovy script engine instead of GroovyShell for session support

#### JDK 11 Specific changes
- Using Renjin j11-beta1 branch (will show up as Renjin 3.5-dev)
- Take advantage of some Java 11 syntactic improvements such as string.isBlank() instead of string.trim().isEmpty().
- Use se.alipsa:maven-utils to interact with Maven instead of internal implementation
- Upgrade jgit to the latest 6.x version that requires java 11 or higher
- Use javafx 17 and remove need to install javafx with the jvm

### 1.2.6, 2021-12-26
- Fixed maven support to be able to parse bom dependencies and poms with a parent.
- improve PDF export of mdr files
- add restart groovy session button, fix prompt after run
- add checkbox to restart session after run if selected.
- upgrade tika to version 2.2.1
- fix bug when initializing nashorn on jdk8

### 1.2.5, 2021-12-13
- Bugfix: downgrade jaxb runtime so that save of munin files works
- Bump mdr2html and htmlcreator, log4j, jsqlparser, spotbugs annotations versions
- Add right click option to export a table view to a csv. 
- bump r2jdbc version to support CHARACTER VARYING data type
- add export MDR to html and PDF

### 1.2.4, 2021-11-28
- Update dependency versions in pom and templates
- Add git clone project menu option
- add datatype tooltip for each column in View
- Fix bug where "view 200 rows" did not add data types properly and hence failed to display
- Improve "console" output by removing the extra line feeds for each line that made things like 
  str(), and writeLines() to look ugly
- Align bootstrap version with munin 1.1.5
- Add support for javascript (using Nashorn with limited es6 support enabled)
  - js tab to execute javascript
  - View support for two-dimensional arrays
- Change to a more robust way to viewing svg files using WebView instead of relying on batik to convert the svg image 
(some features of svg are not supported). Remove dependency on javafxsvg as a result 
- update environment after initializing the renjin script engine (make all methods available for code completion from all standard packages)

### 1.2.3, 2021-04-23
- Treat rmd like a md file
- Support mdr files  
  - View button that renders the mdr as html and shows it in the viewer.
  - should be improved in the future to support syntax highlighting for R sections
  - add highlight js styling for mdr files.
- Move the inout interface to Rideutils, add and implement viewer methods to maintain similarities with RStudio
- Remove Table and RdataTransformer and use the ones in renjin-client-data-utils instead. 
- Format project wizard dialog
- Add base R functions to syntax highlighting and autocomplete, 
- Use the same color for built-in functions as for keywords
- Add right click navigation menu for html views.
- Allow partial view of unmanaged munin reports
- View unmanaged report with bootstrap
- Add view source context menu for html views
- Switch to Tika parser for better file encoding detection.
- Removed the unfinished gradle support, supporting maven is enough.
- Upgrade r2jdbc version for improved mysql support
- Fix bug where headers was from the latest query when copying.
- bump dependency versions
- add title (basename of url) if not given when viewing an url and show the url as a tooltip on the tab title
- make git "status all" prettier

### 1.2.2, 2020-12-23
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

### 1.2.1, 2020-08-16
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

### 1.2 Final, 2020-08-05
- add ability to create a basic pom.xml in "any" directory

### 1.2 Beta 4, Jul 25, 2020
- Add support for env customizations (env.sh/env.cmd called from start scripts if it exists)
- Test and make adjustments, so it also works in Java 11 (with bundled javafx)
- Open a Browser if the artifact lookup comes back empty
- Bump dependency versions, notably rideutils which now 
provides additional gui interactive abilities (prompt, chooseFile and chooseDir)
- doc improvements, fixes to wait cursor, revert to an older (working) version of jgit

### 1.2 Beta 3, Jun 14, 2020
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

### 1.2 Beta 2, May 01, 2020
- Enhanced connection functionality (auto save, jdbc url wizard)
- If the previous working dir has been removed we no longer move to parent as 
that might take forever to parse, instead we just do not initialize the file tree.
- Add styling to dialogs
- Add target/classes and target/test.classes to classpath to classic classloader (adjustable in options)
- Only insert right side of brackets if we are on the end of the line (feels more intuitive this way).
- Make git integration optional (configurable)

### 1.2 Beta 1, Dec 14, 2019
- Add maven build support
- Add a maven classloader that uses the dependencies in the pom
- Add git support
- Preserve indentation on next line
- Create package wizard to give a good starting point for creating packages
- Create project wizard to give a good starting point for creating projects
- Add support for markdown 
- Add support for java files 
- many small improvement, both cosmetic and functional

### 1.1 final, Jul 14, 2019
- Removed empty menus (View, plots, Build, Profile) 
- Add drag and drop support
- Connections: Handle urls containing username and password
- Add dark blue theme
- Dependency versions upgrades
- Expand on user manual
- Fixed links in the user manual when selecting "open link in new window"
- Fixed several minor quirks

### 1.1 beta 2, May 15, 2019
- Theme support
    - There is now a dark theme in addition to the default theme (now called bright theme). Change theme in global options
    
- Add dependency on R2JDBC (forked from rjdbc)
    - Since there are several bugfixes to RJDBC (sql server datatypes, postgres datatypes, proper handling of datetime etc.) 
    that I submitted but have yet to be accepted into upstream I decided to bundle a forked version with ride. 
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
        - Add view top 200 rows option when right-clicking a table.
    - Add "view R code" i.e. generate R code to set up the connection
    
- All code editors now have a button bar
    - this means a save button is always present
        
- Quite few minor bug fixes
    - Since Ride is now good enough for me to use as my primary data analysis tool at work, I use it for several hours daily. 
    Hence, there were quite a few minor quirks that I could spot and fix.      
    
- User manual
    - Ride shortcuts
    - Ride interaction
    - examples     
           
### 1.1 beta, Feb 20, 2019
- Create a new runbutton when hamcrest tests are detected
    - Will run hamcrest tests similar to the renjin-hamcrest-maven-plugin i.e. producing nice output, error messages and
    summary 
    
- Enable execution of SQL queries
    - Support for select and update queries in the SQL code tab 
    - multi row selects of query results are supported for easy copy and paste to a spreadsheet app or similar.    
    
- Create a database browser
  - Tree structure based on INFORMATION_SCHEMA activated when right-clicking on the connection and choosing view connection. 
  - ctrl+c will copy the name of the table or column   

- Edit menu
  - Now supports simple, one directional, find
  
- Code menu improvements
    - Comment/Uncomment lines + the Ctrl+Shift+C combo, supported for R and SQL files
    
- Enable offline mode
    - build fat zip with all dependencies in the lib folder
    - use ant to launch instead of maven
    - start with ClasspathPackageLoader instead of AetherPackageLoader regardless of user pref    

- Add suggestions when pressing ctrl+space (a.k.a. autocomplete)
  - For R: base on R keywords + all base R methods (done)
  - For SQL: use SQL keywords
  
- indentation (Tabbing) support
    - use 2 spaces for tabs
    - shift tab reverses indentation  
    - tab anywhere on the line indents the line
    - handles selected text to indent/unindent whole area
    
### 1.0 Final, Jan 12, 2019
- Run scriptEngine in a separate thread but still make it possible to interact with the gui
    - Enables console output as the R script is executed rather than outputting content at the end as before.
- Syntax highlighting for SQL files as well
- Charset detection of files so at least UTF16LE files (and of course UTF 8 and ISO-8859-x) looks nice in the editor
- Opening unknown files in default associated program
- Various refactoring and minor bugfixes     

### 1.0 beta 4, Jan 03, 2019
- Add support for text, xml and java files
- Add context menu to file tree (add and remove dir/file)
- Track if files are modified and prompts to save when closing if unsaved
    - mark code tab with * in the title as soon as editing started
    - unmark as soon as code is saved
    - tabs that have changed should prompt a save dialog when closing
- Add "save as" menu option
- Add "new -> text file" menu option
 
### 1.0 beta3, Jan 01, 2019
- Add support for running on Windows.
- Add support for switching package loaders
- Various minor improvements.

### 1.0 beta2, Dec 30, 2018
- Will now honor ctrl + enter to execute current row or selection

- Run button will now execute selected text (if any is selected) or execute the whole script.

- Fixed setting working dir properly when changing dirs so that scripts can reference files relatively.

- Tested with Renjin 0.9.2717

### 1.0 beta. Dec 29, 2018
- Initial release