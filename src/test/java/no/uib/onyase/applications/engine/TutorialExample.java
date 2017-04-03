package no.uib.onyase.applications.engine;

import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.exceptions.exception_handlers.CommandLineExceptionHandler;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.ions.ReporterIon;
import com.compomics.util.experiment.identification.spectrum_annotation.SimplePeptideAnnotator;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import com.compomics.util.preferences.DigestionPreferences;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import no.uib.onyase.applications.engine.modules.scoring.PsmScore;
import no.uib.onyase.applications.engine.parameters.EngineParameters;
import no.uib.onyase.applications.engine.parameters.SpectrumAnnotationSettings;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshallerException;

/**
 * Test running the tutorial example dataset.
 *
 * @author Marc Vaudel
 */
public class TutorialExample {
    /**
     * The modifications factory.
     */
    private PTMFactory ptmFactory = PTMFactory.getInstance();

    private String mgfFilePath = "C:\\Projects\\PeptideShaker\\test files\\1 mgf\\qExactive01819.mgf";
    private String destinationFilePath = "C:\\Projects\\Onyase\\test\\output\\qExactive01819.psm";
    private String fastaFilePath = "C:\\Databases\\uniprot-human-reviewed-trypsin-november-2016_concatenated_target_decoy.fasta";

    /**
     * The main method used to start the script.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        TutorialExample instance = new TutorialExample();
        if (args.length > 0) {
            instance.setMgfFilePath(args[0]);
            instance.setFastaFilePath(args[1]);
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
     * @param fastaFilePath the parameters file path
     */
    public void setFastaFilePath(String fastaFilePath) {
        this.fastaFilePath = fastaFilePath;
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
     * @throws InterruptedException exception thrown if a thread is interrupted
     */
    private void launch() throws IOException, ClassNotFoundException, SQLException, MzMLUnmarshallerException, InterruptedException {

        // Files
        File spectrumFile = new File(mgfFilePath);
        File destinationFile = new File(destinationFilePath);
        File fastaFile = new File(fastaFilePath);
        
        // Search parameters
        EngineParameters engineParameters = new EngineParameters();
        engineParameters.setName("Test tutorials Onyase");
        // Score
        engineParameters.setPsmScore(PsmScore.snrScore);
        // Modifications
        ArrayList<String> fixedModifications = new ArrayList<String>(1);
        fixedModifications.add("Carbamidomethylation of C");
        engineParameters.setFixedModifications(fixedModifications);
        String[] variableModifications = new String[]{"Oxidation of M"};
//        String[] variableModifications = new String[]{"Oxidation of M", "Pyrolidone from E", "Pyrolidone from Q", "Pyrolidone from carbamidomethylated C"};
        engineParameters.setVariableModifications(variableModifications);
        HashMap<String, Integer> maxModifications = new HashMap<String, Integer>(1);
        maxModifications.put("Oxidation of M", 2);
        engineParameters.setMaxModifications(maxModifications);
        engineParameters.setMaxSites(5);
        // Tolerances
        engineParameters.setMs1Tolerance(10);
        engineParameters.setMs1TolerancePpm(true);
        engineParameters.setMs2Tolerance(10);
        engineParameters.setMs2TolerancePpm(true);
        engineParameters.setMs1MinMz(500);
        engineParameters.setMs2IntensityThreshold(0.01);
        // Charge
        engineParameters.setUseMgfCharge(false);
        engineParameters.setMinCharge(2);
        engineParameters.setMaxCharge(4);
        // Isotopes
        engineParameters.setMinIsotopicCorrection(0);
        engineParameters.setMaxIsotopicCorrection(1);
        // Digestion
        engineParameters.setDigestionPreferences(DigestionPreferences.getDefaultPreferences());
        // Fragmentation
        engineParameters.setDominantSeries(SimplePeptideAnnotator.IonSeries.by);
        // Reporter ions
        HashMap<Double, ReporterIon> reporterIonsMap = new HashMap<Double, ReporterIon>(1);
        for (String modificationName : variableModifications) {
            PTM modification = ptmFactory.getPTM(modificationName);
            for (ReporterIon reporterIon : modification.getReporterIons()) {
                reporterIonsMap.put(reporterIon.getTheoreticMass(), reporterIon);
            }
        }
        ReporterIon[] reporterIons = new ReporterIon[reporterIonsMap.size()];
        ArrayList<Double> reporterIonsMzs = new ArrayList<Double>(reporterIonsMap.keySet());
        Collections.sort(reporterIonsMzs);
        for (int i = 0 ; i < reporterIonsMzs.size() ; i++) {
            ReporterIon reporterIon = reporterIonsMap.get(reporterIonsMzs.get(i));
            reporterIons[i] = reporterIon;
        }
        // Spectrum annotation
        SpectrumAnnotationSettings spectrumAnnotationSettings = new SpectrumAnnotationSettings();
        spectrumAnnotationSettings.setB(true);
        spectrumAnnotationSettings.setY(true);
        spectrumAnnotationSettings.setPrecursor(true);
        spectrumAnnotationSettings.setImmonium(true);
        spectrumAnnotationSettings.setRelated(true);
        spectrumAnnotationSettings.setNeutralLosses(true);
        spectrumAnnotationSettings.setNeutralLossesSequenceDependent(true);
        spectrumAnnotationSettings.setReporterIons(reporterIons);
        engineParameters.setSpectrumAnnotationSettings(spectrumAnnotationSettings);
        
        // Waiting and exception handling
        WaitingHandler waitingHandler = new WaitingHandlerCLIImpl();
        ExceptionHandler exceptionHandler = new CommandLineExceptionHandler();
        
        // Number of threads
        int nThreads = 3;

        OnyaseEngine onyaseEngine = new OnyaseEngine();
        onyaseEngine.launch(spectrumFile, destinationFile, fastaFile, engineParameters, nThreads, waitingHandler, exceptionHandler);
    }

}
