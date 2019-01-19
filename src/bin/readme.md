This is version 1.0 of Ride.

At this point Ride is usable and provides a nice IDE for developing and testing R scripts aimed to run in the Renjin ScriptEngine.

To run Ride you need to have maven and Java 1.8 or later installed. 
If you use open JDK then you might need to do something like `sudo apt-get install openjfx` depending on your distro.

Ride is started using maven (see ride.sh/ride.cmd for details). 
This is because renjin is not included in the fat jar and needs to be wired in to the classpath upon startup. 
You can specify a different renjin version in the pom if you like but note that it requires renjin version 0.9.2716 or later to work. 

Since you probably want to run the ScriptEngine with the ClassPathPackageLoader when embedding your R app, 
you can set Ride to use this to resolve packages. You then need to include those dependencies to the maven.pom. 

## How to install:
Unzip ride-1.0-final-dist.zip to a directory of choice

## How to run:

`> ./ride.sh`

or on windows

`$ .\ride.cmd`


# Version Descriptions

### 1.1 SNAPSHOT (TODO change version when released)
- Create a new runbutton when hamcrest tests are detected

    Will run hamcrest tests similar to the renjin-hamcrest-maven-plugin i.e. producing nice output, error messages and
    summary 

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