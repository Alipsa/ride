library("grDevices")
library("graphics")
library("se.alipsa:rideutils")

createLinePlot <- function(){

 x <- 1:5
y1 <- 2*x
plot(x, y1, type = "l", frame = TRUE, lty = 5,
     col = "blue", xlab = "x", ylab = "y")
     
}

# plot to a png file
fileName <- file.path(tempdir(), "dotline.png")
png(fileName)
createLinePlot()
dev.off()

display(fileName, "png")

# plot to an svg file
fileName <- file.path(tempdir(), "dotline.svg")
svg(fileName)
createLinePlot()
dev.off()

# View in webview and compare that with readImage conversion
display(fileName, "svg webview")
display(readImage(fileName), "svg fxsvimage")
print(fileName)


#############
fileName <- file.path(tempdir(), "dashed.svg")
display(fileName, "svg webview")
display(readImage(fileName), "svg fxsvimage")