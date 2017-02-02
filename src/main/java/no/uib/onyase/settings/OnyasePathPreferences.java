package no.uib.onyase.settings;

import com.compomics.software.CompomicsWrapper;
import com.compomics.software.settings.PathKey;
import com.compomics.software.settings.UtilitiesPathPreferences;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class sets the path preferences for the files to read/write
 *
 * @author Marc Vaudel
 */
public class OnyasePathPreferences {

    /**
     * Enum of the paths which can be set in SearchGUI.
     */
    public enum OnyasePathKey implements PathKey {

        /**
         * Directory where SearchGUI temporary files should be stored.
         */
        tempDirectory("onyase_temp", "Folder where Onyase temporary files are stored.", "", true);
        /**
         * The key used to refer to this path.
         */
        private String id;
        /**
         * The description of the path usage.
         */
        private String description;
        /**
         * The default sub directory or file to use in case all paths should be
         * included in a single directory.
         */
        private String defaultSubDirectory;
        /**
         * Indicates whether the path should be a folder.
         */
        private boolean isDirectory;

        /**
         * Constructor.
         *
         * @param id the id used to refer to this path key
         * @param description the description of the path usage
         * @param defaultSubDirectory the sub directory to use in case all paths
         * should be included in a single directory
         * @param isDirectory boolean indicating whether a folder is expected
         */
        private OnyasePathKey(String id, String description, String defaultSubDirectory, boolean isDirectory) {
            this.id = id;
            this.description = description;
            this.defaultSubDirectory = defaultSubDirectory;
            this.isDirectory = isDirectory;
        }

        /**
         * Returns the key from its id. Null if not found.
         *
         * @param id the id of the key of interest
         *
         * @return the key of interest
         */
        public static OnyasePathKey getKeyFromId(String id) {
            for (OnyasePathKey pathKey : values()) {
                if (pathKey.id.equals(id)) {
                    return pathKey;
                }
            }
            return null;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getDescription() {
            return description;
        }
    }

    /**
     * Loads the path preferences from a text file.
     *
     * @param inputFile the file to load the path preferences from
     *
     * @throws FileNotFoundException thrown if the input file is not found
     * @throws IOException thrown if there are problems reading the input file
     */
    public static void loadPathPreferencesFromFile(File inputFile) throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(inputFile));
        try {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (!line.equals("") && !line.startsWith("#")) {
                    loadPathPreferenceFromLine(line);
                }
            }
        } finally {
            br.close();
        }
    }

    /**
     * Loads a path to be set from a line.
     *
     * @param line the line where to read the path from
     * @throws FileNotFoundException thrown if the file the path refers to
     * cannot be found
     */
    public static void loadPathPreferenceFromLine(String line) throws FileNotFoundException {
        String id = UtilitiesPathPreferences.getPathID(line);
        if (id.equals("")) {
            throw new IllegalArgumentException("Impossible to parse path in " + line + ".");
        }
        OnyasePathKey onyasePathKey = OnyasePathKey.getKeyFromId(id);
        if (onyasePathKey == null) {
            UtilitiesPathPreferences.loadPathPreferenceFromLine(line);
        } else {
            String path = UtilitiesPathPreferences.getPath(line);
            if (!path.equals(UtilitiesPathPreferences.defaultPath)) {
                File file = new File(path);
                if (!file.exists()) {
                    throw new FileNotFoundException("File " + path + " not found.");
                }
                if (onyasePathKey.isDirectory && !file.isDirectory()) {
                    throw new FileNotFoundException("Found a file when expecting a directory for " + onyasePathKey.id + ".");
                }
                setPathPreference(onyasePathKey, path);
            }
        }
    }

    /**
     * Sets the path according to the given key and path.
     *
     * @param pathKey the key of the path
     * @param path the path to be set
     */
    public static void setPathPreference(OnyasePathKey pathKey, String path) {
        switch (pathKey) {
            case tempDirectory:
                // No temp folder at the moment
                break;
            default:
                throw new UnsupportedOperationException("Path " + pathKey.id + " not implemented.");
        }
    }

    /**
     * Sets the path according to the given key and path.
     *
     * @param pathKey the key of the path
     * @param path the path to be set
     */
    public static void setPathPreference(PathKey pathKey, String path) {
        if (pathKey instanceof OnyasePathKey) {
            setPathPreference((OnyasePathKey) pathKey, path);
        } else if (pathKey instanceof UtilitiesPathPreferences.UtilitiesPathKey) {
            UtilitiesPathPreferences.UtilitiesPathKey utilitiesPathKey = (UtilitiesPathPreferences.UtilitiesPathKey) pathKey;
            UtilitiesPathPreferences.setPathPreference(utilitiesPathKey, path);
        } else {
            throw new UnsupportedOperationException("Path " + pathKey.getId() + " not implemented.");
        }
    }

    /**
     * Returns the path according to the given key and path.
     *
     * @param searchGUIPathKey the key of the path
     * @param jarFilePath path to the jar file
     *
     * @return the path 
     */
    public static String getPathPreference(OnyasePathKey searchGUIPathKey, String jarFilePath) {
        switch (searchGUIPathKey) {
            case tempDirectory:
                return "Not_Implemented";
            default:
                throw new UnsupportedOperationException("Path " + searchGUIPathKey.id + " not implemented.");
        }
    }

    /**
     * Sets all the paths inside a given folder.
     *
     * @param path the path of the folder where to redirect all paths.
     *
     * @throws FileNotFoundException thrown if on of the files the paths refer
     * to cannot be found
     */
    public static void setAllPathsIn(String path) throws FileNotFoundException {
        for (OnyasePathKey searchGUIPathKey : OnyasePathKey.values()) {
            String subDirectory = searchGUIPathKey.defaultSubDirectory;
            File newFile = new File(path, subDirectory);
            if (!newFile.exists()) {
                newFile.mkdirs();
            }
            if (!newFile.exists()) {
                throw new FileNotFoundException(newFile.getAbsolutePath() + " could not be created.");
            }
            setPathPreference(searchGUIPathKey, newFile.getAbsolutePath());
        }
        UtilitiesPathPreferences.setAllPathsIn(path);
    }

    /**
     * Writes all path configurations to the given file.
     *
     * @param file the destination file
     * @param jarFilePath path to the jar file
     *
     * @throws IOException thrown of the file cannot be found
     */
    public static void writeConfigurationToFile(File file, String jarFilePath) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));
        try {
            writeConfigurationToFile(bw, jarFilePath);
        } finally {
            bw.close();
        }
    }

    /**
     * Writes all path configurations to the given file.
     *
     * @param bw the writer to use for writing
     * @param jarFilePath path to the jar file
     *
     * @throws IOException thrown of the file cannot be found
     */
    public static void writeConfigurationToFile(BufferedWriter bw, String jarFilePath) throws IOException {
        for (OnyasePathKey pathKey : OnyasePathKey.values()) {
            writePathToFile(bw, pathKey, jarFilePath);
        }
        UtilitiesPathPreferences.writeConfigurationToFile(bw);
    }

    /**
     * Writes the path of interest using the provided buffered writer.
     *
     * @param bw the writer to use for writing
     * @param pathKey the key of the path of interest
     * @param jarFilePath path to the jar file
     *
     * @throws IOException thrown of the file cannot be found
     */
    public static void writePathToFile(BufferedWriter bw, OnyasePathKey pathKey, String jarFilePath) throws IOException {
        bw.write(pathKey.id + UtilitiesPathPreferences.separator);
        switch (pathKey) {
            case tempDirectory:
                String toWrite = null; // No temporary folder at the moment
                if (toWrite == null) {
                    toWrite = UtilitiesPathPreferences.defaultPath;
                }
                bw.write(toWrite);
                break;
            default:
                throw new UnsupportedOperationException("Path " + pathKey.id + " not implemented.");
        }
        bw.newLine();
    }

    /**
     * Returns a list containing the keys of the paths where the tool is not
     * able to write.
     *
     * @param jarFilePath the path to the jar file
     * 
     * @return a list containing the keys of the paths where the tool is not
     * able to write
     *
     * @throws IOException exception thrown whenever an error occurred while
     * loading the path configuration
     */
    public static ArrayList<PathKey> getErrorKeys(String jarFilePath) throws IOException {
        ArrayList<PathKey> result = new ArrayList<PathKey>();
        for (OnyasePathKey pathKey : OnyasePathKey.values()) {
            String folder = OnyasePathPreferences.getPathPreference(pathKey, jarFilePath);
            if (folder != null && !UtilitiesPathPreferences.testPath(folder)) {
                result.add(pathKey);
            }
        }
        result.addAll(UtilitiesPathPreferences.getErrorKeys());
        return result;
    }
    
    /**
     * Returns the path to the jar file.
     *
     * @return the path to the jar file
     */
    public String getJarFilePath() {
        return CompomicsWrapper.getJarFilePath(this.getClass().getResource("OnyasePathPreferences.class").getPath(), "Onyase");
    }
}
