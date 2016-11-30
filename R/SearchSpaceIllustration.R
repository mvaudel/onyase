library(ggplot2)

matches <- read.table(file="matches.txt", header = T, stringsAsFactors = F)
matches[matches$eValue == "null", "eValue"] <- NA
matches$eValue <- as.numeric(matches$eValue)

knownScores <- read.table(file="scoresComparison.txt", header = T, stringsAsFactors = F)
knownScores[knownScores$New.score == "null","New.score"] <- NA
knownScores[knownScores$New.eValue == "null","New.eValue"] <- NA
knownScores$New.score <- as.numeric(knownScores$New.score)
knownScores$New.eValue <- as.numeric(knownScores$New.eValue)

nMatchesData <- read.table(file="nMatches.txt", header = T, stringsAsFactors = F)
nMatchesPlot <- ggplot()
nMatchesPlot <- nMatchesPlot + geom_histogram(aes(x=nMatchesData$nMatches), fill = "darkblue", col = "darkblue", alpha = 0.5, bins = 100)
nMatchesPlot <- nMatchesPlot + labs(x = "Number of peptides per precursor", y = "number of spectra")
plot(nMatchesPlot)

ms1Plot <- ggplot()
ms1Plot <- ms1Plot + geom_density(aes(x=matches$MS1_deviation, col=matches$Category, fill = matches$Category), alpha = 0.5)
ms1Plot <- ms1Plot + labs(x = "", y = "MS1 deviation [ppm]")
plot(ms1Plot)

fittingData <- read.table(file="C:\\Projects\\scores\\comparison xtandem\\fitting_values.txt", header = T)
abPlot <- ggplot()
abPlot <- abPlot + geom_point(aes(x=fittingData$a, y=fittingData$b), alpha = 0.1, na.rm=TRUE)
abPlot <- abPlot + labs(x = "a", y = "b")
plot(abPlot)


valuesData <- read.table(file="C:\\Projects\\scores\\comparison xtandem\\debug_values.txt", sep = "\t", header = T)
cumulativePPlot <- ggplot()
cumulativePPlot <- cumulativePPlot + geom_point(aes(x=log(valuesData$X), y=valuesData$Y), col="darkred", alpha = 0.5, na.rm=TRUE)
cumulativePPlot <- cumulativePPlot + labs(x = "Hyperscore", y = "# Peptides")
# cumulativePPlot <- cumulativePPlot + lims(x = c(-1,1))
plot(cumulativePPlot)

histogramData <- read.table(file="C:\\Projects\\scores\\comparison xtandem\\debug_histogram.txt", sep = "\t", header = T)
histogramPlot <- ggplot()
histogramPlot <- histogramPlot + geom_smooth(aes(x=histogramData$X, y=histogramData$Y), method='lm', na.rm=TRUE)
histogramPlot <- histogramPlot + geom_point(aes(x=histogramData$X, y=histogramData$Y), col="darkgreen", alpha = 0.5, na.rm=TRUE)
histogramPlot <- histogramPlot + labs(x = "Hyperscore", y = "# Peptides")
plot(histogramPlot)

ms2ScorePlot <- ggplot()
ms2ScorePlot <- ms2ScorePlot + geom_density(aes(x=log(matches$MS2_Score), col=matches$Category, fill = matches$Category), alpha = 0.1, na.rm=TRUE)
ms2ScorePlot <- ms2ScorePlot + labs(x = "", y = "Hyperscore")
ms2ScorePlot <- ms2ScorePlot + lims(y = c(0, 0.005))
plot(ms2ScorePlot)

ms2EValuePlot <- ggplot()
ms2EValuePlot <- ms2EValuePlot + geom_density(aes(x=log(matches$eValue), col=matches$Category, fill = matches$Category), alpha = 0.1, na.rm=TRUE)
ms2EValuePlot <- ms2EValuePlot + labs(x = "", y = "e-value")
ms2EValuePlot <- ms2EValuePlot + lims(y = c(0, 0.005))
plot(ms2EValuePlot)

knownScores$psLog <- log(knownScores$PS.score)
knownScores$pDist <- abs(knownScores$psLog - knownScores$New.eValue) 
correlationPlot <- ggplot()
correlationPlot <- correlationPlot + geom_point(aes(x=knownScores$psLog[knownScores$psLog>-34], y=knownScores$New.eValue[knownScores$psLog>-34], col = knownScores$pDist[knownScores$psLog>-34]), alpha = 0.5, na.rm=TRUE)
# correlationPlot <- correlationPlot + geom_point(aes(x=knownScores$psLog, y=knownScores$New.eValue, col = knownScores$pDist), alpha = 0.5, na.rm=TRUE)
correlationPlot <- correlationPlot + labs(x = "p1", y = "p2", col="Difference")
# correlationPlot <- correlationPlot + lims(y = c(1e-10,1))
plot(correlationPlot)

