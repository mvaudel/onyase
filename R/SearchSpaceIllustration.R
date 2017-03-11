library(ggplot2)

##
#
# This script aggregates the results of the Onyase Figure script on precursors into a single figure.
#
##


# Set the categories according to the parameters table

categoriesNames <- c("Default","Isoforms","Trembl","Vertebrates","4 mc","Semispecific","Variable Cmm","Phosphorylation","AB-Y","ABC-XYZ","MS2 0.5 Da","MS1&2 0.5 Da","Isotope -4 +4 Da","Charge 1 to 4","Charge 1 to 6")
mainCategoriesNames <- c("Default","Database","Database","Database","Digestion","Digestion","Modifications","Modifications","Fragmentation","Fragmentation","Tolerances","Tolerances","Isotopes","Charges","Charges")
categoriesIndexes <- c(0,1,2,3,5,6,8,9,10,11,13,14,15,16,17)

categories <- data.frame(id = categoriesIndexes, mainCategroy = mainCategoriesNames, name = categoriesNames, stringsAsFactors = F)


# Load the precursor histograms from the Onyase export

precursorHistogram0 <- read.table(gzfile("R/resources/precursor_0.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram1 <- read.table(gzfile("R/resources/precursor_1.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram2 <- read.table(gzfile("R/resources/precursor_2.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram3 <- read.table(gzfile("R/resources/precursor_3.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram5 <- read.table(gzfile("R/resources/precursor_5.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram6 <- read.table(gzfile("R/resources/precursor_6.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram8 <- read.table(gzfile("R/resources/precursor_8.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram9 <- read.table(gzfile("R/resources/precursor_9.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram10 <- read.table(gzfile("R/resources/precursor_10.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram11 <- read.table(gzfile("R/resources/precursor_11.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram13 <- read.table(gzfile("R/resources/precursor_13.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram14 <- read.table(gzfile("R/resources/precursor_14.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram15 <- read.table(gzfile("R/resources/precursor_15.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram16 <- read.table(gzfile("R/resources/precursor_16.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram17 <- read.table(gzfile("R/resources/precursor_17.txt.gz"), header = T, sep = " ", stringsAsFactors = F)


# Format the data for ggplot

precursorCategories <- c()
precursorMainCategories <- c()
precursorValues <- c()
precursorRefValue <- c()
precursorHighValue <- c()
precursorLowValue <- c()

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
precursorHighValue <- c(precursorHighValue, quantile(tempValues, 0.75, na.rm = T, names = F))
precursorLowValue <- c(precursorLowValue, quantile(tempValues, 0.25, na.rm = T, names = F))

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
precursorHighValue <- c(precursorHighValue, quantile(tempValues, 0.75, na.rm = T, names = F))
precursorLowValue <- c(precursorLowValue, quantile(tempValues, 0.25, na.rm = T, names = F))

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
precursorHighValue <- c(precursorHighValue, quantile(tempValues, 0.75, na.rm = T, names = F))
precursorLowValue <- c(precursorLowValue, quantile(tempValues, 0.25, na.rm = T, names = F))

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
precursorHighValue <- c(precursorHighValue, quantile(tempValues, 0.75, na.rm = T, names = F))
precursorLowValue <- c(precursorLowValue, quantile(tempValues, 0.25, na.rm = T, names = F))

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
precursorHighValue <- c(precursorHighValue, quantile(tempValues, 0.75, na.rm = T, names = F))
precursorLowValue <- c(precursorLowValue, quantile(tempValues, 0.25, na.rm = T, names = F))

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
precursorHighValue <- c(precursorHighValue, quantile(tempValues, 0.75, na.rm = T, names = F))
precursorLowValue <- c(precursorLowValue, quantile(tempValues, 0.25, na.rm = T, names = F))

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
precursorHighValue <- c(precursorHighValue, quantile(tempValues, 0.75, na.rm = T, names = F))
precursorLowValue <- c(precursorLowValue, quantile(tempValues, 0.25, na.rm = T, names = F))

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
precursorHighValue <- c(precursorHighValue, quantile(tempValues, 0.75, na.rm = T, names = F))
precursorLowValue <- c(precursorLowValue, quantile(tempValues, 0.25, na.rm = T, names = F))

tempValues <- precursorHistogram10$nPeptides[]
tempValues <- tempValues[tempValues > 0]
tempValues <- log10(tempValues)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 10)
categorytemp[] <- categoriesNames[categoryI]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
precursorValues <- c(precursorValues, tempValues)
precursorRefValue <- c(precursorRefValue, median(tempValues, na.rm = T))
precursorHighValue <- c(precursorHighValue, quantile(tempValues, 0.75, na.rm = T, names = F))
precursorLowValue <- c(precursorLowValue, quantile(tempValues, 0.25, na.rm = T, names = F))

tempValues <- precursorHistogram11$nPeptides[]
tempValues <- tempValues[tempValues > 0]
tempValues <- log10(tempValues)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 11)
categorytemp[] <- categoriesNames[categoryI]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
precursorValues <- c(precursorValues, tempValues)
precursorRefValue <- c(precursorRefValue, median(tempValues, na.rm = T))
precursorHighValue <- c(precursorHighValue, quantile(tempValues, 0.75, na.rm = T, names = F))
precursorLowValue <- c(precursorLowValue, quantile(tempValues, 0.25, na.rm = T, names = F))

tempValues <- precursorHistogram13$nPeptides[]
tempValues <- tempValues[tempValues > 0]
tempValues <- log10(tempValues)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 13)
categorytemp[] <- categoriesNames[categoryI]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
precursorValues <- c(precursorValues, tempValues)
precursorRefValue <- c(precursorRefValue, median(tempValues, na.rm = T))
precursorHighValue <- c(precursorHighValue, quantile(tempValues, 0.75, na.rm = T, names = F))
precursorLowValue <- c(precursorLowValue, quantile(tempValues, 0.25, na.rm = T, names = F))

tempValues <- precursorHistogram14$nPeptides[]
tempValues <- tempValues[tempValues > 0]
tempValues <- log10(tempValues)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 14)
categorytemp[] <- categoriesNames[categoryI]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
precursorValues <- c(precursorValues, tempValues)
precursorRefValue <- c(precursorRefValue, median(tempValues, na.rm = T))
precursorHighValue <- c(precursorHighValue, quantile(tempValues, 0.75, na.rm = T, names = F))
precursorLowValue <- c(precursorLowValue, quantile(tempValues, 0.25, na.rm = T, names = F))

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
precursorHighValue <- c(precursorHighValue, quantile(tempValues, 0.75, na.rm = T, names = F))
precursorLowValue <- c(precursorLowValue, quantile(tempValues, 0.25, na.rm = T, names = F))

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
precursorHighValue <- c(precursorHighValue, quantile(tempValues, 0.75, na.rm = T, names = F))
precursorLowValue <- c(precursorLowValue, quantile(tempValues, 0.25, na.rm = T, names = F))

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
precursorHighValue <- c(precursorHighValue, quantile(tempValues, 0.75, na.rm = T, names = F))
precursorLowValue <- c(precursorLowValue, quantile(tempValues, 0.25, na.rm = T, names = F))

categories$median <- precursorRefValue
sortedCategoriesNames <- categories[order(categories$median, categories$name), "name"]
allCategoriesSorted <- c("Default", "AB-Y", "ABC-XYZ", "MS2 0.5 Da")
for (i in 1:length(sortedCategoriesNames)) {
  category <- sortedCategoriesNames[i]
  if (!category %in% allCategoriesSorted) {
    allCategoriesSorted <- c(allCategoriesSorted, category)
    if (category == "MS1 0.5 Da") {
      allCategoriesSorted <- c(allCategoriesSorted,"MS1 MS2 0.5 Da")
    }
  }
}
precursorCategoriesFactors <- factor(precursorCategories, levels = allCategoriesSorted)

sortedMainCategoriesNames <- unique(mainCategoriesNames)
precursorMainCategoriesFactors <- factor(precursorMainCategories, levels = sortedMainCategoriesNames)

medianValues <- categories$median
medianNames <- factor(categories$name)


# Create a plot of the distribution of peptides per precursor

precursorHistogramPlot <- ggplot()
precursorHistogramPlot <- precursorHistogramPlot + geom_violin(aes(x=precursorCategoriesFactors, y=precursorValues, fill = precursorMainCategoriesFactors), scale = "width", width = 0.6, na.rm = T)
precursorHistogramPlot <- precursorHistogramPlot + geom_point(aes(x=medianNames, y=medianValues), shape = 45, size = 6, na.rm = T)
precursorHistogramPlot <- precursorHistogramPlot + geom_point(aes(x=medianNames, y=precursorLowValue), shape = 45, size = 2, na.rm = T)
precursorHistogramPlot <- precursorHistogramPlot + geom_point(aes(x=medianNames, y=precursorHighValue), shape = 45, size = 2, na.rm = T)
precursorHistogramPlot <- precursorHistogramPlot + labs(x = "", y = "# Peptides per Precursor [log10]", fill="", col="")
precursorHistogramPlot <- precursorHistogramPlot + theme(axis.text.x = element_text(angle = 90, hjust = 1), legend.position = "top")
precursorHistogramPlot <- precursorHistogramPlot + scale_fill_brewer(palette="Pastel1")


# Plot to file

png(filename = "R/results/searchSpace.png", width = 800, height = 600)
plot(precursorHistogramPlot)
dev.off()
