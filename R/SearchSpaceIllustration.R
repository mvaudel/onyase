library(ggplot2)

matches <- read.table(file="matches.txt", sep = "\t", header = T)

ms1Plot <- ggplot()
ms1Plot <- ms1Plot + geom_violin(aes(x=matches$Category, y = matches$MS1_deviation), width = 0.6, alpha = 0.5)
ms1Plot <- ms1Plot + labs(x = "", y = "MS1 deviation [ppm]")
plot(ms1Plot)