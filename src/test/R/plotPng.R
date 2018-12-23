library(grDevices)
library(graphics)

fil <- "/tmp/test2.png"
# First plot
#png(fil)
#plot(sin, -pi, 2*pi)

#dev.off()

#install.packages("png")
library(png)
img <- readPNG(fil)
grid::grid.raster(img)