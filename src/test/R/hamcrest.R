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

test.testwith1args <- function(arg1) {
	assertTrue(TRUE)
}
test.testwith2args <- function(arg1, arg2) {
	assertTrue(TRUE)
}

runTests <- function() {
	vars <- ls(envir = .GlobalEnv, pattern = "test.*")
	for(func in vars){
	  numargs <- length(formals(func))
	  if (numargs == 0) {
  	  	print(paste("# Running test", func))
	  	do.call(func, list() )
	  } else {
		print(paste("Tests should not have arguments, skipping", func))
	  }
	}
}

runTests()