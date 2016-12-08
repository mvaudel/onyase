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
    private HashMap<String, ModificationSitesIterator> modificationSitesIterators;
    /**
     * Ordered list of modifications to iterate.
     */
    private ArrayList<String> modifications;
    /**
     * Current map of modification sites.
     */
    private final HashMap<String, ArrayList<Integer>> modificationSites;
    /**
     * Map of possible modification sites.
     */
    private HashMap<String, ArrayList<Integer>> possibleModificationSites;
    /**
     * Map of the occurrences of every modification.
     */
    private HashMap<String, Integer> modificationOccurrence;

    /**
     * Constructor.
     *
     * @param modificationProfile the modification profile containing the
     * occurrences of every modification
     * @param possibleModificationSites the possible modification sites
     * @param orderedModifications an ordered list of modifications
     */
    public OverlappingModificationsIterator(ModificationProfileIterator.ModificationProfile modificationProfile, HashMap<String, ArrayList<Integer>> possibleModificationSites, ArrayList<String> orderedModifications) {
        modificationOccurrence = modificationProfile.getModificationOccurence();
        modifications = orderedModifications;
        modificationSitesIterators = new HashMap<String, ModificationSitesIterator>(modificationOccurrence.size());
        for (String modification : modificationOccurrence.keySet()) {
            Integer occurrence = modificationOccurrence.get(modification);
            ArrayList<Integer> possibleSites = possibleModificationSites.get(modification);
            ModificationSitesIterator modificationSitesIterator;
            if (occurrence == 1) {
                modificationSitesIterator = new SingleModificationSiteIterator(possibleSites);
            } else {
                modificationSitesIterator = new MultipleModificationsSiteIterator(possibleSites, occurrence);
            }
            modificationSitesIterator.setIncrement(1);
            modificationSitesIterators.put(modification, modificationSitesIterator);
        }
        modificationSites = new HashMap<String, ArrayList<Integer>>(modificationOccurrence.size());
        this.possibleModificationSites = possibleModificationSites;
    }

    @Override
    public boolean hasNext() {
        if (modificationSites.isEmpty()) {
            for (String modification : modifications) {
                ModificationSitesIterator modificationSitesIterator = modificationSitesIterators.get(modification);
                ArrayList<Integer> sites = modificationSitesIterator.getNextSites();
                modificationSites.put(modification, sites);
            }
            return true;
        }
        HashSet<Integer> occupancySites = new HashSet<Integer>(modifications.size());
        boolean hasNext = false;
        for (String modification : modifications) {
            if (!hasNext) {
                ModificationSitesIterator modificationSitesIterator = modificationSitesIterators.get(modification);
                if (modificationSitesIterator.hasNext()) {
                    ArrayList<Integer> newSites = modificationSitesIterator.getNextSites();
                    modificationSites.put(modification, newSites);
                    occupancySites.addAll(newSites);
                    for (String modification2 : modifications) {
                        if (modification.equals(modification2)) {
                            break;
                        }
                        ArrayList<Integer> possibleSites = possibleModificationSites.get(modification2);
                        Integer occurrence = modificationOccurrence.get(modification2);
                        if (occurrence == 1) {
                            modificationSitesIterator = new SingleModificationSiteIterator(possibleSites);
                        } else {
                            modificationSitesIterator = new MultipleModificationsSiteIterator(possibleSites, occurrence);
                        }
                        modificationSitesIterator.setIncrement(1);
                        newSites = modificationSitesIterator.getNextSites();
                        for (Integer newSite : newSites) {
                            if (occupancySites.contains(newSite)) {
                                return hasNext();
                            }
                        }
                        occupancySites.addAll(newSites);
                        modificationSites.put(modification2, newSites);
                        modificationSitesIterators.put(modification2, modificationSitesIterator);
                    }
                    hasNext = true;
                }
            } else {
                ModificationSitesIterator modificationSitesIterator = modificationSitesIterators.get(modification);
                ArrayList<Integer> newSites = modificationSitesIterator.getNextSites();
                for (Integer newSite : newSites) {
                    if (occupancySites.contains(newSite)) {
                        return hasNext();
                    }
                }
                occupancySites.addAll(newSites);
            }
        }
        return hasNext;
    }

    @Override
    public HashMap<String, ArrayList<Integer>> next() {
        return modificationSites;
    }

}
