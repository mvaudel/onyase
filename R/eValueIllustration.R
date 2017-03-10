library(ggplot2)

##
#
# This script aggregates the results of the Onyase Figure script on PSM e-values into a single figure.
#
##


# Set working directory to the R folder

# setwd("R")


# Format the data for ggplot

eValueCategories <- c()
eValueMainCategories <- c()
eValueValues <- c()
eValueRefValues <- c()

eValueLimit <- 0

tempValues <- -scores0$E.Value
tempValues <- tempValues[scores0$Decoy == 0 & tempValues >eValueLimit]
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 0)
categorytemp[] <- categoriesNames[categoryI]
eValueCategories <- c(eValueCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
eValueMainCategories <- c(eValueMainCategories, mainCategorytemp)
eValueValues <- c(eValueValues, tempValues)
eValueRefValues <- c(eValueRefValues, median(tempValues, na.rm = T))

tempValues <- -scores1$E.Value
tempValues <- tempValues[scores1$Decoy == 0 & tempValues >eValueLimit]
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 1)
categorytemp[] <- categoriesNames[categoryI]
eValueCategories <- c(eValueCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
eValueMainCategories <- c(eValueMainCategories, mainCategorytemp)
eValueValues <- c(eValueValues, tempValues)
eValueRefValues <- c(eValueRefValues, median(tempValues, na.rm = T))

tempValues <- -scores2$E.Value
tempValues <- tempValues[scores2$Decoy == 0 & tempValues >eValueLimit]
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 2)
categorytemp[] <- categoriesNames[categoryI]
eValueCategories <- c(eValueCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
eValueMainCategories <- c(eValueMainCategories, mainCategorytemp)
eValueValues <- c(eValueValues, tempValues)
eValueRefValues <- c(eValueRefValues, median(tempValues, na.rm = T))

tempValues <- -scores3$E.Value
tempValues <- tempValues[scores3$Decoy == 0 & tempValues >eValueLimit]
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 3)
categorytemp[] <- categoriesNames[categoryI]
eValueCategories <- c(eValueCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
eValueMainCategories <- c(eValueMainCategories, mainCategorytemp)
eValueValues <- c(eValueValues, tempValues)
eValueRefValues <- c(eValueRefValues, median(tempValues, na.rm = T))

tempValues <- -scores5$E.Value
tempValues <- tempValues[scores5$Decoy == 0 & tempValues >eValueLimit]
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 5)
categorytemp[] <- categoriesNames[categoryI]
eValueCategories <- c(eValueCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
eValueMainCategories <- c(eValueMainCategories, mainCategorytemp)
eValueValues <- c(eValueValues, tempValues)
eValueRefValues <- c(eValueRefValues, median(tempValues, na.rm = T))

tempValues <- -scores6$E.Value
tempValues <- tempValues[scores6$Decoy == 0 & tempValues >eValueLimit]
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 6)
categorytemp[] <- categoriesNames[categoryI]
eValueCategories <- c(eValueCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
eValueMainCategories <- c(eValueMainCategories, mainCategorytemp)
eValueValues <- c(eValueValues, tempValues)
eValueRefValues <- c(eValueRefValues, median(tempValues, na.rm = T))

tempValues <- -scores8$E.Value
tempValues <- tempValues[scores8$Decoy == 0 & tempValues >eValueLimit]
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 8)
categorytemp[] <- categoriesNames[categoryI]
eValueCategories <- c(eValueCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
eValueMainCategories <- c(eValueMainCategories, mainCategorytemp)
eValueValues <- c(eValueValues, tempValues)
eValueRefValues <- c(eValueRefValues, median(tempValues, na.rm = T))

tempValues <- -scores9$E.Value
tempValues <- tempValues[scores9$Decoy == 0 & tempValues >eValueLimit]
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 9)
categorytemp[] <- categoriesNames[categoryI]
eValueCategories <- c(eValueCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
eValueMainCategories <- c(eValueMainCategories, mainCategorytemp)
eValueValues <- c(eValueValues, tempValues)
eValueRefValues <- c(eValueRefValues, median(tempValues, na.rm = T))

tempValues <- -scores10$E.Value
tempValues <- tempValues[scores10$Decoy == 0 & tempValues >eValueLimit]
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 10)
categorytemp[] <- categoriesNames[categoryI]
eValueCategories <- c(eValueCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
eValueMainCategories <- c(eValueMainCategories, mainCategorytemp)
eValueValues <- c(eValueValues, tempValues)
eValueRefValues <- c(eValueRefValues, median(tempValues, na.rm = T))

tempValues <- -scores11$E.Value
tempValues <- tempValues[scores11$Decoy == 0 & tempValues >eValueLimit]
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 11)
categorytemp[] <- categoriesNames[categoryI]
eValueCategories <- c(eValueCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
eValueMainCategories <- c(eValueMainCategories, mainCategorytemp)
eValueValues <- c(eValueValues, tempValues)
eValueRefValues <- c(eValueRefValues, median(tempValues, na.rm = T))

tempValues <- -scores13$E.Value
tempValues <- tempValues[scores13$Decoy == 0 & tempValues >eValueLimit]
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 13)
categorytemp[] <- categoriesNames[categoryI]
eValueCategories <- c(eValueCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
eValueMainCategories <- c(eValueMainCategories, mainCategorytemp)
eValueValues <- c(eValueValues, tempValues)
eValueRefValues <- c(eValueRefValues, median(tempValues, na.rm = T))

tempValues <- -scores14$E.Value
tempValues <- tempValues[scores14$Decoy == 0 & tempValues >eValueLimit]
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 14)
categorytemp[] <- categoriesNames[categoryI]
eValueCategories <- c(eValueCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
eValueMainCategories <- c(eValueMainCategories, mainCategorytemp)
eValueValues <- c(eValueValues, tempValues)
eValueRefValues <- c(eValueRefValues, median(tempValues, na.rm = T))

tempValues <- -scores15$E.Value
tempValues <- tempValues[scores15$Decoy == 0 & tempValues >eValueLimit]
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 15)
categorytemp[] <- categoriesNames[categoryI]
eValueCategories <- c(eValueCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
eValueMainCategories <- c(eValueMainCategories, mainCategorytemp)
eValueValues <- c(eValueValues, tempValues)
eValueRefValues <- c(eValueRefValues, median(tempValues, na.rm = T))

tempValues <- -scores16$E.Value
tempValues <- tempValues[scores16$Decoy == 0 & tempValues >eValueLimit]
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 16)
categorytemp[] <- categoriesNames[categoryI]
eValueCategories <- c(eValueCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
eValueMainCategories <- c(eValueMainCategories, mainCategorytemp)
eValueValues <- c(eValueValues, tempValues)
eValueRefValues <- c(eValueRefValues, median(tempValues, na.rm = T))

tempValues <- -scores17$E.Value
tempValues <- tempValues[scores17$Decoy == 0 & tempValues >eValueLimit]
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 17)
categorytemp[] <- categoriesNames[categoryI]
eValueCategories <- c(eValueCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
eValueMainCategories <- c(eValueMainCategories, mainCategorytemp)
eValueValues <- c(eValueValues, tempValues)
eValueRefValues <- c(eValueRefValues, median(tempValues, na.rm = T))

eValueCategoriesFactors <- factor(eValueCategories, levels = allCategoriesSorted)
eValueMainCategoriesFactors <- factor(eValueMainCategories, levels = sortedMainCategoriesNames)


# Plot the distribution of peptides per precursor

eValueHistogramPlot <- ggplot()
eValueHistogramPlot <- eValueHistogramPlot + geom_violin(aes(x=eValueCategoriesFactors, y=eValueValues, fill = eValueMainCategoriesFactors), scale = "area", na.rm = T)
eValueHistogramPlot <- eValueHistogramPlot + labs(x = "", y = "E-value [-log10]", fill="", col="")
eValueHistogramPlot <- eValueHistogramPlot + theme(axis.text.x = element_text(angle = 90, hjust = 1))
eValueHistogramPlot <- eValueHistogramPlot + guides(fill = F)
plot(eValueHistogramPlot)
