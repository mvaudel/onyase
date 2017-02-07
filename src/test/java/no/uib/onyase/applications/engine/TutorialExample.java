package no.uib.onyase.applications.engine;

import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.exceptions.exception_handlers.CommandLineExceptionHandler;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationSettings;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import com.compomics.util.preferences.IdentificationParameters;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshallerException;

/**
 * Test running the tutorial example dataset.
 *
 * @author Marc Vaudel
 */
public class TutorialExample {

    private String mgfFilePath = "C:\\Projects\\PeptideShaker\\test files\\1 mgf\\qExactive01819.mgf";
    private String parametersFilePath = "C:\\Users\\mvaudel\\Desktop\\test\\test onyase\\test.par";
    private String destinationFilePath = "C:\\Users\\mvaudel\\Desktop\\test\\test onyase\\test.psm";

    /**
     * The main method used to start PeptideShaker.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        TutorialExample instance = new TutorialExample();
        if (args.length > 0) {
            instance.setMgfFilePath(args[0]);
            instance.setParametersFilePath(args[1]);
            instance.setDestinationFilePath(args[2]);
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
        this.destinationFilePath = destinationFilePath;
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

        File spectrumFile = new File(mgfFilePath);
        File destinationFile = new File(destinationFilePath);
        File identificationParametersFile = new File(parametersFilePath);
        IdentificationParameters identificationParameters = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
        AnnotationSettings annotationSettings = new AnnotationSettings(identificationParameters.getSearchParameters());
        annotationSettings.setIntensityLimit(0.5);
        identificationParameters.setAnnotationSettings(annotationSettings);
        WaitingHandler waitingHandler = new WaitingHandlerCLIImpl();
        ExceptionHandler exceptionHandler = new CommandLineExceptionHandler();
        SearchParameters searchParameters = identificationParameters.getSearchParameters();
        HashMap<String, Integer> maxModifications = new HashMap<String, Integer>();
        PtmSettings ptmSettings = new PtmSettings();
        PTMFactory ptmFactory = PTMFactory.getInstance();
        String ptmName = "Carbamidomethylation of C";
        PTM ptm = ptmFactory.getPTM(ptmName);
        ptmSettings.addFixedModification(ptm);
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
        searchParameters.setPtmSettings(ptmSettings);
        File newParameters = new File("C:\\Users\\mvaudel\\.compomics\\identification_parameters\\Test Onyase.par");
        IdentificationParameters.saveIdentificationParameters(identificationParameters, newParameters);

        OnyaseEngine onyaseEngine = new OnyaseEngine();
        onyaseEngine.launch(spectrumFile, destinationFile, identificationParameters, 2, 500.0, null, maxModifications, 5, 4, waitingHandler, exceptionHandler);
    }

}
