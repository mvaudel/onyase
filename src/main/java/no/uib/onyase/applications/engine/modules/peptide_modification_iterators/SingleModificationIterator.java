package no.uib.onyase.applications.engine.modules.peptide_modification_iterators;

import java.util.ArrayList;
import java.util.HashMap;
import no.uib.onyase.applications.engine.modules.ModificationSitesIterator;
import no.uib.onyase.applications.engine.modules.PeptideModificationsIterator;
import no.uib.onyase.applications.engine.modules.modification_sites_iterators.MultipleModificationsSiteIterator;
import no.uib.onyase.applications.engine.modules.modification_sites_iterators.SingleModificationSiteIterator;

/**
 * Iterator of the modifications for peptides carrying a single modification.
 *
 * @author Marc Vaudel
 */
public class SingleModificationIterator implements PeptideModificationsIterator {

    /**
     * An iterator of the possible sites.
     */
    private ModificationSitesIterator modificationSitesIterator;
    /**
     * The current map of sites.
     */
    private HashMap<String, int[]> sitesMap;
    /**
     * The name of the modification.
     */
    private String modificationName;

    /**
     * Constructor.
     *
     * @param possibleSites a list of the possible sites
     * @param modificationOccurrence the occurrence of the modification
     * @param modificationName the name of the modification
     */
    public SingleModificationIterator(ArrayList<Integer> possibleSites, Integer modificationOccurrence, String modificationName) {
        if (modificationOccurrence == 1) {
            modificationSitesIterator = new SingleModificationSiteIterator(possibleSites);
        } else {
            modificationSitesIterator = new MultipleModificationsSiteIterator(possibleSites, modificationOccurrence);
        }
        sitesMap = new HashMap<String, int[]>(1);
        this.modificationName = modificationName;
    }

    @Override
    public boolean hasNext() {
        return modificationSitesIterator.hasNext();
    }

    @Override
    public HashMap<String, int[]> next() {
        int[] newSites = modificationSitesIterator.getNextSites();
        sitesMap.put(modificationName, newSites);
        return sitesMap;
    }

}
