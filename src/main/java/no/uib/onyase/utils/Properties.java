package no.uib.onyase.utils;

import java.io.InputStream;

/**
 * This class contains properties extracted from the pom file.
 *
 * @author Marc Vaudel
 */
public class Properties {

    /**
     * Contructor.
     */
    public Properties() {
    }

    /**
     * Retrieves the version number set in the pom file.
     *
     * @return the version number set in the pom file
     */
    public String getVersion() {

        java.util.Properties p = new java.util.Properties();

        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("onyase.properties");
            p.load(is);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return p.getProperty("onyase.version");
    }
}
