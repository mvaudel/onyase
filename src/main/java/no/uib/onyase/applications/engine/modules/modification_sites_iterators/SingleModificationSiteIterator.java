package no.uib.onyase.applications.engine.modules.modification_sites_iterators;

import java.util.ArrayList;
import java.util.Iterator;
import no.uib.onyase.applications.engine.modules.ModificationSitesIterator;

/**
 * Iterator for possible modification sites when one modification is present.
 *
 * @author Marc Vaudel
 */
public class SingleModificationSiteIterator implements ModificationSitesIterator {
    
    private ArrayList<Integer> possibleSites;
    
    private Iterator<Integer> siteIterator;
    
    private ArrayList<Integer> sites;
    /**
     * The increment to use when iterating the possible indexes.
     */
    private int increment;
    /**
     * The preferred maximal number of sites to iterate.
     */
    private static final int maxSites = 5;
    
    public SingleModificationSiteIterator(ArrayList<Integer> possibleSites) {
        increment = Math.max(possibleSites.size() / maxSites, 1);
        this.possibleSites = possibleSites;
        siteIterator = possibleSites.iterator();
        sites = new ArrayList<Integer>(1);
    }

    @Override
    public boolean hasNext() {
        for (int i = 1 ; i < increment && siteIterator.hasNext() ; i++) {
            siteIterator.next();
        }
        return siteIterator.hasNext();
    }

    @Override
    public ArrayList<Integer> getNextSites() {
        sites.clear();
        sites.add(siteIterator.next());
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