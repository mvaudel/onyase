package no.uib.onyase.applications.engine;

import com.compomics.util.Util;
import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.experiment.biology.EnzymeFactory;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.biology.ions.ElementaryIon;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.peptide_fragmentation.PeptideFragmentationModel;
import com.compomics.util.experiment.identification.protein_sequences.ProteinSequenceIterator;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.experiment.identification.psm_scoring.psm_scores.HyperScore;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationSettings;
import com.compomics.util.experiment.identification.spectrum_annotation.SpecificAnnotationSettings;
import com.compomics.util.experiment.identification.spectrum_annotation.spectrum_annotators.PeptideSpectrumAnnotator;
import com.compomics.util.experiment.identification.spectrum_assumptions.PeptideAssumption;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Precursor;
import com.compomics.util.experiment.massspectrometry.Spectrum;
import com.compomics.util.experiment.massspectrometry.SpectrumFactory;
import com.compomics.util.experiment.massspectrometry.indexes.PrecursorMap;
import com.compomics.util.preferences.DigestionPreferences;
import com.compomics.util.preferences.IdentificationParameters;
import com.compomics.util.preferences.ProcessingPreferences;
import com.compomics.util.waiting.Duration;
import com.compomics.util.waiting.WaitingHandler;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import no.uib.onyase.applications.engine.export.TxtExporter;
import no.uib.onyase.applications.engine.modules.EValueEstimator;
import no.uib.onyase.applications.engine.modules.PrecursorProcessor;
import no.uib.onyase.applications.engine.modules.SequencesProcessor;
import no.uib.onyase.scripts.ReviewFigure;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshallerException;

/**
 * The Onyase engine searches spectra according to given identification
 * parameters.
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
     * The handler for the exceptions.
     */
    private ExceptionHandler exceptionHandler;
    /**
     * The waiting handler provides feedback to the user and allowing canceling
     * the process.
     */
    private WaitingHandler waitingHandler;
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
     * @param destinationFile the destination file
     * @param identificationParameters the identification parameters
     * @param maxX the maximal number of Xs to allow in a peptide sequence
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
    public void launch(File spectrumFile, File destinationFile, IdentificationParameters identificationParameters, int maxX, int nThreads, WaitingHandler waitingHandler, ExceptionHandler exceptionHandler) throws IOException, ClassNotFoundException, SQLException, MzMLUnmarshallerException, InterruptedException {

        // Load the spectra in the spectrum factory
        waitingHandler.setSecondaryProgressCounterIndeterminate(true);
        String spectrumFileName = Util.getFileName(spectrumFile);
        waitingHandler.setWaitingText("Loading spectra from " + spectrumFileName + ".");
        spectrumFactory.addSpectra(spectrumFile);

        // Load precursors
        waitingHandler.setWaitingText("Loading precursors from " + spectrumFileName + ".");
        SearchParameters searchParameters = identificationParameters.getSearchParameters();
        precursorProcessor = new PrecursorProcessor(spectrumFileName, searchParameters);

        // Load the sequences in the sequence factory
        File fastaFile = searchParameters.getFastaFile();
        String fastaFileName = Util.getFileName(fastaFile);
        waitingHandler.setWaitingText("Loading sequences from " + fastaFileName + ".");
        sequenceFactory.loadFastaFile(fastaFile);

        // Get PSMs
        waitingHandler.setWaitingText("Getting PSMs according to the identification parameters (" + identificationParameters.getName() + ").");
        SequencesProcessor sequencesProcessor = new SequencesProcessor(waitingHandler, exceptionHandler);
        HashMap<String, HashMap<String, PeptideAssumption>> psmMap = sequencesProcessor.iterateSequences(spectrumFileName, precursorProcessor, identificationParameters, maxX, nThreads);

        // Estimate e-values
        waitingHandler.setWaitingText("Estimating e-values.");
        EValueEstimator eValueEstimator = new EValueEstimator(waitingHandler, exceptionHandler);
        eValueEstimator.estimateEValues(psmMap, nThreads);

        // Export
        waitingHandler.setWaitingText("Exporting PSMs.");
        TxtExporter txtExporter = new TxtExporter(waitingHandler, exceptionHandler);
        txtExporter.writeExport(psmMap, destinationFile, nThreads);
    }
}
