library(ggplot2)

##
#
# This script aggregates the results of the Onyase Figure script on PSM scores into a single figure.
#
##


# Load the precursor histograms from the Onyase export

scores0 <- read.table(gzfile("R/resources/best_psms_0.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
scores1 <- read.table(gzfile("R/resources/best_psms_1.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
scores2 <- read.table(gzfile("R/resources/best_psms_2.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
scores3 <- read.table(gzfile("R/resources/best_psms_3.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
scores5 <- read.table(gzfile("R/resources/best_psms_5.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
scores6 <- read.table(gzfile("R/resources/best_psms_6.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
scores8 <- read.table(gzfile("R/resources/best_psms_8.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
scores9 <- read.table(gzfile("R/resources/best_psms_9.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
scores10 <- read.table(gzfile("R/resources/best_psms_10.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
scores11 <- read.table(gzfile("R/resources/best_psms_11.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
scores13 <- read.table(gzfile("R/resources/best_psms_13.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
scores14 <- read.table(gzfile("R/resources/best_psms_14.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
scores15 <- read.table(gzfile("R/resources/best_psms_15.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
scores16 <- read.table(gzfile("R/resources/best_psms_16.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
scores17 <- read.table(gzfile("R/resources/best_psms_17.txt.gz"), header = T, sep = " ", stringsAsFactors = F)


# Format the data for ggplot

scoreCategories <- c()
scoreMainCategories <- c()
scoreValues <- c()
decoySeries <- c()
decoyMedianValue <- c()
decoyHighValue <- c()
decoyLowValue <- c()

scoreLimit <- 0

tempValues <- scores0$HyperScore
tempDecoys <- scores0$Decoy[tempValues >scoreLimit]
tempValues <- tempValues[tempValues >scoreLimit]
tempValues <- log10(tempValues)
decoySeries <- c(decoySeries, tempDecoys)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 0)
categorytemp[] <- categoriesNames[categoryI]
scoreCategories <- c(scoreCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
scoreMainCategories <- c(scoreMainCategories, mainCategorytemp)
scoreValues <- c(scoreValues, tempValues)
decoyMedianValue <- c(decoyMedianValue, median(tempValues[tempDecoys == 1], na.rm = T))
decoyHighValue <- c(decoyHighValue, quantile(tempValues[tempDecoys == 1], 0.75, na.rm = T, names = F))
decoyLowValue <- c(decoyLowValue, quantile(tempValues[tempDecoys == 1], 0.25, na.rm = T, names = F))

tempValues <- scores1$HyperScore
tempDecoys <- scores1$Decoy[tempValues >scoreLimit]
tempValues <- tempValues[tempValues >scoreLimit]
tempValues <- log10(tempValues)
decoySeries <- c(decoySeries, tempDecoys)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 1)
categorytemp[] <- categoriesNames[categoryI]
scoreCategories <- c(scoreCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
scoreMainCategories <- c(scoreMainCategories, mainCategorytemp)
scoreValues <- c(scoreValues, tempValues)
decoyMedianValue <- c(decoyMedianValue, median(tempValues[tempDecoys == 1], na.rm = T))
decoyHighValue <- c(decoyHighValue, quantile(tempValues[tempDecoys == 1], 0.75, na.rm = T, names = F))
decoyLowValue <- c(decoyLowValue, quantile(tempValues[tempDecoys == 1], 0.25, na.rm = T, names = F))

tempValues <- scores2$HyperScore
tempDecoys <- scores2$Decoy[tempValues >scoreLimit]
tempValues <- tempValues[tempValues >scoreLimit]
tempValues <- log10(tempValues)
decoySeries <- c(decoySeries, tempDecoys)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 2)
categorytemp[] <- categoriesNames[categoryI]
scoreCategories <- c(scoreCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
scoreMainCategories <- c(scoreMainCategories, mainCategorytemp)
scoreValues <- c(scoreValues, tempValues)
decoyMedianValue <- c(decoyMedianValue, median(tempValues[tempDecoys == 1], na.rm = T))
decoyHighValue <- c(decoyHighValue, quantile(tempValues[tempDecoys == 1], 0.75, na.rm = T, names = F))
decoyLowValue <- c(decoyLowValue, quantile(tempValues[tempDecoys == 1], 0.25, na.rm = T, names = F))

tempValues <- scores3$HyperScore
tempDecoys <- scores3$Decoy[tempValues >scoreLimit]
tempValues <- tempValues[tempValues >scoreLimit]
tempValues <- log10(tempValues)
decoySeries <- c(decoySeries, tempDecoys)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 3)
categorytemp[] <- categoriesNames[categoryI]
scoreCategories <- c(scoreCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
scoreMainCategories <- c(scoreMainCategories, mainCategorytemp)
scoreValues <- c(scoreValues, tempValues)
decoyMedianValue <- c(decoyMedianValue, median(tempValues[tempDecoys == 1], na.rm = T))
decoyHighValue <- c(decoyHighValue, quantile(tempValues[tempDecoys == 1], 0.75, na.rm = T, names = F))
decoyLowValue <- c(decoyLowValue, quantile(tempValues[tempDecoys == 1], 0.25, na.rm = T, names = F))

tempValues <- scores5$HyperScore
tempDecoys <- scores5$Decoy[tempValues >scoreLimit]
tempValues <- tempValues[tempValues >scoreLimit]
tempValues <- log10(tempValues)
decoySeries <- c(decoySeries, tempDecoys)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 5)
categorytemp[] <- categoriesNames[categoryI]
scoreCategories <- c(scoreCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
scoreMainCategories <- c(scoreMainCategories, mainCategorytemp)
scoreValues <- c(scoreValues, tempValues)
decoyMedianValue <- c(decoyMedianValue, median(tempValues[tempDecoys == 1], na.rm = T))
decoyHighValue <- c(decoyHighValue, quantile(tempValues[tempDecoys == 1], 0.75, na.rm = T, names = F))
decoyLowValue <- c(decoyLowValue, quantile(tempValues[tempDecoys == 1], 0.25, na.rm = T, names = F))

tempValues <- scores6$HyperScore
tempDecoys <- scores6$Decoy[tempValues >scoreLimit]
tempValues <- tempValues[tempValues >scoreLimit]
tempValues <- log10(tempValues)
decoySeries <- c(decoySeries, tempDecoys)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 6)
categorytemp[] <- categoriesNames[categoryI]
scoreCategories <- c(scoreCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
scoreMainCategories <- c(scoreMainCategories, mainCategorytemp)
scoreValues <- c(scoreValues, tempValues)
decoyMedianValue <- c(decoyMedianValue, median(tempValues[tempDecoys == 1], na.rm = T))
decoyHighValue <- c(decoyHighValue, quantile(tempValues[tempDecoys == 1], 0.75, na.rm = T, names = F))
decoyLowValue <- c(decoyLowValue, quantile(tempValues[tempDecoys == 1], 0.25, na.rm = T, names = F))

tempValues <- scores8$HyperScore
tempDecoys <- scores8$Decoy[tempValues >scoreLimit]
tempValues <- tempValues[tempValues >scoreLimit]
tempValues <- log10(tempValues)
decoySeries <- c(decoySeries, tempDecoys)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 8)
categorytemp[] <- categoriesNames[categoryI]
scoreCategories <- c(scoreCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
scoreMainCategories <- c(scoreMainCategories, mainCategorytemp)
scoreValues <- c(scoreValues, tempValues)
decoyMedianValue <- c(decoyMedianValue, median(tempValues[tempDecoys == 1], na.rm = T))
decoyHighValue <- c(decoyHighValue, quantile(tempValues[tempDecoys == 1], 0.75, na.rm = T, names = F))
decoyLowValue <- c(decoyLowValue, quantile(tempValues[tempDecoys == 1], 0.25, na.rm = T, names = F))

tempValues <- scores9$HyperScore
tempDecoys <- scores9$Decoy[tempValues >scoreLimit]
tempValues <- tempValues[tempValues >scoreLimit]
tempValues <- log10(tempValues)
decoySeries <- c(decoySeries, tempDecoys)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 9)
categorytemp[] <- categoriesNames[categoryI]
scoreCategories <- c(scoreCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
scoreMainCategories <- c(scoreMainCategories, mainCategorytemp)
scoreValues <- c(scoreValues, tempValues)
decoyMedianValue <- c(decoyMedianValue, median(tempValues[tempDecoys == 1], na.rm = T))
decoyHighValue <- c(decoyHighValue, quantile(tempValues[tempDecoys == 1], 0.75, na.rm = T, names = F))
decoyLowValue <- c(decoyLowValue, quantile(tempValues[tempDecoys == 1], 0.25, na.rm = T, names = F))

tempValues <- scores10$HyperScore
tempDecoys <- scores10$Decoy[tempValues >scoreLimit]
tempValues <- tempValues[tempValues >scoreLimit]
tempValues <- log10(tempValues)
decoySeries <- c(decoySeries, tempDecoys)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 10)
categorytemp[] <- categoriesNames[categoryI]
scoreCategories <- c(scoreCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
scoreMainCategories <- c(scoreMainCategories, mainCategorytemp)
scoreValues <- c(scoreValues, tempValues)
decoyMedianValue <- c(decoyMedianValue, median(tempValues[tempDecoys == 1], na.rm = T))
decoyHighValue <- c(decoyHighValue, quantile(tempValues[tempDecoys == 1], 0.75, na.rm = T, names = F))
decoyLowValue <- c(decoyLowValue, quantile(tempValues[tempDecoys == 1], 0.25, na.rm = T, names = F))

tempValues <- scores11$HyperScore
tempDecoys <- scores11$Decoy[tempValues >scoreLimit]
tempValues <- tempValues[tempValues >scoreLimit]
tempValues <- log10(tempValues)
decoySeries <- c(decoySeries, tempDecoys)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 11)
categorytemp[] <- categoriesNames[categoryI]
scoreCategories <- c(scoreCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
scoreMainCategories <- c(scoreMainCategories, mainCategorytemp)
scoreValues <- c(scoreValues, tempValues)
decoyMedianValue <- c(decoyMedianValue, median(tempValues[tempDecoys == 1], na.rm = T))
decoyHighValue <- c(decoyHighValue, quantile(tempValues[tempDecoys == 1], 0.75, na.rm = T, names = F))
decoyLowValue <- c(decoyLowValue, quantile(tempValues[tempDecoys == 1], 0.25, na.rm = T, names = F))

tempValues <- scores13$HyperScore
tempDecoys <- scores13$Decoy[tempValues >scoreLimit]
tempValues <- tempValues[tempValues >scoreLimit]
tempValues <- log10(tempValues)
decoySeries <- c(decoySeries, tempDecoys)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 13)
categorytemp[] <- categoriesNames[categoryI]
scoreCategories <- c(scoreCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
scoreMainCategories <- c(scoreMainCategories, mainCategorytemp)
scoreValues <- c(scoreValues, tempValues)
decoyMedianValue <- c(decoyMedianValue, median(tempValues[tempDecoys == 1], na.rm = T))
decoyHighValue <- c(decoyHighValue, quantile(tempValues[tempDecoys == 1], 0.75, na.rm = T, names = F))
decoyLowValue <- c(decoyLowValue, quantile(tempValues[tempDecoys == 1], 0.25, na.rm = T, names = F))

tempValues <- scores14$HyperScore
tempDecoys <- scores14$Decoy[tempValues >scoreLimit]
tempValues <- tempValues[tempValues >scoreLimit]
tempValues <- log10(tempValues)
decoySeries <- c(decoySeries, tempDecoys)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 14)
categorytemp[] <- categoriesNames[categoryI]
scoreCategories <- c(scoreCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
scoreMainCategories <- c(scoreMainCategories, mainCategorytemp)
scoreValues <- c(scoreValues, tempValues)
decoyMedianValue <- c(decoyMedianValue, median(tempValues[tempDecoys == 1], na.rm = T))
decoyHighValue <- c(decoyHighValue, quantile(tempValues[tempDecoys == 1], 0.75, na.rm = T, names = F))
decoyLowValue <- c(decoyLowValue, quantile(tempValues[tempDecoys == 1], 0.25, na.rm = T, names = F))

tempValues <- scores15$HyperScore
tempDecoys <- scores15$Decoy[tempValues >scoreLimit]
tempValues <- tempValues[tempValues >scoreLimit]
tempValues <- log10(tempValues)
decoySeries <- c(decoySeries, tempDecoys)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 15)
categorytemp[] <- categoriesNames[categoryI]
scoreCategories <- c(scoreCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
scoreMainCategories <- c(scoreMainCategories, mainCategorytemp)
scoreValues <- c(scoreValues, tempValues)
decoyMedianValue <- c(decoyMedianValue, median(tempValues[tempDecoys == 1], na.rm = T))
decoyHighValue <- c(decoyHighValue, quantile(tempValues[tempDecoys == 1], 0.75, na.rm = T, names = F))
decoyLowValue <- c(decoyLowValue, quantile(tempValues[tempDecoys == 1], 0.25, na.rm = T, names = F))

tempValues <- scores16$HyperScore
tempDecoys <- scores16$Decoy[tempValues >scoreLimit]
tempValues <- tempValues[tempValues >scoreLimit]
tempValues <- log10(tempValues)
decoySeries <- c(decoySeries, tempDecoys)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 16)
categorytemp[] <- categoriesNames[categoryI]
scoreCategories <- c(scoreCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
scoreMainCategories <- c(scoreMainCategories, mainCategorytemp)
scoreValues <- c(scoreValues, tempValues)
decoyMedianValue <- c(decoyMedianValue, median(tempValues[tempDecoys == 1], na.rm = T))
decoyHighValue <- c(decoyHighValue, quantile(tempValues[tempDecoys == 1], 0.75, na.rm = T, names = F))
decoyLowValue <- c(decoyLowValue, quantile(tempValues[tempDecoys == 1], 0.25, na.rm = T, names = F))

tempValues <- scores17$HyperScore
tempDecoys <- scores17$Decoy[tempValues >scoreLimit]
tempValues <- tempValues[tempValues >scoreLimit]
tempValues <- log10(tempValues)
decoySeries <- c(decoySeries, tempDecoys)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 17)
categorytemp[] <- categoriesNames[categoryI]
scoreCategories <- c(scoreCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
scoreMainCategories <- c(scoreMainCategories, mainCategorytemp)
scoreValues <- c(scoreValues, tempValues)
decoyMedianValue <- c(decoyMedianValue, median(tempValues[tempDecoys == 1], na.rm = T))
decoyHighValue <- c(decoyHighValue, quantile(tempValues[tempDecoys == 1], 0.75, na.rm = T, names = F))
decoyLowValue <- c(decoyLowValue, quantile(tempValues[tempDecoys == 1], 0.25, na.rm = T, names = F))

decoySeries <- ifelse(decoySeries == 0, "Target", "Decoy")
decoySeriesFactor <- factor(decoySeries, levels = c("Decoy", "Target"))

scoreCategoriesFactors <- factor(scoreCategories, levels = allCategoriesSorted)
scoreMainCategoriesFactors <- factor(scoreMainCategories, levels = sortedMainCategoriesNames)



# Plot the distribution of peptides per precursor

scoreHistogramPlot <- ggplot()
scoreHistogramPlot <- scoreHistogramPlot + geom_violin(aes(x=scoreCategoriesFactors[!is.na(decoySeries) & decoySeries == "Decoy"], y=scoreValues[!is.na(decoySeries) & decoySeries == "Decoy"], fill = scoreMainCategoriesFactors[!is.na(decoySeries) & decoySeries == "Decoy"]), scale = "area", na.rm = T)
scoreHistogramPlot <- scoreHistogramPlot + geom_point(aes(x=medianNames, y=decoyMedianValue), shape = 45, size = 6, na.rm = T)
scoreHistogramPlot <- scoreHistogramPlot + geom_point(aes(x=medianNames, y=decoyLowValue), shape = 45, size = 2, na.rm = T)
scoreHistogramPlot <- scoreHistogramPlot + geom_point(aes(x=medianNames, y=decoyHighValue), shape = 45, size = 2, na.rm = T)
scoreHistogramPlot <- scoreHistogramPlot + labs(x = "", y = "Hyperscore [log10]", fill="", col="")
scoreHistogramPlot <- scoreHistogramPlot + theme(axis.text.x = element_text(angle = 90, hjust = 1))
scoreHistogramPlot <- scoreHistogramPlot + guides(fill = F)
scoreHistogramPlot <- scoreHistogramPlot + scale_fill_brewer(palette="Pastel1")
plot(scoreHistogramPlot)
