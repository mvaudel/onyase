package no.uib.onyase.applications.engine.modules;

import java.util.ArrayList;

/**
 * Iterator for the possible modification sites.
 *
 * @author Marc Vaudel
 */
public class ModificationSitesIterator {
    
    private ArrayList<Integer> possibleSites;
    
    private int[] indexes;
    
    private int increment;
    
    private int nSites;
    
    private static final int maxSites = 5;
    
    public ModificationSitesIterator(ArrayList<Integer> possibleSites, Integer nModifications) {
        increment = Math.max(possibleSites.size() / (maxSites * nModifications), 1);
        indexes = new int[nModifications];
        for (int i = 0 ; i < nModifications ; i++) {
            indexes[i] = i;
        }
        indexes[nModifications-1] -= increment;
        this.possibleSites = possibleSites;
        nSites = possibleSites.size();
    }
    
    public boolean hasNext() {
        for (int i = indexes.length-1 ; i >= 0 ; i--) {
            if (indexes[i] + (indexes.length - i) * increment < nSites) {
                indexes[i] += increment;
                for (int j = i+1 ; j < indexes.length ; j++) {
                    indexes[j] = indexes[j-1] + increment;
                }
                return true;
            }
        }
        return false;
    }
    
    public ArrayList<Integer> getNextSites() {
        ArrayList<Integer> result = new ArrayList<Integer>(indexes.length);
        for (int index : indexes) {
            result.add(possibleSites.get(index));
        }
        return result;
    }

    public int getnSites() {
        return nSites;
    }

    public int getIncrement() {
        return increment;
    }

    public void setIncrement(int increment) {
        this.increment = increment;
    }

}
