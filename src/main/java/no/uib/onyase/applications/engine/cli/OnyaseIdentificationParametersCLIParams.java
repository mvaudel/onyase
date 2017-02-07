package no.uib.onyase.applications.engine.cli;

import com.compomics.cli.identification_parameters.IdentificationParametersCLIParams;
import com.compomics.software.cli.CommandLineUtils;
import org.apache.commons.cli.Options;

/**
 * This class provides the parameters which can be used for the identification
 * parameters cli in Onyase.
 *
 * @author Marc Vaudel
 */
public class OnyaseIdentificationParametersCLIParams {

    /**
     * Creates the options for the command line interface based on the possible
     * values.
     *
     * @param aOptions the options object where the options will be added
     */
    public static void createOptionsCLI(Options aOptions) {
        for (IdentificationParametersCLIParams identificationParametersCLIParams : IdentificationParametersCLIParams.values()) {
            aOptions.addOption(identificationParametersCLIParams.id, identificationParametersCLIParams.hasArgument, identificationParametersCLIParams.description);
        }
    }

    /**
     * Returns the options as a string including file options.
     *
     * @return the options as a string
     */
    public static String getOptionsAsString() {

        String output = "";

        output += "Parameters Files:\n\n";
        output += "-" + String.format(CommandLineUtils.formatter, IdentificationParametersCLIParams.OUT.id) + " " + IdentificationParametersCLIParams.OUT.description + " (Mandatory)\n";
        output += "-" + String.format(CommandLineUtils.formatter, IdentificationParametersCLIParams.IDENTIFICATION_PARAMETERS.id) + " " + IdentificationParametersCLIParams.IDENTIFICATION_PARAMETERS.description + " (Optional)\n";
        output += getParametersOptionsAsString();
        return output;
    }

    /**
     * Returns the options as a string.
     *
     * @return the options as a string
     */
    public static String getParametersOptionsAsString() {

        String output = "";

        output += "\n\nSearch Parameters:\n\n";
        output += "-" + String.format(CommandLineUtils.formatter, IdentificationParametersCLIParams.DB.id) + " " + IdentificationParametersCLIParams.DB.description + "\n";
        output += "-" + String.format(CommandLineUtils.formatter, IdentificationParametersCLIParams.PREC_TOL.id) + " " + IdentificationParametersCLIParams.PREC_TOL.description + "\n";
        output += "-" + String.format(CommandLineUtils.formatter, IdentificationParametersCLIParams.PREC_PPM.id) + " " + IdentificationParametersCLIParams.PREC_PPM.description + "\n";
        output += "-" + String.format(CommandLineUtils.formatter, IdentificationParametersCLIParams.FRAG_TOL.id) + " " + IdentificationParametersCLIParams.FRAG_TOL.description + "\n";
        output += "-" + String.format(CommandLineUtils.formatter, IdentificationParametersCLIParams.ENZYME.id) + " " + IdentificationParametersCLIParams.ENZYME.description + "\n";
        output += "-" + String.format(CommandLineUtils.formatter, IdentificationParametersCLIParams.FIXED_MODS.id) + " " + IdentificationParametersCLIParams.FIXED_MODS.description + "\n";
        output += "-" + String.format(CommandLineUtils.formatter, IdentificationParametersCLIParams.VARIABLE_MODS.id) + " " + IdentificationParametersCLIParams.VARIABLE_MODS.description + "\n";
        output += "-" + String.format(CommandLineUtils.formatter, IdentificationParametersCLIParams.MIN_CHARGE.id) + " " + IdentificationParametersCLIParams.MIN_CHARGE.description + "\n";
        output += "-" + String.format(CommandLineUtils.formatter, IdentificationParametersCLIParams.MAX_CHARGE.id) + " " + IdentificationParametersCLIParams.MAX_CHARGE.description + "\n";
        output += "-" + String.format(CommandLineUtils.formatter, IdentificationParametersCLIParams.MC.id) + " " + IdentificationParametersCLIParams.MC.description + "\n";
        output += "-" + String.format(CommandLineUtils.formatter, IdentificationParametersCLIParams.FI.id) + " " + IdentificationParametersCLIParams.FI.description + "\n";
        output += "-" + String.format(CommandLineUtils.formatter, IdentificationParametersCLIParams.RI.id) + " " + IdentificationParametersCLIParams.RI.description + "\n";
        output += "-" + String.format(CommandLineUtils.formatter, IdentificationParametersCLIParams.MIN_ISOTOPE.id) + " " + IdentificationParametersCLIParams.MIN_ISOTOPE.description + "\n";
        output += "-" + String.format(CommandLineUtils.formatter, IdentificationParametersCLIParams.MAX_ISOTOPE.id) + " " + IdentificationParametersCLIParams.MAX_ISOTOPE.description + "\n";

        output += "\n\nHelp:\n\n";
        output += "-" + String.format(CommandLineUtils.formatter, IdentificationParametersCLIParams.MODS.id) + " " + IdentificationParametersCLIParams.MODS.description + "\n";
        output += "-" + String.format(CommandLineUtils.formatter, IdentificationParametersCLIParams.USAGE.id) + " " + IdentificationParametersCLIParams.USAGE.description + "\n";

        return output;
    }
}
