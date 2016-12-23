package no.uib.onyase.applications.engine.modules;

import java.util.HashMap;

/**
 * Iterator for the possible modifications carried by a peptide.
 *
 * @author Marc Vaudel
 */
public interface PeptideModificationsIterator {

    /**
     * Indicates whether there is a next profile.
     *
     * @return a boolean indicating whether there is a next profile
     */
    public boolean hasNext();

    /**
     * Returns the next profile.
     *
     * @return the next profile
     */
    public HashMap<String, int[]> next();

}
