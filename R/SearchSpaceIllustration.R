library(ggplot2)

##
#
# This script aggregates the results of the Onyase Figure script on precursors into a single figure.
#
##


# Set working directory to the R folder

# setwd("R")


# Set the categories according to the parameters table

categoriesNames <- c("Default","Isoforms","Trembl","Vertebrates", "Proteogenomics","4 mc","Semispecific","Variable Cmm","Phosphorylation","MS1 0.5 Da","Isotope -4 +4 Da","Charge 1 to 4","Charge 1 to 6")
mainCategoriesNames <- c("Default","Database","Database","Database", "Database","Digestion","Digestion","Modifications","Modifications","Tolerances","Isotopes","Charges","Charges")
categoriesIndexes <- c(0,1,2,3,4,5,6,8,9,12,15,16,17)

categories <- data.frame(id = categoriesIndexes, mainCategroy = mainCategoriesNames, name = categoriesNames, stringsAsFactors = F)


# Load the precursor histograms from the Onyase export

precursorHistogram0 <- read.table(gzfile("resources\\precursor_0.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram1 <- read.table(gzfile("resources\\precursor_1.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram2 <- read.table(gzfile("resources\\precursor_2.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram3 <- read.table(gzfile("resources\\precursor_3.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram4 <- read.table(gzfile("resources\\precursor_4.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram5 <- read.table(gzfile("resources\\precursor_5.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram6 <- read.table(gzfile("resources\\precursor_6.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram8 <- read.table(gzfile("resources\\precursor_8.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram9 <- read.table(gzfile("resources\\precursor_9.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram12 <- read.table(gzfile("resources\\precursor_12.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram15 <- read.table(gzfile("resources\\precursor_15.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram16 <- read.table(gzfile("resources\\precursor_16.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram17 <- read.table(gzfile("resources\\precursor_17.txt.gz"), header = T, sep = " ", stringsAsFactors = F)


# Format the data for ggplot

precursorCategories <- c()
precursorMainCategories <- c()
precursorValues <- c()
precursorRefValue <- c()

tempValues <- precursorHistogram0$nPeptides[]
tempValues <- tempValues[tempValues > 0]
tempValues <- log10(tempValues)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 0)
categorytemp[] <- categoriesNames[categoryI]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
precursorValues <- c(precursorValues, tempValues)
precursorRefValue <- c(precursorRefValue, median(tempValues, na.rm = T))

tempValues <- precursorHistogram1$nPeptides[]
tempValues <- tempValues[tempValues > 0]
tempValues <- log10(tempValues)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 1)
categorytemp[] <- categoriesNames[categoryI]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
precursorValues <- c(precursorValues, tempValues)
precursorRefValue <- c(precursorRefValue, median(tempValues, na.rm = T))

tempValues <- precursorHistogram2$nPeptides[]
tempValues <- tempValues[tempValues > 0]
tempValues <- log10(tempValues)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 2)
categorytemp[] <- categoriesNames[categoryI]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
precursorValues <- c(precursorValues, tempValues)
precursorRefValue <- c(precursorRefValue, median(tempValues, na.rm = T))

tempValues <- precursorHistogram3$nPeptides[]
tempValues <- tempValues[tempValues > 0]
tempValues <- log10(tempValues)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 3)
categorytemp[] <- categoriesNames[categoryI]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
precursorValues <- c(precursorValues, tempValues)
precursorRefValue <- c(precursorRefValue, median(tempValues, na.rm = T))

tempValues <- precursorHistogram4$nPeptides[]
tempValues <- tempValues[tempValues > 0]
tempValues <- log10(tempValues)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 4)
categorytemp[] <- categoriesNames[categoryI]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
precursorValues <- c(precursorValues, tempValues)
precursorRefValue <- c(precursorRefValue, median(tempValues, na.rm = T))

tempValues <- precursorHistogram5$nPeptides[]
tempValues <- tempValues[tempValues > 0]
tempValues <- log10(tempValues)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 5)
categorytemp[] <- categoriesNames[categoryI]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
precursorValues <- c(precursorValues, tempValues)
precursorRefValue <- c(precursorRefValue, median(tempValues, na.rm = T))

tempValues <- precursorHistogram6$nPeptides[]
tempValues <- tempValues[tempValues > 0]
tempValues <- log10(tempValues)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 6)
categorytemp[] <- categoriesNames[categoryI]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
precursorValues <- c(precursorValues, tempValues)
precursorRefValue <- c(precursorRefValue, median(tempValues, na.rm = T))

tempValues <- precursorHistogram8$nPeptides[]
tempValues <- tempValues[tempValues > 0]
tempValues <- log10(tempValues)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 8)
categorytemp[] <- categoriesNames[categoryI]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
precursorValues <- c(precursorValues, tempValues)
precursorRefValue <- c(precursorRefValue, median(tempValues, na.rm = T))

tempValues <- precursorHistogram9$nPeptides[]
tempValues <- tempValues[tempValues > 0]
tempValues <- log10(tempValues)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 9)
categorytemp[] <- categoriesNames[categoryI]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
precursorValues <- c(precursorValues, tempValues)
precursorRefValue <- c(precursorRefValue, median(tempValues, na.rm = T))

tempValues <- precursorHistogram12$nPeptides[]
tempValues <- tempValues[tempValues > 0]
tempValues <- log10(tempValues)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 12)
categorytemp[] <- categoriesNames[categoryI]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
precursorValues <- c(precursorValues, tempValues)
precursorRefValue <- c(precursorRefValue, median(tempValues, na.rm = T))

tempValues <- precursorHistogram15$nPeptides[]
tempValues <- tempValues[tempValues > 0]
tempValues <- log10(tempValues)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 15)
categorytemp[] <- categoriesNames[categoryI]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
precursorValues <- c(precursorValues, tempValues)
precursorRefValue <- c(precursorRefValue, median(tempValues, na.rm = T))

tempValues <- precursorHistogram16$nPeptides[]
tempValues <- tempValues[tempValues > 0]
tempValues <- log10(tempValues)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 16)
categorytemp[] <- categoriesNames[categoryI]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
precursorValues <- c(precursorValues, tempValues)
precursorRefValue <- c(precursorRefValue, median(tempValues, na.rm = T))

tempValues <- precursorHistogram17$nPeptides[]
tempValues <- tempValues[tempValues > 0]
tempValues <- log10(tempValues)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 17)
categorytemp[] <- categoriesNames[categoryI]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
precursorValues <- c(precursorValues, tempValues)
precursorRefValue <- c(precursorRefValue, median(tempValues, na.rm = T))

categories$median <- precursorRefValue
sortedCategoriesNames <- categories[order(categories$median, categories$name), "name"]
sortedCategoriesNames <- sortedCategoriesNames[sortedCategoriesNames != "Default"]
sortedCategoriesNames <- c("Default", sortedCategoriesNames)
precursorCategoriesFactors <- factor(precursorCategories, levels = sortedCategoriesNames)

medianValues <- categories$median
medianNames <- factor(categories$name)

# Plot the distribution of peptides per precursor

precursorHistogramPlot <- ggplot()
precursorHistogramPlot <- precursorHistogramPlot + geom_violin(aes(x=precursorCategoriesFactors, y=precursorValues, fill = precursorMainCategories), width = 1.4, na.rm = T)
precursorHistogramPlot <- precursorHistogramPlot + geom_boxplot(aes(x=precursorCategoriesFactors, y=precursorValues, fill = precursorMainCategories), width = 0.2, outlier.size = 0.8, na.rm = T)
precursorHistogramPlot <- precursorHistogramPlot + labs(x = "Category", y = "# Peptides per Precursor [log10]", fill="", col="")
precursorHistogramPlot <- precursorHistogramPlot + theme(axis.text.x = element_text(angle = 90, hjust = 1))
plot(precursorHistogramPlot)


# Plot the distribution of peptides per precursor

precursorHistogramPlot <- ggplot()
precursorHistogramPlot <- precursorHistogramPlot + geom_violin(aes(x=precursorCategoriesFactors, y=precursorValues, fill = precursorMainCategories), width = 1.4, na.rm = T)
precursorHistogramPlot <- precursorHistogramPlot + geom_point(aes(x=medianNames, y=medianValues), shape = 45, size = 6, na.rm = T)
precursorHistogramPlot <- precursorHistogramPlot + labs(x = "Category", y = "# Peptides per Precursor [log10]", fill="", col="")
precursorHistogramPlot <- precursorHistogramPlot + theme(axis.text.x = element_text(angle = 90, hjust = 1))
plot(precursorHistogramPlot)
