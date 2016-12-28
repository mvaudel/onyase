library(ggplot2)


# Categories

categories <- c("Default", "1-4", "1-6")


# Default Data

precursorHistogram0 <- read.table("resources\\precursor_0.txt", header = T, sep = " ", stringsAsFactors = F)
eValuesHistogram0 <- read.table("resources\\all_psms_0.psm", header = T, sep = " ", stringsAsFactors = F)


# Plot mz against rt

precursorMapPlot <- ggplot()
precursorMapPlot <- precursorMapPlot + geom_point(aes(x=precursorHistogram0$rt, y=precursorHistogram0$mz), col = "darkblue", fill = "blue", alpha = 0.5)
precursorMapPlot <- precursorMapPlot + labs(x = "rt", y = "m/z")
plot(precursorMapPlot)


# Load Data

precursorHistogram16 <- read.table("resources\\precursor_16.txt", header = T, sep = " ", stringsAsFactors = F)
eValuesHistogram16 <- read.table("resources\\all_psms_16.psm", header = T, sep = " ", stringsAsFactors = F)

precursorHistogram17 <- read.table("resources\\precursor_17.txt", header = T, sep = " ", stringsAsFactors = F)
eValuesHistogram17 <- read.table("resources\\all_psms_17.psm", header = T, sep = " ", stringsAsFactors = F)


# Peptides per Precursor

medianNPeptides0 = median(precursorHistogram0$nPeptides)
medianNPeptides16 = median(precursorHistogram16$nPeptides)
medianNPeptides17 = median(precursorHistogram17$nPeptides)

precrusorHistogramCategories0 <- character(length(precursorHistogram0$nPeptides))
precrusorHistogramCategories0[] <- categories[1]
precrusorHistogramCategories16 <- character(length(precursorHistogram16$nPeptides))
precrusorHistogramCategories16[] <- categories[2]
precrusorHistogramCategories17 <- character(length(precursorHistogram17$nPeptides))
precrusorHistogramCategories17[] <- categories[3]
precrusorHistogramCategories <- c(precrusorHistogramCategories0, precrusorHistogramCategories16, precrusorHistogramCategories17)
precrusorHistogramValues <- c(precursorHistogram0$nPeptides, precursorHistogram16$nPeptides, precursorHistogram17$nPeptides)

precursorHistogramPlot <- ggplot()
precursorHistogramPlot <- precursorHistogramPlot + geom_histogram(aes(x=precrusorHistogramValues, fill=precrusorHistogramCategories, col=precrusorHistogramCategories), binwidth = 500, alpha = 0.5, position = "dodge")
precursorHistogramPlot <- precursorHistogramPlot + geom_vline(aes(xintercept=medianNPeptides0))
precursorHistogramPlot <- precursorHistogramPlot + geom_vline(aes(xintercept=medianNPeptides16))
precursorHistogramPlot <- precursorHistogramPlot + geom_vline(aes(xintercept=medianNPeptides17))
precursorHistogramPlot <- precursorHistogramPlot + labs(x = "# Peptides per Precursor", y = "Density", fill="", col="")
plot(precursorHistogramPlot)


# Histogram of e-values

target0 <- eValuesHistogram0$E.Value[eValuesHistogram0$Decoy == 0]
decoy0 <- eValuesHistogram0$E.Value[eValuesHistogram0$Decoy == 1]
target16 <- eValuesHistogram10$E.Value[eValuesHistogram16$Decoy == 0]
decoy16 <- eValuesHistogram10$E.Value[eValuesHistogram16$Decoy == 1]
target17 <- eValuesHistogram11$E.Value[eValuesHistogram17$Decoy == 0]
decoy17 <- eValuesHistogram11$E.Value[eValuesHistogram17$Decoy == 1]
eValuesHistogramvalues <- c()
eValuesCategories <- c()
categorytemp <- character(length(target0))
categorytemp[] <- categories[1]
eValuesCategories <- c(eValuesCategories, categorytemp)
eValuesHistogramvalues <- c(eValuesHistogramvalues, target0)
categorytemp <- character(length(target16))
categorytemp[] <- categories[2]
eValuesCategories <- c(eValuesCategories, categorytemp)
eValuesHistogramvalues <- c(eValuesHistogramvalues, target16)
categorytemp <- character(length(target17))
categorytemp[] <- categories[3]
eValuesCategories <- c(eValuesCategories, categorytemp)
eValuesHistogramvalues <- c(eValuesHistogramvalues, target17)

eValueHistogramPlot <- ggplot()
eValueHistogramPlot <- eValueHistogramPlot + geom_histogram(aes(x=eValuesHistogramvalues, fill = eValuesCategories, col = eValuesCategories), alpha = 0.5, binwidth = 5, position = "dodge")
eValueHistogramPlot <- eValueHistogramPlot + labs(x = "E-value [-log10]", y = "# Spectra", col = "Decoy", fill = "Decoy")
plot(eValueHistogramPlot)

