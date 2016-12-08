package no.uib.onyase.applications.engine.modules;

import java.util.ArrayList;

/**
 * Iterator for the possible modification sites.
 *
 * @author Marc Vaudel
 */
public interface ModificationSitesIterator {

    /**
     * Indicates whether there is another site combination and moves the
     * iterator.
     *
     * @return a boolean indicating whether there is another site combination
     * and moves the iterator
     */
    public boolean hasNext();

    /**
     * Returns the next modification sites.
     *
     * @return the next modification sites
     */
    public ArrayList<Integer> getNextSites();

    /**
     * Returns the number of sites.
     *
     * @return the number of sites
     */
    public int getnSites();

    /**
     * Returns the increment.
     *
     * @return the increment.
     */
    public int getIncrement();

    /**
     * Sets the increment.
     *
     * @param increment the increment
     */
    public void setIncrement(int increment);
}
