package no.uib.onyase.applications.engine.modules.peptide_modification_iterators;

import java.util.ArrayList;
import java.util.HashMap;
import no.uib.onyase.applications.engine.modules.ModificationSitesIterator;
import no.uib.onyase.applications.engine.modules.PeptideModificationsIterator;
import no.uib.onyase.applications.engine.modules.modification_sites_iterators.MultipleModificationsSiteIterator;
import no.uib.onyase.applications.engine.modules.modification_sites_iterators.SingleModificationSiteIterator;

/**
 * Iterator of the modifications for peptides carrying multiple modifications.
 *
 * @author Marc Vaudel
 */
public class MultipleModificationsIterators implements PeptideModificationsIterator {

    /**
     * Map of the iterators for the sites of the different modifications.
     */
    private final HashMap<String, ModificationSitesIterator> modificationSitesIterators;
    /**
     * Ordered list of modifications to iterate.
     */
    private final ArrayList<String> modifications;
    /**
     * Current map of modification sites.
     */
    private final HashMap<String, int[]> modificationSites;
    /**
     * Map of possible modification sites.
     */
    private final HashMap<String, Integer[]> possibleModificationSites;
    /**
     * Map of the occurrences of every modification.
     */
    private final HashMap<String, Integer> modificationOccurrence;
        /**
         * The preferred number of sites to consider for every PTM.
         */
        private final int maxSites;

    /**
     * Constructor.
     *
     * @param modificationOccurrence the occurrences of every modification
     * @param possibleModificationSites the possible modification sites
     * @param orderedModifications an ordered list of modifications
     * @param maxSites the preferred number of sites to iterate per modification
     */
    public MultipleModificationsIterators(HashMap<String, Integer> modificationOccurrence, HashMap<String, Integer[]> possibleModificationSites, ArrayList<String> orderedModifications, int maxSites) {
        this.modificationOccurrence = modificationOccurrence;
        modifications = orderedModifications;
        modificationSitesIterators = new HashMap<>(modificationOccurrence.size());
        for (String modification : modificationOccurrence.keySet()) {
            Integer occurrence = modificationOccurrence.get(modification);
            Integer[] possibleSites = possibleModificationSites.get(modification);
            ModificationSitesIterator modificationSitesIterator;
            if (occurrence == 1) {
                modificationSitesIterator = new SingleModificationSiteIterator(possibleSites, maxSites);
            } else {
                modificationSitesIterator = new MultipleModificationsSiteIterator(possibleSites, occurrence, maxSites);
            }
            modificationSitesIterators.put(modification, modificationSitesIterator);
        }
        modificationSites = new HashMap<>(modificationOccurrence.size());
        this.possibleModificationSites = possibleModificationSites;
        this.maxSites = maxSites;
    }

    @Override
    public boolean hasNext() {
        if (modificationSites.isEmpty()) {
            for (String modification : modifications) {
                ModificationSitesIterator modificationSitesIterator = modificationSitesIterators.get(modification);
                modificationSitesIterator.hasNext();
                int[] sites = modificationSitesIterator.getNextSites();
                modificationSites.put(modification, sites);
            }
            return true;
        }
        for (String modification : modifications) {
            ModificationSitesIterator modificationSitesIterator = modificationSitesIterators.get(modification);
            if (modificationSitesIterator.hasNext()) {
                int[] newSites = modificationSitesIterator.getNextSites();
                modificationSites.put(modification, newSites);
                for (String modification2 : modifications) {
                    if (modification.equals(modification2)) {
                        break;
                    }
                    Integer[] possibleSites = possibleModificationSites.get(modification2);
                    Integer occurrence = modificationOccurrence.get(modification2);
                    if (occurrence == 1) {
                        modificationSitesIterator = new SingleModificationSiteIterator(possibleSites, maxSites);
                    } else {
                        modificationSitesIterator = new MultipleModificationsSiteIterator(possibleSites, occurrence, maxSites);
                    }
                    modificationSitesIterator.hasNext();
                    newSites = modificationSitesIterator.getNextSites();
                    modificationSites.put(modification2, newSites);
                    modificationSitesIterators.put(modification2, modificationSitesIterator);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public HashMap<String, int[]> next() {
        return modificationSites;
    }
}
