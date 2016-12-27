package no.uib.onyase.applications.engine.modules.modification_sites_iterators;

import no.uib.onyase.applications.engine.modules.ModificationSitesIterator;

/**
 * Iterator for possible modification sites when one modification is present.
 *
 * @author Marc Vaudel
 */
public class SingleModificationSiteIterator implements ModificationSitesIterator {
    
    /**
     * The list of possible modification sites.
     */
    private Integer[] possibleSites;
    /**
     * An index for iteration.
     */
    private int i;
    /**
     * The current sites.
     */
    private int[] sites;
    /**
     * The increment to use when iterating the possible indexes.
     */
    private int increment;
    
    /**
     * Constructor.
     * 
     * @param possibleSites the sites to iterate
     * @param maxSites the preferred number of sites to iterate
     */
    public SingleModificationSiteIterator(Integer[] possibleSites, int maxSites) {
        if (maxSites > 0) {
            increment = Math.max(possibleSites.length / maxSites, 1);
        } else {
            increment = 1;
        }
        this.possibleSites = possibleSites;
        i = -1;
        sites = new int[1];
    }
    
    /**
     * Constructor.
     * 
     * @param possibleSites the sites to iterate
     */
    public SingleModificationSiteIterator(Integer[] possibleSites) {
        this(possibleSites, 0);
    }

    @Override
    public boolean hasNext() {
        i += increment;
        return i < possibleSites.length;
    }

    @Override
    public int[] getNextSites() {
        sites[0] = possibleSites[i];
        return sites;
    }

    @Override
    public int getnSites() {
        return possibleSites.length;
    }

    @Override
    public int getIncrement() {
        return increment;
    }

    @Override
    public void setIncrement(int increment) {
        this.increment = increment;
    }
}
