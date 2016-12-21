library(ggplot2)

# Histogram of precursors
precursorHistogram <- read.table("resources\\precursor_Default.txt", header = T, sep = " ", stringsAsFactors = F)
precursorHistogram <- precursorHistogram[precursorHistogram$mz > 500,]

precursorHistogramPlot <- ggplot()
precursorHistogramPlot <- precursorHistogramPlot + geom_histogram(aes(x=precursorHistogram$peptides), binwidth = 20, col="darkblue", fill = "blue", alpha = 0.5)
precursorHistogramPlot <- precursorHistogramPlot + labs(x = "# Peptides per Precursor", y = "# Precursors")
plot(precursorHistogramPlot)

# Histogram of e-values
eValuesHistogram <- read.table("resources\\test.psm", header = T, sep = " ", stringsAsFactors = F)
eValuesHistogramNoZero <- eValuesHistogram[eValuesHistogram$HyperScore > 0 & eValuesHistogram$E.Value < 0,]
eValuesHistogramNoZero$E.Value <- -eValuesHistogramNoZero$E.Value

eValueHistogramPlot <- ggplot()
eValueHistogramPlot <- eValueHistogramPlot + geom_histogram(aes(x=eValuesHistogramNoZero$E.Value), binwidth = 1, col="darkblue", fill = "blue", alpha = 0.5)
eValueHistogramPlot <- eValueHistogramPlot + labs(x = "# Peptides per Precursor", y = "# Precursors")
plot(eValueHistogramPlot)
