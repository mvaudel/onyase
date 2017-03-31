package no.uib.onyase.cli.engine;

import com.compomics.software.cli.CommandParameter;
import java.io.File;
import java.io.IOException;
import no.uib.onyase.applications.engine.parameters.EngineParameters;
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
     * The fasta file.
     */
    private File fastaFile;
    /**
     * The output folder.
     */
    private File outputFolder;
    /**
     * the path to the exclusion list
     */
    private String exclusionListPath = null;
    /**
     * The engine parameters.
     */
    private EngineParameters engineParameters;
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
        String arg = aLine.getOptionValue(OnyaseEngineCLIParameter.SPECTRUM_FILE.id);
        spectrumFile = new File(arg);

        // fasta file
        arg = aLine.getOptionValue(OnyaseEngineCLIParameter.FASTA.id);
        fastaFile = new File(arg);

        // output folder
        arg = aLine.getOptionValue(OnyaseEngineCLIParameter.OUTPUT.id);
        outputFolder = new File(arg);

        // get the path to the exclusion list
        if (aLine.hasOption(OnyaseEngineCLIParameter.EXCLUSION_LIST.id)) {
            arg = aLine.getOptionValue(OnyaseEngineCLIParameter.EXCLUSION_LIST.id);
            exclusionListPath = arg;
        }

        // get the number of threads
        if (aLine.hasOption(OnyaseEngineCLIParameter.THREADS.id)) {
            arg = aLine.getOptionValue(OnyaseEngineCLIParameter.THREADS.id);
            nThreads = new Integer(arg);
        }

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
    public File getOutputFolder() {
        return outputFolder;
    }

    /**
     * Returns the fasta file.
     * 
     * @return the fasta file
     */
    public File getFastaFile() {
        return fastaFile;
    }

    /**
     * Returns the path to the exclusion list.
     * 
     * @return the path to the exclusion list
     */
    public String getExclusionListPath() {
        return exclusionListPath;
    }

    /**
     * Returns the engine parameters.
     * 
     * @return the engine parameters
     */
    public EngineParameters getEngineParameters() {
        return engineParameters;
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
        if (!aLine.hasOption(OnyaseEngineCLIParameter.SPECTRUM_FILE.id) || ((String) aLine.getOptionValue(OnyaseEngineCLIParameter.SPECTRUM_FILE.id)).equals("")) {
            System.out.println(System.getProperty("line.separator") + "Spectrum file not specified." + System.getProperty("line.separator"));
            return false;
        } else {
            File file = new File(((String) aLine.getOptionValue(OnyaseEngineCLIParameter.SPECTRUM_FILE.id)));
            if (!file.exists()) {
                System.out.println(System.getProperty("line.separator") + "Spectrum file \'" + file.getName() + "\' not found." + System.getProperty("line.separator"));
                return false;
            }
        }

        // check the fasta file
        if (!aLine.hasOption(OnyaseEngineCLIParameter.FASTA.id) || ((String) aLine.getOptionValue(OnyaseEngineCLIParameter.FASTA.id)).equals("")) {
            System.out.println(System.getProperty("line.separator") + "Fasta file not specified." + System.getProperty("line.separator"));
            return false;
        } else {
            File file = new File(((String) aLine.getOptionValue(OnyaseEngineCLIParameter.FASTA.id)));
            if (!file.exists()) {
                System.out.println(System.getProperty("line.separator") + "Fasta file \'" + file.getName() + "\' not found." + System.getProperty("line.separator"));
                return false;
            }
        }

        // check the parameters file
        if (!aLine.hasOption(OnyaseEngineCLIParameter.PARAMS.id) || ((String) aLine.getOptionValue(OnyaseEngineCLIParameter.PARAMS.id)).equals("")) {
            System.out.println(System.getProperty("line.separator") + "Parameters file not specified." + System.getProperty("line.separator"));
            return false;
        } else {
            File file = new File(((String) aLine.getOptionValue(OnyaseEngineCLIParameter.PARAMS.id)));
            if (!file.exists()) {
                System.out.println(System.getProperty("line.separator") + "Parameters file \'" + file.getName() + "\' not found." + System.getProperty("line.separator"));
                return false;
            }
            try {
                EngineParameters.getIdentificationParameters(file);
            } catch (Exception e) {
                System.out.println(System.getProperty("line.separator") + "An error occurres while reading the parameters file \'" + file.getName() + "\', see below." + System.getProperty("line.separator"));
                e.printStackTrace();
                return false;
            }
        }

        // check the output folder
        if (!aLine.hasOption(OnyaseEngineCLIParameter.OUTPUT.id) || ((String) aLine.getOptionValue(OnyaseEngineCLIParameter.OUTPUT.id)).equals("")) {
            System.out.println(System.getProperty("line.separator") + "Output folder not specified." + System.getProperty("line.separator"));
            return false;
        } else {
            File file = new File(((String) aLine.getOptionValue(OnyaseEngineCLIParameter.OUTPUT.id)));
            if (!file.exists()) {
                System.out.println(System.getProperty("line.separator") + "Output folder \'" + file.getName() + "\' not found." + System.getProperty("line.separator"));
                return false;
            }
        }

        // check the exclusion list
        if (aLine.hasOption(OnyaseEngineCLIParameter.EXCLUSION_LIST.id)) {
            File file = new File(((String) aLine.getOptionValue(OnyaseEngineCLIParameter.EXCLUSION_LIST.id)));
            if (!file.exists()) {
                System.out.println(System.getProperty("line.separator") + "Exclusion list \'" + file.getName() + "\' not found." + System.getProperty("line.separator"));
                return false;
            }
        }

        // check the number of threads
        if (aLine.hasOption(OnyaseEngineCLIParameter.THREADS.id)) {
            String arg = aLine.getOptionValue(OnyaseEngineCLIParameter.THREADS.id);
            if (!CommandParameter.isPositiveInteger(OnyaseEngineCLIParameter.THREADS.id, arg, false)) {
                return false;
            }
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
