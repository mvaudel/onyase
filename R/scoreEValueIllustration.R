library(ggplot2)

##
#
# This script aggregates the results of the Onyase Figure script on PSM e-values against hyperscore into a single figure.
#
##


# Format the data for ggplot

eValueCategories <- c()
eValueMainCategories <- c()
eValueValues <- c()
scoreValues <- c()
eValueRefValues <- c()

eValueLimit <- 0

eValues <- -scores0$E.Value
hyperScores <- scores0$HyperScore
tempValues1 <- eValues[eValues >eValueLimit & hyperScores > 0]
categorytemp <- character(length(tempValues1))
categoryI <- which(categories$id == 0)
categorytemp[] <- categoriesNames[categoryI]
eValueCategories <- c(eValueCategories, categorytemp)
mainCategorytemp <- character(length(tempValues1))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
eValueMainCategories <- c(eValueMainCategories, mainCategorytemp)
eValueValues <- c(eValueValues, tempValues1)
eValueRefValues <- c(eValueRefValues, median(tempValues1, na.rm = T))
tempValues2 <- scores0$HyperScore[eValues >eValueLimit & hyperScores > 0]
tempValues2 <- log10(tempValues2)
scoreValues <- c(scoreValues, tempValues2)

eValues <- -scores1$E.Value
hyperScores <- scores1$HyperScore
tempValues1 <- eValues[eValues >eValueLimit & hyperScores > 0]
categorytemp <- character(length(tempValues1))
categoryI <- which(categories$id == 1)
categorytemp[] <- categoriesNames[categoryI]
eValueCategories <- c(eValueCategories, categorytemp)
mainCategorytemp <- character(length(tempValues1))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
eValueMainCategories <- c(eValueMainCategories, mainCategorytemp)
eValueValues <- c(eValueValues, tempValues1)
eValueRefValues <- c(eValueRefValues, median(tempValues1, na.rm = T))
tempValues2 <- scores1$HyperScore[eValues >eValueLimit & hyperScores > 0]
tempValues2 <- log10(tempValues2)
scoreValues <- c(scoreValues, tempValues2)

eValues <- -scores2$E.Value
hyperScores <- scores2$HyperScore
tempValues1 <- eValues[eValues >eValueLimit & hyperScores > 0]
categorytemp <- character(length(tempValues1))
categoryI <- which(categories$id == 2)
categorytemp[] <- categoriesNames[categoryI]
eValueCategories <- c(eValueCategories, categorytemp)
mainCategorytemp <- character(length(tempValues1))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
eValueMainCategories <- c(eValueMainCategories, mainCategorytemp)
eValueValues <- c(eValueValues, tempValues1)
eValueRefValues <- c(eValueRefValues, median(tempValues1, na.rm = T))
tempValues2 <- scores2$HyperScore[eValues >eValueLimit & hyperScores > 0]
tempValues2 <- log10(tempValues2)
scoreValues <- c(scoreValues, tempValues2)

eValues <- -scores3$E.Value
hyperScores <- scores3$HyperScore
tempValues1 <- eValues[eValues >eValueLimit & hyperScores > 0]
categorytemp <- character(length(tempValues1))
categoryI <- which(categories$id == 3)
categorytemp[] <- categoriesNames[categoryI]
eValueCategories <- c(eValueCategories, categorytemp)
mainCategorytemp <- character(length(tempValues1))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
eValueMainCategories <- c(eValueMainCategories, mainCategorytemp)
eValueValues <- c(eValueValues, tempValues1)
eValueRefValues <- c(eValueRefValues, median(tempValues1, na.rm = T))
tempValues2 <- scores3$HyperScore[eValues >eValueLimit & hyperScores > 0]
tempValues2 <- log10(tempValues2)
scoreValues <- c(scoreValues, tempValues2)

eValues <- -scores5$E.Value
hyperScores <- scores5$HyperScore
tempValues1 <- eValues[eValues >eValueLimit & hyperScores > 0]
categorytemp <- character(length(tempValues1))
categoryI <- which(categories$id == 5)
categorytemp[] <- categoriesNames[categoryI]
eValueCategories <- c(eValueCategories, categorytemp)
mainCategorytemp <- character(length(tempValues1))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
eValueMainCategories <- c(eValueMainCategories, mainCategorytemp)
eValueValues <- c(eValueValues, tempValues1)
eValueRefValues <- c(eValueRefValues, median(tempValues1, na.rm = T))
tempValues2 <- scores5$HyperScore[eValues >eValueLimit & hyperScores > 0]
tempValues2 <- log10(tempValues2)
scoreValues <- c(scoreValues, tempValues2)

eValues <- -scores6$E.Value
hyperScores <- scores6$HyperScore
tempValues1 <- eValues[eValues >eValueLimit & hyperScores > 0]
categorytemp <- character(length(tempValues1))
categoryI <- which(categories$id == 6)
categorytemp[] <- categoriesNames[categoryI]
eValueCategories <- c(eValueCategories, categorytemp)
mainCategorytemp <- character(length(tempValues1))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
eValueMainCategories <- c(eValueMainCategories, mainCategorytemp)
eValueValues <- c(eValueValues, tempValues1)
eValueRefValues <- c(eValueRefValues, median(tempValues1, na.rm = T))
tempValues2 <- scores6$HyperScore[eValues >eValueLimit & hyperScores > 0]
tempValues2 <- log10(tempValues2)
scoreValues <- c(scoreValues, tempValues2)

eValues <- -scores8$E.Value
hyperScores <- scores8$HyperScore
tempValues1 <- eValues[eValues >eValueLimit & hyperScores > 0]
categorytemp <- character(length(tempValues1))
categoryI <- which(categories$id == 8)
categorytemp[] <- categoriesNames[categoryI]
eValueCategories <- c(eValueCategories, categorytemp)
mainCategorytemp <- character(length(tempValues1))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
eValueMainCategories <- c(eValueMainCategories, mainCategorytemp)
eValueValues <- c(eValueValues, tempValues1)
eValueRefValues <- c(eValueRefValues, median(tempValues1, na.rm = T))
tempValues2 <- scores8$HyperScore[eValues >eValueLimit & hyperScores > 0]
tempValues2 <- log10(tempValues2)
scoreValues <- c(scoreValues, tempValues2)

eValues <- -scores9$E.Value
hyperScores <- scores9$HyperScore
tempValues1 <- eValues[eValues >eValueLimit & hyperScores > 0]
categorytemp <- character(length(tempValues1))
categoryI <- which(categories$id == 9)
categorytemp[] <- categoriesNames[categoryI]
eValueCategories <- c(eValueCategories, categorytemp)
mainCategorytemp <- character(length(tempValues1))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
eValueMainCategories <- c(eValueMainCategories, mainCategorytemp)
eValueValues <- c(eValueValues, tempValues1)
eValueRefValues <- c(eValueRefValues, median(tempValues1, na.rm = T))
tempValues2 <- scores9$HyperScore[eValues >eValueLimit & hyperScores > 0]
tempValues2 <- log10(tempValues2)
scoreValues <- c(scoreValues, tempValues2)

eValues <- -scores10$E.Value
hyperScores <- scores10$HyperScore
tempValues1 <- eValues[eValues >eValueLimit & hyperScores > 0]
categorytemp <- character(length(tempValues1))
categoryI <- which(categories$id == 10)
categorytemp[] <- categoriesNames[categoryI]
eValueCategories <- c(eValueCategories, categorytemp)
mainCategorytemp <- character(length(tempValues1))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
eValueMainCategories <- c(eValueMainCategories, mainCategorytemp)
eValueValues <- c(eValueValues, tempValues1)
eValueRefValues <- c(eValueRefValues, median(tempValues1, na.rm = T))
tempValues2 <- scores10$HyperScore[eValues >eValueLimit & hyperScores > 0]
tempValues2 <- log10(tempValues2)
scoreValues <- c(scoreValues, tempValues2)

eValues <- -scores11$E.Value
hyperScores <- scores11$HyperScore
tempValues1 <- eValues[eValues >eValueLimit & hyperScores > 0]
categorytemp <- character(length(tempValues1))
categoryI <- which(categories$id == 11)
categorytemp[] <- categoriesNames[categoryI]
eValueCategories <- c(eValueCategories, categorytemp)
mainCategorytemp <- character(length(tempValues1))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
eValueMainCategories <- c(eValueMainCategories, mainCategorytemp)
eValueValues <- c(eValueValues, tempValues1)
eValueRefValues <- c(eValueRefValues, median(tempValues1, na.rm = T))
tempValues2 <- scores11$HyperScore[eValues >eValueLimit & hyperScores > 0]
tempValues2 <- log10(tempValues2)
scoreValues <- c(scoreValues, tempValues2)

eValues <- -scores15$E.Value
hyperScores <- scores15$HyperScore
tempValues1 <- eValues[eValues >eValueLimit & hyperScores > 0]
categorytemp <- character(length(tempValues1))
categoryI <- which(categories$id == 15)
categorytemp[] <- categoriesNames[categoryI]
eValueCategories <- c(eValueCategories, categorytemp)
mainCategorytemp <- character(length(tempValues1))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
eValueMainCategories <- c(eValueMainCategories, mainCategorytemp)
eValueValues <- c(eValueValues, tempValues1)
eValueRefValues <- c(eValueRefValues, median(tempValues1, na.rm = T))
tempValues2 <- scores15$HyperScore[eValues >eValueLimit & hyperScores > 0]
tempValues2 <- log10(tempValues2)
scoreValues <- c(scoreValues, tempValues2)

eValues <- -scores16$E.Value
hyperScores <- scores16$HyperScore
tempValues1 <- eValues[eValues >eValueLimit & hyperScores > 0]
categorytemp <- character(length(tempValues1))
categoryI <- which(categories$id == 16)
categorytemp[] <- categoriesNames[categoryI]
eValueCategories <- c(eValueCategories, categorytemp)
mainCategorytemp <- character(length(tempValues1))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
eValueMainCategories <- c(eValueMainCategories, mainCategorytemp)
eValueValues <- c(eValueValues, tempValues1)
eValueRefValues <- c(eValueRefValues, median(tempValues1, na.rm = T))
tempValues2 <- scores16$HyperScore[eValues >eValueLimit & hyperScores > 0]
tempValues2 <- log10(tempValues2)
scoreValues <- c(scoreValues, tempValues2)

eValues <- -scores17$E.Value
hyperScores <- scores17$HyperScore
tempValues1 <- eValues[eValues >eValueLimit & hyperScores > 0]
categorytemp <- character(length(tempValues1))
categoryI <- which(categories$id == 17)
categorytemp[] <- categoriesNames[categoryI]
eValueCategories <- c(eValueCategories, categorytemp)
mainCategorytemp <- character(length(tempValues1))
mainCategorytemp[] <- mainCategoriesNames[categoryI]
eValueMainCategories <- c(eValueMainCategories, mainCategorytemp)
eValueValues <- c(eValueValues, tempValues1)
eValueRefValues <- c(eValueRefValues, median(tempValues1, na.rm = T))
tempValues2 <- scores17$HyperScore[eValues >eValueLimit & hyperScores > 0]
tempValues2 <- log10(tempValues2)
scoreValues <- c(scoreValues, tempValues2)

eValueCategoriesFactors <- factor(eValueCategories, levels = allCategoriesSorted)
eValueMainCategoriesFactors <- factor(eValueMainCategories, levels = sortedMainCategoriesNames)


# Plot the distribution of peptides per precursor

eValueScorePlot <- ggplot()
eValueScorePlot <- eValueScorePlot + geom_point(aes(x=scoreValues, y=eValueValues, col = eValueCategoriesFactors), na.rm = T, alpha = 0.5)
eValueScorePlot <- eValueScorePlot + geom_smooth(aes(x=scoreValues, y=eValueValues, col = eValueCategoriesFactors), se = FALSE, method = "lm", na.rm = T, alpha = 0.5)
eValueScorePlot <- eValueScorePlot + labs(x = "", y = "E-value [-log10]", fill="", col="")
eValueScorePlot <- eValueScorePlot + theme(axis.text.x = element_text(angle = 90, hjust = 1))
eValueScorePlot <- eValueScorePlot + scale_color_grey()
eValueScorePlot <- eValueScorePlot + guides(fill = F)
plot(eValueScorePlot)
