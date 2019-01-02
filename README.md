# Ride
A RStudio like gui for Renjin 

The purpose of this gui is to provide a nice R development environment, similar to R Studio, for the Renjin 
JVM implementation of R (www.renjin.org). It runs R code in the renjin ScriptEngine thus allowing you to run and verify 
R scripts before creating an embedded R application in java code, or to just use it as a data analysis tool
much like you would use R Studio. 

It was created to have a familiar (similar to RStudio) interface to develop R scripts that
will later run in embedded mode on application servers such as Wildfly or Play Framework.

It is functional i.e. it is possible to create, open, save and execute R scripts, 
run selected text, ctrl + enter execution of current line or selected text, view packages loaded, 
see variables created etc. but it has some way to go compared to all the rich functionality of RStudio at this point. 

![alt text](https://raw.githubusercontent.com/perNyfelt/ride/master/docs/Screenshot.png "Screenshot")

As Renjins support for graphics such as plotting is still somewhat limited (It just pops up an AWT window with the plot).
An alternative way is to use the inout component that has been injected into the session to display files e.g.

````
library(grDevices)
library(graphics)

# plot a svg image to a file
fileName <- "/tmp/svgplot.svg"
svg(fileName)
plot(sin, -pi, 2*pi)

dev.off()

# rideutils provides ways to bridge over from R to Java to be able to interact with the IDE
library("se.alipsa:rideutils")
# convert the image to a a javafx Image and display it in ride
inout$display(readImage(fileName), "svgplot")
````


![alt text](https://raw.githubusercontent.com/perNyfelt/ride/master/docs/Plot.png "Plot")


The AetherPackageLoader is used per default so libraries will be fetched automatically from 
bedatadriven or maven central repos. This can be modified in the Global Options menu.

If you change to the ClasspathPackageLoader you need to add dependencies to the maven pom.xml
when running Ride. The change is persistent so just add any required dependency and start Ride.

This is useful when moving from development to test to make sure dependencies are correct 
prior to integration testing in embedded mode on e.g. an app server. In the Packages tab you can see
a list of packages loaded for the session (in case you missed a library call in the script but loaded 
it from another script - the session is shared).

# Building and compiling

To build simply do ` mvn clean install`

There are some wrapper scripts that you might find useful.  
To create a runnable jar and run it do 
```
./devrun.sh
``` 
or the equivalent `devrun.cmd` for windows.

To just run it without rebuilding use `run.sh` or `run.cmd` depending on environment.

For released versions there is another shell script (ride.sh or ride.cmd) that should be used to start Ride.

## 3:rd party software used

### org.renjin:renjin-script-engine, tools, and renjin-aether-package-loader
The components that actually does something ;) i.e. executes R code. It is not included in the fat jar and 
dependecies are scoped as provided. This enables us to run Ride with any version of Renjin (0.9.2716 or later)

Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors under GNU General Public License v2.0

### org.fxmisc.richtext:richtextfx
Used to color R code.

Copyright (c) 2013-2017, Tomas Mikula and contributors under BSD 2-Clause "Simplified" License

### org.jfree:fxgraphics2d
A bridge between awt and javafx so we can use the awt graphics devices in renjin

Copyright (c) 2014-2018, Object Refinery Limited. Licensed under a BSD-style license

### org.slf4j:slf4j-api and slf4j-log4j12
The logging framework used.

Copyright (c) 2004-2017 QOS.ch under MIT license

### com.fasterxml.jackson.core:jackson-core and jackson-databind
Used for JSON handling in various places.
Copyright Fasterxml under Apache 2.0 license.