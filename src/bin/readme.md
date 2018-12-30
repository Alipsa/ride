At this point Ride is usable and provides a nice IDE for developing and testing R scripts aimed to run in the Renjin ScriptEngine.

To run Ride you need to have maven and Java 1.8 or later installed. 
This is because renjin is not included in the fat jar and needs to be wired in to the classpath upon startup. 
You can specify a different renjin version in the pom if you like but note that it requires renjin version 0.9.2716 or later to work. 

## How to install:
Unzip ride-1.0-beta2-dist.zip to a directory of choice

## How to run:

`> ./ride.sh`

or on windows

`$ .\ride.cmd`


# Version Descriptions

### 1.0 beta3
Add support for running on Windows

### 1.0 beta2
Will now honor ctrl + enter to execute current row or selection

Run button will now execute selected text (if any is selected) or execute the whole script.

Fixed setting working dir properly when changing dirs so that scripts can reference files relatively.

Tested with Renjin 0.9.2717

### 1.0 beta
Initial release