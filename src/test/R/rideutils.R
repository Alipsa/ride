library("se.alipsa:rideutils")

# read the file and return it as a javafx Image
img <- readImage("image/logo.png")
# display it in my javafx application
inout$display(img, "logo")

img2 <- readImage("image/logo2.png")

# convert the image to a a javafx ImageView
view <- as.imageView(img2)

# display it in my javafx application
inout$display(view, "logo2")