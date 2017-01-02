package no.uib.onyase.scripts.review_figure.full;

import com.compomics.util.experiment.identification.psm_scoring.psm_scores.HyperScore;
import com.compomics.util.waiting.WaitingHandler;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.HashSet;
import org.apache.commons.math.util.FastMath;

/**
 * The e-value estimator estimates the e-values from the temporary file and
 * write the result file.
 *
 * @author Marc Vaudel
 */
public class EValueEstimtor {

    /**
     * Separator for the columns.
     */
    public final static char separator = ' ';
    /**
     * The end of line separator.
     */
    public static final String END_LINE = System.getProperty("line.separator");
    /**
     * A waiting handler providing feedback to the user and allowing canceling
     * the process.
     */
    private WaitingHandler waitingHandler;
    private HyperScore hyperScore = new HyperScore();
    private HashMap<String, double[]> interpolationValuesMap;
    private HashMap<String, Integer> nHitsMap;
    private double[] abZero = new double[]{0.0, 0.0};

    /**
     * Constructor.
     *
     * @param waitingHandler a waiting handler providing feedback to the user
     * and allowing canceling the process
     */
    public EValueEstimtor(WaitingHandler waitingHandler) {
        this.waitingHandler = waitingHandler;
    }

    /**
     * Estimates the e-values of the PSMs and sets -log10(e-value) as score.
     *
     * @param scoresMap Map of the different scores
     * @param tempFile file where the preliminary results are stored
     * @param nLines the number of lines in the temporary file
     * @param allHitsFile file where to save all hits
     * @param bestHitsFile file where to save the best hits
     *
     * @throws InterruptedException exception thrown if a threading issue
     * occurs.
     */
    public void estimateScores(HashMap<String, HashMap<String, FigureMetrics>> scoresMap, File tempFile, int nLines, File allHitsFile, File bestHitsFile) throws InterruptedException, IOException {

        // Temp file to store intermediate results
        File tempFile2 = new File(allHitsFile.getPath() + "_temp2");

        // Set up the file reader and writers
        BufferedReader br = new BufferedReader(new FileReader(tempFile));
        BufferedWriter bwTemp = new BufferedWriter(new FileWriter(tempFile2));

        // Get header
        String line = br.readLine();
        bwTemp.write(line);
        String headerLine = line;

        // Set the progress handler
        waitingHandler.setSecondaryProgressCounterIndeterminate(false);
        waitingHandler.setMaxSecondaryProgressCounter(nLines);

        // Set maps to store the best hits
        HashMap<String, String> bestHitMaps = new HashMap<String, String>();
        HashMap<String, Double> bestHitEValues = new HashMap<String, Double>();

        // Keep track of spectra where no interpolation was possible
        HashSet<String> missingValues = new HashSet<String>();

        // Iterate all lines and add the e-value
        while ((line = br.readLine()) != null) {

            // Get spectrum title and score
            String spectrumTitle = getSpectrumTitle(line);
            double score = getScore(line);

            // Get the interpolation values
            double[] ab = interpolationValuesMap.get(spectrumTitle);
            if (ab == null) {
                HashMap<String, FigureMetrics> spectrumMetrics = scoresMap.get(spectrumTitle);
                ab = getInterpolationValues(spectrumTitle, spectrumMetrics);
                if (ab == null) {
                    missingValues.add(spectrumTitle);
                } else {
                    interpolationValuesMap.put(spectrumTitle, ab);
                }
            }

            // Skip the spectra with missing values
            if (ab != null) {

                // Get the e-value
                double eValue;
                if (score == 0.0) {
                    int nHits = nHitsMap.get(spectrumTitle);
                    eValue = nHits;
                } else {
                    double logScore = FastMath.log10(score);
                    eValue = HyperScore.getInterpolation(logScore, ab[0], ab[1]);
                }

                // Write to the file
                StringBuilder newLineBuilder = new StringBuilder(line);
                newLineBuilder.append(separator).append(eValue).append(END_LINE);
                String newLine = newLineBuilder.toString();
                bwTemp.write(newLine);

                // Save if best hit
                if (score > 0) {
                    Double bestEvalue = bestHitEValues.get(spectrumTitle);
                    if (bestEvalue == null || bestEvalue > eValue) {
                        bestHitMaps.put(spectrumTitle, newLine);
                        bestHitEValues.put(spectrumTitle, eValue);
                    }
                }
            }
        }

        // Close connections to files
        br.close();
        bwTemp.close();

        // see if there are missing values
        if (!missingValues.isEmpty()) {

            // Use values from other spectra
            double a = hyperScore.getMendianA();
            double b = hyperScore.getMendianB();

            // Set up the file reader and writers
            br = new BufferedReader(new FileReader(tempFile2));
            line = br.readLine();
            BufferedWriter bwAll = new BufferedWriter(new FileWriter(allHitsFile));
            bwAll.write(line);

            // Iterate the temp file again
            while ((line = br.readLine()) != null) {

                // Get spectrum title
                String spectrumTitle = getSpectrumTitle(line);

                // See if it is missing
                if (missingValues.contains(spectrumTitle)) {

                    // Get e-value
                    double score = getScore(line);
                    double logScore = FastMath.log10(score);
                    double eValue = HyperScore.getInterpolation(logScore, a, b);

                    // Write to the file
                    StringBuilder newLineBuilder = new StringBuilder(line);
                    newLineBuilder.append(separator).append(eValue).append(END_LINE);
                    String newLine = newLineBuilder.toString();
                    bwAll.write(newLine);

                    // Save if best hit
                    if (score > 0) {
                        Double bestEvalue = bestHitEValues.get(spectrumTitle);
                        if (bestEvalue == null || bestEvalue > eValue) {
                            bestHitMaps.put(spectrumTitle, newLine);
                            bestHitEValues.put(spectrumTitle, eValue);
                        }
                    }

                } else {
                    bwAll.write(line);
                }
            }

            // Close connections to files
            br.close();
            bwAll.close();

        } else {
            tempFile2.renameTo(allHitsFile);
        }

        // Write best hits
        BufferedWriter bwBest = new BufferedWriter(new FileWriter(bestHitsFile));
        bwBest.write(headerLine);
        for (String newLine : bestHitMaps.values()) {
            bwBest.write(newLine);
        }
        bwBest.close();
    }

    /**
     * Returns the decoded spectrum title from the given line in the temporary
     * file.
     *
     * @param line the line of interest
     *
     * @return the spectrum title
     * 
     * @throws UnsupportedEncodingException exception thrown whenever an error occurred while decoding the spectrum title
     */
    private String getSpectrumTitle(String line) throws UnsupportedEncodingException {
        int firstSeparator = line.indexOf(separator);
        String title = line.substring(0, firstSeparator);
        title = URLDecoder.decode(title, "utf-8");
        return title;
    }

    /**
     * Returns the score from the given line in the temporary file.
     * 
     * @param line the line of interest
     * 
     * @return the score
     */
    private double getScore(String line) {
        int lastSeparator = line.lastIndexOf(separator);
        String scoreAsString = line.substring(lastSeparator);
        return Double.valueOf(scoreAsString);
    }

    /**
     * Returns the interpolation values for the given spectrum metrics in the form {a, b}. This methods also stores the number of hits for this spectrum in the map in the attributes.
     * 
     * @param spectrumTitle the title of the spectrum
     * @param spectrumMetrics the spectrum metrics
     * 
     * @return the interpolation values
     */
    private double[] getInterpolationValues(String spectrumTitle, HashMap<String, FigureMetrics> spectrumMetrics) {
        int[] scores = new int[spectrumMetrics.size()];
        int i = 0;
        int nHits = 0;
        boolean validScore = false;
        for (FigureMetrics figureMetrics : spectrumMetrics.values()) {
            int currentScore = figureMetrics.getScore();
            scores[i] = currentScore;
            if (currentScore > 0 && !validScore) {
                validScore = true;
            }
            nHits += figureMetrics.getnHits();
            i++;
        }
        nHitsMap.put(spectrumTitle, nHits);
        if (validScore) {
            return hyperScore.getInterpolationValues(scores);
        } else {
            return abZero;
        }
    }
}
