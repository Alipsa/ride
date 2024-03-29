library("se.alipsa:rideutils")
# illustrating a a linear model and examples of under and overfitting
viewPlot({
# som initial data for our illustration
age <- c(21, 46, 55, 35, 28)
income <- c(1850, 2500, 2560, 2230, 1800)
data <- data.frame(age, income)
plot(data, pch = 16)


#print(cor(data)) # correlation )
# building a linear regression model
LinReg <- lm(income ~ age) # assign model to variable
abline(LinReg, col = "green", lwd = 2) # add regression line
#print(LinReg) # coefficients
#print(summary(LinReg)) # model summary
# predicting income with linear model
#print(predict(LinReg, data.frame(age = 20)))
pred_LinReg <- predict(LinReg, data.frame(age = seq(from = 0, to = 80, by = 5)))
names(pred_LinReg) <- seq(0, 80, 5)
#print(pred_LinReg)

# polynomial regression, to illustrate overfitting
PolyReg <- lm(income ~ poly(age, 4, raw = TRUE))
lines(c(20:55), predict(PolyReg, data.frame(age = c(20:55))), col = "red")
pred_PolyReg <- predict(PolyReg, data.frame(age = seq(0, 80, 5)))
names(pred_PolyReg) <- seq(0, 80, 5)
#print(pred_PolyReg)

# mean income as a model to illustrate undefitting
abline(h = mean(income), col = "blue")

title(main = "All models are wrong, some are useful")
legend("bottomright", c("Mean: Underfitting", "Linear fit", "Polynomial Reg.: Overfitting"), col = c("blue", "green", "red"), lwd = 3)

}, "Fitting a model")
#print(mean(income))
# dev.off()
