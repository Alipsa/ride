vec <- c(1, 3, 4, 5, 7)
inout$View(vec, "Vector")

df <- data.frame("SN" = 1:3, "Age" = c(48,17,49), "Name" = c("Per","Ian","Louise"))
str(df)
print(df)
inout$View(df, "persons")

mat <- matrix(1:9, nrow = 3, ncol = 3)
inout$View(mat, "a matrix")

arr <- array(1:48, dim=c(3,4,2, 2))
inout$View(as.data.frame(arr), "a 4 dimentional array as df")
#inout$View(arr, "a 4 dimentional array")