library("se.alipsa:rideutils")
library("hamcrest")


greaterThan <- function(expected) {
	function(actual) {
		all(actual > expected)
  	}
}

test.readImage <- function() {
    print("test.readImage")
    import(javafx.scene.image.Image)
    
    img <- readImage("image/logo.png")
    assertTrue(exists("img"))
    assertThat(img$getHeight(), greaterThan(0))
    print("done")
}

runTests <- function() {
	vars <- ls(envir = .GlobalEnv, pattern = "test.*")
	#str(vars)
	for(func in vars){
  	  print(paste("Running test", func))
	  do.call(func, list() )
	}
}

runTests()