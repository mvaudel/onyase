library(ggplot2)

# violin plots

categories <- c("Default","Isoforms","Trembl","Vertebrates","Proteogenomics","4mc","semispecific","unspecific","Cmm","Phospho","AB-Y","ABC-XYZ","MS1","MS2","MS1 MS2","-4 +4 Da","1-4+","1-6+")

violinCategories <- c()
violinMainCategories <- c()
violinValues <- c()
categorytemp <- character(length(precursorHistogram0$nPeptides[]))
categorytemp[] <- categories[1]
violinCategories <- c(violinCategories, categorytemp)
mainCategorytemp <- character(length(precursorHistogram0$nPeptides[]))
mainCategorytemp[] <- "Default"
violinMainCategories <- c(violinMainCategories, mainCategorytemp)
violinValues <- c(violinValues, log10(precursorHistogram0$nPeptides))
categorytemp <- character(length(precursorHistogram1$nPeptides))
categorytemp[] <- categories[2]
violinCategories <- c(violinCategories, categorytemp)
mainCategorytemp <- character(length(precursorHistogram1$nPeptides[]))
mainCategorytemp[] <- "Database"
violinMainCategories <- c(violinMainCategories, mainCategorytemp)
violinValues <- c(violinValues, log10(precursorHistogram1$nPeptides))
categorytemp <- character(length(precursorHistogram2$nPeptides))
categorytemp[] <- categories[3]
violinCategories <- c(violinCategories, categorytemp)
mainCategorytemp <- character(length(precursorHistogram2$nPeptides[]))
mainCategorytemp[] <- "Database"
violinMainCategories <- c(violinMainCategories, mainCategorytemp)
violinValues <- c(violinValues, log10(precursorHistogram2$nPeptides))
categorytemp <- character(length(precursorHistogram3$nPeptides))
categorytemp[] <- categories[4]
violinCategories <- c(violinCategories, categorytemp)
mainCategorytemp <- character(length(precursorHistogram3$nPeptides[]))
mainCategorytemp[] <- "Database"
violinMainCategories <- c(violinMainCategories, mainCategorytemp)
violinValues <- c(violinValues, log10(precursorHistogram3$nPeptides))
# categorytemp <- character(length(precursorHistogram4$nPeptides))
# categorytemp[] <- categories[5]
# violinCategories <- c(violinCategories, categorytemp)
# mainCategorytemp <- character(length(precursorHistogram4$nPeptides[]))
# mainCategorytemp[] <- "Database"
# violinMainCategories <- c(violinMainCategories, mainCategorytemp)
# violinValues <- c(violinValues, log10(precursorHistogram4$nPeptides))
categorytemp <- character(length(precursorHistogram5$nPeptides))
categorytemp[] <- categories[6]
violinCategories <- c(violinCategories, categorytemp)
mainCategorytemp <- character(length(precursorHistogram5$nPeptides[]))
mainCategorytemp[] <- "Enzymaticity"
violinMainCategories <- c(violinMainCategories, mainCategorytemp)
violinValues <- c(violinValues, log10(precursorHistogram5$nPeptides))
# categorytemp <- character(length(precursorHistogram6$nPeptides))
# categorytemp[] <- categories[7]
# violinCategories <- c(violinCategories, categorytemp)
# mainCategorytemp <- character(length(precursorHistogram6$nPeptides[]))
# mainCategorytemp[] <- "Enzymaticity"
# violinMainCategories <- c(violinMainCategories, mainCategorytemp)
# violinValues <- c(violinValues, log10(precursorHistogram6$nPeptides))
# categorytemp <- character(length(precursorHistogram7$nPeptides))
# categorytemp[] <- categories[8]
# violinCategories <- c(violinCategories, categorytemp)
# mainCategorytemp <- character(length(precursorHistogram7$nPeptides[]))
# mainCategorytemp[] <- "Enzymaticity"
# violinMainCategories <- c(violinMainCategories, mainCategorytemp)
# violinValues <- c(violinValues, log10(precursorHistogram7$nPeptides))
categorytemp <- character(length(precursorHistogram8$nPeptides))
categorytemp[] <- categories[9]
violinCategories <- c(violinCategories, categorytemp)
mainCategorytemp <- character(length(precursorHistogram8$nPeptides[]))
mainCategorytemp[] <- "Modification"
violinMainCategories <- c(violinMainCategories, mainCategorytemp)
violinValues <- c(violinValues, log10(precursorHistogram8$nPeptides))
# categorytemp <- character(length(precursorHistogram9$nPeptides))
# categorytemp[] <- categories[10]
# violinCategories <- c(violinCategories, categorytemp)
# mainCategorytemp <- character(length(precursorHistogram9$nPeptides[]))
# mainCategorytemp[] <- "Modification"
# violinMainCategories <- c(violinMainCategories, mainCategorytemp)
# violinValues <- c(violinValues, log10(precursorHistogram9$nPeptides))
categorytemp <- character(length(precursorHistogram10$nPeptides))
categorytemp[] <- categories[11]
violinCategories <- c(violinCategories, categorytemp)
mainCategorytemp <- character(length(precursorHistogram10$nPeptides[]))
mainCategorytemp[] <- "Ions"
violinMainCategories <- c(violinMainCategories, mainCategorytemp)
violinValues <- c(violinValues, log10(precursorHistogram10$nPeptides))
categorytemp <- character(length(precursorHistogram11$nPeptides))
categorytemp[] <- categories[12]
violinCategories <- c(violinCategories, categorytemp)
mainCategorytemp <- character(length(precursorHistogram11$nPeptides[]))
mainCategorytemp[] <- "Ions"
violinMainCategories <- c(violinMainCategories, mainCategorytemp)
violinValues <- c(violinValues, log10(precursorHistogram11$nPeptides))
# categorytemp <- character(length(precursorHistogram12$nPeptides))
# categorytemp[] <- categories[13]
# violinCategories <- c(violinCategories, categorytemp)
# mainCategorytemp <- character(length(precursorHistogram12$nPeptides[]))
# mainCategorytemp[] <- "Toleance"
# violinMainCategories <- c(violinMainCategories, mainCategorytemp)
# violinValues <- c(violinValues, log10(precursorHistogram12$nPeptides))
# categorytemp <- character(length(precursorHistogram13$nPeptides))
# categorytemp[] <- categories[14]
# violinCategories <- c(violinCategories, categorytemp)
# mainCategorytemp <- character(length(precursorHistogram13$nPeptides[]))
# mainCategorytemp[] <- "Toleance"
# violinMainCategories <- c(violinMainCategories, mainCategorytemp)
# violinValues <- c(violinValues, log10(precursorHistogram13$nPeptides))
# categorytemp <- character(length(precursorHistogram14$nPeptides))
# categorytemp[] <- "categories[15]
# violinCategories <- c(violinCategories, categorytemp)
# mainCategorytemp <- character(length(precursorHistogram14$nPeptides[]))
# mainCategorytemp[] <- "Toleance"
# violinMainCategories <- c(violinMainCategories, mainCategorytemp)
# violinValues <- c(violinValues, log10(precursorHistogram14$nPeptides))
categorytemp <- character(length(precursorHistogram15$nPeptides))
categorytemp[] <- categories[16]
violinCategories <- c(violinCategories, categorytemp)
mainCategorytemp <- character(length(precursorHistogram15$nPeptides[]))
mainCategorytemp[] <- "Isotopes"
violinMainCategories <- c(violinMainCategories, mainCategorytemp)
violinValues <- c(violinValues, log10(precursorHistogram15$nPeptides))
categorytemp <- character(length(precursorHistogram16$nPeptides))
categorytemp[] <- categories[17]
violinCategories <- c(violinCategories, categorytemp)
mainCategorytemp <- character(length(precursorHistogram16$nPeptides[]))
mainCategorytemp[] <- "Charges"
violinMainCategories <- c(violinMainCategories, mainCategorytemp)
violinValues <- c(violinValues, log10(precursorHistogram16$nPeptides))
categorytemp <- character(length(precursorHistogram17$nPeptides))
categorytemp[] <- categories[18]
violinCategories <- c(violinCategories, categorytemp)
mainCategorytemp <- character(length(precursorHistogram17$nPeptides[]))
mainCategorytemp[] <- "Charges"
violinMainCategories <- c(violinMainCategories, mainCategorytemp)
violinValues <- c(violinValues, log10(precursorHistogram17$nPeptides))
violinCategoriesSorted <- factor(violinCategories, levels = categories)

precursorHistogramPlot <- ggplot()
precursorHistogramPlot <- precursorHistogramPlot + geom_violin(aes(x=violinCategoriesSorted, y=violinValues, fill = violinMainCategories))
precursorHistogramPlot <- precursorHistogramPlot + geom_boxplot(aes(x=violinCategoriesSorted, y=violinValues, fill = violinMainCategories), width = 0.1, outlier.shape = NA)
precursorHistogramPlot <- precursorHistogramPlot + labs(x = "Category", y = "# Peptides per Precursor [log10]", fill="", col="")
precursorHistogramPlot <- precursorHistogramPlot + theme(axis.text.x = element_text(angle = 90, hjust = 1))
plot(precursorHistogramPlot)
