library(ggplot2)


# Categories

categories <- c("Default", "4mc", "Semi", "No")


# Default Data

precursorHistogram0 <- read.table("resources\\precursor_0.txt", header = T, sep = " ", stringsAsFactors = F)
eValuesHistogram0 <- read.table("resources\\all_psms_0.psm", header = T, sep = " ", stringsAsFactors = F)


# Plot mz against rt

precursorMapPlot <- ggplot()
precursorMapPlot <- precursorMapPlot + geom_point(aes(x=precursorHistogram0$rt, y=precursorHistogram0$mz), col = "darkblue", fill = "blue", alpha = 0.5)
precursorMapPlot <- precursorMapPlot + labs(x = "rt", y = "m/z")
plot(precursorMapPlot)


# Load Data

precursorHistogram5 <- read.table("resources\\precursor_5.txt", header = T, sep = " ", stringsAsFactors = F)
eValuesHistogram5 <- read.table("resources\\all_psms_5.psm", header = T, sep = " ", stringsAsFactors = F)

precursorHistogram6 <- read.table("resources\\precursor_6.txt", header = T, sep = " ", stringsAsFactors = F)
eValuesHistogram6 <- read.table("resources\\all_psms_6.psm", header = T, sep = " ", stringsAsFactors = F)

precursorHistogram7 <- read.table("resources\\precursor_7.txt", header = T, sep = " ", stringsAsFactors = F)
eValuesHistogram7 <- read.table("resources\\all_psms_7.psm", header = T, sep = " ", stringsAsFactors = F)


# Peptides per Precursor

medianNPeptides0 = median(precursorHistogram0$nPeptides)
medianNPeptides5 = median(precursorHistogram5$nPeptides)
medianNPeptides6 = median(precursorHistogram6$nPeptides)
medianNPeptides7 = median(precursorHistogram7$nPeptides)

precrusorHistogramCategories0 <- character(length(precursorHistogram0$nPeptides))
precrusorHistogramCategories0[] <- categories[1]
precrusorHistogramCategories5 <- character(length(precursorHistogram5$nPeptides))
precrusorHistogramCategories5[] <- categories[2]
precrusorHistogramCategories6 <- character(length(precursorHistogram6$nPeptides))
precrusorHistogramCategories6[] <- categories[3]
precrusorHistogramCategories7 <- character(length(precursorHistogram7$nPeptides))
precrusorHistogramCategories7[] <- categories[4]
precrusorHistogramCategories <- c(precrusorHistogramCategories0, precrusorHistogramCategories5, precrusorHistogramCategories6, precrusorHistogramCategories7)
precrusorHistogramValues <- c(precursorHistogram0$nPeptides, precursorHistogram5$nPeptides, precursorHistogram6$nPeptides, precursorHistogram7$nPeptides)

precursorHistogramPlot <- ggplot()
precursorHistogramPlot <- precursorHistogramPlot + geom_histogram(aes(x=precrusorHistogramValues, fill=precrusorHistogramCategories, col=precrusorHistogramCategories), binwidth = 100, alpha = 0.5, position = "dodge")
precursorHistogramPlot <- precursorHistogramPlot + geom_vline(aes(xintercept=medianNPeptides0))
precursorHistogramPlot <- precursorHistogramPlot + geom_vline(aes(xintercept=medianNPeptides5))
precursorHistogramPlot <- precursorHistogramPlot + geom_vline(aes(xintercept=medianNPeptides6))
precursorHistogramPlot <- precursorHistogramPlot + geom_vline(aes(xintercept=medianNPeptides7))
precursorHistogramPlot <- precursorHistogramPlot + labs(x = "# Peptides per Precursor", y = "Density", fill="", col="")
plot(precursorHistogramPlot)


# Histogram of e-values

target0 <- eValuesHistogram0$E.Value[eValuesHistogram0$Decoy == 0]
decoy0 <- eValuesHistogram0$E.Value[eValuesHistogram0$Decoy == 1]
target5 <- eValuesHistogram5$E.Value[eValuesHistogram5$Decoy == 0]
decoy5 <- eValuesHistogram5$E.Value[eValuesHistogram5$Decoy == 1]
target6 <- eValuesHistogram6$E.Value[eValuesHistogram6$Decoy == 0]
decoy6 <- eValuesHistogram6$E.Value[eValuesHistogram6$Decoy == 1]
target7 <- eValuesHistogram7$E.Value[eValuesHistogram7$Decoy == 0]
decoy7 <- eValuesHistogram7$E.Value[eValuesHistogram7$Decoy == 1]
eValuesHistogramvalues <- c()
eValuesCategories <- c()
categorytemp <- character(length(target0))
categorytemp[] <- categories[1]
eValuesCategories <- c(eValuesCategories, categorytemp)
eValuesHistogramvalues <- c(eValuesHistogramvalues, target0)
categorytemp <- character(length(target5))
categorytemp[] <- categories[2]
eValuesCategories <- c(eValuesCategories, categorytemp)
eValuesHistogramvalues <- c(eValuesHistogramvalues, target5)
categorytemp <- character(length(target6))
categorytemp[] <- categories[3]
eValuesCategories <- c(eValuesCategories, categorytemp)
eValuesHistogramvalues <- c(eValuesHistogramvalues, target6)
categorytemp <- character(length(target7))
categorytemp[] <- categories[4]
eValuesCategories <- c(eValuesCategories, categorytemp)
eValuesHistogramvalues <- c(eValuesHistogramvalues, target7)

eValueHistogramPlot <- ggplot()
eValueHistogramPlot <- eValueHistogramPlot + geom_histogram(aes(x=eValuesHistogramvalues, fill = eValuesCategories, col = eValuesCategories), alpha = 0.5, binwidth = 1, position = "dodge")
eValueHistogramPlot <- eValueHistogramPlot + labs(x = "E-value [-log10]", y = "# Spectra", col = "Decoy", fill = "Decoy")
eValueHistogramPlot <- eValueHistogramPlot + lims(x=c(0, 10))
plot(eValueHistogramPlot)

