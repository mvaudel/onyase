library(ggplot2)

# Default Data
precursorHistogram0 <- read.table("resources\\precursor_0.txt", header = T, sep = " ", stringsAsFactors = F)
eValuesHistogram0 <- read.table("resources\\all_psms_0.psm", header = T, sep = " ", stringsAsFactors = F)
eValuesHistogramNoZero0 <- eValuesHistogram0[eValuesHistogram0$HyperScore > 0 & eValuesHistogram0$E.Value < 1,]
eValuesHistogramNoZero0$E.Value <- -eValuesHistogramNoZero0$E.Value

# Plot mz against rt
precursorMapPlot <- ggplot()
precursorMapPlot <- precursorMapPlot + geom_point(aes(x=precursorHistogram0$rt, y=precursorHistogram0$mz), col = "darkblue", fill = "blue", alpha = 0.5)
precursorMapPlot <- precursorMapPlot + labs(x = "rt", y = "m/z")
plot(precursorMapPlot)

# Database



# Enzymaticity

enzymaticityCategories <- c("Default", "4mc", "semi", "unspecific")

precursorHistogram5 <- read.table("resources\\precursor_5.txt", header = T, sep = " ", stringsAsFactors = F)
eValuesHistogram5 <- read.table("resources\\all_psms_5.psm", header = T, sep = " ", stringsAsFactors = F)
eValuesHistogramNoZero5 <- eValuesHistogram0[eValuesHistogram5$HyperScore > 0 & eValuesHistogram5$E.Value < 1,]
eValuesHistogramNoZero5$E.Value <- -eValuesHistogramNoZero5$E.Value

precursorHistogram6 <- read.table("resources\\precursor_6.txt", header = T, sep = " ", stringsAsFactors = F)
eValuesHistogram6 <- read.table("resources\\all_psms_6.psm", header = T, sep = " ", stringsAsFactors = F)
eValuesHistogramNoZero6 <- eValuesHistogram0[eValuesHistogram6$HyperScore > 0 & eValuesHistogram6$E.Value < 1,]
eValuesHistogramNoZero6$E.Value <- -eValuesHistogramNoZero6$E.Value

precursorHistogram7 <- read.table("resources\\precursor_7.txt", header = T, sep = " ", stringsAsFactors = F)
eValuesHistogram7 <- read.table("resources\\all_psms_7.psm", header = T, sep = " ", stringsAsFactors = F)
eValuesHistogramNoZero7 <- eValuesHistogram0[eValuesHistogram7$HyperScore > 0 & eValuesHistogram6$E.Value < 1,]
eValuesHistogramNoZero7$E.Value <- -eValuesHistogramNoZero7$E.Value

medianNPeptides0 = median(precursorHistogram0$nPeptides)
medianNPeptides5 = median(precursorHistogram5$nPeptides)
medianNPeptides6 = median(precursorHistogram6$nPeptides)
medianNPeptides7 = median(precursorHistogram7$nPeptides)

precrusorHistogramCategories0 <- character(length(precursorHistogram0$nPeptides))
precrusorHistogramCategories0[] <- enzymaticityCategories[1]
precrusorHistogramCategories5 <- character(length(precursorHistogram5$nPeptides))
precrusorHistogramCategories5[] <- enzymaticityCategories[2]
precrusorHistogramCategories6 <- character(length(precursorHistogram6$nPeptides))
precrusorHistogramCategories6[] <- enzymaticityCategories[3]
precrusorHistogramCategories7 <- character(length(precursorHistogram7$nPeptides))
precrusorHistogramCategories7[] <- enzymaticityCategories[4]
enzymaticityPrecrusorHistogramCategories <- c(precrusorHistogramCategories0, precrusorHistogramCategories5, precrusorHistogramCategories6, precrusorHistogramCategories7)
enzymaticityPrecrusorHistogramValues <- c(precursorHistogram0$nPeptides, precursorHistogram5$nPeptides, precursorHistogram6$nPeptides, precursorHistogram7$nPeptides)

enzymaticityPrecursorHistogramPlot <- ggplot()
enzymaticityPrecursorHistogramPlot <- enzymaticityPrecursorHistogramPlot + geom_histogram(aes(x=enzymaticityPrecrusorHistogramValues, fill=enzymaticityPrecrusorHistogramCategories, col=enzymaticityPrecrusorHistogramCategories), binwidth = 100, alpha = 0.5, position = "dodge")
enzymaticityPrecursorHistogramPlot <- enzymaticityPrecursorHistogramPlot + geom_vline(aes(xintercept=medianNPeptides0))
enzymaticityPrecursorHistogramPlot <- enzymaticityPrecursorHistogramPlot + geom_vline(aes(xintercept=medianNPeptides5))
enzymaticityPrecursorHistogramPlot <- enzymaticityPrecursorHistogramPlot + geom_vline(aes(xintercept=medianNPeptides6))
enzymaticityPrecursorHistogramPlot <- enzymaticityPrecursorHistogramPlot + geom_vline(aes(xintercept=medianNPeptides7))
enzymaticityPrecursorHistogramPlot <- enzymaticityPrecursorHistogramPlot + labs(x = "# Peptides per Precursor", y = "Density", fill="", col="")
plot(enzymaticityPrecursorHistogramPlot)


# Variable modifications

modificationsCategories <- c("Default", "Cmm", "Phospho")

precursorHistogram8 <- read.table("resources\\precursor_8.txt", header = T, sep = " ", stringsAsFactors = F)
eValuesHistogram8 <- read.table("resources\\all_psms_8.psm", header = T, sep = " ", stringsAsFactors = F)
eValuesHistogramNoZero8 <- eValuesHistogram0[eValuesHistogram8$HyperScore > 0 & eValuesHistogram8$E.Value < 1,]
eValuesHistogramNoZero8$E.Value <- -eValuesHistogramNoZero8$E.Value

precursorHistogram9 <- read.table("resources\\precursor_9.txt", header = T, sep = " ", stringsAsFactors = F)
eValuesHistogram9 <- read.table("resources\\all_psms_9.psm", header = T, sep = " ", stringsAsFactors = F)
eValuesHistogramNoZero9 <- eValuesHistogram0[eValuesHistogram9$HyperScore > 0 & eValuesHistogram9$E.Value < 1,]
eValuesHistogramNoZero9$E.Value <- -eValuesHistogramNoZero9$E.Value

medianNPeptides0 = median(precursorHistogram0$nPeptides)
medianNPeptides8 = median(precursorHistogram5$nPeptides)
medianNPeptides9 = median(precursorHistogram6$nPeptides)

precrusorHistogramCategories0 <- character(length(precursorHistogram0$nPeptides))
precrusorHistogramCategories0[] <- modificationsCategories[1]
precrusorHistogramCategories8 <- character(length(precursorHistogram8$nPeptides))
precrusorHistogramCategories8[] <- modificationsCategories[2]
precrusorHistogramCategories9 <- character(length(precursorHistogram9$nPeptides))
precrusorHistogramCategories9[] <- modificationsCategories[3]
modificationsPrecrusorHistogramCategories <- c(precrusorHistogramCategories0, precrusorHistogramCategories8, precrusorHistogramCategories9)
modificationsPrecrusorHistogramValues <- c(precursorHistogram0$nPeptides, precursorHistogram8$nPeptides, precursorHistogram9$nPeptides)

modificationsPrecursorHistogramPlot <- ggplot()
modificationsPrecursorHistogramPlot <- modificationsPrecursorHistogramPlot + geom_histogram(aes(x=modificationsPrecrusorHistogramValues, fill=modificationsPrecrusorHistogramCategories, col=modificationsPrecrusorHistogramCategories), binwidth = 500, alpha = 0.5, position = "dodge")
modificationsPrecursorHistogramPlot <- modificationsPrecursorHistogramPlot + geom_vline(aes(xintercept=medianNPeptides0))
modificationsPrecursorHistogramPlot <- modificationsPrecursorHistogramPlot + geom_vline(aes(xintercept=medianNPeptides5))
modificationsPrecursorHistogramPlot <- modificationsPrecursorHistogramPlot + geom_vline(aes(xintercept=medianNPeptides6))
modificationsPrecursorHistogramPlot <- modificationsPrecursorHistogramPlot + geom_vline(aes(xintercept=medianNPeptides7))
modificationsPrecursorHistogramPlot <- modificationsPrecursorHistogramPlot + labs(x = "# Peptides per Precursor", y = "Density", fill="", col="")
plot(modificationsPrecursorHistogramPlot)



# Histogram of e-values
decoyAsFactor <- as.factor(eValuesHistogramNoZero0$Decoy)
eValueHistogramPlot <- ggplot()
eValueHistogramPlot <- eValueHistogramPlot + geom_histogram(aes(x=eValuesHistogramNoZero0$E.Value, fill = decoyAsFactor, col = decoyAsFactor), binwidth = 1, alpha = 0.5)
eValueHistogramPlot <- eValueHistogramPlot + labs(x = "E-value [-log10]", y = "# Spectra", col = "Decoy", fill = "Decoy")
plot(eValueHistogramPlot)

