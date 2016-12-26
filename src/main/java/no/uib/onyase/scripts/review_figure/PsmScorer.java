package no.uib.onyase.scripts.review_figure;

import no.uib.onyase.applications.engine.model.PeptideDraft;
import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.psm_scoring.psm_scores.HyperScore;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationSettings;
import com.compomics.util.experiment.identification.spectrum_annotation.SpecificAnnotationSettings;
import com.compomics.util.experiment.identification.spectrum_annotation.spectrum_annotators.PeptideSpectrumAnnotator;
import com.compomics.util.experiment.identification.spectrum_assumptions.PeptideAssumption;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Spectrum;
import com.compomics.util.experiment.massspectrometry.SpectrumFactory;
import com.compomics.util.math.HistogramUtils;
import com.compomics.util.preferences.IdentificationParameters;
import com.compomics.util.waiting.WaitingHandler;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import no.uib.onyase.applications.engine.modules.PeptideModificationsIterator;
import no.uib.onyase.applications.engine.modules.peptide_modification_iterators.MultipleModificationsIterators;
import no.uib.onyase.applications.engine.modules.peptide_modification_iterators.OverlappingModificationsIterator;
import no.uib.onyase.applications.engine.modules.peptide_modification_iterators.SingleModificationIterator;
import org.apache.commons.math.util.FastMath;

/**
 * The e-value estimator estimates the e-values for sets of PSMs-
 *
 * @author Marc Vaudel
 */
public class PsmScorer {

    /**
     * Separator for the columns.
     */
    public final static char separator = ' ';
    /**
     * The end of line separator.
     */
    public static final String END_LINE = System.getProperty("line.separator");
    /**
     * Map of the PSMs to score
     */
    private HashMap<String, HashMap<String, PeptideDraft>> psmMap;
    /**
     * A handler for the exceptions.
     */
    private ExceptionHandler exceptionHandler;
    /**
     * A waiting handler providing feedback to the user and allowing canceling
     * the process.
     */
    private WaitingHandler waitingHandler;

    /**
     * Constructor.
     *
     * @param waitingHandler a waiting handler providing feedback to the user
     * and allowing canceling the process
     * @param exceptionHandler a handler for the exceptions
     */
    public PsmScorer(WaitingHandler waitingHandler, ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        this.waitingHandler = waitingHandler;
    }

    /**
     * Estimates the e-values of the PSMs and sets -log10(e-value) as score.
     *
     * @param psmMap the map of PSMs to get the e-value for indexed by spectrum
     * and peptide key
     * @param spectrumFileName the name of the spectrum file scored
     * @param identificationParameters the identification parameters
     * @param maxPtms the preferred number of sites to consider for every PTM
     * @param nThreads the number of threads to use
     * @param allHitsFile file where to save all hits
     * @param bestHitsFile file where to save the best hits
     *
     * @throws InterruptedException exception thrown if a threading issue
     * occurs.
     */
    public void estimateScores(String spectrumFileName, HashMap<String, HashMap<String, PeptideDraft>> psmMap, IdentificationParameters identificationParameters, int maxPtms, int nThreads, File allHitsFile, File bestHitsFile) throws InterruptedException, IOException {

        this.psmMap = psmMap;

        // Create an exporter
        TxtExporter textExporter = new TxtExporter(bestHitsFile, allHitsFile, identificationParameters);

        // Iterate all protein sequences in the factory and get the possible PSMs
        waitingHandler.setSecondaryProgressCounterIndeterminate(false);
        waitingHandler.setMaxSecondaryProgressCounter(psmMap.size());
        Iterator<String> spectrumTitlesIterator = psmMap.keySet().iterator();
        ArrayList<SpectrumProcessor> spectrumProcessors = new ArrayList<SpectrumProcessor>(nThreads);
        ExecutorService pool = Executors.newFixedThreadPool(nThreads);
        for (int i = 0; i < nThreads; i++) {
            SpectrumProcessor spectrumProcessor = new SpectrumProcessor(spectrumFileName, spectrumTitlesIterator, identificationParameters, maxPtms, textExporter);
            pool.submit(spectrumProcessor);
            spectrumProcessors.add(spectrumProcessor);
        }
        pool.shutdown();
        if (!pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS)) {
            waitingHandler.appendReport("E-value estimation timed out. Please contact the developers.", true, true);
        }

        // See if we have missing values
        boolean missingValues = false;
        for (SpectrumProcessor spectrumProcessor : spectrumProcessors) {
            if (!spectrumProcessor.getMissingValues().isEmpty()) {
                missingValues = true;
                break;
            }
        }

        if (missingValues) {
            // Gather the interpolation values as histograms
            ArrayList<HashMap<Double, Integer>> aHistograms = new ArrayList<HashMap<Double, Integer>>(nThreads);
            ArrayList<HashMap<Double, Integer>> bHistograms = new ArrayList<HashMap<Double, Integer>>(nThreads);
            for (SpectrumProcessor spectrumProcessor : spectrumProcessors) {
                HyperScore hyperScore = spectrumProcessor.getHyperScore();
                aHistograms.add(hyperScore.getAs());
                bHistograms.add(hyperScore.getBs());
            }
            HashMap<Double, Integer> aHistogram = HistogramUtils.mergeHistograms(aHistograms);
            HashMap<Double, Integer> bHistogram = HistogramUtils.mergeHistograms(bHistograms);

            // If interpolation values were found, impute using the median
            if (!aHistogram.isEmpty() && !bHistogram.isEmpty()) {
                Double defaultA = HistogramUtils.getMedianValue(aHistogram);
                Double defaultB = HistogramUtils.getMedianValue(bHistogram);
                pool = Executors.newFixedThreadPool(nThreads);
                for (SpectrumProcessor spectrumProcessor : spectrumProcessors) {
                    HashMap<String, ArrayList<PeptideAssumption>> threadMissingValues = spectrumProcessor.getMissingValues();
                    if (!threadMissingValues.isEmpty()) {
                        MissingValuesRunnable missingValuesRunnable = new MissingValuesRunnable(spectrumFileName, threadMissingValues, defaultA, defaultB, textExporter);
                        pool.submit(missingValuesRunnable);
                    }
                }
                pool.shutdown();
                if (!pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS)) {
                    waitingHandler.appendReport("Mapping tags timed out. Please contact the developers.", true, true);
                }
            }
        }

        textExporter.close();
    }

    /**
     * Private runnable to process the scores of a spectrum.
     */
    private class SpectrumProcessor implements Runnable {

        /**
         * Iterator for the spectrum titles.
         */
        private final Iterator<String> spectrumTitlesIterator;
        /**
         * The hyperscore to use.
         */
        private final HyperScore hyperScore = new HyperScore();
        /**
         * A spectrum annotator.
         */
        private PeptideSpectrumAnnotator peptideSpectrumAnnotator = new PeptideSpectrumAnnotator();
        /**
         * A list where to store the missing values.
         */
        private final HashMap<String, ArrayList<PeptideAssumption>> missingValues = new HashMap<String, ArrayList<PeptideAssumption>>();
        /**
         * The name of the spectrum file scored.
         */
        private final String spectrumFileName;
        /**
         * The identification parameters.
         */
        private final IdentificationParameters identificationParameters;
        /**
         * The PTM factory.
         */
        private PTMFactory ptmFactory = PTMFactory.getInstance();
        /**
         * The spectrum factory.
         */
        private SpectrumFactory spectrumFactory = SpectrumFactory.getInstance();
        /**
         * The preferred number of sites to consider for every PTM.
         */
        private int maxPtms;
        /**
         * The exporter to use to export hits.
         */
        private TxtExporter textExporter;

        /**
         * Constructor.
         *
         * @param spectrumTitlesIterator an iterator for the spectra to process
         * @param spectrumFileName the name of the spectrum file scored
         * @param identificationParameters the identification parameters
         * @param maxPtms the preferred number of sites to consider for every
         * PTM
         * @param textExporter the exporter to use to export hits
         */
        public SpectrumProcessor(String spectrumFileName, Iterator<String> spectrumTitlesIterator, IdentificationParameters identificationParameters, int maxPtms, TxtExporter textExporter) {
            this.spectrumTitlesIterator = spectrumTitlesIterator;
            this.spectrumFileName = spectrumFileName;
            this.identificationParameters = identificationParameters;
            this.maxPtms = maxPtms;
            this.textExporter = textExporter;
        }

        @Override
        public void run() {

            try {
                // Update the progress only every second missing value
                boolean progress = true;

                // Get the annotation settings for spectrum annotation
                AnnotationSettings annotationSettings = identificationParameters.getAnnotationPreferences();

                // See what modifications where searched for and if their target can overlap
                SearchParameters searchParameters = identificationParameters.getSearchParameters();
                PtmSettings ptmSettings = searchParameters.getPtmSettings();
                int nVariableModifications = ptmSettings.getVariableModifications().size();
                HashMap<String, HashSet<String>> overlappingModifications = new HashMap<String, HashSet<String>>(nVariableModifications);
                for (String ptmName : ptmSettings.getVariableModifications()) {
                    PTM ptm = ptmFactory.getPTM(ptmName);
                    if (ptm.getType() == PTM.MODAA) {
                        for (String ptmName2 : ptmSettings.getVariableModifications()) {
                            if (!ptmName.equals(ptmName2)) {
                                PTM ptm2 = ptmFactory.getPTM(ptmName2);
                                if (ptm2.getType() == PTM.MODAA) {
                                    HashSet<Character> aas1 = ptm.getPattern().getAminoAcidsAtTargetSet();
                                    for (Character aa2 : ptm2.getPattern().getAminoAcidsAtTarget()) {
                                        if (aas1.contains(aa2)) {
                                            HashSet<String> conflicts = overlappingModifications.get(ptmName);
                                            if (conflicts == null) {
                                                conflicts = new HashSet<String>(1);
                                                overlappingModifications.put(ptmName, conflicts);
                                            }
                                            conflicts.add(ptmName2);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                ArrayList<String> orderedModificationsName = new ArrayList<String>(ptmSettings.getVariableModifications());
                Collections.sort(orderedModificationsName);
                ArrayList<String> orderedPeptideModificationsName = new ArrayList<String>(orderedModificationsName.size());

                // Iterate all spectra where a peptide could be found
                while (spectrumTitlesIterator.hasNext()) {
                    String spectrumTitle = spectrumTitlesIterator.next();
                    String spectrumKey = Spectrum.getSpectrumKey(spectrumFileName, spectrumTitle);
                    HashMap<String, PeptideDraft> assumptions = psmMap.get(spectrumTitle);
                    ArrayList<PeptideAssumption> peptideAssumptions = new ArrayList<PeptideAssumption>(assumptions.size());
                    ArrayList<Double> scores = new ArrayList<Double>(assumptions.size());
                    for (PeptideDraft peptideDraft : assumptions.values()) {
                        HashMap<String, Integer> modificationOccurrence = peptideDraft.getVariableModifications();
                        if (modificationOccurrence == null) {

                            // Create a peptide
                            Peptide modifiedPeptide = new Peptide(peptideDraft.getSequence(), null);

                            // Estimate the score
                            PeptideAssumption peptideAssumption = new PeptideAssumption(modifiedPeptide, new Charge(Charge.PLUS, peptideDraft.getCharge()));
                            MSnSpectrum spectrum = (MSnSpectrum) spectrumFactory.getSpectrum(spectrumFileName, spectrumTitle);
                            SpecificAnnotationSettings specificAnnotationSettings = annotationSettings.getSpecificAnnotationPreferences(spectrumKey, peptideAssumption, identificationParameters.getSequenceMatchingPreferences(), identificationParameters.getPtmScoringPreferences().getSequenceMatchingPreferences());
                            ArrayList<IonMatch> ionMatches = peptideSpectrumAnnotator.getSpectrumAnnotation(annotationSettings, specificAnnotationSettings, spectrum, modifiedPeptide);
                            Double score = hyperScore.getScore(modifiedPeptide, spectrum, annotationSettings, specificAnnotationSettings, ionMatches);
                            peptideAssumption.setRawScore(score);
                            peptideAssumptions.add(peptideAssumption);
                            scores.add(score);
                            FigureMetrics figureMetrics = new FigureMetrics();
                            figureMetrics.setIsDecoy(peptideDraft.isDecoy());
                            figureMetrics.setIsTarget(peptideDraft.isTarget());
                            peptideAssumption.addUrParam(figureMetrics);

                        } else {

                            // Get the modification sites
                            HashMap<String, Integer[]> possibleModificationSites = peptideDraft.getVariableModificationsSites();

                            // Create an iterator for the possible sites
                            PeptideModificationsIterator peptideModificationsIterator;
                            if (modificationOccurrence.size() == 1) {
                                String modificationName = modificationOccurrence.keySet().iterator().next();
                                Integer[] possibleSites = possibleModificationSites.get(modificationName);
                                Integer occurrence = modificationOccurrence.get(modificationName);
                                peptideModificationsIterator = new SingleModificationIterator(possibleSites, occurrence, modificationName, maxPtms);
                            } else {
                                boolean overlap = false;
                                for (String modification1 : modificationOccurrence.keySet()) {
                                    HashSet<String> potentialConflicts = overlappingModifications.get(modification1);
                                    if (potentialConflicts != null) {
                                        for (String modification2 : modificationOccurrence.keySet()) {
                                            if (potentialConflicts.contains(modification2)) {
                                                overlap = true;
                                                break;
                                            }
                                        }
                                    }
                                    if (overlap) {
                                        break;
                                    }
                                }
                                orderedPeptideModificationsName.clear();
                                for (String modification : orderedModificationsName) {
                                    if (modificationOccurrence.keySet().contains(modification)) {
                                        orderedPeptideModificationsName.add(modification);
                                    }
                                }
                                if (overlap) {
                                    peptideModificationsIterator = new OverlappingModificationsIterator(modificationOccurrence, possibleModificationSites, orderedPeptideModificationsName, maxPtms);
                                } else {
                                    peptideModificationsIterator = new MultipleModificationsIterators(modificationOccurrence, possibleModificationSites, orderedPeptideModificationsName, maxPtms);
                                }
                            }

                            // Go through all possibilities
                            while (peptideModificationsIterator.hasNext()) {

                                // Create a modified peptide
                                HashMap<String, int[]> modificationSitesMap = peptideModificationsIterator.next();
                                ArrayList<ModificationMatch> modificationMatches = new ArrayList<ModificationMatch>();
                                for (String modificationName : modificationSitesMap.keySet()) {
                                    int[] sites = modificationSitesMap.get(modificationName);
                                    for (Integer site : sites) {
                                        ModificationMatch modificationMatch = new ModificationMatch(modificationName, true, site);
                                        modificationMatch.setConfident(true);
                                        modificationMatches.add(modificationMatch);
                                    }
                                }
                                Peptide modifiedPeptide = new Peptide(peptideDraft.getSequence(), modificationMatches);

                                // Estimate the score
                                PeptideAssumption peptideAssumption = new PeptideAssumption(modifiedPeptide, new Charge(Charge.PLUS, peptideDraft.getCharge()));
                                MSnSpectrum spectrum = (MSnSpectrum) spectrumFactory.getSpectrum(spectrumFileName, spectrumTitle);
                                SpecificAnnotationSettings specificAnnotationSettings = annotationSettings.getSpecificAnnotationPreferences(spectrumKey, peptideAssumption, identificationParameters.getSequenceMatchingPreferences(), identificationParameters.getPtmScoringPreferences().getSequenceMatchingPreferences());
                                ArrayList<IonMatch> ionMatches = peptideSpectrumAnnotator.getSpectrumAnnotation(annotationSettings, specificAnnotationSettings, spectrum, modifiedPeptide);
                                Double score = hyperScore.getScore(modifiedPeptide, spectrum, annotationSettings, specificAnnotationSettings, ionMatches);
                                peptideAssumption.setRawScore(score);
                                peptideAssumptions.add(peptideAssumption);
                                scores.add(score);
                                FigureMetrics figureMetrics = new FigureMetrics();
                                figureMetrics.setIsDecoy(peptideDraft.isDecoy());
                                figureMetrics.setIsTarget(peptideDraft.isTarget());
                                peptideAssumption.addUrParam(figureMetrics);
                            }
                        }
                    }

                    // Get e-value map
                    HashMap<Double, Double> eValueMap = hyperScore.getEValueHistogram(scores);

                    // See if an interpolation was possible
                    if (eValueMap != null) {

                        // Set e-values
                        for (PeptideAssumption peptideAssumption : peptideAssumptions) {
                            Double score = peptideAssumption.getRawScore();
                            if (!score.equals(0.0)) {
                                Double eValue = eValueMap.get(score);
                                peptideAssumption.setScore(eValue);
                            } else {
                                peptideAssumption.setScore(peptideAssumptions.size());
                            }
                        }
                        progress = true;

                        // Export hits
                        textExporter.writeAssumptions(spectrumFileName, spectrumTitle, peptideAssumptions);

                    } else {
                        // Save this spectrum and assumptions as not processed
                        missingValues.put(spectrumTitle, peptideAssumptions);
                        progress = !progress;
                    }

                    // check for cancellation and update progress
                    if (waitingHandler.isRunCanceled()) {
                        return;
                    } else if (progress) {
                        waitingHandler.increaseSecondaryProgressCounter();
                    }

                }
            } catch (NoSuchElementException exception) {
                // the last spectrum got processed by another thread.
            } catch (Exception e) {
                if (!waitingHandler.isRunCanceled()) {
                    exceptionHandler.catchException(e);
                    waitingHandler.setRunCanceled();
                }
            }
        }

        /**
         * Returns a list containing the titles of the spectra with missing
         * values.
         *
         * @return a list containing the titles of the spectra with missing
         * values
         */
        public HashMap<String, ArrayList<PeptideAssumption>> getMissingValues() {
            return missingValues;
        }

        /**
         * Returns the HyperScore used to estimate the e-values.
         *
         * @return the HyperScore used to estimate the e-values
         */
        public HyperScore getHyperScore() {
            return hyperScore;
        }
    }

    /**
     * Private runnable to impute missing values.
     */
    private class MissingValuesRunnable implements Runnable {

        /**
         * The name of the spectrum file scored.
         */
        private final String spectrumFileName;
        /**
         * The list of titles of spectra where to impute missing values.
         */
        private final HashMap<String, ArrayList<PeptideAssumption>> missingValues;
        /**
         * The slope to use for the interpolation.
         */
        private final Double defaultA;
        /**
         * The offset to use for the interpolation.
         */
        private final Double defaultB;
        /**
         * The exporter to use to export hits.
         */
        private TxtExporter textExporter;

        /**
         * Constructor.
         *
         * @param spectrumFileName the name of the spectrum file scored
         * @param missingValues a list of titles of spectra where to impute
         * missing values
         * @param defaultA the slope to use for the interpolation
         * @param defaultB the offset to use for the interpolation
         * @param textExporter the exporter to use to export hits
         */
        public MissingValuesRunnable(String spectrumFileName, HashMap<String, ArrayList<PeptideAssumption>> missingValues, Double defaultA, Double defaultB, TxtExporter textExporter) {
            this.spectrumFileName = spectrumFileName;
            this.missingValues = missingValues;
            this.defaultA = defaultA;
            this.defaultB = defaultB;
            this.textExporter = textExporter;
        }

        @Override
        public void run() {

            try {

                // Update every second spectrum
                boolean progress = false;

                // Iterate the PSMs with missing values and set a p-value
                for (String spectrumTitle : missingValues.keySet()) {

                    ArrayList<PeptideAssumption> peptideAssumptions = missingValues.get(spectrumTitle);

                    // Use a default interpolation
                    for (PeptideAssumption peptideAssumption : peptideAssumptions) {
                        Double score = peptideAssumption.getRawScore();
                        if (!score.equals(0.0)) {
                            score = FastMath.log10(score);
                            Double interpolation = HyperScore.getInterpolation(score, defaultA, defaultB);
                            peptideAssumption.setScore(interpolation);
                        } else {
                            peptideAssumption.setScore((double) peptideAssumptions.size());
                        }
                    }

                    // Export hits
                    textExporter.writeAssumptions(spectrumFileName, spectrumTitle, peptideAssumptions);

                    // check for cancellation and update progress
                    progress = !progress;
                    if (waitingHandler.isRunCanceled()) {
                        return;
                    } else if (progress) {
                        waitingHandler.increaseSecondaryProgressCounter();
                    }
                }
            } catch (Exception e) {
                if (!waitingHandler.isRunCanceled()) {
                    exceptionHandler.catchException(e);
                    waitingHandler.setRunCanceled();
                }
            }
        }
    }
}
