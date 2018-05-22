package no.uib.onyase.applications.engine.modules.peptide_modification_iterators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import no.uib.onyase.applications.engine.modules.ModificationProfileIterator;
import no.uib.onyase.applications.engine.modules.ModificationSitesIterator;
import no.uib.onyase.applications.engine.modules.PeptideModificationsIterator;
import no.uib.onyase.applications.engine.modules.modification_sites_iterators.MultipleModificationsSiteIterator;
import no.uib.onyase.applications.engine.modules.modification_sites_iterators.SingleModificationSiteIterator;

/**
 * Iterator of the modifications for peptides carrying multiple modifications
 * that can overlap.
 *
 * @author Marc Vaudel
 */
public class OverlappingModificationsIterator implements PeptideModificationsIterator {

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
     * Constructor.
     *
     * @param modificationOccurrence the occurrences of every modification
     * @param possibleModificationSites the possible modification sites
     * @param orderedModifications an ordered list of modifications
     * @param maxSites the preferred number of sites to iterate per modification
     */
    public OverlappingModificationsIterator(HashMap<String, Integer> modificationOccurrence, HashMap<String, Integer[]> possibleModificationSites, ArrayList<String> orderedModifications, int maxSites) {
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
            modificationSitesIterator.setIncrement(1);
            modificationSitesIterators.put(modification, modificationSitesIterator);
        }
        modificationSites = new HashMap<>(modificationOccurrence.size());
        this.possibleModificationSites = possibleModificationSites;
    }

    @Override
    public boolean hasNext() {
        if (modificationSites.isEmpty()) {
            for (String modification : modifications) {
                ModificationSitesIterator modificationSitesIterator = modificationSitesIterators.get(modification);
                int[] sites = modificationSitesIterator.getNextSites();
                modificationSites.put(modification, sites);
            }
            return true;
        }
        HashSet<Integer> occupancySites = new HashSet<>(modifications.size());
        boolean hasNext = false;
        for (String modification : modifications) {
            if (!hasNext) {
                ModificationSitesIterator modificationSitesIterator = modificationSitesIterators.get(modification);
                if (modificationSitesIterator.hasNext()) {
                    int[] newSites = modificationSitesIterator.getNextSites();
                    modificationSites.put(modification, newSites);
                    for (int newSite : newSites) {
                        occupancySites.add(newSite);
                    }
                    for (String modification2 : modifications) {
                        if (modification.equals(modification2)) {
                            break;
                        }
                        Integer[] possibleSites = possibleModificationSites.get(modification2);
                        Integer occurrence = modificationOccurrence.get(modification2);
                        if (occurrence == 1) {
                            modificationSitesIterator = new SingleModificationSiteIterator(possibleSites);
                        } else {
                            modificationSitesIterator = new MultipleModificationsSiteIterator(possibleSites, occurrence);
                        }
                        modificationSitesIterator.hasNext();
                        newSites = modificationSitesIterator.getNextSites();
                        for (Integer newSite : newSites) {
                            if (occupancySites.contains(newSite)) {
                                return hasNext();
                            }
                            occupancySites.add(newSite);
                        }
                        modificationSites.put(modification2, newSites);
                        modificationSitesIterators.put(modification2, modificationSitesIterator);
                    }
                    hasNext = true;
                }
            } else {
                ModificationSitesIterator modificationSitesIterator = modificationSitesIterators.get(modification);
                int[] newSites = modificationSitesIterator.getNextSites();
                for (Integer newSite : newSites) {
                    if (occupancySites.contains(newSite)) {
                        return hasNext();
                    }
                    occupancySites.add(newSite);
                }
            }
        }
        return hasNext;
    }

    @Override
    public HashMap<String, int[]> next() {
        return modificationSites;
    }

}
