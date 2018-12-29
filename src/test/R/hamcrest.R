library("se.alipsa:rideutils")
library("hamcrest")

test.readImage <- function() {
    import(javafx.scene.image.Image)
    
    img <- readImage("image/logo.png")
    assertTrue(exists("img"))
    assertTrue(img$getHeight() > 0)
    
}