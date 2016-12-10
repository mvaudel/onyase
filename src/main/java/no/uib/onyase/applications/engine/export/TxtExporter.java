package no.uib.onyase.applications.engine.export;

import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.spectrum_assumptions.PeptideAssumption;
import com.compomics.util.experiment.io.identifications.idfilereaders.OnyaseIdfileReader;
import com.compomics.util.preferences.IdentificationParameters;
import com.compomics.util.waiting.WaitingHandler;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import no.uib.onyase.utils.Properties;

/**
 * Simple exporter for text files.
 *
 * @author Marc Vaudel
 */
public class TxtExporter {

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
     * @param spectrumFile the file containing the spectra
     * @param psmMap the psms as a map
     * @param parametersFile the file containing the parameters
     * @param identificationParameters the identification parameters
     * @param destinationFile the destination file
     * @param nThreads the number of threads to use
     *
     * @throws IOException exception thrown whenever an error occurs while
     * writing the file
     * @throws InterruptedException exception thrown if a threading issue
     * occurs.
     */
    public void writeExport(File spectrumFile, HashMap<String, HashMap<String, PeptideAssumption>> psmMap, File parametersFile, IdentificationParameters identificationParameters, File destinationFile, int nThreads) throws IOException, InterruptedException {

        this.psmMap = psmMap;

        waitingHandler.setSecondaryProgressCounterIndeterminate(false);
        waitingHandler.setMaxSecondaryProgressCounter(psmMap.size());
        Iterator<String> spectrumTitlesIterator = psmMap.keySet().iterator();
        BufferedWriter bw = new BufferedWriter(new FileWriter(destinationFile));
        Properties properties = new Properties();
        bw.write("" + OnyaseIdfileReader.comment + OnyaseIdfileReader.separator + OnyaseIdfileReader.versionTag + OnyaseIdfileReader.separator + properties.getVersion());
        bw.newLine();
        bw.write("" + OnyaseIdfileReader.comment + OnyaseIdfileReader.separator + OnyaseIdfileReader.spectraTag + OnyaseIdfileReader.separator + spectrumFile.getAbsolutePath());
        bw.newLine();
        bw.write("" + OnyaseIdfileReader.comment + OnyaseIdfileReader.separator + OnyaseIdfileReader.fastaTag + OnyaseIdfileReader.separator + identificationParameters.getSearchParameters().getFastaFile());
        bw.newLine();
        bw.write("" + OnyaseIdfileReader.comment + OnyaseIdfileReader.separator + OnyaseIdfileReader.paramsTag + OnyaseIdfileReader.separator + parametersFile.getAbsolutePath());
        bw.newLine();
        bw.write("" + OnyaseIdfileReader.comment + OnyaseIdfileReader.separator + "Spectrum_Title" + OnyaseIdfileReader.separator + "Sequence" + OnyaseIdfileReader.separator + "Modifications" + OnyaseIdfileReader.separator + "Charge" + OnyaseIdfileReader.separator + "HyperScore" + OnyaseIdfileReader.separator + "E-Value");
        bw.newLine();
        ExecutorService pool = Executors.newFixedThreadPool(nThreads);
        for (int i = 0; i < nThreads; i++) {
            SpectrumProcessor spectrumProcessor = new SpectrumProcessor(spectrumTitlesIterator, bw);
            pool.submit(spectrumProcessor);
        }
        pool.shutdown();
        if (!pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS)) {
            waitingHandler.appendReport("Export timed out.", true, true);
        }
        bw.close();
    }

    /**
     * Returns the variable modifications of the given peptide in an utf-8
     * encoded String in the form: modificationName1 +
     * Peptide.MODIFICATION_LOCALIZATION_SEPARATOR + modificationSite1 +
     * Peptide.MODIFICATION_SEPARATOR + modificationName2 +
     * Peptide.MODIFICATION_LOCALIZATION_SEPARATOR + modificationSite2.
     *
     * @param peptide the peptide of interest
     *
     * @return the variable modifications in a string
     *
     * @throws UnsupportedEncodingException exception thrown if the
     * modifications could not be encoded
     */
    private String getModifications(Peptide peptide) throws UnsupportedEncodingException {
        if (peptide.getModificationMatches() == null) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (ModificationMatch modificationMatch : peptide.getModificationMatches()) {
            if (modificationMatch.isVariable()) {
                if (stringBuilder.length() > 0) {
                    stringBuilder.append(Peptide.MODIFICATION_SEPARATOR);
                }
                stringBuilder.append(modificationMatch.getTheoreticPtm()).append(Peptide.MODIFICATION_LOCALIZATION_SEPARATOR).append(modificationMatch.getModificationSite());
            }
        }
        String result = URLEncoder.encode(stringBuilder.toString(), "utf-8");
        return result;
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
                        stringBuilder.append(OnyaseIdfileReader.separator);
                        Peptide peptide = peptideAssumption.getPeptide();
                        stringBuilder.append(peptide.getSequence());
                        stringBuilder.append(OnyaseIdfileReader.separator);
                        String modificationsAsString = getModifications(peptide);
                        stringBuilder.append(modificationsAsString);
                        stringBuilder.append(OnyaseIdfileReader.separator);
                        stringBuilder.append(peptideAssumption.getIdentificationCharge().value);
                        stringBuilder.append(OnyaseIdfileReader.separator);
                        stringBuilder.append(peptideAssumption.getRawScore());
                        stringBuilder.append(OnyaseIdfileReader.separator);
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
            } catch (NoSuchElementException exception) {
                // the last spectrum got processed by another thread.
            } catch (Exception e) {
                if (!waitingHandler.isRunCanceled()) {
                    exceptionHandler.catchException(e);
                    waitingHandler.setRunCanceled();
                }
            }
        }
    }
}
