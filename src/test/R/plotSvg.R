library(grDevices)
library(graphics)

fileName <- file.path(tempdir(), "svgplot.svg")

# First plot
svg(fileName)
plot(sin, -pi, 2*pi)

dev.off()

library("se.alipsa:rideutils")
# convert the image to a a javafx ImageView and display it in ride
#inout$display(as.imageView(test2), "test2")
inout$display(readImage(fileName), "svgplot")
if (file.exists(fileName)) {
	print(paste("removing file", fileName))
	file.remove(fileName)
}