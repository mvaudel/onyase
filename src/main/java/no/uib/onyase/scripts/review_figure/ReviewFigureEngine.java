package no.uib.onyase.scripts.review_figure;

import com.compomics.util.Util;
import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.experiment.biology.EnzymeFactory;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.experiment.identification.spectrum_assumptions.PeptideAssumption;
import com.compomics.util.experiment.massspectrometry.Precursor;
import com.compomics.util.experiment.massspectrometry.Spectrum;
import com.compomics.util.experiment.massspectrometry.SpectrumFactory;
import com.compomics.util.preferences.IdentificationParameters;
import com.compomics.util.waiting.Duration;
import com.compomics.util.waiting.WaitingHandler;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.HashMap;
import no.uib.onyase.applications.engine.modules.EValueEstimator;
import no.uib.onyase.applications.engine.modules.precursor_handling.PrecursorProcessor;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshallerException;

/**
 *
 * @author Marc Vaudel
 */
public class ReviewFigureEngine {

    /**
     * Separator for the exports.
     */
    public final static String separator = " ";
    /**
     * The spectrum factory.
     */
    private SpectrumFactory spectrumFactory;
    /**
     * The sequence factory.
     */
    private SequenceFactory sequenceFactory;
    /**
     * The enzyme factory.
     */
    private EnzymeFactory enzymeFactory;
    /**
     * The PTM factory.
     */
    private PTMFactory ptmFactory;
    /**
     * The module handling the precursors.
     */
    private PrecursorProcessor precursorProcessor;

    /**
     * Constructor.
     */
    public ReviewFigureEngine() {
        initializeFactories();
    }

    /**
     * Initializes the factories to default.
     */
    private void initializeFactories() {
        spectrumFactory = SpectrumFactory.getInstance();
        sequenceFactory = SequenceFactory.getInstance();
        enzymeFactory = EnzymeFactory.getInstance();
        ptmFactory = PTMFactory.getInstance();
    }

    /**
     * Launches the search.
     *
     * @param jobName the job name
     * @param spectrumFile the spectrum file to search
     * @param allPsmsFile the file where to export all psms
     * @param bestPsmsFile the file where to export the best psms
     * @param identificationParametersFile the file where the identification
     * parameters are stored
     * @param identificationParameters the identification parameters
     * @param maxX the maximal number of Xs to allow in a peptide sequence
     * @param removeZeros boolean indicating whether the peptide assumptions of
     * score zero should be removed
     * @param minMz the minimal m/z to consider
     * @param maxMz the maximal m/z to consider
     * @param nThreads the number of threads to use
     * @param waitingHandler a waiting handler providing feedback to the user
     * and allowing canceling the process
     * @param exceptionHandler a handler for the exceptions
     *
     * @throws IOException exception thrown if an error occurred while reading
     * or writing a file
     * @throws ClassNotFoundException exception thrown if an error occurred
     * while casting an object
     * @throws SQLException exception thrown if an error occurred while
     * interacting with a database
     * @throws MzMLUnmarshallerException exception thrown if an error occurred
     * while reading an mzML file
     * @throws InterruptedException exception thrown if a threading error
     * occurred
     */
    public void launch(String jobName, File spectrumFile, File allPsmsFile, File bestPsmsFile, File identificationParametersFile, IdentificationParameters identificationParameters, int maxX, boolean removeZeros, Double minMz, Double maxMz, int nThreads, WaitingHandler waitingHandler, ExceptionHandler exceptionHandler) throws IOException, ClassNotFoundException, SQLException, MzMLUnmarshallerException, InterruptedException {

        Duration totalDuration = new Duration();
        totalDuration.start();
        waitingHandler.setWaitingText("Review Figure " + jobName + " start.");

        // Load the spectra in the spectrum factory
        Duration localDuration = new Duration();
        localDuration.start();
        waitingHandler.setSecondaryProgressCounterIndeterminate(true);
        String spectrumFileName = Util.getFileName(spectrumFile);
        waitingHandler.setWaitingText("Loading spectra from " + spectrumFileName + ".");
        spectrumFactory.addSpectra(spectrumFile);
        localDuration.end();
        waitingHandler.setWaitingText("Loading spectra completed (" + localDuration + ").");

        // Load precursors
        localDuration = new Duration();
        localDuration.start();
        waitingHandler.setWaitingText("Loading precursors from " + spectrumFileName + ".");
        SearchParameters searchParameters = identificationParameters.getSearchParameters();
        precursorProcessor = new PrecursorProcessor(spectrumFileName, searchParameters, minMz, maxMz);
        localDuration.end();
        waitingHandler.setWaitingText("Loading precursors completed (" + localDuration + ").");

        // Load the sequences in the sequence factory
        localDuration = new Duration();
        localDuration.start();
        File fastaFile = searchParameters.getFastaFile();
        String fastaFileName = Util.getFileName(fastaFile);
        waitingHandler.setWaitingText("Loading sequences from " + fastaFileName + ".");
        sequenceFactory.loadFastaFile(fastaFile);
        localDuration.end();
        waitingHandler.setWaitingText("Loading sequences completed (" + localDuration + ").");

        // Get PSMs
        localDuration = new Duration();
        localDuration.start();
        waitingHandler.setWaitingText("Getting PSMs according to the identification parameters " + identificationParameters.getName() + ".");
        SequencesProcessor sequencesProcessor = new SequencesProcessor(waitingHandler, exceptionHandler);
        HashMap<String, HashMap<String, PeptideAssumption>> psmMap = sequencesProcessor.iterateSequences(spectrumFileName, precursorProcessor, identificationParameters, maxX, removeZeros, nThreads, minMz, maxMz);
        localDuration.end();
        waitingHandler.setWaitingText("Getting PSMs completed (" + localDuration + ").");

        // Estimate e-values
        localDuration = new Duration();
        localDuration.start();
        waitingHandler.setWaitingText("Estimating e-values.");
        EValueEstimator eValueEstimator = new EValueEstimator(waitingHandler, exceptionHandler);
        eValueEstimator.estimateEValues(psmMap, nThreads);
        localDuration.end();
        waitingHandler.setWaitingText("Estimating e-values completed (" + localDuration + ").");

        // Export all psms
        localDuration = new Duration();
        localDuration.start();
        waitingHandler.setWaitingText("Exporting all PSMs.");
        TxtExporter txtExporter = new TxtExporter(waitingHandler, exceptionHandler);
        txtExporter.writeExport(spectrumFile, psmMap, identificationParametersFile, identificationParameters, allPsmsFile, nThreads, false);
        localDuration.end();
        waitingHandler.setWaitingText("Exporting completed (" + localDuration + ").");

        // Export best psms
        localDuration = new Duration();
        localDuration.start();
        waitingHandler.setWaitingText("Exporting best PSMs.");
        txtExporter.writeExport(spectrumFile, psmMap, identificationParametersFile, identificationParameters, bestPsmsFile, nThreads, true);
        localDuration.end();
        waitingHandler.setWaitingText("Exporting completed (" + localDuration + ").");

        // Export histograms
        localDuration = new Duration();
        localDuration.start();
        waitingHandler.setWaitingText("Exporting Histograms.");
        exportHistograms(psmMap, spectrumFile.getName(), jobName, waitingHandler);
        localDuration.end();
        waitingHandler.setWaitingText("Exporting completed (" + localDuration + ").");

        // Finished
        totalDuration.end();
        waitingHandler.appendReportEndLine();
        waitingHandler.setWaitingText("Onyase engine completed (" + totalDuration + ").");

        // Write report
        File reportFile = new File("C:\\Github\\onyase\\R\\resources\\report_" + jobName + ".txt");
        BufferedWriter reportBw = new BufferedWriter(new FileWriter(reportFile));
        reportBw.write("Duration: " + totalDuration.getDuration());
        reportBw.close();

    }

    private void exportHistograms(HashMap<String, HashMap<String, PeptideAssumption>> psmMap, String fileName, String suffix, WaitingHandler waitingHandler) throws IOException, MzMLUnmarshallerException {

        File precursorFile = new File("C:\\Github\\onyase\\R\\resources\\precursor_" + suffix + ".txt");
        BufferedWriter precursorBw = new BufferedWriter(new FileWriter(precursorFile));

        precursorBw.write("title" + separator + "mz" + separator + "rt" + separator + "nPeptides");
        precursorBw.newLine();

        waitingHandler.setSecondaryProgressCounterIndeterminate(false);
        waitingHandler.setMaxSecondaryProgressCounter(psmMap.size());

        HashMap<Integer, Integer> ionsToPeptideMap = new HashMap<Integer, Integer>();
        FigureMetrics figureMetrics = new FigureMetrics();

        for (String spectrumTitle : psmMap.keySet()) {
            String spectrumKey = Spectrum.getSpectrumKey(fileName, spectrumTitle);
            Precursor precursor = spectrumFactory.getPrecursor(spectrumKey);
            String encodedTitle = URLEncoder.encode(spectrumTitle, "utf-8");
            HashMap<String, PeptideAssumption> assumptions = psmMap.get(spectrumTitle);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(encodedTitle).append(separator).append(precursor.getMz()).append(separator).append(precursor.getRtInMinutes()).append(separator).append(assumptions.size());
            precursorBw.write(stringBuilder.toString());
            ionsToPeptideMap.clear();
            waitingHandler.increaseSecondaryProgressCounter();
        }

        precursorBw.close();
    }
}
