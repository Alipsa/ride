At this point Ride is usable and provides a nice IDE for developing and testing R scripts aimed to run in the Renjin ScriptEngine.

To run Ride you need to have maven and Java 1.8 or later installed. 
This is because renjin is not included in the fat jar and needs to be wired in to the classpath upon startup. 
You can specify a different renjin version in the pom if you like but note that it requires renjin version 0.9.2716 or later to work. 
The rudimentary run.sh is just a call to maven (mvn exec:java)

## How to install:
Unzip ride-1.0-beta-dist.zip to a directory of choice

## How to run:

`> ./run.sh`