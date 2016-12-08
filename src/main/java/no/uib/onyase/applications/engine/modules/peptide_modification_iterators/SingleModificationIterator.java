package no.uib.onyase.applications.engine.modules.peptide_modification_iterators;

import java.util.ArrayList;
import java.util.HashMap;
import no.uib.onyase.applications.engine.modules.ModificationSitesIterator;
import no.uib.onyase.applications.engine.modules.PeptideModificationsIterator;

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
    
    private HashMap<String, ArrayList<Integer>> sitesMap;
    
    private String modificationName;

    /**
     * Constructor.
     * 
     * @param possibleSites a list of the possible sites
     * @param modificationOccurrence the occurrence of the modification
     * @param modificationName the name of the modification
     */
    public SingleModificationIterator(ArrayList<Integer> possibleSites, Integer modificationOccurrence, String modificationName) {
        modificationSitesIterator = new ModificationSitesIterator(possibleSites, modificationOccurrence);
        sitesMap = new HashMap<String, ArrayList<Integer>>(1);
        this.modificationName = modificationName;
    }

    @Override
    public boolean hasNext() {
        return modificationSitesIterator.hasNext();
    }

    @Override
    public HashMap<String, ArrayList<Integer>> next() {
        ArrayList<Integer> newSites = modificationSitesIterator.getNextSites();
        sitesMap.put(modificationName, newSites);
        return sitesMap;
    }

}
