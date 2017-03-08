library(ggplot2)

##
#
# This script aggregates the results of the Onyase Figure script on null scores into a single figure.
#
##


# Set working directory to the R folder

# setwd("R")


# Set the categories according to the parameters table

categoriesNames <- c("Default","Isoforms","Trembl","Vertebrates", "Proteogenomics","4 mc","Semispecific","Variable Cmm","Phosphorylation","AB-Y","ABC-XYZ","MS1 0.5 Da","MS2 0.5 Da","MS1 MS2 0.5 Da","Isotope -4 +4 Da","Charge 1 to 4","Charge 1 to 6")
mainCategoriesNames <- c("Default","Database","Database","Database", "Database","Digestion","Digestion","Modifications","Modifications","Fragmentation","Fragmentation","Tolerances","Tolerances","Tolerances","Isotopes","Charges","Charges")
categoriesIndexes <- c(0,1,2,3,4,5,6,8,9,10,11,12,13,14,15,16,17)

categories <- data.frame(id = categoriesIndexes, mainCategroy = mainCategoriesNames, name = categoriesNames, stringsAsFactors = F)


# Number of null values from the Onyase export

nZeros <- c(8293947, 8886003, 14299638, 182096459, 5222906, 12102140, 130297465, 13694912, 105926826, 8293950, 8293978, 142412796, 8293947, 142412294, 37014133, 9394845, 12728318)
nTotal <- c(8429815, 9030225, 14494238, 184339585, 5311065, 12255889, 132487080, 13870655, 106275895, 8429818, 8429845, 143679099, 8429814, 143678592, 37512992, 9543463, 12878454)
nPositive <- nTotal - nZeros

valuesList <- c()
seriesList <- c()
categoriesList <- c()
mainCategoriesList <- c()
for (i in 1 : length(nZeros)) {
  
  valuesList <- c(valuesList, nZeros[i])
  seriesList <- c(seriesList, "Hyperscore 0")
  categoriesList <- c(categoriesList, categoriesNames[i])
  mainCategoriesList <- c(mainCategoriesList, mainCategoriesNames[i])
  
  valuesList <- c(valuesList, nPositive[i])
  seriesList <- c(seriesList, "Hyperscore >0")
  categoriesList <- c(categoriesList, categoriesNames[i])
  mainCategoriesList <- c(mainCategoriesList, mainCategoriesNames[i])
  
}

categories$nZeros <- nZeros
categories$nPositive <- nPositive
categories$nTotal <- nTotal
sortedCategoriesNames <- categories[order(categories$nTotal, categories$name), "name"]
sortedCategoriesNames <- sortedCategoriesNames[sortedCategoriesNames != "Default"]
sortedCategoriesNames <- c("Default", sortedCategoriesNames)
categoriesListFactors <- factor(categoriesList, levels = sortedCategoriesNames)

# Plot the number of null values in every condition

zerosPlot <- ggplot()
zerosPlot <- zerosPlot + geom_bar(aes(x=categoriesListFactors, y=valuesList, fill = seriesList, col = mainCategoriesList), stat="identity")
zerosPlot <- zerosPlot + labs(x = "Category", y = "# Peptides", fill="", col="")
zerosPlot <- zerosPlot + theme(axis.text.x = element_text(angle = 90, hjust = 1))
zerosPlot <- zerosPlot + scale_fill_grey()
zerosPlot <- zerosPlot + guides(colour = guide_legend(override.aes = list(fill = "white", size = 1)))
plot(zerosPlot)
