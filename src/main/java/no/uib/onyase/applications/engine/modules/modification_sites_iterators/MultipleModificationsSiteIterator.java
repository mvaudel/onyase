package no.uib.onyase.applications.engine.modules.modification_sites_iterators;

import java.util.ArrayList;
import no.uib.onyase.applications.engine.modules.ModificationSitesIterator;

/**
 * Iterator for possible modification sites when more than one modification is present.
 *
 * @author Marc Vaudel
 */
public class MultipleModificationsSiteIterator implements ModificationSitesIterator {
    
    /**
     * List of the possible sites.
     */
    private ArrayList<Integer> possibleSites;
    /**
     * Current indexes.
     */
    private int[] indexes;
    /**
     * The current sites.
     */
    private int[] sites;
    /**
     * The increment to use when iterating the possible indexes.
     */
    private int increment;
    /**
     * The preferred maximal number of sites to iterate.
     */
    private static final int maxSites = 5;
    
    /**
     * Constructor.
     * 
     * @param possibleSites the possible sites
     * @param nModifications the number of modifications
     */
    public MultipleModificationsSiteIterator(ArrayList<Integer> possibleSites, Integer nModifications) {
        increment = Math.max(possibleSites.size() / maxSites, 1);
        indexes = new int[nModifications];
        for (int i = 0 ; i < nModifications ; i++) {
            indexes[i] = i;
        }
        indexes[nModifications-1] -= increment;
        this.possibleSites = possibleSites;
        sites = new int[nModifications];
    }
    
    @Override
    public boolean hasNext() {
        for (int i = indexes.length-1 ; i >= 0 ; i--) {
            if (indexes[i] + (indexes.length - i) * increment < possibleSites.size()) {
                indexes[i] += increment;
                for (int j = i+1 ; j < indexes.length ; j++) {
                    indexes[j] = indexes[j-1] + increment;
                }
                return true;
            }
        }
        return false;
    }
    
    @Override
    public int[] getNextSites() {
        for (int i = 0 ; i < indexes.length ; i++) {
            sites[i] = possibleSites.get(indexes[i]);
        }
        return sites;
    }

    @Override
    public int getnSites() {
        return possibleSites.size();
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
