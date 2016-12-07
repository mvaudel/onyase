package no.uib.onyase.applications.engine.modules;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This modules allows iterating the different modification profiles possible
 * for a peptide.
 *
 * @author Marc Vaudel
 */
public class ModificationProfileIterator {

    /**
     * The separator for the profiles keys.
     */
    public static final char SEPARATOR = '_';

    /**
     * A cache for the modification profiles already created.
     */
    private HashMap<String, ModificationProfile> modificationProfilesCache = new HashMap<String, ModificationProfile>();

    /**
     * Constructor.
     */
    public ModificationProfileIterator() {
        ModificationProfile modificationProfile = new ModificationProfile();
        modificationProfilesCache.put(modificationProfile.getKey(), modificationProfile);
    }

    /**
     * Returns the possible modification profiles given possible occurrences of
     * modifications.
     *
     * @param possibleModifications the number of possible occurrences for every
     * modification indexed by modification name
     * @param modificationMasses the masses of the modifications
     *
     * @return the possible modification profiles in a list
     */
    public ArrayList<ModificationProfile> getPossibleModificationProfiles(HashMap<String, Integer> possibleModifications, HashMap<String, Double> modificationMasses) {
        ArrayList<ModificationProfile> result = new ArrayList<ModificationProfile>(possibleModifications.size() + 1);
        result.add(modificationProfilesCache.get(""));
        for (String modification : possibleModifications.keySet()) {
            Integer occurrenceMax = possibleModifications.get(modification);
            ArrayList<ModificationProfile> newProfiles = new ArrayList<ModificationProfile>(occurrenceMax * result.size());
            for (ModificationProfile modificationProfile : result) {
                String previousKey = modificationProfile.getKey();
                for (int occurrence = 1; occurrence <= occurrenceMax; occurrence++) {
                    StringBuilder newKeyBuilder = new StringBuilder(previousKey.length() + modification.length() + 3);
                    newKeyBuilder.append(previousKey);
                    if (newKeyBuilder.length() > 0) {
                        newKeyBuilder.append(SEPARATOR);
                    }
                    newKeyBuilder.append(modification);
                    if (occurrence > 1) {
                        newKeyBuilder.append(SEPARATOR).append(occurrence);
                    }
                    String newKey = newKeyBuilder.toString();
                    ModificationProfile newModificationProfile = modificationProfilesCache.get(newKey);
                    if (newModificationProfile == null) {
                        Double modificationMass = modificationMasses.get(modification);
                        newModificationProfile = modificationProfile.getCopy();
                        newModificationProfile.addModification(modification, modificationMass, occurrence);
                        newModificationProfile.setKey(newKey);
                        modificationProfilesCache.put(newKey, newModificationProfile);
                    }
                    newProfiles.add(newModificationProfile);
                }
            }
            result.addAll(newProfiles);
        }
        result.remove(0);
        return result;
    }

    /**
     * Class representing a modification profile.
     */
    public class ModificationProfile {

        /**
         * Map of the occurrences of every modification in this profile.
         */
        private HashMap<String, Integer> modificationOccurence;
        /**
         * Mass difference induced by this profile.
         */
        private Double mass;
        /**
         * Key for this profile.
         */
        private String key;

        /**
         * Constructor for an empty profile.
         */
        public ModificationProfile() {
            modificationOccurence = new HashMap<String, Integer>(1);
            mass = 0.0;
            key = "";
        }

        /**
         * Constructor based on known modification occurrences. The key is not
         * set.
         *
         * @param modificationOccurence a map of the occurrences of the
         * modifications in this profile
         * @param mass the mass difference induced by this profile
         */
        public ModificationProfile(HashMap<String, Integer> modificationOccurence, Double mass) {
            this.modificationOccurence = modificationOccurence;
            this.mass = mass;
        }

        /**
         * Returns the occurrence of every modification in a map.
         *
         * @return the occurrence of every modification in a map
         */
        public HashMap<String, Integer> getModificationOccurence() {
            return modificationOccurence;
        }

        /**
         * Returns the mass difference induced by this modification profile.
         *
         * @return the mass difference induced by this modification profile
         */
        public Double getMass() {
            return mass;
        }

        /**
         * Returns the key of this profile.
         *
         * @return the key of this profile
         */
        public String getKey() {
            return key;
        }

        /**
         * Sets the key for this profile.
         *
         * @param key the key to set
         */
        public void setKey(String key) {
            this.key = key;
        }

        /**
         * Returns a copy of the modification profile. Warning: the key is not
         * set.
         *
         * @return a copy of the modification profile
         */
        public ModificationProfile getCopy() {
            return new ModificationProfile(modificationOccurence, mass);
        }

        /**
         * Add a modification to the profile. The key is not updated.
         *
         * @param modification the name of the modification
         * @param modificationMass the mass of the modification
         * @param occurrence the occurrence of the modification
         */
        public void addModification(String modification, Double modificationMass, Integer occurrence) {
            modificationOccurence.put(modification, occurrence);
            mass += occurrence * modificationMass;
        }
    }

}
