package no.uib.onyase.applications.engine.export;

import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.spectrum_assumptions.PeptideAssumption;
import com.compomics.util.waiting.WaitingHandler;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Simple exporter for text files.
 *
 * @author Marc Vaudel
 */
public class TxtExporter {
    
    /**
     * The column separator.
     */
    public static final String SEPARATOR = " ";
    /**
     * The end of line separator.
     */
    public static final String END_LINE = System.getProperty("line.separator");
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
    public TxtExporter(WaitingHandler waitingHandler, ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        this.waitingHandler = waitingHandler;
    }
    
    /**
     * Writes the given psms to a file.
     * 
     * @param psmMap the psms as a map
     * @param destinationFile the destination file
     * @param nThreads the number of threads to use
     * 
     * @throws IOException exception thrown whenever an error occurs while writing the file
     * @throws InterruptedException exception thrown if a threading issue
     * occurs.
     */
    public void writeExport(HashMap<String, HashMap<String, PeptideAssumption>> psmMap, File destinationFile, int nThreads) throws IOException, InterruptedException {
        
        waitingHandler.setSecondaryProgressCounterIndeterminate(false);
        waitingHandler.setMaxSecondaryProgressCounter(psmMap.size());
        Iterator<String> spectrumTitlesIterator = psmMap.keySet().iterator();
        BufferedWriter bw = new BufferedWriter(new FileWriter(destinationFile));
        ExecutorService pool = Executors.newFixedThreadPool(nThreads);
        for (int i = 0; i < nThreads; i++) {
            SpectrumProcessor spectrumProcessor = new SpectrumProcessor(spectrumTitlesIterator, bw);
            pool.submit(spectrumProcessor);
        }
        pool.shutdown();
        if (!pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS)) {
            waitingHandler.appendReport("Mapping tags timed out. Please contact the developers.", true, true);
        }
        
    }

    /**
     * Private runnable to write the results on a spectrum.
     */
    private class SpectrumProcessor implements Runnable {

        /**
         * Iterator for the spectrum titles.
         */
        private final Iterator<String> spectrumTitlesIterator;
        /**
         * The writer to use.
         */
        private final BufferedWriter bw;

        /**
         * Constructor.
         *
         * @param spectrumTitlesIterator An iterator for the spectra to process
         * @param bw the writer to use
         */
        public SpectrumProcessor(Iterator<String> spectrumTitlesIterator, BufferedWriter bw) {
            this.spectrumTitlesIterator = spectrumTitlesIterator;
            this.bw = bw;
        }

        @Override
        public void run() {

            try {

                // Iterate the PSMs and write the details to the file
                while (spectrumTitlesIterator.hasNext()) {

                    // get the PSMs for the next spectrum
                    String spectrumTitle = spectrumTitlesIterator.next();
                    HashMap<String, PeptideAssumption> peptideAssumptions = psmMap.get(spectrumTitle);

                    StringBuilder stringBuilder = new StringBuilder();
                    for (PeptideAssumption peptideAssumption : peptideAssumptions.values()) {
                        if (stringBuilder.length() == 0) {
                            spectrumTitle = URLEncoder.encode(spectrumTitle, "utf-8");
                            stringBuilder.append(spectrumTitle);
                        }
                        stringBuilder.append(SEPARATOR);
                        Peptide peptide = peptideAssumption.getPeptide();
                        stringBuilder.append(peptide.getSequence());
                        stringBuilder.append(SEPARATOR);
                        stringBuilder.append(peptideAssumption.getIdentificationCharge().value);
                        stringBuilder.append(SEPARATOR);
                        stringBuilder.append(peptideAssumption.getRawScore());
                        stringBuilder.append(SEPARATOR);
                        stringBuilder.append(peptideAssumption.getScore());
                        stringBuilder.append(END_LINE);
                    }
                    bw.write(stringBuilder.toString());
                    

                    // check for cancellation and update progress
                    if (waitingHandler.isRunCanceled()) {
                        return;
                    } else {
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
