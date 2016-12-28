library(ggplot2)


# Categories

categories <- c("Default", "ABY", "ABCXYZ")


# Default Data

precursorHistogram0 <- read.table("resources\\precursor_0.txt", header = T, sep = " ", stringsAsFactors = F)
eValuesHistogram0 <- read.table("resources\\all_psms_0.psm", header = T, sep = " ", stringsAsFactors = F)


# Plot mz against rt

precursorMapPlot <- ggplot()
precursorMapPlot <- precursorMapPlot + geom_point(aes(x=precursorHistogram0$rt, y=precursorHistogram0$mz), col = "darkblue", fill = "blue", alpha = 0.5)
precursorMapPlot <- precursorMapPlot + labs(x = "rt", y = "m/z")
plot(precursorMapPlot)


# Load Data

precursorHistogram10 <- read.table("resources\\precursor_10.txt", header = T, sep = " ", stringsAsFactors = F)
eValuesHistogram10 <- read.table("resources\\all_psms_10.psm", header = T, sep = " ", stringsAsFactors = F)

precursorHistogram11 <- read.table("resources\\precursor_11.txt", header = T, sep = " ", stringsAsFactors = F)
eValuesHistogram11 <- read.table("resources\\all_psms_11.psm", header = T, sep = " ", stringsAsFactors = F)


# Peptides per Precursor

medianNPeptides0 = median(precursorHistogram0$nPeptides)
medianNPeptides10 = median(precursorHistogram10$nPeptides)
medianNPeptides11 = median(precursorHistogram11$nPeptides)

precrusorHistogramCategories0 <- character(length(precursorHistogram0$nPeptides))
precrusorHistogramCategories0[] <- categories[1]
precrusorHistogramCategories10 <- character(length(precursorHistogram10$nPeptides))
precrusorHistogramCategories10[] <- categories[2]
precrusorHistogramCategories11 <- character(length(precursorHistogram11$nPeptides))
precrusorHistogramCategories11[] <- categories[3]
precrusorHistogramCategories <- c(precrusorHistogramCategories0, precrusorHistogramCategories10, precrusorHistogramCategories11)
precrusorHistogramValues <- c(precursorHistogram0$nPeptides, precursorHistogram10$nPeptides, precursorHistogram11$nPeptides)

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
target10 <- eValuesHistogram10$E.Value[eValuesHistogram10$Decoy == 0]
decoy10 <- eValuesHistogram10$E.Value[eValuesHistogram10$Decoy == 1]
target11 <- eValuesHistogram11$E.Value[eValuesHistogram11$Decoy == 0]
decoy11 <- eValuesHistogram11$E.Value[eValuesHistogram11$Decoy == 1]
eValuesHistogramvalues <- c()
eValuesCategories <- c()
categorytemp <- character(length(target0))
categorytemp[] <- categories[1]
eValuesCategories <- c(eValuesCategories, categorytemp)
eValuesHistogramvalues <- c(eValuesHistogramvalues, target0)
categorytemp <- character(length(target10))
categorytemp[] <- categories[2]
eValuesCategories <- c(eValuesCategories, categorytemp)
eValuesHistogramvalues <- c(eValuesHistogramvalues, target10)
categorytemp <- character(length(target11))
categorytemp[] <- categories[3]
eValuesCategories <- c(eValuesCategories, categorytemp)
eValuesHistogramvalues <- c(eValuesHistogramvalues, target11)

eValueHistogramPlot <- ggplot()
eValueHistogramPlot <- eValueHistogramPlot + geom_histogram(aes(x=eValuesHistogramvalues, fill = eValuesCategories, col = eValuesCategories), alpha = 0.5, binwidth = 5, position = "dodge")
eValueHistogramPlot <- eValueHistogramPlot + labs(x = "E-value [-log10]", y = "# Spectra", col = "Decoy", fill = "Decoy")
plot(eValueHistogramPlot)

