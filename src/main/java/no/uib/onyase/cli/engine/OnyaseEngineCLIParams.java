package no.uib.onyase.cli.engine;

import com.compomics.cli.identification_parameters.IdentificationParametersCLIParams;
import com.compomics.software.cli.CommandLineUtils;
import no.uib.onyase.cli.identification_parameters.OnyaseIdentificationParametersCLIParams;
import no.uib.onyase.cli.paths.PathSettingsCLIParams;
import org.apache.commons.cli.Options;

/**
 * Command line option parameters for OnyaseCLI.
 *
 * @author Marc Vaudel
 */
public enum OnyaseEngineCLIParams {

    SPECTRUM_FILE("spectra", "Spectrum file (mgf format).", true),
    OUTPUT("output", "The folder where to write the output file.", true),
    
    THREADS("threads", "Number of threads to use for the processing, default: the number of cores.", false);

    /**
     * Short Id for the CLI parameter.
     */
    public String id;
    /**
     * Explanation for the CLI parameter.
     */
    public String description;
    /**
     * Boolean indicating whether the parameter is mandatory.
     */
    public boolean mandatory;

    /**
     * Private constructor managing the various variables for the enum
     * instances.
     *
     * @param id the id
     * @param description the description
     * @param mandatory is the parameter mandatory
     */
    private OnyaseEngineCLIParams(String id, String description, boolean mandatory) {
        this.id = id;
        this.description = description;
        this.mandatory = mandatory;
    }

    /**
     * Creates the options for the command line interface based on the possible
     * values.
     *
     * @param aOptions the options object where the options will be added
     */
    public static void createOptionsCLI(Options aOptions) {
        
        for (OnyaseEngineCLIParams identificationParametersCLIParams : values()) {
            aOptions.addOption(identificationParametersCLIParams.id, true, identificationParametersCLIParams.description);
        }
        
        OnyaseIdentificationParametersCLIParams.createOptionsCLI(aOptions);
        
        // Path setup
        PathSettingsCLIParams.createOptionsCLI(aOptions);
    }

    /**
     * Returns the options as a string.
     *
     * @return the options as a string
     */
    public static String getOptionsAsString() {

        String output = "";

        output += "Mandatory Parameters:\n\n";
        output += "-" + String.format(CommandLineUtils.formatter, SPECTRUM_FILE.id) + " " + SPECTRUM_FILE.description + "\n";
        output += "-" + String.format(CommandLineUtils.formatter, OUTPUT.id) + " " + OUTPUT.description + "\n";

        output += "\n\nOptional Input Parameters:\n\n";
        output += "-" + String.format(CommandLineUtils.formatter, IdentificationParametersCLIParams.IDENTIFICATION_PARAMETERS.id) + " " + IdentificationParametersCLIParams.IDENTIFICATION_PARAMETERS.description + "\n";
        
        output += "\n\nProcessing Options:\n\n";
        output += "-" + String.format(CommandLineUtils.formatter, THREADS.id) + " " + THREADS.description + "\n";
        
        output += "\n\nOptional Temporary Folder:\n\n";
        output += "-" + String.format(CommandLineUtils.formatter, PathSettingsCLIParams.ALL.id) + " " + PathSettingsCLIParams.ALL.description + "\n";
        
        output += "\n\n\nFor identification parameters options:\nReplace eu.isas.searchgui.cmd.SearchCLI with eu.isas.searchgui.cmd.IdentificationParametersCLI\n\n";

        return output;
    }
}
