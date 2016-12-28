package no.uib.onyase.scripts.review_figure.full;

import no.uib.onyase.applications.engine.model.PeptideDraft;
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
import no.uib.onyase.applications.engine.modules.PeptideModificationsIterator;
import no.uib.onyase.applications.engine.modules.peptide_modification_iterators.MultipleModificationsIterators;
import no.uib.onyase.applications.engine.modules.peptide_modification_iterators.OverlappingModificationsIterator;
import no.uib.onyase.applications.engine.modules.peptide_modification_iterators.SingleModificationIterator;

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
    public HistogramExporter(WaitingHandler waitingHandler, ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        this.waitingHandler = waitingHandler;
    }

    /**
     * Writes the given histogram data to a file.
     *
     * @param spectrumFile the file containing the spectra
     * @param psmMap the psms as a map
     * @param identificationParameters the identification parameters
     * @param destinationFile the destination file
     * @param nThreads the number of threads to use
     *
     * @throws IOException exception thrown whenever an error occurs while
     * writing the file
     * @throws InterruptedException exception thrown if a threading issue
     * occurs.
     */
    public void writeExport(File spectrumFile, HashMap<String, HashMap<String, PeptideDraft>> psmMap, IdentificationParameters identificationParameters, File destinationFile, int nThreads) throws IOException, InterruptedException {

        this.psmMap = psmMap;

        waitingHandler.setSecondaryProgressCounterIndeterminate(false);
        waitingHandler.setMaxSecondaryProgressCounter(psmMap.size());
        Iterator<String> spectrumTitlesIterator = psmMap.keySet().iterator();
        BufferedWriter bw = new BufferedWriter(new FileWriter(destinationFile));
        bw.write("title" + separator + "mz" + separator + "rt" + separator + "nPeptides");
        bw.newLine();
        ExecutorService pool = Executors.newFixedThreadPool(nThreads);
        for (int i = 0; i < nThreads; i++) {
            Exporter spectrumProcessor = new Exporter(spectrumTitlesIterator, bw, spectrumFile.getName(), identificationParameters);
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
        public Exporter(Iterator<String> spectrumTitlesIterator, BufferedWriter bw, String mgfFileName, IdentificationParameters identificationParameters) {
            this.spectrumTitlesIterator = spectrumTitlesIterator;
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
                ArrayList<String> orderedModificationsName = new ArrayList<String>(ptmSettings.getVariableModifications());
                Collections.sort(orderedModificationsName);
                ArrayList<String> orderedPeptideModificationsName = new ArrayList<String>(orderedModificationsName.size());

                while (spectrumTitlesIterator.hasNext()) {
                    String spectrumTitle = spectrumTitlesIterator.next();
                    String spectrumKey = Spectrum.getSpectrumKey(mgfFileName, spectrumTitle);
                    Precursor precursor = spectrumFactory.getPrecursor(spectrumKey);
                    String encodedTitle = URLEncoder.encode(spectrumTitle, "utf-8");
                    HashMap<String, PeptideDraft> assumptions = psmMap.get(spectrumTitle);
                    int nPeptides = 0;
                    for (PeptideDraft peptideDraft : assumptions.values()) {
                        HashMap<String, Integer> modificationOccurrence = peptideDraft.getVariableModifications();
                        if (modificationOccurrence == null) {
                            nPeptides++;
                        } else {
                            HashMap<String, Integer[]> possibleModificationSites = peptideDraft.getVariableModificationsSites();
                            // Create an iterator for the possible sites
                            PeptideModificationsIterator peptideModificationsIterator;
                            if (modificationOccurrence.size() == 1) {
                                String modificationName = modificationOccurrence.keySet().iterator().next();
                                Integer[] possibleSites = possibleModificationSites.get(modificationName);
                                Integer occurrence = modificationOccurrence.get(modificationName);
                                peptideModificationsIterator = new SingleModificationIterator(possibleSites, occurrence, modificationName, 0);
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
                                    peptideModificationsIterator = new OverlappingModificationsIterator(modificationOccurrence, possibleModificationSites, orderedPeptideModificationsName, 0);
                                } else {
                                    peptideModificationsIterator = new MultipleModificationsIterators(modificationOccurrence, possibleModificationSites, orderedPeptideModificationsName, 0);
                                }
                            }
                            while (peptideModificationsIterator.hasNext()) {
                                nPeptides++;
                            }
                        }
                    }
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(encodedTitle).append(separator).append(precursor.getMz()).append(separator).append(precursor.getRtInMinutes()).append(separator).append(nPeptides).append(END_LINE);
                    bw.write(stringBuilder.toString());
                    waitingHandler.increaseSecondaryProgressCounter();
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
