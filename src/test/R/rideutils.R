library("se.alipsa:rideutils")

img <- readImage("image/logo.png")

img2 <- readImage("image/logo2.png")
view <- as.imageView(img2)

inout$plot(img)
inout$plot(view, "logo2")