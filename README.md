# Ride
An integrated development environment for Renjin R

The purpose of this gui is to provide a nice R development environment, similar to R Studio, for the Renjin 
JVM implementation of R (www.renjin.org). It runs R code in the renjin ScriptEngine thus allowing you to run and verify 
R scripts before creating an embedded R application in java code, or to just use it as a data analysis tool
much like you would use R Studio. 

It was created to have a familiar (similar to RStudio) interface to develop R scripts that
will later run in embedded mode on application servers such as Spring Boot or Play Framework.
I use it daily at work and have been doing so for almost two years now. It started as a tool to develop and test R code 
that I created using RStudio but needed a manageable intermediary to make sure my code worked in Renjin before integrating 
it with the java application servers I use at work - due to fact that some packages commonly used in GNU R does not yet work 
in Renjin. Later, it evolved to the point where I now use it as my primary data analysis tool.

It is fully functional i.e. it is possible to create, open, save and execute R scripts, 
run selected text, ctrl + enter execution of current line or selected text, view packages loaded, 
see variables created, syntax highlighting for R, XML, SQL, Java, Groovy, Javascript etc. 
It also has some Renjin specific features such as support for the different package loaders, 
wizards to create Renjin projects and Renjin extensions, maven support making a very
convenient environment for serious R development targeted for the JVM using Renjin.

![Screenshot](https://raw.githubusercontent.com/Alipsa/ride/master/docs/Screenshot.png "Screenshot")

As Renjins support for graphics such as plotting is still somewhat limited (It just pops up an AWT window with the plot).
An alternative way is to use the inout component that has been injected into the session to display files e.g.

```r
library(grDevices)
library(graphics)

# plot a svg image to a file
fileName <- file.path(tempdir(), "svgplot.svg")
svg(fileName)
plot(sin, -pi, 2*pi)

dev.off()

# rideutils provides ways to bridge over from R to Java to be able to interact with the IDE
library("se.alipsa:rideutils")
# convert the image to a a javafx Image and display it in ride
display(fileName, "svgplot")
```

![Plot](https://raw.githubusercontent.com/Alipsa/ride/master/docs/Plot.png "Plot")

An alternative way when you just want to view the plot in Ride is to use viewPlot e.g:
```r
library("se.alipsa:rideutils")
viewPlot({
  plot(mtcars$mpg)
  abline(h=mean(mtcars$mpg))
})
```

#### Package and class loading
The AetherPackageLoader is used per default so libraries will be fetched automatically from 
bedatadriven or maven central repos. This can be modified in the Global Options menu.

If you change to the ClasspathPackageLoader it makes sense to add dependencies to the maven pom.xml
when running Ride. This will give you similar functionality as the AetherPackageLoader but restricted to 
the versions you have defined in your pom. The change is persistent so just add any required dependency and start Ride.

For a smooth development experience you should also add the build dir to the classpath (which is how maven 
normally works).  This combination (ClasspathPackageLoader + add pom dependencies + add build dir) is a 
setup that I think is very useful, maybe even better than using AetherPackageLoader in some cases e.g. for
package development and when you aim to embed your R code in an app server as it enables you to make sure 
that dependencies are correct prior to integration testing in embedded mode on e.g. an app server. 
In the Packages tab you can see a list of packages loaded for the session (in case you missed a library 
call in the script but loaded it from another script - the session is shared).

#### Creating R reports in Munin
Ride also supports developing, downloading and publishing reports for the [Munin Reports server](https://github.com/Alipsa/munin). 
Both formats (R based reports producing HTML i.e. mr files, and markdown with R enhancements (similar to rmd) i.e. mdr files) 
are supported natively in Ride. When you edit an mr or mdr file a view button will apear in the button bar allowing 
you to preview the report. Parameterized reports are also supported. 


### Installing and Running ride
Go to the releases tab and expand the assets section at the bottom of the release. 
Unzip the zip file ride-<version>-dist.zip e.g. ride-1.2.6-GA-dist.zip to a directory of choice.

There are icons for windows and Linux in the base folder of the unpacked zip that can be used to create a desktop shortcut/launcher.

Use ride.cmd or ride.sh to start ride. There is also an (experimental) executable for Windows aimed to
provide a slightly more "integrated" experience. To run it make sure you have JAVA_HOME and path set to 
s java 8 version that includes javafx. I have seen some issues running with OpenJDK which seems to be
related to not setting registry values in the same way as the Oracle JDK. The workaround to this would be to 
bundle the entire JDK which is not something I want to do, so if the exe is not working for you, use the cmd script.  

If you want to run the bash script from windows git bash (msys), create a shortcut with the comment
`D:\whatever\Git\bin\bash.exe ./ride.sh`
Where D:\whatever is the path to your git for windows installation
Also set the working dir in the shortcut to wherever you installed ride.

If you want to override or customize startup options you can create a file called env.sh (or env.cmd) in the base directory
where you installed (unzipped) Ride. An example would be to set the right java environment variables so the launcher works
properly, e.g.
```shell script
#!/usr/bin/env bash

# Java is managed by SDK, load it
source ~/.sdkman/bin/sdkman-init.sh
# We want to run with java 17 with embedded javafx so that scaling works
sdk use java 17.0.10.fx-librca
# Scale the application 200% as we have a Hi-DPI screen, see https://wiki.archlinux.org/index.php/HiDPI#Java_applications
JAVA_OPTS="-Dglass.gtk.uiScale=200%"
```
The last setting (JAVA_OPTS) is a special variable to add system properties (-D values) to java starting up.

### A SQL script screenshot
Showing the result of a select query in the viewer tab and the connection view that is shown when you right click 
a connection and choose "view connection".

![SQL Screenshot](https://raw.githubusercontent.com/Alipsa/ride/master/docs/SQLScreenshot.png "SQL Screenshot")

# Issues and Feature requests
For any issues or feature request etc. on Ride please use the [github issue feature](https://github.com/Alipsa/ride/issues)

For questions related to Renjin please refer to the [Google discussion group](https://groups.google.com/g/renjin-dev).
If you think you found a bug in Renjin please report that to the [Github issue pages](https://github.com/bedatadriven/renjin/issues).

# Building and compiling

To build Ride, simply do `mvn clean install`

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
The components that actually does something ;) i.e. executes R code.

Copyright © 2010-2019 BeDataDriven Groep B.V. and contributors under GNU General Public License v2.0

### org.fxmisc.richtext:richtextfx
Used to color R code.

Copyright (c) 2013-2017, Tomas Mikula and contributors under BSD 2-Clause "Simplified" License

### org.jfree:fxgraphics2d
A bridge between awt and javafx so we can use the awt graphics devices in renjin
Copyright (c) 2014-2018, Object Refinery Limited. Licensed under a BSD-style license

### org.girod:fxsvgimage
Indirect dependency from rideutils. Allows conversion of svg files to javafx Image nodes.
Since this is not available on maven central (yet) a snapshot version is built and used based on the latest
code at the time of the Ride release. Licensed under a MIT license. 

### org.apache:log4j
The logging framework used.
Apache 2.0 license

### com.fasterxml.jackson.core:jackson-core and jackson-databind
Used for JSON handling in various places.
Copyright Fasterxml under Apache 2.0 license.

### org.apache.tika:tika-core
Used to detect file types as Files.probeContentType is inconsistent over different OS's;
Apache 2.0 license.

### org.apache.tika:tika-parsers
Used to detect file encoding.
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

### org.jdom:jdom2
Used to read and write xml files. [Apache style license](https://github.com/hunterhacker/jdom/blob/master/LICENSE.txt)

### Various jdbc drivers
These are included for convenience only. Licenced under various open source licenses. 
There is no direct dependency on any jdbc driver in Ride.

### org.codehaus.groovy:groovy-all
Makes it possible to run Groovy scripts. Apache Software License, Version 2.0.

### io.github.classgraph:classgraph
Makes it possible to scan for available libraries (Renjin extensions) i.e. libraries on the classpath which
are not necessarily loaded with library("x.y.z:abc"). MIT License

# Contributing
If you are interested in helping out, reporting issues, creating tests or implementing new features
are all warmly welcome. See also [todo](todo.md) for roadmap.

# Known Issues
There is some problem with using the AetherPackageLoader when developing packages and running tests.
The issue is that renjin will report "failed to find package" even though you have run `mvn package` or even 
`mvn install`. The same problem exists for the Renjin CLI as well so this is not due to Ride.
What DOES work is to use the classpathPackageLoader with the option for "include build dirs in classpath" enabled.

