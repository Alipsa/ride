print("#what is the mean value of the vector containing the elements 2.23, 3.45, 1.87, 2.11, 7.33, 18.34, 19.23")
numvec <- c(2.23, 3.45, 1.87, 2.11, 7.33, 18.34, 19.23)
print(mean(numvec))

# Exercise #3
print("# Use a for loop to determine the value of sum_{i=1}^{25} i^2")

k = 0
for (i in 1:22) {
  k <- k + (i^2)
}
print(k)

# Exercise #4
print("# The cars dataset is available in base R. You can type cars to see it. Use the class function to determine what type of object is cars.")
print(class(cars))

str(cars)

# Exercise #6
print("#What is the name of the second column of cars?")
str(cars)

#  Exercise #7
print("#What is the average distance traveled in this dataset?")
print(mean(cars[,2]))

# Exercise #8
print("# What row of cars has a a distance of 85?")
print(which(cars[,2] == 85))

print("Nu är det klart!")