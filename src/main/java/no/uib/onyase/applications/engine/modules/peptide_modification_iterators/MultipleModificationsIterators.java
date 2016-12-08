package no.uib.onyase.applications.engine.modules.peptide_modification_iterators;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import no.uib.onyase.applications.engine.modules.ModificationProfileIterator;
import no.uib.onyase.applications.engine.modules.ModificationSitesIterator;
import no.uib.onyase.applications.engine.modules.PeptideModificationsIterator;

/**
 * Iterator of the modifications for peptides carrying multiple modifications.
 *
 * @author Marc Vaudel
 */
public class MultipleModificationsIterators implements PeptideModificationsIterator {

    private HashMap<String, ModificationSitesIterator> modificationSitesIterators;

    private ArrayList<String> modifications;

    private final HashMap<String, ArrayList<Integer>> modificationSites;

    private HashMap<String, ArrayList<Integer>> possibleModificationSites;

    private HashMap<String, Integer> modificationOccurrence;

    public MultipleModificationsIterators(ModificationProfileIterator.ModificationProfile modificationProfile, HashMap<String, ArrayList<Integer>> possibleModificationSites, ArrayList<String> orderedModifications) {
        modificationOccurrence = modificationProfile.getModificationOccurence();
        modifications = orderedModifications;
        modificationSitesIterators = new HashMap<String, ModificationSitesIterator>(modificationOccurrence.size());
        for (String modification : modificationOccurrence.keySet()) {
            Integer occurrence = modificationOccurrence.get(modification);
            ArrayList<Integer> possibleSites = possibleModificationSites.get(modification);
            ModificationSitesIterator modificationSitesIterator = new ModificationSitesIterator(possibleSites, occurrence);
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
        for (String modification : modifications) {
            ModificationSitesIterator modificationSitesIterator = modificationSitesIterators.get(modification);
            if (modificationSitesIterator.hasNext()) {
                ArrayList<Integer> newSites = modificationSitesIterator.getNextSites();
                modificationSites.put(modification, newSites);
                for (String modification2 : modifications) {
                    if (modification.equals(modification2)) {
                        break;
                    }
                    ArrayList<Integer> possibleSites = possibleModificationSites.get(modification2);
                    Integer occurrence = modificationOccurrence.get(modification2);
                    modificationSitesIterator = new ModificationSitesIterator(possibleSites, occurrence);
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
    public HashMap<String, ArrayList<Integer>> next() {
        return modificationSites;
    }
}
