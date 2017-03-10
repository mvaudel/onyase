library(ggplot2)

##
#
# This script aggregates the results of the Onyase Figure script on null scores into a single figure.
#
##


# Set working directory to the R folder

# setwd("R")


# Number of null values from the Onyase export

nZeros <- c(8293947, 8886003, 14299638, 182096459, 12102140, 130297465, 13694912, 105926826, 8293950, 8293978, 8704551, 155163611, 37014133, 9394845, 12728318)
nTotal <- c(8429815, 9030225, 14494238, 184339585, 12255889, 132487080, 13870655, 106275895, 8429818, 8429845, 10813674, 192392071, 37512992, 9543463, 12878454)
nPositive <- nTotal - nZeros

valuesList <- c()
seriesList <- c()
categoriesList <- c()
mainCategoriesList <- c()
for (i in 1 : length(nZeros)) {
  
  valuesList <- c(valuesList, nZeros[i])
  seriesList <- c(seriesList, "=0")
  categoriesList <- c(categoriesList, categoriesNames[i])
  mainCategoriesList <- c(mainCategoriesList, mainCategoriesNames[i])
  
  valuesList <- c(valuesList, nPositive[i])
  seriesList <- c(seriesList, ">0")
  categoriesList <- c(categoriesList, categoriesNames[i])
  mainCategoriesList <- c(mainCategoriesList, mainCategoriesNames[i])
  
}

categories$nZeros <- nZeros
categories$nPositive <- nPositive
categories$nTotal <- nTotal
categoriesListFactors <- factor(categoriesList, levels = allCategoriesSorted)

mainCategoriesFactors <- factor(mainCategoriesList, levels = sortedMainCategoriesNames)
seriesList <- factor(seriesList, levels = c(">0", "=0"))


# Plot the number of null values in every condition

zerosPlot <- ggplot()
zerosPlot <- zerosPlot + geom_bar(aes(x=categoriesListFactors, y=valuesList, col = seriesList, fill = mainCategoriesFactors), stat="identity")
zerosPlot <- zerosPlot + labs(x = "", y = "# Peptides", fill="", col="Hyperscore")
zerosPlot <- zerosPlot + theme(axis.text.x = element_text(angle = 90, hjust = 1), legend.position = "top")
zerosPlot <- zerosPlot + scale_color_manual(values = c("black", "gray"))
zerosPlot <- zerosPlot + scale_fill_brewer(palette="Pastel1")
zerosPlot <- zerosPlot + guides(colour = guide_legend(override.aes = list(fill = "white", size = 1)), fill = F)
plot(zerosPlot)
