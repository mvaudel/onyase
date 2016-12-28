library(ggplot2)


# Categories

categories <- c("Default", "Cmm", "Phospho")


# Default Data

precursorHistogram0 <- read.table("resources\\precursor_0.txt", header = T, sep = " ", stringsAsFactors = F)
eValuesHistogram0 <- read.table("resources\\all_psms_0.psm", header = T, sep = " ", stringsAsFactors = F)


# Plot mz against rt

precursorMapPlot <- ggplot()
precursorMapPlot <- precursorMapPlot + geom_point(aes(x=precursorHistogram0$rt, y=precursorHistogram0$mz), col = "darkblue", fill = "blue", alpha = 0.5)
precursorMapPlot <- precursorMapPlot + labs(x = "rt", y = "m/z")
plot(precursorMapPlot)


# Load Data

precursorHistogram8 <- read.table("resources\\precursor_8.txt", header = T, sep = " ", stringsAsFactors = F)
eValuesHistogram8 <- read.table("resources\\all_psms_8.psm", header = T, sep = " ", stringsAsFactors = F)

precursorHistogram9 <- read.table("resources\\precursor_9.txt", header = T, sep = " ", stringsAsFactors = F)
eValuesHistogram9 <- read.table("resources\\all_psms_9.psm", header = T, sep = " ", stringsAsFactors = F)


# Peptides per Precursor

medianNPeptides0 = median(precursorHistogram0$nPeptides)
medianNPeptides8 = median(precursorHistogram8$nPeptides)
medianNPeptides9 = median(precursorHistogram9$nPeptides)

precrusorHistogramCategories0 <- character(length(precursorHistogram0$nPeptides))
precrusorHistogramCategories0[] <- categories[1]
precrusorHistogramCategories8 <- character(length(precursorHistogram8$nPeptides))
precrusorHistogramCategories8[] <- categories[2]
precrusorHistogramCategories9 <- character(length(precursorHistogram9$nPeptides))
precrusorHistogramCategories9[] <- categories[3]
precrusorHistogramCategories <- c(precrusorHistogramCategories0, precrusorHistogramCategories8, precrusorHistogramCategories9)
precrusorHistogramValues <- c(precursorHistogram0$nPeptides, precursorHistogram8$nPeptides, precursorHistogram9$nPeptides)

precursorHistogramPlot <- ggplot()
precursorHistogramPlot <- precursorHistogramPlot + geom_histogram(aes(x=precrusorHistogramValues, fill=precrusorHistogramCategories, col=precrusorHistogramCategories), binwidth = 100, alpha = 0.5, position = "dodge")
precursorHistogramPlot <- precursorHistogramPlot + geom_vline(aes(xintercept=medianNPeptides0))
precursorHistogramPlot <- precursorHistogramPlot + geom_vline(aes(xintercept=medianNPeptides10))
precursorHistogramPlot <- precursorHistogramPlot + geom_vline(aes(xintercept=medianNPeptides11))
precursorHistogramPlot <- precursorHistogramPlot + labs(x = "# Peptides per Precursor", y = "Density", fill="", col="")
plot(precursorHistogramPlot)


# Histogram of e-values

target0 <- eValuesHistogram0$E.Value[eValuesHistogram0$Decoy == 0]
decoy0 <- eValuesHistogram0$E.Value[eValuesHistogram0$Decoy == 1]
target8 <- eValuesHistogram10$E.Value[eValuesHistogram8$Decoy == 0]
decoy8 <- eValuesHistogram10$E.Value[eValuesHistogram8$Decoy == 1]
target9 <- eValuesHistogram11$E.Value[eValuesHistogram9$Decoy == 0]
decoy9 <- eValuesHistogram11$E.Value[eValuesHistogram9$Decoy == 1]
eValuesHistogramvalues <- c()
eValuesCategories <- c()
categorytemp <- character(length(target0))
categorytemp[] <- categories[1]
eValuesCategories <- c(eValuesCategories, categorytemp)
eValuesHistogramvalues <- c(eValuesHistogramvalues, target0)
categorytemp <- character(length(target8))
categorytemp[] <- categories[2]
eValuesCategories <- c(eValuesCategories, categorytemp)
eValuesHistogramvalues <- c(eValuesHistogramvalues, target8)
categorytemp <- character(length(target9))
categorytemp[] <- categories[3]
eValuesCategories <- c(eValuesCategories, categorytemp)
eValuesHistogramvalues <- c(eValuesHistogramvalues, target9)

eValueHistogramPlot <- ggplot()
eValueHistogramPlot <- eValueHistogramPlot + geom_histogram(aes(x=eValuesHistogramvalues, fill = eValuesCategories, col = eValuesCategories), alpha = 0.5, binwidth = 5, position = "dodge")
eValueHistogramPlot <- eValueHistogramPlot + labs(x = "E-value [-log10]", y = "# Spectra", col = "Decoy", fill = "Decoy")
plot(eValueHistogramPlot)

