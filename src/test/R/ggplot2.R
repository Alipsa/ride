 library("ggplot2")
df <- data.frame(dose=c("D0.5", "D1", "D2"),
                len=c(4.2, 10, 29.5))
p<-ggplot(data=df, aes(x=dose, y=len)) +
  geom_bar(stat="identity")