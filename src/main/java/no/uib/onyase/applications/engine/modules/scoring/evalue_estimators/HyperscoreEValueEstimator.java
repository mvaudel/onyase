package no.uib.onyase.applications.engine.modules.scoring.evalue_estimators;

import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.experiment.identification.psm_scoring.psm_scores.HyperScore;
import com.compomics.util.math.BasicMathFunctions;
import com.compomics.util.waiting.WaitingHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import no.uib.onyase.applications.engine.model.Psm;
import no.uib.onyase.utils.TitlesIterator;
import org.apache.commons.math.util.FastMath;
import no.uib.onyase.applications.engine.modules.scoring.EValueEstimator;

/**
 * This class estimates e-values from hyperscores.
 *
 * @author Marc Vaudel
 */
public class HyperscoreEValueEstimator implements EValueEstimator {

    /**
     * Map of the PSMs to score
     */
    private HashMap<String, HashMap<Long, Psm>> PsmsMap;
    /**
     * A handler for the exceptions.
     */
    private final ExceptionHandler exceptionHandler;
    /**
     * A waiting handler providing feedback to the user and allowing canceling
     * the process.
     */
    private final WaitingHandler waitingHandler;
    /**
     * Map of coefficients for every spectrum.
     */
    private HashMap<String, double[]> interpolationValuesMap;
    /**
     * The default interpolation values to use.
     */
    private double[] defaultInterpolationValues;

    /**
     * Constructor.
     *
     * @param waitingHandler a waiting handler providing feedback to the user
     * and allowing canceling the process
     * @param exceptionHandler a handler for the exceptions
     */
    public HyperscoreEValueEstimator(WaitingHandler waitingHandler, ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        this.waitingHandler = waitingHandler;
    }

    /**
     * Estimates the e-values of the PSMs and sets -log10(e-value) as score.
     *
     * @param spectrumFileName the name of the spectrum file scored
     * @param psmsMap map of the PSMs for every spectrum
     * @param nThreads the number of threads to use
     *
     * @throws InterruptedException exception thrown if a threading issue
     * occurs.
     */
    public void estimateInterpolationCoefficients(String spectrumFileName, HashMap<String, HashMap<Long, Psm>> psmsMap, int nThreads) throws InterruptedException {

        this.PsmsMap = psmsMap;
        waitingHandler.setSecondaryProgressCounterIndeterminate(false);
        waitingHandler.setMaxSecondaryProgressCounter(psmsMap.size());

        // Iterate all spectra and do the score interpolation
        TitlesIterator titlesIterator = new TitlesIterator(psmsMap.keySet());
        ArrayList<SpectrumProcessor> spectrumProcessors = new ArrayList<>(nThreads);
        ExecutorService pool = Executors.newFixedThreadPool(nThreads);
        for (int i = 0; i < nThreads; i++) {
            SpectrumProcessor spectrumProcessor = new SpectrumProcessor(titlesIterator);
            pool.submit(spectrumProcessor);
            spectrumProcessors.add(spectrumProcessor);
        }
        pool.shutdown();
        if (!pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS)) {
            waitingHandler.appendReport("E-value estimation timed out. Please contact the developers.", true, true);
        }

        // Merge the results from the different threads
        SpectrumProcessor spectrumProcessor = spectrumProcessors.get(0);
        interpolationValuesMap = spectrumProcessor.getInterpolationValuesMap();
        for (int i = 1; i < nThreads; i++) {
            spectrumProcessor = spectrumProcessors.get(i);
            interpolationValuesMap.putAll(spectrumProcessor.getInterpolationValuesMap());
        }

        // Get defaut interpolation values
        double[] as = new double[interpolationValuesMap.size()];
        double[] bs = new double[interpolationValuesMap.size()];
        int i = 0;
        for (double[] ab : interpolationValuesMap.values()) {
            as[i] = ab[0];
            bs[i] = ab[1];
            i++;
        }
        double defaultA = BasicMathFunctions.median(as);
        double defaultB = BasicMathFunctions.median(bs);
        defaultInterpolationValues = new double[]{defaultA, defaultB};

        // Remove unused map
        psmsMap = null;
    }

    @Override
    public double getEValue(String spectrumTitle, double score) {

        // If null, return the number of hits
        if (score == 0.0) {
            return (double) PsmsMap.get(spectrumTitle).size();
        }

        // Get interpolation values
        double[] ab = interpolationValuesMap.get(spectrumTitle);

        // If no interpolation was possible, use default values
        if (ab == null) {
            ab = defaultInterpolationValues;
        }

        // Interpolate the log of the score
        double logScore = FastMath.log10(score);
        return HyperScore.getInterpolation(logScore, ab[0], ab[1]);
    }

    /**
     * Private runnable to process the scores of a spectrum.
     */
    private class SpectrumProcessor implements Runnable {

        /**
         * Iterator for the spectrum titles.
         */
        private final TitlesIterator titlesIterator;
        /**
         * The hyperscore to use.
         */
        private final HyperScore hyperScore = new HyperScore();
        /**
         * Map of coefficients for every spectrum.
         */
        private final HashMap<String, double[]> threadInterpolationValuesMap = new HashMap<>();
        /**
         * Map of the number of hits for every spectrum.
         */
        private final HashMap<String, Integer> threadHitsMap = new HashMap<>();

        /**
         * Constructor.
         *
         * @param titlesIterator an iterator for the spectra to process
         */
        public SpectrumProcessor(TitlesIterator titlesIterator) {
            this.titlesIterator = titlesIterator;
        }

        @Override
        public void run() {

            try {

                String spectrumTitle;
                while ((spectrumTitle = titlesIterator.next()) != null) {
                    HashMap<Long, Psm> spectrumPsms = PsmsMap.get(spectrumTitle);
                    if (spectrumPsms.size() > 1) {
                        int[] scores = new int[spectrumPsms.size()];
                        int i = 0;
                        for (Psm psm : spectrumPsms.values()) {
                            double score = psm.getScore();
                            int scoreValue = (int) score;
                            scores[i] = scoreValue;
                            i++;
                        }
                        threadHitsMap.put(spectrumTitle, i);
                        double[] ab = hyperScore.getInterpolationValues(scores, false);
                        if (ab != null) {
                            threadInterpolationValuesMap.put(spectrumTitle, ab);
                        }
                    }
                }

            } catch (Exception e) {
                if (!waitingHandler.isRunCanceled()) {
                    exceptionHandler.catchException(e);
                    waitingHandler.setRunCanceled();
                }
            }
        }

        /**
         * Returns a map of the interpolation metrics.
         *
         * @return a map of the interpolation metrics
         */
        public HashMap<String, double[]> getInterpolationValuesMap() {
            return threadInterpolationValuesMap;
        }

        /**
         * Returns a map of the number of hits per spectrum.
         *
         * @return a map of the number of hits per spectrum
         */
        public HashMap<String, Integer> getnHitsMap() {
            return threadHitsMap;
        }
    }
}
