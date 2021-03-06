package no.uib.onyase.scripts.review_figure;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.preferences.SequenceMatchingPreferences;
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

/**
 * This class appends e-values to a result file.
 *
 * @author Marc Vaudel
 */
public class EValueExporter {

    /**
     * Separator for the columns.
     */
    public final static char separator = ' ';
    /**
     * Separator for the columns as string.
     */
    public final static String separatorAsString = separator + "";
    /**
     * The end of line separator.
     */
    public static final String END_LINE = System.getProperty("line.separator");
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
     */
    public EValueExporter(WaitingHandler waitingHandler) {
        this.waitingHandler = waitingHandler;
    }

    /**
     * Appends the e-value to the line of every PSM.
     *
     * @param eValueEstimator the estimator to use to estimate the e-value
     * @param scoresMap map of the figure metrics
     * @param tempFile file where the preliminary results are stored
     * @param nLines the number of lines in the temporary file
     * @param allHitsFile file where to save all hits
     * @param bestHitsFile file where to save the best hits
     *
     * @throws InterruptedException exception thrown if a threading issue
     * occurs.
     * @throws java.io.IOException exception thrown whenever an error occurred
     * while reading or writing the file
     */
    public void writeEvalues(EValueEstimator eValueEstimator, HashMap<String, HashMap<String, FigureMetrics>> scoresMap, File tempFile, int nLines, File allHitsFile, File bestHitsFile) throws InterruptedException, IOException {

        // Sequence settings for the keys of the peptides
        SequenceMatchingPreferences sequenceMatchingPreferences = SequenceMatchingPreferences.getDefaultSequenceMatching();

        // Set up the file reader and writers
        BufferedReader br = new BufferedReader(new FileReader(tempFile));
        BufferedWriter bwAll = new BufferedWriter(new FileWriter(allHitsFile));
        BufferedWriter bwBest = new BufferedWriter(new FileWriter(bestHitsFile));

        // Get header
        String line = br.readLine();
        bwAll.write(line);
        bwAll.newLine();
        bwBest.write(line);
        bwBest.newLine();

        // Set the progress handler
        waitingHandler.setSecondaryProgressCounterIndeterminate(false);
        waitingHandler.setMaxSecondaryProgressCounter(2*nLines);

        // Set maps to store the best hits
        HashMap<String, BestHit> bestHitMaps = new HashMap<String, BestHit>();

        // Iterate all lines and add the e-value
        int cpt = 0;
        while ((line = br.readLine()) != null) {

            // Get spectrum title and score
            String spectrumTitle = getSpectrumTitle(line);
            double score = getScore(line);
            String sequence = getSequence(line);

            // Get the e-value
            double eValue = eValueEstimator.getEValue(spectrumTitle, score);

            // Write to the file
            StringBuilder newLineBuilder = new StringBuilder(line);
            newLineBuilder.append(separator).append(eValue);
            HashMap<String, FigureMetrics> spectrumScores = scoresMap.get(spectrumTitle);
            String key = AminoAcid.getMatchingSequence(sequence, sequenceMatchingPreferences);
            FigureMetrics figureMetrics = spectrumScores.get(key);
            if (figureMetrics.isIsDecoy() && figureMetrics.isIsTarget()) {
                newLineBuilder.append(separator).append(0.5).append(separator).append(0.5);
            } else if (figureMetrics.isIsDecoy()) {
                newLineBuilder.append(separator).append(1).append(separator).append(0);
            } else if (figureMetrics.isIsTarget()) {
                newLineBuilder.append(separator).append(0).append(separator).append(1);
            }
            newLineBuilder.append(END_LINE);
            String newLine = newLineBuilder.toString();
            bwAll.write(newLine);

            // Save if best hit
            if (score > 0) {
                BestHit bestEvalue = bestHitMaps.get(spectrumTitle);
                if (bestEvalue == null || bestEvalue.getScore() > eValue) {
                    BestHit newEvalue = new BestHit(eValue, cpt);
                    bestHitMaps.put(spectrumTitle, newEvalue);
                }
            }
            cpt++;
            
            // Increase progress
            waitingHandler.increaseSecondaryProgressCounter();
        }

        // Restart iterating
        br.close();
        br = new BufferedReader(new FileReader(allHitsFile));

        // Write best hits
        HashSet<Integer> bestLines = new HashSet<Integer>(bestHitMaps.size());
        for (BestHit bestEvalues : bestHitMaps.values()) {
            bestLines.add(bestEvalues.getLine());
        }
        cpt = 0;
        while ((line = br.readLine()) != null) {
            
            // Export best hit
            if (bestLines.contains(cpt)) {
                bwBest.write(line);
                bwBest.newLine();
            }
            cpt++;
            
            // Increase progress
            waitingHandler.increaseSecondaryProgressCounter();
        }

        // Close connections to files
        br.close();
        bwAll.close();
        bwBest.close();

        // Delete the temporary file
        tempFile.delete();
    }

    /**
     * Returns the decoded spectrum title from the given line in the temporary
     * file.
     *
     * @param line the line of interest
     *
     * @return the spectrum title
     *
     * @throws UnsupportedEncodingException exception thrown whenever an error
     * occurred while decoding the spectrum title
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
     * Returns the score from the given line in the temporary file.
     *
     * @param line the line of interest
     *
     * @return the score
     */
    private String getSequence(String line) {
        String[] lineSplit = line.split(separatorAsString);
        return lineSplit[3];
    }

    /**
     * Class used to store the best hit details.
     */
    private class BestHit {

        /**
         * The score.
         */
        private double score;
        /**
         * The line number.
         */
        private int line;

        /**
         * Constructor.
         *
         * @param score the score
         * @param line the line number
         */
        public BestHit(double score, int line) {
            this.score = score;
            this.line = line;
        }

        /**
         * Returns the score of the best hit.
         *
         * @return the score of the best hit
         */
        public double getScore() {
            return score;
        }

        /**
         * Returns the line number.
         *
         * @return the line number
         */
        public int getLine() {
            return line;
        }

    }
}
