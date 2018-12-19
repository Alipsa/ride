# renjinStudio
A RStudio like gui for Renjin 

It is functional i.e. it is possible to create, open, save and execute R scripts 
but it is far from all the rich functionality of RStudio at this point. 

To create a runnable jar do `mvn package` and then run it with `java -jar renjinStudio-1.0-SNAPSHOT-jar-with-dependencies.jar`


![alt text](https://raw.githubusercontent.com/perNyfelt/renjinStudio/7343abdd71cd28f1ac030ae4d539ca599ee4bc75/docs/Screenshot.png "Screenshot")

## 3:rd party software used

### org.renjin:renjin-script-engine, tools, and renjin-aether-package-loader
The components that actually does something ;) i.e. executes R code.

Copyright © 2010-2018 BeDataDriven Groep B.V. and contributors under GNU General Public License v2.0

### org.fxmisc.richtext:richtextfx
Used to color R code.

Copyright (c) 2013-2017, Tomas Mikula and contributors under BSD 2-Clause "Simplified" License

### org.slf4j:slf4j-api and slf4j-log4j12
The logging framework used.

Copyright (c) 2004-2017 QOS.ch under MIT license
