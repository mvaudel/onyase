package no.uib.onyase.cli.engine;

import com.compomics.software.cli.CommandParameter;
import com.compomics.cli.identification_parameters.IdentificationParametersInputBean;
import com.compomics.util.preferences.IdentificationParameters;
import java.io.File;
import java.io.IOException;
import no.uib.onyase.cli.paths.PathSettingsCLIInputBean;
import org.apache.commons.cli.CommandLine;

/**
 * The OnyaseCLIInputBean reads and stores command line options from a command
 * line.
 *
 * @author Marc Vaudel
 */
public class OnyaseEngineCLIInputBean {

    /**
     * The spectrum files.
     */
    private File spectrumFile;
    /**
     * The output folder.
     */
    private File outputFolder;
    /**
     * The identification parameters input.
     */
    private IdentificationParametersInputBean identificationParametersInputBean;
    /**
     * The path settings.
     */
    private PathSettingsCLIInputBean pathSettingsCLIInputBean;
    /**
     * Number of threads to use. Defaults to the number of cores available.
     */
    private int nThreads = Runtime.getRuntime().availableProcessors();

    /**
     * Parses the arguments of a command line.
     *
     * @param aLine the command line
     * 
     * @throws IOException thrown if an error occurred while reading the parameters
     * file
     * @throws ClassNotFoundException thrown if the search parameters cannot be
     * converted
     */
    public OnyaseEngineCLIInputBean(CommandLine aLine) throws IOException, ClassNotFoundException {

        // get the files needed for the search
        String arg = aLine.getOptionValue(OnyaseEngineCLIParams.SPECTRUM_FILE.id);
        spectrumFile = new File(arg);

        // output folder
        arg = aLine.getOptionValue(OnyaseEngineCLIParams.OUTPUT_FOLDER.id);
        outputFolder = new File(arg);

        // get the number of threads
        if (aLine.hasOption(OnyaseEngineCLIParams.THREADS.id)) {
            arg = aLine.getOptionValue(OnyaseEngineCLIParams.THREADS.id);
            nThreads = new Integer(arg);
        }

        // identification parameters
        identificationParametersInputBean = new IdentificationParametersInputBean(aLine);

        // path settings
        pathSettingsCLIInputBean = new PathSettingsCLIInputBean(aLine);
    }

    /**
     * Returns the spectrum file.
     * 
     * @return the spectrum file
     */
    public File getSpectrumFile() {
        return spectrumFile;
    }

    /**
     * Returns the output folder.
     *
     * @return the output folder
     */
    public File getOutputFile() {
        return outputFolder;
    }

    /**
     * Returns the identification parameters.
     *
     * @return the identification parameters
     */
    public IdentificationParameters getIdentificationParameters() {
        return identificationParametersInputBean.getIdentificationParameters();
    }

    /**
     * Returns the identification parameters file.
     *
     * @return the identification parameters file
     */
    public File getIdentificationParametersFile() {
        if (identificationParametersInputBean.getDestinationFile() != null) {
            return identificationParametersInputBean.getDestinationFile();
        } else {
            return identificationParametersInputBean.getInputFile();
        }
    }

    /**
     * Returns the number of threads to use.
     *
     * @return the number of threads to use
     */
    public int getNThreads() {
        return nThreads;
    }

    /**
     * Verifies the command line start parameters.
     *
     * @param aLine the command line to validate
     *
     * @return true if the startup was valid
     *
     * @throws IOException if the spectrum file(s) are not found
     */
    public static boolean isValidStartup(CommandLine aLine) throws IOException {

        if (aLine.getOptions().length == 0) {
            return false;
        }

        // check the spectrum file
        if (!aLine.hasOption(OnyaseEngineCLIParams.SPECTRUM_FILE.id) || ((String) aLine.getOptionValue(OnyaseEngineCLIParams.SPECTRUM_FILE.id)).equals("")) {
            System.out.println(System.getProperty("line.separator") + "Spectrum file not specified." + System.getProperty("line.separator"));
            return false;
        } else {
            File file = new File(((String) aLine.getOptionValue(OnyaseEngineCLIParams.SPECTRUM_FILE.id)));
            if (!file.exists()) {
                System.out.println(System.getProperty("line.separator") + "Spectrum file \'" + file.getName() + "\' not found." + System.getProperty("line.separator"));
                return false;
            }
        }

        // check the output folder
        if (!aLine.hasOption(OnyaseEngineCLIParams.OUTPUT_FOLDER.id) || ((String) aLine.getOptionValue(OnyaseEngineCLIParams.OUTPUT_FOLDER.id)).equals("")) {
            System.out.println(System.getProperty("line.separator") + "Output folder not specified." + System.getProperty("line.separator"));
            return false;
        } else {
            File file = new File(((String) aLine.getOptionValue(OnyaseEngineCLIParams.OUTPUT_FOLDER.id)));
            if (!file.exists()) {
                System.out.println(System.getProperty("line.separator") + "Output folder \'" + file.getName() + "\' not found." + System.getProperty("line.separator"));
                return false;
            }
        }

        // check the number of threads
        if (aLine.hasOption(OnyaseEngineCLIParams.THREADS.id)) {
            String arg = aLine.getOptionValue(OnyaseEngineCLIParams.THREADS.id);
            if (!CommandParameter.isPositiveInteger(OnyaseEngineCLIParams.THREADS.id, arg, false)) {
                return false;
            }
        }

        // check the identification parameters
        if (!IdentificationParametersInputBean.isValidStartup(aLine, false)) {
            return false;
        }

        return true;
    }

    /**
     * Returns the path settings provided by the user.
     *
     * @return the path settings provided by the user
     */
    public PathSettingsCLIInputBean getPathSettingsCLIInputBean() {
        return pathSettingsCLIInputBean;
    }
}
