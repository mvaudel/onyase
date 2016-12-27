library(ggplot2)

# Load data
precursorHistogram0 <- read.table("resources\\precursor_0.txt", header = T, sep = " ", stringsAsFactors = F)
eValuesHistogram0 <- read.table("resources\\all_psms_0.psm", header = T, sep = " ", stringsAsFactors = F)
eValuesHistogramNoZero0 <- eValuesHistogram0[eValuesHistogram0$HyperScore > 0 & eValuesHistogram0$E.Value < 1,]
eValuesHistogramNoZero0$E.Value <- -eValuesHistogramNoZero0$E.Value


precursorHistogram6 <- read.table("resources\\precursor_6.txt", header = T, sep = " ", stringsAsFactors = F)
eValuesHistogram6 <- read.table("resources\\all_psms_6.psm", header = T, sep = " ", stringsAsFactors = F)
eValuesHistogramNoZero6 <- eValuesHistogram0[eValuesHistogram6$HyperScore > 0 & eValuesHistogram6$E.Value < 1,]
eValuesHistogramNoZero6$E.Value <- -eValuesHistogramNoZero6$E.Value

# Plot mz against rt
precursorMapPlot <- ggplot()
precursorMapPlot <- precursorMapPlot + geom_point(aes(x=precursorHistogram0$rt, y=precursorHistogram0$mz), col = "darkblue", fill = "blue", alpha = 0.5)
precursorMapPlot <- precursorMapPlot + labs(x = "rt", y = "m/z")
plot(precursorMapPlot)

# Histogram of precursors
medianNPeptides0 = median(precursorHistogram0$nPeptides)
medianNPeptides6 = median(precursorHistogram6$nPeptides)
precrusorHistogramCategories0 <- character(length(precursorHistogram0$nPeptides))
precrusorHistogramCategories0[] <- "Default"
precrusorHistogramCategories6 <- character(length(precursorHistogram6$nPeptides))
precrusorHistogramCategories6[] <- "Phospho"
precrusorHistogramCategories <- c(precrusorHistogramCategories0, precrusorHistogramCategories6)
precrusorHistogramValues <- c(precursorHistogram0$nPeptides, precursorHistogram6$nPeptides)
precursorHistogramPlot <- ggplot()
precursorHistogramPlot <- precursorHistogramPlot + geom_histogram(aes(x=precrusorHistogramValues, fill=precrusorHistogramCategories, col=precrusorHistogramCategories), binwidth = 100, alpha = 0.8)
precursorHistogramPlot <- precursorHistogramPlot + geom_vline(aes(xintercept=medianNPeptides0))
precursorHistogramPlot <- precursorHistogramPlot + geom_vline(aes(xintercept=medianNPeptides6))
precursorHistogramPlot <- precursorHistogramPlot + scale_fill_manual(c("darkred", "darkblue"))
precursorHistogramPlot <- precursorHistogramPlot + labs(x = "# Peptides per Precursor", y = "# Precursors", fill="", col="")
plot(precursorHistogramPlot)

# Histogram of e-values
decoyAsFactor <- as.factor(eValuesHistogramNoZero0$Decoy)
eValueHistogramPlot <- ggplot()
eValueHistogramPlot <- eValueHistogramPlot + geom_histogram(aes(x=eValuesHistogramNoZero0$E.Value, fill = decoyAsFactor, col = decoyAsFactor), binwidth = 1, alpha = 0.5)
eValueHistogramPlot <- eValueHistogramPlot + labs(x = "E-value [-log10]", y = "# Spectra", col = "Decoy", fill = "Decoy")
plot(eValueHistogramPlot)

