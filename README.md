# Ride
A RStudio like gui for Renjin 

The purpose of this gui is to provide a nice R development environment, similar to R Studio, for the Renjin 
JVM implementation of R (www.renjin.org). It runs R code in the renjin ScriptEngine thus allowing you to run and verify 
R scripts before creating an embedded R application in java code, or to just use it as a data analysis tool
much like you would use R Studio. 

It is functional i.e. it is possible to create, open, save and execute R scripts 
but it is far from all the rich functionality of RStudio at this point. 


![alt text](https://raw.githubusercontent.com/perNyfelt/ride/master/docs/Screenshot.png "Screenshot")


As Renjin does not yet fully support graphics such as plotting the only support for displaying graphics in Ride
is to use the inout component that has been injected into the session to display files e.g.

````
# rideutils provides ways to bridge over from R to Java to be able to interact with the IDE
library("se.alipsa:rideutils")

# read the file and return it as a javafx Image
img <- readImage("image/logo.png")
# display it in my javafx application
inout$display(img, "logo")
````

The AetherPackageLoader is used per default so libraries will be fetched automatically from 
bedatadriven or maven central repos.

To create a runnable jar and run it do 
```
./devrun.sh
``` 

For released versions there is another shell script (ride.sh or ride.cmd) that should be used to start Ride.

## 3:rd party software used

### org.renjin:renjin-script-engine, tools, and renjin-aether-package-loader
The components that actually does something ;) i.e. executes R code. It is not included in the fat jar and 
dependecies are scoped as provided. This enables us to run Ride with any version of Renjin (0.9.2716 or later)

Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors under GNU General Public License v2.0

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
