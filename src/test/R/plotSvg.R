library(grDevices)
library(graphics)

# First plot
svg("/tmp/test2.svg")
plot(sin, -pi, 2*pi)

dev.off()

library("se.alipsa:rideutils")
test2 <- readImage("/tmp/test2.svg")
# convert the image to a a javafx ImageView and display it in ride
inout$display(as.imageView(test2), "test2")