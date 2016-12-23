package no.uib.onyase.scripts.review_figure;

import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.exceptions.exception_handlers.CommandLineExceptionHandler;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationSettings;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import com.compomics.util.preferences.DigestionPreferences;
import com.compomics.util.preferences.IdentificationParameters;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshallerException;

/**
 *
 * @author Marc Vaudel
 */
public class ReviewFigure {

    private String mgfFilePath = "C:\\Projects\\PeptideShaker\\test files\\1 mgf\\qExactive01819.mgf";
    private String parametersFilePath = "C:\\Users\\mvaudel\\Desktop\\test\\test onyase\\test.par";
    private String resourcesFolderPath = "C:\\Github\\onyase\\R\\resources";

    /**
     * The main method used to start PeptideShaker.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        ReviewFigure instance = new ReviewFigure();
        try {
            instance.launch();
            System.out.println(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(0);
        }
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

        String jobName = "9";

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
        PtmSettings ptmSettings = searchParameters.getPtmSettings();
        PTMFactory ptmFactory = PTMFactory.getInstance();
        PTM ptm = ptmFactory.getPTM("Pyrolidone from E");
        ptmSettings.addVariableModification(ptm);
        ptm = ptmFactory.getPTM("Pyrolidone from Q");
        ptmSettings.addVariableModification(ptm);
        ptm = ptmFactory.getPTM("Pyrolidone from carbamidomethylated C");
        ptmSettings.addVariableModification(ptm);
        ptm = ptmFactory.getPTM("Phosphorylation of S");
        ptmSettings.addVariableModification(ptm);
        ptm = ptmFactory.getPTM("Phosphorylation of T");
        ptmSettings.addVariableModification(ptm);
        ptm = ptmFactory.getPTM("Phosphorylation of Y");
        ptmSettings.addVariableModification(ptm);
        DigestionPreferences digestionPreferences = searchParameters.getDigestionPreferences();
//        digestionPreferences.setSpecificity("Trypsin", DigestionPreferences.Specificity.semiSpecific);
//        searchParameters.setMinChargeSearched(new Charge(Charge.PLUS, 1));
//        searchParameters.setMaxChargeSearched(new Charge(Charge.PLUS, 6));
//        searchParameters.setMinIsotopicCorrection(-4);
//        searchParameters.setMaxIsotopicCorrection(4);
        File newParameters = new File(resourcesFolderPath, "Test_Onyase_" + jobName + ".par");
        IdentificationParameters.saveIdentificationParameters(identificationParameters, newParameters);

        ReviewFigureEngine engine = new ReviewFigureEngine();
        engine.launch(jobName, spectrumFile, allPsmsFile, bestPsmsFile, identificationParametersFile, identificationParameters, 2, false, 500.0, null, 3, waitingHandler, exceptionHandler);
    }

}
