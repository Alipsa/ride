library("grDevices")
library("graphics")
library("se.alipsa:rideutils")

createLinePlot <- function(){

  Orange$Tree <- as.numeric(Orange$Tree)
  ntrees <- max(Orange$Tree)

  xrange <- range(Orange$age)
  yrange <- range(Orange$circumference)

  plot(xrange, yrange, type="n", xlab="Age (days)",
     ylab="Circumference (mm)" )
  colors <- rainbow(ntrees)
  linetype <- c(1:ntrees)
  plotchar <- seq(18,18+ntrees,1)

  # add lines
  for (i in 1:ntrees) {
    tree <- subset(Orange, Tree==i)
    lines(tree$age, tree$circumference, type="b", lwd=1.5,
      lty=linetype[i], col=colors[i], pch=plotchar[i])
  }

  title("Tree Growth", "example of line plot")

  # add a legend
  legend(xrange[1], yrange[2], 1:ntrees, cex=0.8, col=colors,
     pch=plotchar, lty=linetype, title="Tree")
}

# plot to a png file
fileName <- file.path(tempdir(), "orange.png")
png(fileName)
createLinePlot()
dev.off()

display(fileName, "png")

# plot to an svg file
fileName <- file.path(tempdir(), "orange.svg")
svg(fileName)
createLinePlot()
dev.off()

# View in webview and compare that with readImage conversion
display(fileName, "svg webview")
display(readImage(fileName), "svg fxsvimage")
print(fileName)