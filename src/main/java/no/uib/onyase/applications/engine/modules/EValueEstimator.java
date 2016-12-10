package no.uib.onyase.applications.engine.modules;

import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.experiment.identification.psm_scoring.psm_scores.HyperScore;
import com.compomics.util.experiment.identification.spectrum_assumptions.PeptideAssumption;
import com.compomics.util.math.HistogramUtils;
import com.compomics.util.waiting.WaitingHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.commons.math.util.FastMath;

/**
 * The e-value estimator estimates the e-values for sets of PSMs-
 *
 * @author Marc Vaudel
 */
public class EValueEstimator {

    /**
     * Map of the PSMs to score
     */
    private HashMap<String, HashMap<String, PeptideAssumption>> psmMap;
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
    public EValueEstimator(WaitingHandler waitingHandler, ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        this.waitingHandler = waitingHandler;
    }

    /**
     * Estimates the e-values of the PSMs and sets -log10(e-value) as score.
     *
     * @param psmMap the map of PSMs to get the e-value for indexed by spectrum
     * and peptide key
     * @param nThreads the number of threads to use
     *
     * @throws InterruptedException exception thrown if a threading issue
     * occurs.
     */
    public void estimateEValues(HashMap<String, HashMap<String, PeptideAssumption>> psmMap, int nThreads) throws InterruptedException {

        this.psmMap = psmMap;

        // Iterate all protein sequences in the factory and get the possible PSMs
        waitingHandler.setSecondaryProgressCounterIndeterminate(false);
        waitingHandler.setMaxSecondaryProgressCounter(psmMap.size());
        Iterator<String> spectrumTitlesIterator = psmMap.keySet().iterator();
        ArrayList<SpectrumProcessor> spectrumProcessors = new ArrayList<SpectrumProcessor>(nThreads);
        ExecutorService pool = Executors.newFixedThreadPool(nThreads);
        for (int i = 0; i < nThreads; i++) {
            SpectrumProcessor spectrumProcessor = new SpectrumProcessor(spectrumTitlesIterator);
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
                    ArrayList<String> threadMissingValues = spectrumProcessor.getMissingValues();
                    if (!threadMissingValues.isEmpty()) {
                        MissingValuesRunnable missingValuesRunnable = new MissingValuesRunnable(threadMissingValues, defaultA, defaultB);
                        pool.submit(missingValuesRunnable);
                    }
                }
                pool.shutdown();
                if (!pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS)) {
                    waitingHandler.appendReport("Mapping tags timed out. Please contact the developers.", true, true);
                }
            }

        }
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
         * A list where to store the missing values.
         */
        private final ArrayList<String> missingValues = new ArrayList<String>();

        /**
         * Constructor.
         *
         * @param spectrumTitlesIterator An iterator for the spectra to process
         */
        public SpectrumProcessor(Iterator<String> spectrumTitlesIterator) {
            this.spectrumTitlesIterator = spectrumTitlesIterator;
        }

        @Override
        public void run() {

            try {

                boolean progress = true;

                // Iterate the PSMs and set a p-value
                while (spectrumTitlesIterator.hasNext()) {

                    // get the PSMs for the next spectrum
                    String spectrumTitle = spectrumTitlesIterator.next();
                    HashMap<String, PeptideAssumption> peptideAssumptions = psmMap.get(spectrumTitle);

                    // Gather all scores
                    ArrayList<Double> psmScores = new ArrayList<Double>(peptideAssumptions.size());
                    for (PeptideAssumption peptideAssumption : peptideAssumptions.values()) {
                        Double score = peptideAssumption.getRawScore();
                        if (!score.equals(0.0)) {
                            psmScores.add(peptideAssumption.getRawScore());
                        }
                    }

                    // Get e-value map
                    HashMap<Double, Double> eValueMap = hyperScore.getEValueHistogram(psmScores);

                    // See if an interpolation was possible
                    if (eValueMap != null) {
                        // Set e-values
                        for (PeptideAssumption peptideAssumption : peptideAssumptions.values()) {
                            Double score = peptideAssumption.getRawScore();
                            if (!score.equals(0.0)) {
                                Double eValue = eValueMap.get(score);
                                peptideAssumption.setScore(eValue);
                            } else {
                                peptideAssumption.setScore(peptideAssumptions.size());
                            }
                        }
                        progress = true;
                    } else {
                        // Save this spectrum as not processed
                        missingValues.add(spectrumTitle);
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
        public ArrayList<String> getMissingValues() {
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
         * The list of titles of spectra where to impute missing values.
         */
        private final ArrayList<String> missingValues;
        /**
         * The slope to use for the interpolation.
         */
        private final Double defaultA;
        /**
         * The offset to use for the interpolation.
         */
        private final Double defaultB;

        /**
         * Constructor.
         *
         * @param missingValues a list of titles of spectra where to impute
         * missing values
         * @param defaultA the slope to use for the interpolation
         * @param defaultB the offset to use for the interpolation
         */
        public MissingValuesRunnable(ArrayList<String> missingValues, Double defaultA, Double defaultB) {
            this.missingValues = missingValues;
            this.defaultA = defaultA;
            this.defaultB = defaultB;
        }

        @Override
        public void run() {

            try {

                boolean progress = false;

                // Iterate the PSMs with missing values and set a p-value
                for (String spectrumTitle : missingValues) {

                    HashMap<String, PeptideAssumption> peptideAssumptions = psmMap.get(spectrumTitle);

                    // Use a default interpolation
                    for (PeptideAssumption peptideAssumption : peptideAssumptions.values()) {
                        Double score = peptideAssumption.getRawScore();
                        if (!score.equals(0.0)) {
                            score = FastMath.log10(score);
                            Double interpolation = HyperScore.getInterpolation(score, defaultA, defaultB);
                            peptideAssumption.setScore(interpolation);
                        } else {
                            peptideAssumption.setScore(peptideAssumptions.size());
                        }
                    }

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
