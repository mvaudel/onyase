package no.uib.onyase.scripts.review_figure;

import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.massspectrometry.Precursor;
import com.compomics.util.experiment.massspectrometry.Spectrum;
import com.compomics.util.experiment.massspectrometry.SpectrumFactory;
import com.compomics.util.preferences.IdentificationParameters;
import com.compomics.util.waiting.WaitingHandler;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
import no.uib.onyase.utils.TitlesIterator;

/**
 * This class exports the data needed to create histograms of the number of
 * peptides per precursor.
 *
 * @author Marc Vaudel
 */
public class HistogramExporter {

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
    private HashMap<String, HashMap<String, FigureMetrics>> scoreMap;
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
    public HistogramExporter(WaitingHandler waitingHandler, ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        this.waitingHandler = waitingHandler;
    }

    /**
     * Writes the given histogram data to a file.
     *
     * @param spectrumFile the file containing the spectra
     * @param scoreMap map of the peptides found
     * @param identificationParameters the identification parameters
     * @param destinationFile the destination file
     * @param nThreads the number of threads to use
     *
     * @throws IOException exception thrown whenever an error occurs while
     * writing the file
     * @throws InterruptedException exception thrown if a threading issue
     * occurs.
     */
    public void writeExport(File spectrumFile, HashMap<String, HashMap<String, FigureMetrics>> scoreMap, IdentificationParameters identificationParameters, File destinationFile, int nThreads) throws IOException, InterruptedException {

        this.scoreMap = scoreMap;

        waitingHandler.setSecondaryProgressCounterIndeterminate(false);
        waitingHandler.setMaxSecondaryProgressCounter(scoreMap.size());
        TitlesIterator titlesIterator = new TitlesIterator(scoreMap.keySet());
        BufferedWriter bw = new BufferedWriter(new FileWriter(destinationFile));
        bw.write("title" + separator + "mz" + separator + "rt" + separator + "nPeptides");
        bw.newLine();
        ExecutorService pool = Executors.newFixedThreadPool(nThreads);
        for (int i = 0; i < nThreads; i++) {
            Exporter spectrumProcessor = new Exporter(titlesIterator, bw, spectrumFile.getName(), identificationParameters);
            pool.submit(spectrumProcessor);
        }
        pool.shutdown();
        if (!pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS)) {
            waitingHandler.appendReport("Export timed out.", true, true);
        }
        bw.close();
    }

    /**
     * Private runnable to write the results on a spectrum.
     */
    private class Exporter implements Runnable {

        /**
         * Iterator for the spectrum titles.
         */
        private final TitlesIterator titlesIterator;
        /**
         * The writer to use.
         */
        private final BufferedWriter bw;
        /**
         * The name of the mgf file.
         */
        private final String mgfFileName;
        /**
         * The PTM factory.
         */
        private PTMFactory ptmFactory = PTMFactory.getInstance();
        /**
         * The spectrum factory.
         */
        private SpectrumFactory spectrumFactory = SpectrumFactory.getInstance();
        /**
         * The identification parameters.
         */
        private final IdentificationParameters identificationParameters;

        /**
         * Constructor.
         *
         * @param spectrumTitlesIterator An iterator for the spectra to process
         * @param bw the writer to use
         * @param mgfFileName the name of the mgf file
         * @param identificationParameters the identification parameters
         */
        public Exporter(TitlesIterator titlesIterator, BufferedWriter bw, String mgfFileName, IdentificationParameters identificationParameters) {
            this.titlesIterator = titlesIterator;
            this.bw = bw;
            this.mgfFileName = mgfFileName;
            this.identificationParameters = identificationParameters;
        }

        @Override
        public void run() {

            try {

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

                String spectrumTitle;
                while ((spectrumTitle = titlesIterator.next()) != null) {
                    String spectrumKey = Spectrum.getSpectrumKey(mgfFileName, spectrumTitle);
                    Precursor precursor = spectrumFactory.getPrecursor(spectrumKey);
                    String encodedTitle = URLEncoder.encode(spectrumTitle, "utf-8");
                    HashMap<String, FigureMetrics> assumptions = scoreMap.get(spectrumTitle);
                    StringBuilder stringBuilder = new StringBuilder();
                    for (FigureMetrics figureMetrics : assumptions.values()) {
                        stringBuilder.append(encodedTitle).append(separator).append(precursor.getMz()).append(separator).append(precursor.getRtInMinutes()).append(separator).append(figureMetrics.getnHits()).append(END_LINE);
                    }
                    bw.write(stringBuilder.toString());
                    waitingHandler.increaseSecondaryProgressCounter();
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
