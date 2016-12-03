package no.uib.onyase.applications.engine;

import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.exceptions.exception_handlers.CommandLineExceptionHandler;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import com.compomics.util.preferences.IdentificationParameters;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
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
        try {
            instance.launch();
            System.out.println(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(0);
        }
    }
    
    private void launch() throws IOException, ClassNotFoundException, SQLException, MzMLUnmarshallerException, InterruptedException {
        
        File spectrumFile = new File(mgfFilePath);
        File destinationFile = new File(destinationFilePath);
        File identificationParametersFile = new File(parametersFilePath);
        IdentificationParameters identificationParameters = IdentificationParameters.getIdentificationParameters(identificationParametersFile);
                WaitingHandler waitingHandler = new WaitingHandlerCLIImpl();
                ExceptionHandler exceptionHandler = new CommandLineExceptionHandler();
        
        OnyaseEngine onyaseEngine = new OnyaseEngine();
        onyaseEngine.launch(spectrumFile, destinationFile, identificationParameters, 2, 4, waitingHandler, exceptionHandler);
    }
    
}
