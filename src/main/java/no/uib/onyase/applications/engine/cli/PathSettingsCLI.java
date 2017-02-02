package no.uib.onyase.applications.engine.cli;

import com.compomics.software.CompomicsWrapper;
import com.compomics.software.settings.UtilitiesPathPreferences;
import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import no.uib.onyase.settings.OnyasePathPreferences;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

/**
 * Allows the user to set the path settings in command line.
 *
 * @author Marc Vaudel
 */
public class PathSettingsCLI {

    /**
     * The input bean containing the user parameters.
     */
    private PathSettingsCLIInputBean pathSettingsCLIInputBean;

    /**
     * Constructor.
     *
     * @param pathSettingsCLIInputBean an input bean containing the user
     * parameters
     */
    public PathSettingsCLI(PathSettingsCLIInputBean pathSettingsCLIInputBean) {
        this.pathSettingsCLIInputBean = pathSettingsCLIInputBean;
    }

    /**
     * Sets the path settings and returns null.
     * 
     * @return null
     */
    public Object call() {
        setPathSettings();
        return null;
    }

    /**
     * Sets the path settings according to the pathSettingsCLIInputBean.
     */
    public void setPathSettings() {

        if (pathSettingsCLIInputBean.getLogFolder() != null) {
            OnyaseCLI.redirectErrorStream(pathSettingsCLIInputBean.getLogFolder());
        }

        String path = pathSettingsCLIInputBean.getTempFolder();
        if (!path.equals("")) {
            try {
                OnyasePathPreferences.setAllPathsIn(path);
            } catch (Exception e) {
                System.out.println("An error occurred when setting the temporary folder path.");
                e.printStackTrace();
            }
        }

        HashMap<String, String> pathInput = pathSettingsCLIInputBean.getPaths();
        for (String id : pathInput.keySet()) {
            try {
                OnyasePathPreferences.OnyasePathKey onyasePathKey = OnyasePathPreferences.OnyasePathKey.getKeyFromId(id);
                if (onyasePathKey == null) {
                    UtilitiesPathPreferences.UtilitiesPathKey utilitiesPathKey = UtilitiesPathPreferences.UtilitiesPathKey.getKeyFromId(id);
                    if (utilitiesPathKey == null) {
                        System.out.println("Path id " + id + " not recognized.");
                    } else {
                        UtilitiesPathPreferences.setPathPreference(utilitiesPathKey, pathInput.get(id));
                    }
                } else {
                    OnyasePathPreferences.setPathPreference(onyasePathKey, pathInput.get(id));
                }
            } catch (Exception e) {
                System.out.println("An error occurred when setting the path " + id + ".");
                e.printStackTrace();
            }
        }

        // write path file preference
        File destinationFile = new File(getJarFilePath(), UtilitiesPathPreferences.configurationFileName);
        try {
            OnyasePathPreferences.writeConfigurationToFile(destinationFile, getJarFilePath());
        } catch (Exception e) {
            System.out.println("An error occurred when saving the path preference to " + destinationFile.getAbsolutePath() + ".");
            e.printStackTrace();
        }

        System.out.println("Path configuration completed.");
    }

    /**
     * Returns the path to the jar file.
     *
     * @return the path to the jar file
     */
    public String getJarFilePath() {
        return CompomicsWrapper.getJarFilePath(this.getClass().getResource("PathSettingsCLI.class").getPath(), "Onyase");
    }

    /**
     * CLI header message when printing the usage.
     */
    private static String getHeader() {
        return System.getProperty("line.separator")
                + "The Onyase path settings command line allows setting the path of every configuration file created by Onyase or set a temporary folder where all files will be stored." + System.getProperty("line.separator")
                + System.getProperty("line.separator")
                + "For further help, create an issue in Github." + System.getProperty("line.separator")
                + System.getProperty("line.separator")
                + "----------------------"
                + System.getProperty("line.separator")
                + "OPTIONS"
                + System.getProperty("line.separator")
                + "----------------------" + System.getProperty("line.separator")
                + System.getProperty("line.separator");
    }

    /**
     * Starts the launcher by calling the launch method. Use this as the main
     * class in the jar file.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        try {
            Options lOptions = new Options();
            PathSettingsCLIParams.createOptionsCLI(lOptions);
            BasicParser parser = new BasicParser();
            CommandLine line = parser.parse(lOptions, args);

            if (args.length == 0) {
                PrintWriter lPrintWriter = new PrintWriter(System.out);
                lPrintWriter.print(System.getProperty("line.separator") + "========================================" + System.getProperty("line.separator"));
                lPrintWriter.print("Onyase Path Settings - Command Line" + System.getProperty("line.separator"));
                lPrintWriter.print("========================================" + System.getProperty("line.separator"));
                lPrintWriter.print(getHeader());
                lPrintWriter.print(PathSettingsCLIParams.getOptionsAsString());
                lPrintWriter.flush();
                lPrintWriter.close();

                System.exit(0);
            } else {
                PathSettingsCLIInputBean cliInputBean = new PathSettingsCLIInputBean(line);
                PathSettingsCLI pathSettingsCLI = new PathSettingsCLI(cliInputBean);
                pathSettingsCLI.call();
            }
        } catch (OutOfMemoryError e) {
            System.out.println("Onyase used up all the memory and had to be stopped. See the log for details.");
            System.err.println("Ran out of memory!");
            System.err.println("Memory given to the Java virtual machine: " + Runtime.getRuntime().maxMemory() + ".");
            System.err.println("Memory used by the Java virtual machine: " + Runtime.getRuntime().totalMemory() + ".");
            System.err.println("Free memory in the Java virtual machine: " + Runtime.getRuntime().freeMemory() + ".");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Onyase processing failed. See the log for details.");
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "PathSettingsCLI{"
                + ", cliInputBean=" + pathSettingsCLIInputBean
                + '}';
    }
}
