library("se.alipsa:rideutils")
generateRLineTypes<-function(){
  oldPar<-par()
  par(font=2, mar=c(0,0,0,0))
  plot(1, pch="", ylim=c(0,6), xlim=c(0,0.7),  axes=FALSE,xlab="", ylab="")
  for(i in 0:6) lines(c(0.3,0.7), c(i,i), lty=i, lwd=3)
  text(rep(0.1,6), 0:6, labels=c("0.'blank'", "1.'solid'", "2.'dashed'", "3.'dotted'",
                                 "4.'dotdash'", "5.'longdash'", "6.'twodash'"))
  par(mar=oldPar$mar,font=oldPar$font )
}

fileName <- file.path(tempdir(), "linetypes.svg")
svg(fileName)
generateRLineTypes()
dev.off()
display(fileName, "svg webview")
display(readImage(fileName), "svg fxsvimage")
print(fileName)

fileName <- file.path(tempdir(), "linetypes.png")
png(fileName)
generateRLineTypes()
dev.off()
display(fileName, "png")