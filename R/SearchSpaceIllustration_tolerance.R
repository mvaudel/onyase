library(ggplot2)


# Categories

categories <- c("Default", "MS1", "MS2", "MS1-2")


# Default Data

precursorHistogram0 <- read.table("resources\\precursor_0.txt", header = T, sep = " ", stringsAsFactors = F)
eValuesHistogram0 <- read.table("resources\\all_psms_0.psm", header = T, sep = " ", stringsAsFactors = F)


# Plot mz against rt

precursorMapPlot <- ggplot()
precursorMapPlot <- precursorMapPlot + geom_point(aes(x=precursorHistogram0$rt, y=precursorHistogram0$mz), col = "darkblue", fill = "blue", alpha = 0.5)
precursorMapPlot <- precursorMapPlot + labs(x = "rt", y = "m/z")
plot(precursorMapPlot)


# Load Data

precursorHistogram12 <- read.table("resources\\precursor_12.txt", header = T, sep = " ", stringsAsFactors = F)
eValuesHistogram12 <- read.table("resources\\all_psms_12.psm", header = T, sep = " ", stringsAsFactors = F)

precursorHistogram13 <- read.table("resources\\precursor_13.txt", header = T, sep = " ", stringsAsFactors = F)
eValuesHistogram13 <- read.table("resources\\all_psms_13.psm", header = T, sep = " ", stringsAsFactors = F)

precursorHistogram14 <- read.table("resources\\precursor_14.txt", header = T, sep = " ", stringsAsFactors = F)
eValuesHistogram14 <- read.table("resources\\all_psms_14.psm", header = T, sep = " ", stringsAsFactors = F)


# Peptides per Precursor

medianNPeptides0 = median(precursorHistogram0$nPeptides)
medianNPeptides12 = median(precursorHistogram12$nPeptides)
medianNPeptides13 = median(precursorHistogram13$nPeptides)
medianNPeptides14 = median(precursorHistogram14$nPeptides)

precrusorHistogramCategories0 <- character(length(precursorHistogram0$nPeptides))
precrusorHistogramCategories0[] <- categories[1]
precrusorHistogramCategories12 <- character(length(precursorHistogram12$nPeptides))
precrusorHistogramCategories12[] <- categories[2]
precrusorHistogramCategories13 <- character(length(precursorHistogram13$nPeptides))
precrusorHistogramCategories13[] <- categories[3]
precrusorHistogramCategories14 <- character(length(precursorHistogram14$nPeptides))
precrusorHistogramCategories14[] <- categories[4]
precrusorHistogramCategories <- c(precrusorHistogramCategories0, precrusorHistogramCategories12, precrusorHistogramCategories13, precrusorHistogramCategories14)
precrusorHistogramValues <- c(precursorHistogram0$nPeptides, precursorHistogram12$nPeptides, precursorHistogram13$nPeptides, precursorHistogram14$nPeptides)

precursorHistogramPlot <- ggplot()
precursorHistogramPlot <- precursorHistogramPlot + geom_histogram(aes(x=precrusorHistogramValues, fill=precrusorHistogramCategories, col=precrusorHistogramCategories), binwidth = 100, alpha = 0.5, position = "dodge")
precursorHistogramPlot <- precursorHistogramPlot + geom_vline(aes(xintercept=medianNPeptides0))
precursorHistogramPlot <- precursorHistogramPlot + geom_vline(aes(xintercept=medianNPeptides12))
precursorHistogramPlot <- precursorHistogramPlot + geom_vline(aes(xintercept=medianNPeptides13))
precursorHistogramPlot <- precursorHistogramPlot + geom_vline(aes(xintercept=medianNPeptides14))
precursorHistogramPlot <- precursorHistogramPlot + labs(x = "# Peptides per Precursor", y = "Density", fill="", col="")
plot(precursorHistogramPlot)


# Histogram of e-values

target0 <- eValuesHistogram0$E.Value[eValuesHistogram0$Decoy == 0]
decoy0 <- eValuesHistogram0$E.Value[eValuesHistogram0$Decoy == 1]
target12 <- eValuesHistogram12$E.Value[eValuesHistogram12$Decoy == 0]
decoy12 <- eValuesHistogram12$E.Value[eValuesHistogram12$Decoy == 1]
target13 <- eValuesHistogram13$E.Value[eValuesHistogram13$Decoy == 0]
decoy13 <- eValuesHistogram13$E.Value[eValuesHistogram13$Decoy == 1]
target14 <- eValuesHistogram14$E.Value[eValuesHistogram14$Decoy == 0]
decoy14 <- eValuesHistogram14$E.Value[eValuesHistogram14$Decoy == 1]
eValuesHistogramvalues <- c()
eValuesCategories <- c()
categorytemp <- character(length(target0))
categorytemp[] <- categories[1]
eValuesCategories <- c(eValuesCategories, categorytemp)
eValuesHistogramvalues <- c(eValuesHistogramvalues, target0)
categorytemp <- character(length(target12))
categorytemp[] <- categories[2]
eValuesCategories <- c(eValuesCategories, categorytemp)
eValuesHistogramvalues <- c(eValuesHistogramvalues, target12)
categorytemp <- character(length(target13))
categorytemp[] <- categories[3]
eValuesCategories <- c(eValuesCategories, categorytemp)
eValuesHistogramvalues <- c(eValuesHistogramvalues, target13)
categorytemp <- character(length(target14))
categorytemp[] <- categories[4]
eValuesCategories <- c(eValuesCategories, categorytemp)
eValuesHistogramvalues <- c(eValuesHistogramvalues, target14)

eValueHistogramPlot <- ggplot()
eValueHistogramPlot <- eValueHistogramPlot + geom_histogram(aes(x=eValuesHistogramvalues, fill = eValuesCategories, col = eValuesCategories), alpha = 0.5, binwidth = 1, position = "dodge")
eValueHistogramPlot <- eValueHistogramPlot + labs(x = "E-value [-log10]", y = "# Spectra", col = "Decoy", fill = "Decoy")
eValueHistogramPlot <- eValueHistogramPlot + lims(x=c(0, 10))
plot(eValueHistogramPlot)

