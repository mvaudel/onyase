library(ggplot2)


# Categories

categories <- c("Default", "Isoforms", "All", "Vertebrates")


# Default Data

precursorHistogram0 <- read.table("resources\\precursor_0.txt", header = T, sep = " ", stringsAsFactors = F)
eValuesHistogram0 <- read.table("resources\\all_psms_0.psm", header = T, sep = " ", stringsAsFactors = F)


# Plot mz against rt

precursorMapPlot <- ggplot()
precursorMapPlot <- precursorMapPlot + geom_point(aes(x=precursorHistogram0$rt, y=precursorHistogram0$mz), col = "darkblue", fill = "blue", alpha = 0.5)
precursorMapPlot <- precursorMapPlot + labs(x = "rt", y = "m/z")
plot(precursorMapPlot)


# Load Data

precursorHistogram1 <- read.table("resources\\precursor_1.txt", header = T, sep = " ", stringsAsFactors = F)
eValuesHistogram1 <- read.table("resources\\all_psms_1.psm", header = T, sep = " ", stringsAsFactors = F)

precursorHistogram2 <- read.table("resources\\precursor_2.txt", header = T, sep = " ", stringsAsFactors = F)
eValuesHistogram2 <- read.table("resources\\all_psms_2.psm", header = T, sep = " ", stringsAsFactors = F)

precursorHistogram3 <- read.table("resources\\precursor_3.txt", header = T, sep = " ", stringsAsFactors = F)
eValuesHistogram3 <- read.table("resources\\all_psms_3.psm", header = T, sep = " ", stringsAsFactors = F)


# Peptides per Precursor

medianNPeptides0 = median(precursorHistogram0$nPeptides)
medianNPeptides1 = median(precursorHistogram1$nPeptides)
medianNPeptides2 = median(precursorHistogram2$nPeptides)
medianNPeptides3 = median(precursorHistogram3$nPeptides)

precrusorHistogramCategories0 <- character(length(precursorHistogram0$nPeptides))
precrusorHistogramCategories0[] <- categories[1]
precrusorHistogramCategories1 <- character(length(precursorHistogram1$nPeptides))
precrusorHistogramCategories1[] <- categories[2]
precrusorHistogramCategories2 <- character(length(precursorHistogram2$nPeptides))
precrusorHistogramCategories2[] <- categories[3]
precrusorHistogramCategories3 <- character(length(precursorHistogram3$nPeptides))
precrusorHistogramCategories3[] <- categories[4]
precrusorHistogramCategories <- c(precrusorHistogramCategories0, precrusorHistogramCategories1, precrusorHistogramCategories2, precrusorHistogramCategories3)
precrusorHistogramValues <- c(precursorHistogram0$nPeptides, precursorHistogram1$nPeptides, precursorHistogram2$nPeptides, precursorHistogram3$nPeptides)

precursorHistogramPlot <- ggplot()
precursorHistogramPlot <- precursorHistogramPlot + geom_histogram(aes(x=precrusorHistogramValues, fill=precrusorHistogramCategories, col=precrusorHistogramCategories), binwidth = 10000, alpha = 0.5, position = "dodge")
precursorHistogramPlot <- precursorHistogramPlot + geom_vline(aes(xintercept=medianNPeptides0))
precursorHistogramPlot <- precursorHistogramPlot + geom_vline(aes(xintercept=medianNPeptides1))
precursorHistogramPlot <- precursorHistogramPlot + geom_vline(aes(xintercept=medianNPeptides2))
precursorHistogramPlot <- precursorHistogramPlot + geom_vline(aes(xintercept=medianNPeptides3))
precursorHistogramPlot <- precursorHistogramPlot + labs(x = "# Peptides per Precursor", y = "Density", fill="", col="")
plot(precursorHistogramPlot)


# Histogram of e-values

target0 <- eValuesHistogram0$E.Value[eValuesHistogram0$Decoy == 0]
decoy0 <- eValuesHistogram0$E.Value[eValuesHistogram0$Decoy == 1]
target1 <- eValuesHistogram1$E.Value[eValuesHistogram1$Decoy == 0]
decoy1 <- eValuesHistogram1$E.Value[eValuesHistogram1$Decoy == 1]
target2 <- eValuesHistogram2$E.Value[eValuesHistogram2$Decoy == 0]
decoy2 <- eValuesHistogram2$E.Value[eValuesHistogram2$Decoy == 1]
target3 <- eValuesHistogram3$E.Value[eValuesHistogram3$Decoy == 0]
decoy3 <- eValuesHistogram3$E.Value[eValuesHistogram3$Decoy == 1]
eValuesHistogramvalues <- c()
eValuesCategories <- c()
categorytemp <- character(length(target0))
categorytemp[] <- categories[1]
eValuesCategories <- c(eValuesCategories, categorytemp)
eValuesHistogramvalues <- c(eValuesHistogramvalues, target0)
categorytemp <- character(length(target1))
categorytemp[] <- categories[2]
eValuesCategories <- c(eValuesCategories, categorytemp)
eValuesHistogramvalues <- c(eValuesHistogramvalues, target1)
categorytemp <- character(length(target2))
categorytemp[] <- categories[3]
eValuesCategories <- c(eValuesCategories, categorytemp)
eValuesHistogramvalues <- c(eValuesHistogramvalues, target2)
categorytemp <- character(length(target3))
categorytemp[] <- categories[4]
eValuesCategories <- c(eValuesCategories, categorytemp)
eValuesHistogramvalues <- c(eValuesHistogramvalues, target3)

eValueHistogramPlot <- ggplot()
eValueHistogramPlot <- eValueHistogramPlot + geom_histogram(aes(x=eValuesHistogramvalues, fill = eValuesCategories, col = eValuesCategories), alpha = 0.5, binwidth = 1, position = "dodge")
eValueHistogramPlot <- eValueHistogramPlot + labs(x = "E-value [-log10]", y = "# Spectra", col = "Decoy", fill = "Decoy")
eValueHistogramPlot <- eValueHistogramPlot + lims(x=c(0, 10))
plot(eValueHistogramPlot)

