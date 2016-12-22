library(ggplot2)

# Load data
precursorHistogram0 <- read.table("resources\\precursor_0.txt", header = T, sep = " ", stringsAsFactors = F)
eValuesHistogram0 <- read.table("resources\\all_psms_0.psm", header = T, sep = " ", stringsAsFactors = F)
eValuesHistogramNoZero0 <- eValuesHistogram0[eValuesHistogram0$HyperScore > 0 & eValuesHistogram0$E.Value < 1,]
eValuesHistogramNoZero0$E.Value <- -eValuesHistogramNoZero0$E.Value

# Plot mz against rt
precursorMapPlot <- ggplot()
precursorMapPlot <- precursorMapPlot + geom_point(aes(x=precursorHistogram0$rt, y=precursorHistogram0$mz), col = "darkblue", fill = "blue", alpha = 0.5)
precursorMapPlot <- precursorMapPlot + labs(x = "rt", y = "m/z")
plot(precursorMapPlot)

# Histogram of precursors
precursorHistogramPlot <- ggplot()
precursorHistogramPlot <- precursorHistogramPlot + geom_histogram(aes(x=precursorHistogram0$nPeptides), binwidth = 20, alpha = 0.8)
precursorHistogramPlot <- precursorHistogramPlot + labs(x = "# Peptides per Precursor", y = "# Precursors")
plot(precursorHistogramPlot)

# Histogram of e-values
decoyAsFactor <- as.factor(eValuesHistogramNoZero0$Decoy)
eValueHistogramPlot <- ggplot()
eValueHistogramPlot <- eValueHistogramPlot + geom_histogram(aes(x=eValuesHistogramNoZero0$E.Value, fill = decoyAsFactor, col = decoyAsFactor), binwidth = 1, alpha = 0.5)
eValueHistogramPlot <- eValueHistogramPlot + labs(x = "E-value [-log10]", y = "# Spectra", col = "Decoy", fill = "Decoy")
plot(eValueHistogramPlot)

