library(ggplot2)

##
#
# This script aggregates the results of the Onyase Figure script on PSM scores into a single figure.
#
##


# Set working directory to the R folder

# setwd("R")


# Set the categories according to the parameters table

categoriesNames <- c("Default","Isoforms","Trembl","Vertebrates", "Proteogenomics","4 mc","Semispecific","Variable Cmm","Phosphorylation","AB-Y","ABC-XYZ","MS1 0.5 Da","MS2 0.5 Da","MS1 MS2 0.5 Da","Isotope -4 +4 Da","Charge 1 to 4","Charge 1 to 6")
mainCategoriesNames <- c("Default","Database","Database","Database", "Database","Digestion","Digestion","Modifications","Modifications","Fragmentation","Fragmentation","Tolerances","Tolerances","Tolerances","Isotopes","Charges","Charges")
categoriesIndexes <- c(0,1,2,3,4,5,6,8,9,10,11,12,13,14,15,16,17)

categories <- data.frame(id = categoriesIndexes, mainCategroy = mainCategoriesNames, name = categoriesNames)


# Load the precursor histograms from the Onyase export

scores0 <- read.table("resources\\all_psms_0_filtered.psm", header = T, sep = " ", stringsAsFactors = F)
scores1 <- read.table("resources\\all_psms_1_filtered.psm", header = T, sep = " ", stringsAsFactors = F)
scores2 <- read.table("resources\\all_psms_2_filtered.psm", header = T, sep = " ", stringsAsFactors = F)
scores3 <- read.table("resources\\all_psms_3_filtered.psm", header = T, sep = " ", stringsAsFactors = F)
scores4 <- read.table("resources\\all_psms_4_filtered.psm", header = T, sep = " ", stringsAsFactors = F)
scores5 <- read.table("resources\\all_psms_5_filtered.psm", header = T, sep = " ", stringsAsFactors = F)
scores6 <- read.table("resources\\all_psms_6_filtered.psm", header = T, sep = " ", stringsAsFactors = F)
scores8 <- read.table("resources\\all_psms_8_filtered.psm", header = T, sep = " ", stringsAsFactors = F)
scores9 <- read.table("resources\\all_psms_9_filtered.psm", header = T, sep = " ", stringsAsFactors = F)
scores10 <- read.table("resources\\all_psms_10_filtered.psm", header = T, sep = " ", stringsAsFactors = F)
scores11 <- read.table("resources\\all_psms_11_filtered.psm", header = T, sep = " ", stringsAsFactors = F)
scores12 <- read.table("resources\\all_psms_12_filtered.psm", header = T, sep = " ", stringsAsFactors = F)
scores13 <- read.table("resources\\all_psms_13_filtered.psm", header = T, sep = " ", stringsAsFactors = F)
scores14 <- read.table("resources\\all_psms_14_filtered.psm", header = T, sep = " ", stringsAsFactors = F)
scores15 <- read.table("resources\\all_psms_15_filtered.psm", header = T, sep = " ", stringsAsFactors = F)
scores16 <- read.table("resources\\all_psms_16_filtered.psm", header = T, sep = " ", stringsAsFactors = F)
scores17 <- read.table("resources\\all_psms_17_filtered.psm", header = T, sep = " ", stringsAsFactors = F)


# Format the data for ggplot

scoreCategories <- c()
scoreMainCategories <- c()
scoreValues <- c()
scoreRefValues <- c()

tempValues <- scores0$HyperScore[]
tempValues <- log10(tempValues)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 0)
categorytemp[] <- categoriesNames[categoryI]
scoreCategories <- c(scoreCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
scoreMainCategories <- c(scoreMainCategories, mainCategorytemp)
scoreValues <- c(scoreValues, tempValues)
scoreRefValues <- c(scoreRefValues, median(tempValues, na.rm = T))

tempValues <- scores1$HyperScore[]
tempValues <- log10(tempValues)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 1)
categorytemp[] <- categoriesNames[categoryI]
scoreCategories <- c(scoreCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
scoreMainCategories <- c(scoreMainCategories, mainCategorytemp)
scoreValues <- c(scoreValues, tempValues)
scoreRefValues <- c(scoreRefValues, median(tempValues, na.rm = T))

tempValues <- scores2$HyperScore[]
tempValues <- log10(tempValues)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 2)
categorytemp[] <- categoriesNames[categoryI]
scoreCategories <- c(scoreCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
scoreMainCategories <- c(scoreMainCategories, mainCategorytemp)
scoreValues <- c(scoreValues, tempValues)
scoreRefValues <- c(scoreRefValues, median(tempValues, na.rm = T))

tempValues <- scores3$HyperScore[]
tempValues <- log10(tempValues)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 3)
categorytemp[] <- categoriesNames[categoryI]
scoreCategories <- c(scoreCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
scoreMainCategories <- c(scoreMainCategories, mainCategorytemp)
scoreValues <- c(scoreValues, tempValues)
scoreRefValues <- c(scoreRefValues, median(tempValues, na.rm = T))

tempValues <- scores4$HyperScore[]
tempValues <- log10(tempValues)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 4)
categorytemp[] <- categoriesNames[categoryI]
scoreCategories <- c(scoreCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
scoreMainCategories <- c(scoreMainCategories, mainCategorytemp)
scoreValues <- c(scoreValues, tempValues)
scoreRefValues <- c(scoreRefValues, median(tempValues, na.rm = T))

tempValues <- scores5$HyperScore[]
tempValues <- log10(tempValues)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 5)
categorytemp[] <- categoriesNames[categoryI]
scoreCategories <- c(scoreCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
scoreMainCategories <- c(scoreMainCategories, mainCategorytemp)
scoreValues <- c(scoreValues, tempValues)
scoreRefValues <- c(scoreRefValues, median(tempValues, na.rm = T))

tempValues <- scores6$HyperScore[]
tempValues <- log10(tempValues)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 6)
categorytemp[] <- categoriesNames[categoryI]
scoreCategories <- c(scoreCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
scoreMainCategories <- c(scoreMainCategories, mainCategorytemp)
scoreValues <- c(scoreValues, tempValues)
scoreRefValues <- c(scoreRefValues, median(tempValues, na.rm = T))

tempValues <- scores8$HyperScore[]
tempValues <- log10(tempValues)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 8)
categorytemp[] <- categoriesNames[categoryI]
scoreCategories <- c(scoreCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
scoreMainCategories <- c(scoreMainCategories, mainCategorytemp)
scoreValues <- c(scoreValues, tempValues)
scoreRefValues <- c(scoreRefValues, median(tempValues, na.rm = T))

tempValues <- scores9$HyperScore[]
tempValues <- log10(tempValues)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 9)
categorytemp[] <- categoriesNames[categoryI]
scoreCategories <- c(scoreCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
scoreMainCategories <- c(scoreMainCategories, mainCategorytemp)
scoreValues <- c(scoreValues, tempValues)
scoreRefValues <- c(scoreRefValues, median(tempValues, na.rm = T))

tempValues <- scores10$HyperScore[]
tempValues <- log10(tempValues)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 10)
categorytemp[] <- categoriesNames[categoryI]
scoreCategories <- c(scoreCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
scoreMainCategories <- c(scoreMainCategories, mainCategorytemp)
scoreValues <- c(scoreValues, tempValues)
scoreRefValues <- c(scoreRefValues, median(tempValues, na.rm = T))

tempValues <- scores11$HyperScore[]
tempValues <- log10(tempValues)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 11)
categorytemp[] <- categoriesNames[categoryI]
scoreCategories <- c(scoreCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
scoreMainCategories <- c(scoreMainCategories, mainCategorytemp)
scoreValues <- c(scoreValues, tempValues)
scoreRefValues <- c(scoreRefValues, median(tempValues, na.rm = T))

tempValues <- scores12$HyperScore[]
tempValues <- log10(tempValues)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 12)
categorytemp[] <- categoriesNames[categoryI]
scoreCategories <- c(scoreCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
scoreMainCategories <- c(scoreMainCategories, mainCategorytemp)
scoreValues <- c(scoreValues, tempValues)
scoreRefValues <- c(scoreRefValues, median(tempValues, na.rm = T))

tempValues <- scores13$HyperScore[]
tempValues <- log10(tempValues)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 13)
categorytemp[] <- categoriesNames[categoryI]
scoreCategories <- c(scoreCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
scoreMainCategories <- c(scoreMainCategories, mainCategorytemp)
scoreValues <- c(scoreValues, tempValues)
scoreRefValues <- c(scoreRefValues, median(tempValues, na.rm = T))

tempValues <- scores14$HyperScore[]
tempValues <- log10(tempValues)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 14)
categorytemp[] <- categoriesNames[categoryI]
scoreCategories <- c(scoreCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
scoreMainCategories <- c(scoreMainCategories, mainCategorytemp)
scoreValues <- c(scoreValues, tempValues)
scoreRefValues <- c(scoreRefValues, median(tempValues, na.rm = T))

tempValues <- scores15$HyperScore[]
tempValues <- log10(tempValues)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 15)
categorytemp[] <- categoriesNames[categoryI]
scoreCategories <- c(scoreCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
scoreMainCategories <- c(scoreMainCategories, mainCategorytemp)
scoreValues <- c(scoreValues, tempValues)
scoreRefValues <- c(scoreRefValues, median(tempValues, na.rm = T))

tempValues <- scores16$HyperScore[]
tempValues <- log10(tempValues)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 16)
categorytemp[] <- categoriesNames[categoryI]
scoreCategories <- c(scoreCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
scoreMainCategories <- c(scoreMainCategories, mainCategorytemp)
scoreValues <- c(scoreValues, tempValues)
scoreRefValues <- c(scoreRefValues, median(tempValues, na.rm = T))

tempValues <- scores17$HyperScore[]
tempValues <- log10(tempValues)
categorytemp <- character(length(tempValues))
categoryI <- which(categories$id == 17)
categorytemp[] <- categoriesNames[categoryI]
scoreCategories <- c(scoreCategories, categorytemp)
mainCategorytemp <- character(length(tempValues))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
scoreMainCategories <- c(scoreMainCategories, mainCategorytemp)
scoreValues <- c(scoreValues, tempValues)
scoreRefValues <- c(scoreRefValues, median(tempValues, na.rm = T))

categories$median <- scoreRefValues
sortedCategoriesNames <- categories[order(categories$median, categories$name), "name"]
scoreCategoriesFactors <- factor(precursorCategories, levels = sortedCategoriesNames)

medianValues <- categories$median
medianNames <- factor(categories$name)

# Plot the distribution of peptides per precursor

scoreHistogramPlot <- ggplot()
scoreHistogramPlot <- scoreHistogramPlot + geom_violin(aes(x=scoreCategoriesFactors, y=scoreValues, fill = scoreMainCategories), width = 1.4, na.rm = T)
scoreHistogramPlot <- scoreHistogramPlot + geom_boxplot(aes(x=scoreCategoriesFactors, y=scoreValues, fill = scoreMainCategories), width = 0.2, outlier.size = 0.8, na.rm = T)
scoreHistogramPlot <- scoreHistogramPlot + labs(x = "Category", y = "# Peptides per Precursor [log10]", fill="", col="")
scoreHistogramPlot <- scoreHistogramPlot + theme(axis.text.x = element_text(angle = 90, hjust = 1))
plot(scoreHistogramPlot)


# Plot the distribution of peptides per precursor

scoreHistogramPlot <- ggplot()
scoreHistogramPlot <- scoreHistogramPlot + geom_violin(aes(x=scoreCategoriesFactors, y=scoreValues, fill = scoreMainCategories), width = 1.4, na.rm = T)
scoreHistogramPlot <- scoreHistogramPlot + geom_point(aes(x=medianNames, y=medianValues), shape = 45, size = 6, na.rm = T)
scoreHistogramPlot <- scoreHistogramPlot + labs(x = "Category", y = "# Peptides per Precursor [log10]", fill="", col="")
scoreHistogramPlot <- scoreHistogramPlot + theme(axis.text.x = element_text(angle = 90, hjust = 1))
plot(scoreHistogramPlot)
