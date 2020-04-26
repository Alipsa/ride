# Ride
An integrated development environment for Renjin R

The purpose of this gui is to provide a nice R development environment, similar to R Studio, for the Renjin 
JVM implementation of R (www.renjin.org). It runs R code in the renjin ScriptEngine thus allowing you to run and verify 
R scripts before creating an embedded R application in java code, or to just use it as a data analysis tool
much like you would use R Studio. 

It was created to have a familiar (similar to RStudio) interface to develop R scripts that
will later run in embedded mode on application servers such as Wildfly or Play Framework.
I use it daily at work and have been doing so for several months now. It started as a tool to develop and test R code 
that I created using RStudio but needed a manageable intermediary to make sure my code worked in Renjin before integrating it with the java application servers I use at work - due to fact that many packages commonly used in GNU R does not yet work 
in Renjin. Later, it evolved to the point where I now use it as my primary data analysis tool.


It is fully functional i.e. it is possible to create, open, save and execute R scripts, 
run selected text, ctrl + enter execution of current line or selected text, view packages loaded, 
see variables created, syntax highlighting for R, XML, SQL  and Java files etc. but it has some way to go compared to 
all the rich functionality of RStudio at this point. 

![Screenshot](https://raw.githubusercontent.com/perNyfelt/ride/master/docs/Screenshot.png "Screenshot")

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


![Plot](https://raw.githubusercontent.com/perNyfelt/ride/master/docs/Plot.png "Plot")


The AetherPackageLoader is used per default so libraries will be fetched automatically from 
bedatadriven or maven central repos. This can be modified in the Global Options menu.

If you change to the ClasspathPackageLoader you need to add dependencies to the maven pom.xml
when running Ride. The change is persistent so just add any required dependency and start Ride.

This is useful when moving from development to test to make sure dependencies are correct 
prior to integration testing in embedded mode on e.g. an app server. In the Packages tab you can see
a list of packages loaded for the session (in case you missed a library call in the script but loaded 
it from another script - the session is shared).

### Installing and Running ride
There are two distributions of Ride. Go to the releases tab and expand the assets section at the bottom of the release. The zip file called ride-<version>-dist.zip e.g. ride-1.1-final-dist.zip is the "slim" distribution whereas the ride-fat-<version>-dist.zip e.g. ride-fat-1.1-final-dist.zip is the "fat" distribution.

There are icons for windows and Linux in the base folder of the unpacked zip that can be used to create a desktop shortcut/launcher.

#### Ride slim
This uses maven as the means to download resources and start Ride. All external dependencies are described in the pom.xml file. When preparing for your R code to be intergrated into your application it is quite useful to use the ClasspathPackageLoader and modify the pom.xml to include your dependencies and then verify that your code runs as expected. If it does you can be sure it will work in your application as well once you have added those same dependecies. 
Use ride.cmd or ride.sh to start ride. 

#### Ride fat
This uses ant to start Ride. It contains all the artifacts needed to run Ride. Anything that is in the lib folder will be added to the classpath. To add a new artefact just put it in the lib dir and restart ride. This is useful if you are working off the grid or manage your artefacts in some other way that makes them unavailable from a maven remote repository. Note that you can combine Ride off line with AetherPackageLoader to still dynamically download packages as needed. 
Use ride-offline.cmd or ride-offline.sh to start ride. 

### A SQL script screenshot
Showing the result of a select query in the viewer tab and the connection view that is shown when you right click 
a connection and choose "view connection".

![SQL Screenshot](https://raw.githubusercontent.com/perNyfelt/ride/master/docs/SQLScreenshot.png "SQL Screenshot")

# Building and compiling

To build Ride, simply do ` mvn -Ponline clean install`

There are some wrapper scripts that you might find useful.  
To create a runnable jar and run it do 
```
./devrun.sh
``` 
or the equivalent `devrun.cmd` for windows.

To just run it without rebuilding use `run.sh` or `run.cmd` depending on environment.

For released versions there is another shell script (ride.sh or ride.cmd) that should be used to start Ride.

## 3:rd party software used
Note: only direct dependencies are listed below.

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

### org.apache.tika:tika-core
Used to detect file types as Files.probeContentType is inconsistent over different OS's;
Apache 2.0 license.

### org.apache.commons-lang3
Used for time and string formatting
Apache 2.0 license.

### org.apache.commons-io
Used for reading files content
Apache 2.0 license.

### codehaus:guessencoding
Used to detect file encoding (Charset). Copyright 2002-2007 Guillaume Laforge under Apache 2.0 License

###  com.github.jsqlparser:jsqlparser
Used to validate and analyse SQL code. Apache Software License, Version 2.0.

### org.apache.maven.shared:maven-invoker,maven-shared-utils
Used to run maven (run pom.xml files). Apache Software License, Version 2.0.

### org.eclipse.jgit:org.eclipse.jgit
Used to provide git support. Eclipse Distribution License v1.0

### Various jdbc drivers
These are included for convenience only. Licenced under various open source licenses. There is no direct dependency on any jdbc driver in Ride.

# Contributing
If you are interested in helping out, reporting issues, creating tests or implementing new features
are all warmly welcome. See also [todo](todo.md) for roadmap.

# Known Issues
There is some problem with using the AetherPackageLoader when developing packages and running tests.
The issue is that renjin will report "failed to find package" even though you have run `mvn package` or even 
`mvn install`. The same problem exists for the Renjin CLI as well so this is not due to Ride.
What DOES work is to use the classpathpackage loader with the option for "include build dirs in classpath" enabled.

