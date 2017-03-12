library(ggplot2)

##
#
# This script displays the identification rate at various FDRs.
#
##

# Number of ids values from the Onyase export

n1 <- c(3860, 3872, 3907, 3728, 3876, 3554, 3901, 3773, 3860, 3860, 3358, 3114, 3792, 3856, 3856)
n5 <- c(4212, 4221, 4258, 4136, 4234, 4039, 4197, 4152, 4212, 4212, 4020, 3813, 4169, 4208, 4206)
n10 <- c(4455, 4461, 4497, 4431, 4487, 4333, 4453, 4405, 4455, 4455, 4346, 4265, 4471, 4459, 4445)


# Format the data for ggplot

valuesList <- c()
seriesList <- c()
categoriesList <- c()
mainCategoriesList <- c()
for (category in allCategoriesSorted) {
  
  i <- which (categories$name == category)
  
  valuesList <- c(valuesList, n1[i])
  seriesList <- c(seriesList, "1%")
  categoriesList <- c(categoriesList, categoriesNames[i])
  mainCategoriesList <- c(mainCategoriesList, mainCategoriesNames[i])
  
  valuesList <- c(valuesList, n5[i])
  seriesList <- c(seriesList, "5%")
  categoriesList <- c(categoriesList, categoriesNames[i])
  mainCategoriesList <- c(mainCategoriesList, mainCategoriesNames[i])
  
  valuesList <- c(valuesList, n10[i])
  seriesList <- c(seriesList, "10%")
  categoriesList <- c(categoriesList, categoriesNames[i])
  mainCategoriesList <- c(mainCategoriesList, mainCategoriesNames[i])
  
}

categoriesListFactors <- factor(categoriesList, levels = allCategoriesSorted)
seriesListFactors <- factor(seriesList, levels <- c("1%", "5%", "10%"))
mainCategoriesFactors <- factor(mainCategoriesList, levels = sortedMainCategoriesNames)


# Create a plot of the id rates

idRatePlot <- ggplot()
idRatePlot <- idRatePlot + geom_path(aes(x=categoriesListFactors, y=valuesList, group = seriesListFactors, col=seriesListFactors))
idRatePlot <- idRatePlot + labs(x = "", y = "# PSMs", col="FDR")
idRatePlot <- idRatePlot + theme(axis.text.x = element_text(angle = 90, hjust = 1))
idRatePlot <- idRatePlot + scale_color_manual(values=c("darkred", "darkorange", "darkgreen"))


# Plot to file

png(filename = "R/results/idRate.png", width = 800, height = 600)
plot(idRatePlot)
dev.off()
