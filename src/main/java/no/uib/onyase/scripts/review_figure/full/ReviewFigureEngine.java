package no.uib.onyase.scripts.review_figure.full;

import com.compomics.util.Util;
import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.experiment.biology.EnzymeFactory;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.experiment.massspectrometry.SpectrumFactory;
import com.compomics.util.preferences.IdentificationParameters;
import com.compomics.util.waiting.Duration;
import com.compomics.util.waiting.WaitingHandler;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import no.uib.onyase.applications.engine.modules.precursor_handling.PrecursorProcessor;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshallerException;

/**
 *
 * @author Marc Vaudel
 */
public class ReviewFigureEngine {

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
     * @param minMz the minimal m/z to consider
     * @param maxMz the maximal m/z to consider
     * @param maxModifications the maximal number of modifications
     * @param maxSites the preferred number of sites to iterate for every PTM
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
    public void launch(String jobName, File spectrumFile, File allPsmsFile, File bestPsmsFile, File identificationParametersFile, IdentificationParameters identificationParameters, int maxX, Double minMz, Double maxMz, HashMap<String, Integer> maxModifications, int maxSites, int nThreads, WaitingHandler waitingHandler, ExceptionHandler exceptionHandler) throws IOException, ClassNotFoundException, SQLException, MzMLUnmarshallerException, InterruptedException {

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
        
        // Get temporary file to export the PSMs prior to e-value estimation
        File tempFile = new File(allPsmsFile.getPath() + "_temp");

        // Get PSMs
        localDuration = new Duration();
        localDuration.start();
        waitingHandler.setWaitingText("Getting PSMs according to the identification parameters " + identificationParameters.getName() + ".");
        SequencesProcessor sequencesProcessor = new SequencesProcessor(waitingHandler, exceptionHandler);
        sequencesProcessor.iterateSequences(spectrumFileName, precursorProcessor, identificationParameters, maxX, nThreads, minMz, maxMz, maxModifications, maxSites, tempFile);
        localDuration.end();
        waitingHandler.setWaitingText("Getting PSMs completed (" + localDuration + ").");
        
        // Get scores and Figure Data
        HashMap<String, HashMap<String, FigureMetrics>> scoreMap = sequencesProcessor.getScoresMap();
        int nLines = sequencesProcessor.getnLines();

        // Export histograms
        File precursorFile = new File("C:\\Github\\onyase\\R\\resources\\precursor_" + jobName + ".txt");
        localDuration = new Duration();
        localDuration.start();
        waitingHandler.setWaitingText("Exporting Histograms.");
        HistogramExporter histogramExporter = new HistogramExporter(waitingHandler, exceptionHandler);
        histogramExporter.writeExport(spectrumFile, scoreMap, identificationParameters, precursorFile, nThreads);
        localDuration.end();
        waitingHandler.setWaitingText("Exporting completed (" + localDuration + ").");

        // Estimate e-values
        localDuration = new Duration();
        localDuration.start();
        waitingHandler.setWaitingText("Estimating e-values.");
        EValueEstimator eValueEstimator = new EValueEstimator(waitingHandler, exceptionHandler);
        eValueEstimator.estimateInterpolationCoefficients(spectrumFileName, scoreMap, nThreads);
        localDuration.end();
        waitingHandler.setWaitingText("Estimating e-values completed (" + localDuration + ").");

        // Estimate e-values
        localDuration = new Duration();
        localDuration.start();
        waitingHandler.setWaitingText("Exporting e-values.");
        EValueExporter eValueExporter = new EValueExporter(waitingHandler);
        eValueExporter.writeEvalues(eValueEstimator, tempFile, nLines, allPsmsFile, bestPsmsFile);
        localDuration.end();
        waitingHandler.setWaitingText("Exporting e-values completed (" + localDuration + ").");
        
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
}
