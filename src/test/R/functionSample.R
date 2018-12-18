meanTrim <- function(x) {
  x <- x[x != max(x)]
  x <- x[x != min(x)]
  return(mean(x))
}

# Example:
vec <- c(2.3, 2.7, 3.1, 4.9, 1.0, 2.4, 2.6, 2.1, 2.0, 1.9)
print(paste("mean is ", mean(vec)))
print(paste("meanTrim is ", meanTrim(vec)))