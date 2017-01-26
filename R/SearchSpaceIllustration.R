library(ggplot2)

##
#
# This script aggregates the results of the Onyase Figure script into a single figure 
#
##


# Set working directory to the R folder

setwd("R")


# Set the categories according to the parameters table

categoriesNames <- c("Default","Isoforms","Trembl","Vertebrates", "proteogenomics","4mc","semispecific", "unspecific","Variable Cmm","Phosphorylation","AB-Y","ABC-XYZ","MS1 0.5 Da","MS2 0.5 Da","MS1 MS2 0.5 Da","-4 +4 Da","1 to 4","1 to 6")
categoriesIndexes <- c(0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17)
categories <- data.frame(id = categoriesIndexes, name = categoriesNames)


# Load the precursor histograms from the Onyase export

precursorHistogram0 <- read.table(gzfile("resources\\precursor_0.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram0 <- precursorHistogram0[1:1000,]
precursorHistogram1 <- read.table(gzfile("resources\\precursor_1.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram1 <- precursorHistogram1[1:1000,]
precursorHistogram2 <- read.table(gzfile("resources\\precursor_2.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram2 <- precursorHistogram2[1:1000,]
precursorHistogram3 <- read.table(gzfile("resources\\precursor_3.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram3 <- precursorHistogram3[1:1000,]
precursorHistogram5 <- read.table(gzfile("resources\\precursor_5.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram5 <- precursorHistogram5[1:1000,]
# precursorHistogram6 <- read.table(gzfile("resources\\precursor_6.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram6 <- precursorHistogram0
precursorHistogram6 <- precursorHistogram6[1:1000,]
precursorHistogram8 <- read.table(gzfile("resources\\precursor_8.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram8 <- precursorHistogram8[1:1000,]
precursorHistogram9 <- read.table(gzfile("resources\\precursor_9.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram9 <- precursorHistogram9[1:1000,]
precursorHistogram10 <- read.table(gzfile("resources\\precursor_10.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram10 <- precursorHistogram10[1:1000,]
precursorHistogram11 <- read.table(gzfile("resources\\precursor_11.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram11 <- precursorHistogram11[1:1000,]
precursorHistogram12 <- read.table(gzfile("resources\\precursor_12.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram12 <- precursorHistogram0
precursorHistogram12 <- precursorHistogram12[1:1000,]
precursorHistogram13 <- read.table(gzfile("resources\\precursor_13.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram13 <- precursorHistogram13[1:1000,]
precursorHistogram14 <- read.table(gzfile("resources\\precursor_14.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram14 <- precursorHistogram0
precursorHistogram14 <- precursorHistogram14[1:1000,]
precursorHistogram15 <- read.table(gzfile("resources\\precursor_15.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram15 <- precursorHistogram15[1:1000,]
precursorHistogram16 <- read.table(gzfile("resources\\precursor_16.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram16 <- precursorHistogram16[1:1000,]
precursorHistogram17 <- read.table(gzfile("resources\\precursor_17.txt.gz"), header = T, sep = " ", stringsAsFactors = F)
precursorHistogram17 <- precursorHistogram17[1:1000,]


# Format the data for ggplot

precursorCategories <- c()
precursorMainCategories <- c()
precursorValues <- c()
precursorMedians <- c()

categorytemp <- character(length(precursorHistogram0$nPeptides[]))
categorytemp[] <- categoriesNames[1]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(precursorHistogram0$nPeptides[]))
mainCategorytemp[] <- "Default"
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
logValues <- log10(precursorHistogram0$nPeptides)
precursorValues <- c(precursorValues, logValues)
precursorMedians <- c(precursorMedians, median(logValues))

categorytemp <- character(length(precursorHistogram1$nPeptides[]))
categorytemp[] <- categoriesNames[2]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(precursorHistogram1$nPeptides[]))
mainCategorytemp[] <- "Database"
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
logValues <- log10(precursorHistogram1$nPeptides)
precursorValues <- c(precursorValues, logValues)
precursorMedians <- c(precursorMedians, median(logValues))

categorytemp <- character(length(precursorHistogram2$nPeptides[]))
categorytemp[] <- categoriesNames[3]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(precursorHistogram2$nPeptides[]))
mainCategorytemp[] <- "Database"
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
logValues <- log10(precursorHistogram2$nPeptides)
precursorValues <- c(precursorValues, logValues)
precursorMedians <- c(precursorMedians, median(logValues))

categorytemp <- character(length(precursorHistogram3$nPeptides[]))
categorytemp[] <- categoriesNames[4]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(precursorHistogram3$nPeptides[]))
mainCategorytemp[] <- "Database"
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
logValues <- log10(precursorHistogram3$nPeptides)
precursorValues <- c(precursorValues, logValues)
precursorMedians <- c(precursorMedians, median(logValues))

precursorMedians <- c(precursorMedians, 0)

categorytemp <- character(length(precursorHistogram5$nPeptides[]))
categorytemp[] <- categoriesNames[6]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(precursorHistogram5$nPeptides[]))
mainCategorytemp[] <- "Digestion"
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
logValues <- log10(precursorHistogram5$nPeptides)
precursorValues <- c(precursorValues, logValues)
precursorMedians <- c(precursorMedians, median(logValues))

categorytemp <- character(length(precursorHistogram6$nPeptides[]))
categorytemp[] <- categoriesNames[7]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(precursorHistogram6$nPeptides[]))
mainCategorytemp[] <- "Digestion"
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
logValues <- log10(precursorHistogram6$nPeptides)
precursorValues <- c(precursorValues, logValues)
precursorMedians <- c(precursorMedians, median(logValues))

precursorMedians <- c(precursorMedians, 0)

categorytemp <- character(length(precursorHistogram8$nPeptides[]))
categorytemp[] <- categoriesNames[9]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(precursorHistogram8$nPeptides[]))
mainCategorytemp[] <- "Modifications"
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
logValues <- log10(precursorHistogram8$nPeptides)
precursorValues <- c(precursorValues, logValues)
precursorMedians <- c(precursorMedians, median(logValues))

categorytemp <- character(length(precursorHistogram9$nPeptides[]))
categorytemp[] <- categoriesNames[10]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(precursorHistogram9$nPeptides[]))
mainCategorytemp[] <- "Modifications"
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
logValues <- log10(precursorHistogram9$nPeptides)
precursorValues <- c(precursorValues, logValues)
precursorMedians <- c(precursorMedians, median(logValues))

categorytemp <- character(length(precursorHistogram10$nPeptides[]))
categorytemp[] <- categoriesNames[11]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(precursorHistogram10$nPeptides[]))
mainCategorytemp[] <- "Ions"
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
logValues <- log10(precursorHistogram10$nPeptides)
precursorValues <- c(precursorValues, logValues)
precursorMedians <- c(precursorMedians, median(logValues))

categorytemp <- character(length(precursorHistogram11$nPeptides[]))
categorytemp[] <- categoriesNames[12]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(precursorHistogram11$nPeptides[]))
mainCategorytemp[] <- "Ions"
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
logValues <- log10(precursorHistogram11$nPeptides)
precursorValues <- c(precursorValues, logValues)
precursorMedians <- c(precursorMedians, median(logValues))

categorytemp <- character(length(precursorHistogram12$nPeptides[]))
categorytemp[] <- categoriesNames[13]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(precursorHistogram12$nPeptides[]))
mainCategorytemp[] <- "Tolerances"
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
logValues <- log10(precursorHistogram12$nPeptides)
precursorValues <- c(precursorValues, logValues)
precursorMedians <- c(precursorMedians, median(logValues))

categorytemp <- character(length(precursorHistogram13$nPeptides[]))
categorytemp[] <- categoriesNames[14]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(precursorHistogram13$nPeptides[]))
mainCategorytemp[] <- "Tolerances"
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
logValues <- log10(precursorHistogram13$nPeptides)
precursorValues <- c(precursorValues, logValues)
precursorMedians <- c(precursorMedians, median(logValues))

categorytemp <- character(length(precursorHistogram14$nPeptides[]))
categorytemp[] <- categoriesNames[15]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(precursorHistogram14$nPeptides[]))
mainCategorytemp[] <- "Tolerances"
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
logValues <- log10(precursorHistogram14$nPeptides)
precursorValues <- c(precursorValues, logValues)
precursorMedians <- c(precursorMedians, median(logValues))

categorytemp <- character(length(precursorHistogram15$nPeptides[]))
categorytemp[] <- categoriesNames[16]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(precursorHistogram15$nPeptides[]))
mainCategorytemp[] <- "Isotopes"
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
logValues <- log10(precursorHistogram15$nPeptides)
precursorValues <- c(precursorValues, logValues)
precursorMedians <- c(precursorMedians, median(logValues))

categorytemp <- character(length(precursorHistogram16$nPeptides[]))
categorytemp[] <- categoriesNames[17]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(precursorHistogram16$nPeptides[]))
mainCategorytemp[] <- "Charges"
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
logValues <- log10(precursorHistogram16$nPeptides)
precursorValues <- c(precursorValues, logValues)
precursorMedians <- c(precursorMedians, median(logValues))

categorytemp <- character(length(precursorHistogram17$nPeptides[]))
categorytemp[] <- categoriesNames[18]
precursorCategories <- c(precursorCategories, categorytemp)
mainCategorytemp <- character(length(precursorHistogram17$nPeptides[]))
mainCategorytemp[] <- "Charges"
precursorMainCategories <- c(precursorMainCategories, mainCategorytemp)
logValues <- log10(precursorHistogram17$nPeptides)
precursorValues <- c(precursorValues, logValues)
precursorMedians <- c(precursorMedians, median(logValues))

categories$median <- precursorMedians
sortedCategoriesNames <- categories[order(categories$median, categories$name), "name"]
precursorCategoriesFactors <- factor(precursorCategories, levels = sortedCategoriesNames)


# Plot the distribution of peptides per precursor

precursorHistogramPlot <- ggplot()
precursorHistogramPlot <- precursorHistogramPlot + geom_violin(aes(x=precursorCategoriesFactors, y=precursorValues, fill = precursorMainCategories))
precursorHistogramPlot <- precursorHistogramPlot + geom_boxplot(aes(x=precursorCategoriesFactors, y=precursorValues, fill = precursorMainCategories), width = 0.1, outlier.shape = NA)
precursorHistogramPlot <- precursorHistogramPlot + labs(x = "Category", y = "# Peptides per Precursor [log10]", fill="", col="")
precursorHistogramPlot <- precursorHistogramPlot + theme(axis.text.x = element_text(angle = 90, hjust = 1))
plot(precursorHistogramPlot)
