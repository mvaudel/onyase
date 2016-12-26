package no.uib.onyase.scripts.review_figure.full;

import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.spectrum_assumptions.PeptideAssumption;
import com.compomics.util.experiment.io.identifications.idfilereaders.OnyaseIdfileReader;
import com.compomics.util.experiment.massspectrometry.Precursor;
import com.compomics.util.experiment.massspectrometry.SpectrumFactory;
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
     * The spectrum factory.
     */
    private SpectrumFactory spectrumFactory = SpectrumFactory.getInstance();
    /**
     * Boolean indicating whether only the best hit should be exported.
     */
    private boolean onlyBestHit = false;

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
     * @param onlyBestHit boolean indicating whether only the best hit should be
     * exported
     *
     * @throws IOException exception thrown whenever an error occurs while
     * writing the file
     * @throws InterruptedException exception thrown if a threading issue
     * occurs.
     */
    public void writeExport(File spectrumFile, HashMap<String, HashMap<String, PeptideAssumption>> psmMap, File parametersFile, IdentificationParameters identificationParameters, File destinationFile, int nThreads, boolean onlyBestHit) throws IOException, InterruptedException {

        this.psmMap = psmMap;

        waitingHandler.setSecondaryProgressCounterIndeterminate(false);
        waitingHandler.setMaxSecondaryProgressCounter(psmMap.size());
        Iterator<String> spectrumTitlesIterator = psmMap.keySet().iterator();
        BufferedWriter bw = new BufferedWriter(new FileWriter(destinationFile));
        bw.write("Spectrum_Title" + OnyaseIdfileReader.separator + "mz" + OnyaseIdfileReader.separator + "mz_deviation" + OnyaseIdfileReader.separator + "rt" + OnyaseIdfileReader.separator + "Sequence" + OnyaseIdfileReader.separator + "Modifications" + OnyaseIdfileReader.separator + "Charge" + OnyaseIdfileReader.separator + "HyperScore" + OnyaseIdfileReader.separator + "E-Value" + OnyaseIdfileReader.separator + "Decoy" + OnyaseIdfileReader.separator + "Target" + OnyaseIdfileReader.separator + "nIons");
        bw.newLine();
        ExecutorService pool = Executors.newFixedThreadPool(nThreads);
        for (int i = 0; i < nThreads; i++) {
            if (onlyBestHit) {
                BestHitsExporter spectrumProcessor = new BestHitsExporter(spectrumTitlesIterator, bw, spectrumFile.getName(), identificationParameters);
                pool.submit(spectrumProcessor);
            } else {
                AllHitsExporter spectrumProcessor = new AllHitsExporter(spectrumTitlesIterator, bw, spectrumFile.getName(), identificationParameters);
                pool.submit(spectrumProcessor);
            }
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
    private class BestHitsExporter implements Runnable {

        /**
         * Iterator for the spectrum titles.
         */
        private final Iterator<String> spectrumTitlesIterator;
        /**
         * The writer to use.
         */
        private final BufferedWriter bw;
        /**
         * The name of the mgf file.
         */
        private final String mgfFileName;
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
         */
        public BestHitsExporter(Iterator<String> spectrumTitlesIterator, BufferedWriter bw, String mgfFileName, IdentificationParameters identificationParameters) {
            this.spectrumTitlesIterator = spectrumTitlesIterator;
            this.bw = bw;
            this.mgfFileName = mgfFileName;
            this.identificationParameters = identificationParameters;
        }

        @Override
        public void run() {

            try {

                boolean ppm = identificationParameters.getSearchParameters().isPrecursorAccuracyTypePpm();
                int minIsotope = identificationParameters.getSearchParameters().getMinIsotopicCorrection();
                int maxIsotope = identificationParameters.getSearchParameters().getMaxIsotopicCorrection();

                FigureMetrics figureMetrics = new FigureMetrics();

                // Iterate the PSMs and write the details to the file
                while (spectrumTitlesIterator.hasNext()) {

                    // get the PSMs for the next spectrum
                    String spectrumTitle = spectrumTitlesIterator.next();
                    String encodedSpectrumTitle = URLEncoder.encode(spectrumTitle, "utf-8");
                    HashMap<String, PeptideAssumption> peptideAssumptions = psmMap.get(spectrumTitle);

                    if (!peptideAssumptions.isEmpty()) {

                        Precursor precursor = spectrumFactory.getPrecursor(mgfFileName, spectrumTitle);

                        Double bestEvalue = null;
                        PeptideAssumption bestPeptideAssumption = null;
                        for (PeptideAssumption peptideAssumption : peptideAssumptions.values()) {
                            Double eValue = peptideAssumption.getScore();
                            if (bestEvalue == null || eValue < bestEvalue) {
                                bestPeptideAssumption = peptideAssumption;
                                bestEvalue = eValue;
                            }
                        }

                        StringBuilder stringBuilder = new StringBuilder();

                        figureMetrics = (FigureMetrics) bestPeptideAssumption.getUrParam(figureMetrics);

                        stringBuilder.append(encodedSpectrumTitle);
                        stringBuilder.append(OnyaseIdfileReader.separator);
                        stringBuilder.append(precursor.getMz());
                        stringBuilder.append(OnyaseIdfileReader.separator);
                        stringBuilder.append(bestPeptideAssumption.getDeltaMass(precursor.getMz(), ppm, minIsotope, maxIsotope));
                        stringBuilder.append(OnyaseIdfileReader.separator);
                        stringBuilder.append(precursor.getRtInMinutes());
                        stringBuilder.append(OnyaseIdfileReader.separator);
                        Peptide peptide = bestPeptideAssumption.getPeptide();
                        stringBuilder.append(peptide.getSequence());
                        stringBuilder.append(OnyaseIdfileReader.separator);
                        String modificationsAsString = getModifications(peptide);
                        stringBuilder.append(modificationsAsString);
                        stringBuilder.append(OnyaseIdfileReader.separator);
                        stringBuilder.append(bestPeptideAssumption.getIdentificationCharge().value);
                        stringBuilder.append(OnyaseIdfileReader.separator);
                        stringBuilder.append(bestPeptideAssumption.getRawScore());
                        stringBuilder.append(OnyaseIdfileReader.separator);
                        stringBuilder.append(bestPeptideAssumption.getScore());
                        if (figureMetrics.isIsDecoy() && figureMetrics.isIsTarget()) {
                            stringBuilder.append(OnyaseIdfileReader.separator);
                            stringBuilder.append(0.5);
                            stringBuilder.append(OnyaseIdfileReader.separator);
                            stringBuilder.append(0.5);
                        } else if (figureMetrics.isIsDecoy()) {
                            stringBuilder.append(OnyaseIdfileReader.separator);
                            stringBuilder.append(1);
                            stringBuilder.append(OnyaseIdfileReader.separator);
                            stringBuilder.append(0);
                        } else {
                            stringBuilder.append(OnyaseIdfileReader.separator);
                            stringBuilder.append(0);
                            stringBuilder.append(OnyaseIdfileReader.separator);
                            stringBuilder.append(1);
                        }
                        stringBuilder.append(OnyaseIdfileReader.separator);
                        stringBuilder.append(figureMetrics.getnIons());
                        stringBuilder.append(END_LINE);
                        bw.write(stringBuilder.toString());

                        // check for cancellation and update progress
                        if (waitingHandler.isRunCanceled()) {
                            return;
                        } else {
                            waitingHandler.increaseSecondaryProgressCounter();
                        }
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

    /**
     * Private runnable to write the results on a spectrum.
     */
    private class AllHitsExporter implements Runnable {

        /**
         * Iterator for the spectrum titles.
         */
        private final Iterator<String> spectrumTitlesIterator;
        /**
         * The writer to use.
         */
        private final BufferedWriter bw;
        /**
         * The name of the mgf file.
         */
        private final String mgfFileName;
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
         */
        public AllHitsExporter(Iterator<String> spectrumTitlesIterator, BufferedWriter bw, String mgfFileName, IdentificationParameters identificationParameters) {
            this.spectrumTitlesIterator = spectrumTitlesIterator;
            this.bw = bw;
            this.mgfFileName = mgfFileName;
            this.identificationParameters = identificationParameters;
        }

        @Override
        public void run() {

            try {

                boolean ppm = identificationParameters.getSearchParameters().isPrecursorAccuracyTypePpm();
                int minIsotope = identificationParameters.getSearchParameters().getMinIsotopicCorrection();
                int maxIsotope = identificationParameters.getSearchParameters().getMaxIsotopicCorrection();

                FigureMetrics figureMetrics = new FigureMetrics();

                // Iterate the PSMs and write the details to the file
                while (spectrumTitlesIterator.hasNext()) {

                    // get the PSMs for the next spectrum
                    String spectrumTitle = spectrumTitlesIterator.next();
                    String encodedSpectrumTitle = URLEncoder.encode(spectrumTitle, "utf-8");
                    HashMap<String, PeptideAssumption> peptideAssumptions = psmMap.get(spectrumTitle);

                    Precursor precursor = spectrumFactory.getPrecursor(mgfFileName, spectrumTitle);

                    StringBuilder stringBuilder = new StringBuilder();
                    for (PeptideAssumption peptideAssumption : peptideAssumptions.values()) {

                        figureMetrics = (FigureMetrics) peptideAssumption.getUrParam(figureMetrics);

                        stringBuilder.append(encodedSpectrumTitle);
                        stringBuilder.append(OnyaseIdfileReader.separator);
                        stringBuilder.append(precursor.getMz());
                        stringBuilder.append(OnyaseIdfileReader.separator);
                        stringBuilder.append(peptideAssumption.getDeltaMass(precursor.getMz(), ppm, minIsotope, maxIsotope));
                        stringBuilder.append(OnyaseIdfileReader.separator);
                        stringBuilder.append(precursor.getRtInMinutes());
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
                        if (figureMetrics.isIsDecoy() && figureMetrics.isIsTarget()) {
                            stringBuilder.append(OnyaseIdfileReader.separator);
                            stringBuilder.append(0.5);
                            stringBuilder.append(OnyaseIdfileReader.separator);
                            stringBuilder.append(0.5);
                        } else if (figureMetrics.isIsDecoy()) {
                            stringBuilder.append(OnyaseIdfileReader.separator);
                            stringBuilder.append(1);
                            stringBuilder.append(OnyaseIdfileReader.separator);
                            stringBuilder.append(0);
                        } else {
                            stringBuilder.append(OnyaseIdfileReader.separator);
                            stringBuilder.append(0);
                            stringBuilder.append(OnyaseIdfileReader.separator);
                            stringBuilder.append(1);
                        }
                        stringBuilder.append(OnyaseIdfileReader.separator);
                        stringBuilder.append(figureMetrics.getnIons());
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
