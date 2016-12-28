package no.uib.onyase.applications.engine;

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
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import no.uib.onyase.applications.engine.model.PeptideDraft;
import no.uib.onyase.applications.engine.modules.precursor_handling.PrecursorProcessor;
import no.uib.onyase.scripts.review_figure.partial.PsmScorer;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshallerException;

/**
 * The Onyase Engine runs a standard database search.
 *
 * @author Marc Vaudel
 */
public class OnyaseEngine {

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
    public OnyaseEngine() {
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
     * @param spectrumFile the spectrum file to search
     * @param allPsmsFile the file where to export all psms
     * @param identificationParametersFile the file where the identification
     * parameters are stored
     * @param identificationParameters the identification parameters
     * @param maxX the maximal number of Xs to allow in a peptide sequence
     * @param minMz the minimal m/z to consider
     * @param maxMz the maximal m/z to consider
     * @param maxModifications the maximal number of modifications
     * @param maxPtmSites the preferred number of PTM sites to inspect for every
     * PTM.
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
    public void launch(File spectrumFile, File allPsmsFile, File identificationParametersFile, IdentificationParameters identificationParameters, int maxX, Double minMz, Double maxMz, HashMap<String, Integer> maxModifications, int maxPtmSites, int nThreads, WaitingHandler waitingHandler, ExceptionHandler exceptionHandler) throws IOException, ClassNotFoundException, SQLException, MzMLUnmarshallerException, InterruptedException {

        Duration totalDuration = new Duration();
        totalDuration.start();
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
        HashMap<String, HashMap<String, PeptideDraft>> psmMap = sequencesProcessor.iterateSequences(spectrumFileName, precursorProcessor, identificationParameters, maxX, nThreads, minMz, maxMz, maxModifications);
        localDuration.end();
        waitingHandler.setWaitingText("Getting PSMs completed (" + localDuration + ").");

        // Estimate Scores
        localDuration = new Duration();
        localDuration.start();
        waitingHandler.setWaitingText("Scoring PSMs.");
        PsmScorer psmScorer = new PsmScorer(waitingHandler, exceptionHandler);
        psmScorer.estimateScores(spectrumFileName, psmMap, identificationParameters, maxPtmSites, nThreads, allPsmsFile, null);
        localDuration.end();
        waitingHandler.setWaitingText("Scoring PSMs completed (" + localDuration + ").");

        // Finished
        totalDuration.end();
        waitingHandler.appendReportEndLine();
        waitingHandler.setWaitingText("Onyase engine completed (" + totalDuration + ").");

    }
}
