library(ggplot2)

##
#
# This script aggregates the results of the Onyase Figure script into a single figure 
#
##


# Set working directory to the R folder

# setwd("R")


# Set the categories according to the parameters table

# categoriesNames <- c("Default","Isoforms","Trembl","Vertebrates", "proteogenomics","4 mc","semispecific", "unspecific","Variable Cmm","Phosphorylation","AB-Y","ABC-XYZ","MS1 0.5 Da","MS2 0.5 Da","MS1 MS2 0.5 Da","Isotope -4 +4 Da","Charge 1 to 4","Charge 1 to 6")
# categoriesIndexes <- c(0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17)

categoriesNames <- c("Default","Isoforms","Trembl","Vertebrates", "proteogenomics","4 mc","semispecific", "unspecific","Variable Cmm","Phosphorylation","MS1 0.5 Da","Isotope -4 +4 Da","Charge 1 to 4","Charge 1 to 6")
categoriesIndexes <- c(0,1,2,3,4,5,6,7,8,9,12,15,16,17)

categories <- data.frame(id = categoriesIndexes, name = categoriesNames)


# Load the precursor histograms from the Onyase export

precursorHistogram0 <- read.table(gzfile("resources\\precursor_0.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram1 <- read.table(gzfile("resources\\precursor_1.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram2 <- read.table(gzfile("resources\\precursor_2.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram3 <- read.table(gzfile("resources\\precursor_3.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
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

categorytemp <- character(length(precursorHistogram0$nPeptides[]))
categorytemp[] <- categoriesNames[which(categories$id == 0)]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(precursorHistogram0$nPeptides[]))
mainCategorytemp[] <- "Default"
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
tempValues <- log10(precursorHistogram0$nPeptides)
# tempValues <- precursorHistogram0$nPeptides
precursorValues <- c(precursorValues, tempValues)
precursorRefValue <- c(precursorRefValue, quantile(tempValues, 0.5, na.rm = T))

categorytemp <- character(length(precursorHistogram1$nPeptides[]))
categorytemp[] <- categoriesNames[which(categories$id == 1)]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(precursorHistogram1$nPeptides[]))
mainCategorytemp[] <- "Database"
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
tempValues <- log10(precursorHistogram1$nPeptides)
# tempValues <- precursorHistogram1$nPeptides
precursorValues <- c(precursorValues, tempValues)
precursorRefValue <- c(precursorRefValue, quantile(tempValues, 0.5, na.rm = T))

categorytemp <- character(length(precursorHistogram2$nPeptides[]))
categorytemp[] <- categoriesNames[which(categories$id == 2)]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(precursorHistogram2$nPeptides[]))
mainCategorytemp[] <- "Database"
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
tempValues <- log10(precursorHistogram2$nPeptides)
# tempValues <- precursorHistogram2$nPeptides
precursorValues <- c(precursorValues, tempValues)
precursorRefValue <- c(precursorRefValue, quantile(tempValues, 0.5, na.rm = T))

categorytemp <- character(length(precursorHistogram3$nPeptides[]))
categorytemp[] <- categoriesNames[which(categories$id == 3)]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(precursorHistogram3$nPeptides[]))
mainCategorytemp[] <- "Database"
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
tempValues <- log10(precursorHistogram3$nPeptides)
# tempValues <- precursorHistogram3$nPeptides
precursorValues <- c(precursorValues, tempValues)
precursorRefValue <- c(precursorRefValue, quantile(tempValues, 0.5, na.rm = T))

precursorRefValue <- c(precursorRefValue, 0)

categorytemp <- character(length(precursorHistogram5$nPeptides[]))
categorytemp[] <- categoriesNames[which(categories$id == 5)]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(precursorHistogram5$nPeptides[]))
mainCategorytemp[] <- "Digestion"
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
tempValues <- log10(precursorHistogram5$nPeptides)
# tempValues <- precursorHistogram5$nPeptides
precursorValues <- c(precursorValues, tempValues)
precursorRefValue <- c(precursorRefValue, quantile(tempValues, 0.5, na.rm = T))

categorytemp <- character(length(precursorHistogram6$nPeptides[]))
categorytemp[] <- categoriesNames[which(categories$id == 6)]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(precursorHistogram6$nPeptides[]))
mainCategorytemp[] <- "Digestion"
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
tempValues <- log10(precursorHistogram6$nPeptides)
# tempValues <- precursorHistogram6$nPeptides
precursorValues <- c(precursorValues, tempValues)
precursorRefValue <- c(precursorRefValue, quantile(tempValues, 0.5, na.rm = T))

precursorRefValue <- c(precursorRefValue, 0)

categorytemp <- character(length(precursorHistogram8$nPeptides[]))
categorytemp[] <- categoriesNames[which(categories$id == 8)]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(precursorHistogram8$nPeptides[]))
mainCategorytemp[] <- "Modifications"
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
tempValues <- log10(precursorHistogram8$nPeptides)
# tempValues <- precursorHistogram8$nPeptides
precursorValues <- c(precursorValues, tempValues)
precursorRefValue <- c(precursorRefValue, quantile(tempValues, 0.5, na.rm = T))

categorytemp <- character(length(precursorHistogram9$nPeptides[]))
categorytemp[] <- categoriesNames[which(categories$id == 9)]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(precursorHistogram9$nPeptides[]))
mainCategorytemp[] <- "Modifications"
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
tempValues <- log10(precursorHistogram9$nPeptides)
# tempValues <- precursorHistogram9$nPeptides
precursorValues <- c(precursorValues, tempValues)
precursorRefValue <- c(precursorRefValue, quantile(tempValues, 0.5, na.rm = T))

categorytemp <- character(length(precursorHistogram12$nPeptides[]))
categorytemp[] <- categoriesNames[which(categories$id == 12)]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(precursorHistogram12$nPeptides[]))
mainCategorytemp[] <- "Tolerances"
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
tempValues <- log10(precursorHistogram12$nPeptides)
# tempValues <- precursorHistogram12$nPeptides
precursorValues <- c(precursorValues, tempValues)
precursorRefValue <- c(precursorRefValue, quantile(tempValues, 0.5, na.rm = T))

categorytemp <- character(length(precursorHistogram15$nPeptides[]))
categorytemp[] <- categoriesNames[which(categories$id == 15)]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(precursorHistogram15$nPeptides[]))
mainCategorytemp[] <- "Isotopes"
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
tempValues <- log10(precursorHistogram15$nPeptides)
# tempValues <- precursorHistogram15$nPeptides
precursorValues <- c(precursorValues, tempValues)
precursorRefValue <- c(precursorRefValue, quantile(tempValues, 0.5, na.rm = T))

categorytemp <- character(length(precursorHistogram16$nPeptides[]))
categorytemp[] <- categoriesNames[which(categories$id == 16)]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(precursorHistogram16$nPeptides[]))
mainCategorytemp[] <- "Charges"
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
tempValues <- log10(precursorHistogram16$nPeptides)
# tempValues <- precursorHistogram16$nPeptides
precursorValues <- c(precursorValues, tempValues)
precursorRefValue <- c(precursorRefValue, quantile(tempValues, 0.5, na.rm = T))

categorytemp <- character(length(precursorHistogram17$nPeptides[]))
categorytemp[] <- categoriesNames[which(categories$id == 17)]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(precursorHistogram17$nPeptides[]))
mainCategorytemp[] <- "Charges"
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
tempValues <- log10(precursorHistogram17$nPeptides)
# tempValues <- precursorHistogram17$nPeptides
precursorValues <- c(precursorValues, tempValues)
precursorRefValue <- c(precursorRefValue, quantile(tempValues, 0.5, na.rm = T))

categories$median <- precursorRefValue
sortedCategoriesNames <- categories[order(categories$median, categories$name), "name"]
precursorCategoriesFactors <- factor(precursorCategories, levels = sortedCategoriesNames)


# Plot the distribution of peptides per precursor

precursorHistogramPlot <- ggplot()
precursorHistogramPlot <- precursorHistogramPlot + geom_violin(aes(x=precursorCategoriesFactors, y=precursorValues, fill = precursorMainCategories), na.rm = T)
precursorHistogramPlot <- precursorHistogramPlot + geom_boxplot(aes(x=precursorCategoriesFactors, y=precursorValues, fill = precursorMainCategories), width = 0.2, outlier.size = 1, na.rm = T)
precursorHistogramPlot <- precursorHistogramPlot + labs(x = "Category", y = "# Peptides per Precursor [log10]", fill="", col="")
precursorHistogramPlot <- precursorHistogramPlot + theme(axis.text.x = element_text(angle = 90, hjust = 1))
plot(precursorHistogramPlot)
