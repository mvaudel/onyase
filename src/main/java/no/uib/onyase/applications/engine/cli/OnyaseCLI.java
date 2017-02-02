package no.uib.onyase.applications.engine.cli;

import com.compomics.software.CompomicsWrapper;
import com.compomics.software.settings.PathKey;
import com.compomics.software.settings.UtilitiesPathPreferences;
import com.compomics.util.experiment.biology.*;
import com.compomics.util.experiment.massspectrometry.SpectrumFactory;
import com.compomics.util.gui.filehandling.TempFilesManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Callable;
import no.uib.onyase.settings.OnyasePathPreferences;
import no.uib.onyase.utils.Properties;
import org.apache.commons.cli.*;

/**
 * This starts an Onyase application in command line.
 *
 * @author Marc Vaudel
 */
public class OnyaseCLI implements Callable {

    /**
     * The command line parameters.
     */
    private OnyaseCLIInputBean onyaseCLIInputBean;
    /**
     * The enzyme factory.
     */
    private EnzymeFactory enzymeFactory;
    /**
     * The modifications factory.
     */
    private PTMFactory ptmFactory;
    /**
     * The spectrum factory.
     */
    private SpectrumFactory spectrumFactory = SpectrumFactory.getInstance();

    /**
     * Construct a new runnable from a list of arguments. When initialization is
     * successful, calling run() will start the processing.
     *
     * @param args the command line arguments
     */
    public OnyaseCLI(String[] args) {

        try {

            Options lOptions = new Options();
            OnyaseCLIParams.createOptionsCLI(lOptions);
            BasicParser parser = new BasicParser();
            CommandLine line = parser.parse(lOptions, args);

            if (!OnyaseCLIInputBean.isValidStartup(line)) {
                PrintWriter lPrintWriter = new PrintWriter(System.out);
                lPrintWriter.print(System.getProperty("line.separator") + "======================" + System.getProperty("line.separator"));
                lPrintWriter.print("OnyaseCLI" + System.getProperty("line.separator"));
                lPrintWriter.print("======================" + System.getProperty("line.separator"));
                lPrintWriter.print(getHeader());
                lPrintWriter.print(OnyaseCLIParams.getOptionsAsString());
                lPrintWriter.flush();
                lPrintWriter.close();

                System.exit(0);
            } else {
                onyaseCLIInputBean = new OnyaseCLIInputBean(line);
                call();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Calling this method will run the configured process.
     */
    public Object call() {

        PathSettingsCLIInputBean pathSettingsCLIInputBean = onyaseCLIInputBean.getPathSettingsCLIInputBean();

        if (pathSettingsCLIInputBean.getLogFolder() != null) {
            redirectErrorStream(pathSettingsCLIInputBean.getLogFolder());
        }

        if (pathSettingsCLIInputBean.hasInput()) {
            PathSettingsCLI pathSettingsCLI = new PathSettingsCLI(pathSettingsCLIInputBean);
            pathSettingsCLI.setPathSettings();
        } else {
            try {
                File pathConfigurationFile = new File(getJarFilePath(), UtilitiesPathPreferences.configurationFileName);
                if (pathConfigurationFile.exists()) {
                    OnyasePathPreferences.loadPathPreferencesFromFile(pathConfigurationFile);
                }
            } catch (Exception e) {
                System.out.println("An error occurred when setting path configuration. Default paths will be used.");
                e.printStackTrace();
            }
            try {
                ArrayList<PathKey> errorKeys = OnyasePathPreferences.getErrorKeys(getJarFilePath());
                if (!errorKeys.isEmpty()) {
                    System.out.println("Unable to write in the following configuration folders. Please use a temporary folder, "
                            + "the path configuration command line, or edit the configuration paths from the graphical interface.");
                    for (PathKey pathKey : errorKeys) {
                        System.out.println(pathKey.getId() + ": " + pathKey.getDescription());
                    }
                }
            } catch (Exception e) {
                System.out.println("Unable to load the path configurations. Default pathswill be used.");
            }
        }

        // load enzymes
        enzymeFactory = EnzymeFactory.getInstance();

        // load modifications
        ptmFactory = PTMFactory.getInstance();

        try {
            
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            TempFilesManager.deleteTempFolders();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Header message when printing the usage.
     */
    private static String getHeader() {
        return System.getProperty("line.separator")
                + "OnyaseCLI is a command line interface for the Onyase applications." + System.getProperty("line.separator")
                + System.getProperty("line.separator")
                + "Spectra must be provided in the Mascot Generic File (mgf) format." + System.getProperty("line.separator")
                + System.getProperty("line.separator")
                + "The identification parameters can be provided as a file as saved from the GUI or generated using the IdentificationParametersCLI." + System.getProperty("line.separator")
                + "See http://compomics.github.io/compomics-utilities/wiki/identificationparameterscli.html for more details." + System.getProperty("line.separator")
                + System.getProperty("line.separator")
                + "For further help and issue report see https://github.com/mvaudel/onyase." + System.getProperty("line.separator")
                + System.getProperty("line.separator")
                + "----------------------"
                + System.getProperty("line.separator")
                + "OPTIONS"
                + System.getProperty("line.separator")
                + "----------------------" + System.getProperty("line.separator")
                + "\n";
    }

    /**
     * Starts the launcher creating a new instance.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new OnyaseCLI(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * redirects the error stream to the log of a given folder.
     *
     * @param logFolder the folder where to save the log
     */
    public static void redirectErrorStream(File logFolder) {

        try {
            logFolder.mkdirs();
            File file = new File(logFolder, "Onyase.log");
            System.setErr(new java.io.PrintStream(new FileOutputStream(file, true)));

            System.err.println(System.getProperty("line.separator") + System.getProperty("line.separator") + new Date()
                    + ": Onyase version " + new Properties().getVersion() + ".");
            System.err.println("Memory given to the Java virtual machine: " + Runtime.getRuntime().maxMemory() + ".");
            System.err.println("Total amount of memory in the Java virtual machine: " + Runtime.getRuntime().totalMemory() + ".");
            System.err.println("Free memory: " + Runtime.getRuntime().freeMemory() + ".");
            System.err.println("Java version: " + System.getProperty("java.version") + ".");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the path to the jar file.
     *
     * @return the path to the jar file
     */
    public String getJarFilePath() {
        return CompomicsWrapper.getJarFilePath(this.getClass().getResource("OnyaseCLI.class").getPath(), "Onyase");
    }
}
