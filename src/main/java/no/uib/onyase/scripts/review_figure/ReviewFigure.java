package no.uib.onyase.scripts.review_figure;

import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.exceptions.exception_handlers.CommandLineExceptionHandler;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationSettings;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import com.compomics.util.preferences.DigestionPreferences;
import com.compomics.util.preferences.IdentificationParameters;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshallerException;

/**
 *
 * @author Marc Vaudel
 */
public class ReviewFigure {

    private String mgfFilePath = "C:\\Projects\\PeptideShaker\\test files\\1 mgf\\qExactive01819.mgf";
    private String parametersFilePath = "C:\\Users\\mvaudel\\Desktop\\test\\test onyase\\test.par";
    private String resourcesFolderPath = "C:\\Github\\onyase\\R\\resources";
    private int nThreads = 3;

    /**
     * The main method used to start the script.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        ReviewFigure instance = new ReviewFigure();
        if (args.length > 0) {
            instance.setMgfFilePath(args[0]);
            instance.setParametersFilePath(args[1]);
            instance.setDestinationFilePath(args[2]);
            instance.setnThreads(Integer.parseInt(args[3]));
        }
        try {
            instance.launch();
            System.out.println(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(0);
        }
    }

    /**
     * Sets the mgf file path.
     * 
     * @param mgfFilePath the mgf file path
     */
    public void setMgfFilePath(String mgfFilePath) {
        this.mgfFilePath = mgfFilePath;
    }

    /**
     * Sets the parameters file path.
     * 
     * @param parametersFilePath the parameters file path
     */
    public void setParametersFilePath(String parametersFilePath) {
        this.parametersFilePath = parametersFilePath;
    }

    /**
     * Sets the destination file path.
     * 
     * @param destinationFilePath the destination file path
     */
    public void setDestinationFilePath(String destinationFilePath) {
        this.resourcesFolderPath = destinationFilePath;
    }

    /**
     * Sets the number of threads.
     * 
     * @param nThreads the number of threads
     */
    public void setnThreads(int nThreads) {
        this.nThreads = nThreads;
    }
    
    /**
     * Launches the search.
     *
     * @throws IOException exception thrown if an error occurs while reading or
     * writing a file
     * @throws ClassNotFoundException exception thrown if an error occurs while
     * uncasting a file
     * @throws SQLException exception thrown if an error occurs while
     * interacting with a database
     * @throws MzMLUnmarshallerException exception thrown if an error occurs
     * while interacting with an mzML file
     * @throws InterruptedException exception thrown if a threading error occurs
     */
    private void launch() throws IOException, ClassNotFoundException, SQLException, MzMLUnmarshallerException, InterruptedException {

        String jobName = "14";

        File spectrumFile = new File(mgfFilePath);
        File allPsmsFile = new File(resourcesFolderPath, "all_psms_" + jobName + ".psm");
        File bestPsmsFile = new File(resourcesFolderPath, "best_psms_" + jobName + ".psm");
        File identificationParametersFile = new File(parametersFilePath);
        IdentificationParameters identificationParameters = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
        AnnotationSettings annotationSettings = new AnnotationSettings(identificationParameters.getSearchParameters());
        annotationSettings.setIntensityLimit(0.5);
        identificationParameters.setAnnotationSettings(annotationSettings);
        WaitingHandler waitingHandler = new WaitingHandlerCLIImpl();
        ExceptionHandler exceptionHandler = new CommandLineExceptionHandler();
        SearchParameters searchParameters = identificationParameters.getSearchParameters();
//        searchParameters.setFastaFile(new File("/media/local-disk/marcvaue/figure/resources/hman_all_23.16_concatenated_target_decoy.fasta"));
       searchParameters.setFastaFile(new File("/media/local-disk/marcvaue/figure/resources/uniprot-human-reviewed-trypsin-november-2016_concatenated_target_decoy.fasta"));
//        searchParameters.setFastaFile(new File("C:\\Databases\\hman_all_23.16_concatenated_target_decoy.fasta"));
        searchParameters.setPrecursorAccuracy(0.5);
        searchParameters.setPrecursorAccuracyType(SearchParameters.MassAccuracyType.DA);
        searchParameters.setFragmentIonAccuracy(0.5);
        searchParameters.setFragmentAccuracyType(SearchParameters.MassAccuracyType.DA);
        annotationSettings.setFragmentIonAccuracy(0.5);
        annotationSettings.setFragmentIonPpm(false);
        HashMap<String, Integer> maxModifications = new HashMap<String, Integer>();
        PtmSettings ptmSettings = new PtmSettings();
        PTMFactory ptmFactory = PTMFactory.getInstance();
        String ptmName = "Carbamidomethylation of C";
        PTM ptm = ptmFactory.getPTM(ptmName);
        ptmSettings.addFixedModification(ptm);
//        ptmSettings.addVariableModification(ptm);
        ptmName = "Oxidation of M";
        maxModifications.put(ptmName, 4);
        ptm = ptmFactory.getPTM(ptmName);
        ptmSettings.addVariableModification(ptm);
        ptmName = "Pyrolidone from E";
        ptm = ptmFactory.getPTM(ptmName);
        ptmSettings.addVariableModification(ptm);
        ptmName = "Pyrolidone from Q";
        ptm = ptmFactory.getPTM(ptmName);
        ptmSettings.addVariableModification(ptm);
        ptmName = "Pyrolidone from carbamidomethylated C";
        ptm = ptmFactory.getPTM(ptmName);
        ptmSettings.addVariableModification(ptm);
//        ptmName = "Phosphorylation of S";
//        maxModifications.put(ptmName, 3);
//        ptm = ptmFactory.getPTM(ptmName);
//        ptmSettings.addVariableModification(ptm);
//        ptmName = "Phosphorylation of T";
//        maxModifications.put(ptmName, 3);
//        ptm = ptmFactory.getPTM(ptmName);
//        ptmSettings.addVariableModification(ptm);
//        ptmName = "Phosphorylation of Y";
//        maxModifications.put(ptmName, 3);
//        ptm = ptmFactory.getPTM(ptmName);
//        ptmSettings.addVariableModification(ptm);
        searchParameters.setPtmSettings(ptmSettings);
        DigestionPreferences digestionPreferences = searchParameters.getDigestionPreferences();
//        digestionPreferences.setSpecificity("Trypsin", DigestionPreferences.Specificity.semiSpecific);
//        digestionPreferences.setCleavagePreference(DigestionPreferences.CleavagePreference.unSpecific);
//digestionPreferences.setnMissedCleavages("Trypsin", 4);
//        searchParameters.setMinChargeSearched(new Charge(Charge.PLUS, 1));
//        searchParameters.setMaxChargeSearched(new Charge(Charge.PLUS, 6));
//        searchParameters.setMinIsotopicCorrection(-4);
//        searchParameters.setMaxIsotopicCorrection(4);
//        ArrayList<Integer> forwardIons = new ArrayList<Integer>();
//        forwardIons.add(PeptideFragmentIon.A_ION);
//        forwardIons.add(PeptideFragmentIon.B_ION);
//        forwardIons.add(PeptideFragmentIon.C_ION);
//        searchParameters.setForwardIons(forwardIons);
//        ArrayList<Integer> rewindIons = new ArrayList<Integer>();
//        rewindIons.add(PeptideFragmentIon.X_ION);
//        rewindIons.add(PeptideFragmentIon.Y_ION);
//        rewindIons.add(PeptideFragmentIon.Z_ION);
//        searchParameters.setRewindIons(rewindIons);
        File newParameters = new File(resourcesFolderPath, "Test_Onyase_" + jobName + ".par");
        IdentificationParameters.saveIdentificationParameters(identificationParameters, newParameters);

        ReviewFigureEngine engine = new ReviewFigureEngine();
        engine.launch(jobName, spectrumFile, allPsmsFile, bestPsmsFile, identificationParametersFile, identificationParameters, 2, 500.0, null, maxModifications, 5, nThreads, waitingHandler, exceptionHandler);
    }
}
