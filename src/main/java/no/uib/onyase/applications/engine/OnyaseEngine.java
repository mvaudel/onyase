package no.uib.onyase.applications.engine;

import com.compomics.util.Util;
import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.experiment.biology.enzymes.EnzymeFactory;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.mass_spectrometry.SpectrumFactory;
import com.compomics.util.waiting.Duration;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import no.uib.onyase.applications.engine.export.TextExporter;
import no.uib.onyase.applications.engine.model.Psm;
import no.uib.onyase.applications.engine.modules.precursor_handling.PrecursorProcessor;
import no.uib.onyase.applications.engine.modules.scoring.EValueEstimator;
import no.uib.onyase.applications.engine.modules.scoring.evalue_estimators.HyperscoreEValueEstimator;
import no.uib.onyase.applications.engine.modules.scoring.evalue_estimators.SnrEvalueEstimator;
import no.uib.onyase.applications.engine.parameters.EngineParameters;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshallerException;

/**
 * This class launches searches using the Onyase engine.
 *
 * @author Marc Vaudel
 */
public class OnyaseEngine {

    /**
     * The spectrum factory.
     */
    private SpectrumFactory spectrumFactory;
    /**
     * The enzyme factory.
     */
    private EnzymeFactory enzymeFactory;
    /**
     * The PTM factory.
     */
    private ModificationFactory ptmFactory;
    /**
     * The module handling the precursors.
     */
    private PrecursorProcessor precursorProcessor;

    /**
     * Constructor.
     */
    public OnyaseEngine() {
        initializeFactories();
    }

    /**
     * Initializes the factories to default.
     */
    private void initializeFactories() {
        spectrumFactory = SpectrumFactory.getInstance();
        enzymeFactory = EnzymeFactory.getInstance();
        ptmFactory = ModificationFactory.getInstance();
    }

    /**
     * Launches the search.
     *
     * @param spectrumFile the spectrum file to search
     * @param psmsFile the file where to export all psms
     * @param fastaFile the file containing the protein sequences in fasta
     * format
     * @param engineParameters the engine parameters
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
    public void launch(File spectrumFile, File psmsFile, File fastaFile, EngineParameters engineParameters, int nThreads, WaitingHandler waitingHandler, ExceptionHandler exceptionHandler) throws IOException, ClassNotFoundException, SQLException, MzMLUnmarshallerException, InterruptedException {
        launch(spectrumFile, psmsFile, fastaFile, null, engineParameters, nThreads, waitingHandler, exceptionHandler);
    }

    /**
     * Launches the search.
     *
     * @param spectrumFile the spectrum file to search
     * @param psmsFile the file where to export all psms
     * @param fastaFile the file containing the protein sequences in fasta
     * format
     * @param exclusionListFilePath path of the exclusion list to use
     * @param engineParameters the engine parameters
     * @param nThreads the number of threads to use
     * @param waitingHandler a waiting handler providing feedback to the user
     * and allowing canceling the process
     * @param exceptionHandler a handler for the exceptions
     *
     * @throws IOException exception thrown if an error occurred while reading
     * or writing a file
     * @throws MzMLUnmarshallerException exception thrown if an error occurred
     * while reading an mzML file
     * @throws InterruptedException exception thrown if a threading error
     * occurred
     */
    public void launch(
            File spectrumFile, 
            File psmsFile, 
            File fastaFile, 
            String exclusionListFilePath, 
            EngineParameters engineParameters, 
            int nThreads, 
            WaitingHandler waitingHandler, 
            ExceptionHandler exceptionHandler
    ) throws IOException, MzMLUnmarshallerException, InterruptedException {

        Duration totalDuration = new Duration();
        totalDuration.start();
        waitingHandler.appendReportEndLine();
        waitingHandler.setWaitingText("Onyase engine start.");

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
        precursorProcessor = new PrecursorProcessor(spectrumFileName, engineParameters.getMs1Tolerance(), engineParameters.isMs1TolerancePpm(), engineParameters.getMs2Tolerance(), engineParameters.isMs2TolerancePpm(), engineParameters.getMinCharge(), engineParameters.getMaxSites(), engineParameters.getMs1MinMz(), engineParameters.getMs1MaxMz());
        localDuration.end();
        waitingHandler.setWaitingText("Loading precursors completed (" + localDuration + ").");

        // Get PSMs
        Duration psmDuration = new Duration();
        psmDuration.start();
        waitingHandler.setWaitingText("Getting PSMs according to the identification parameters " + engineParameters.getName() + ".");
        SequencesProcessor sequencesProcessor = new SequencesProcessor(waitingHandler, exceptionHandler);
        sequencesProcessor.iterateSequences(fastaFile, spectrumFileName, precursorProcessor, exclusionListFilePath, engineParameters, nThreads);
        psmDuration.end();
        waitingHandler.setWaitingText("Getting PSMs completed (" + psmDuration + ").");

        // Get PSMs
        HashMap<String, HashMap<Long, Psm>> psmsMap = sequencesProcessor.getPsms();

        // Estimate e-values
        localDuration = new Duration();
        localDuration.start();
        waitingHandler.setWaitingText("Estimating e-values.");
        EValueEstimator eValueEstimator;
        switch (engineParameters.getPsmScore()) {
            case hyperscore:
                HyperscoreEValueEstimator hyperscoreEValueEstimator = new HyperscoreEValueEstimator(waitingHandler, exceptionHandler);
                hyperscoreEValueEstimator.estimateInterpolationCoefficients(spectrumFileName, psmsMap, nThreads);
                eValueEstimator = hyperscoreEValueEstimator;
                break;
            case snrScore:
                eValueEstimator = new SnrEvalueEstimator();
                break;
            default:
                throw new UnsupportedOperationException("Score " + engineParameters.getPsmScore() + " not implemented.");
        }

        localDuration.end();
        waitingHandler.setWaitingText("Estimating e-values completed (" + localDuration + ").");

        // Export PSMs
        localDuration = new Duration();
        localDuration.start();
        waitingHandler.setWaitingText("Exporting results to " + psmsFile.getAbsolutePath() + ".");
        TextExporter textExporter = new TextExporter(waitingHandler, exceptionHandler);
        textExporter.writePsms(spectrumFileName, psmsMap, eValueEstimator, psmsFile);
        localDuration.end();
        waitingHandler.setWaitingText("Exporting results completed (" + localDuration + ").");

        // Finished
        totalDuration.end();
        waitingHandler.appendReportEndLine();
        waitingHandler.setWaitingText("Onyase engine completed (" + totalDuration + ").");

    }
}
