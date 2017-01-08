package no.uib.onyase.applications.engine.model;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import java.util.HashMap;

/**
 * This class contains partial information needed to build a PSM.
 *
 * @author Marc Vaudel
 */
public class PeptideDraft {

    /**
     * Separator for the modification index and occurrence in the key.
     */
    private static final char separatorOccurrence = '-';
    /**
     * Separator for the modifications in the key.
     */
    private static final char separatorModification = '_';
    /**
     * The sequence.
     */
    private String sequence;
    /**
     * The charge.
     */
    private int charge;
    /**
     * The occurrence of variable modifications indexed by modification name.
     */
    private HashMap<String, Integer> variableModifications;
    /**
     * The possible modification sites for every modification.
     */
    private HashMap<String, Integer[]> variableModificationsSites;
    /**
     * Boolean indicating whether the peptide was found on a decoy protein.
     */
    private boolean decoy = false;
    /**
     * Boolean indicating whether the peptide was found on a target protein.
     */
    private boolean target = false;
    /**
     * The raw score.
     */
    private Double rawScore;
    /**
     * The e-value.
     */
    private Double eValue;

    /**
     * Constructor.
     *
     * @param sequence the peptide sequence
     * @param charge the charge
     * @param variableModifications a map of the number of variable
     * modifications
     * @param variableModificationsSites a map of the possible modification
     * sites
     * @param decoy a boolean indicating whether the peptide was found on a
     * decoy protein
     */
    public PeptideDraft(String sequence, int charge, HashMap<String, Integer> variableModifications, HashMap<String, Integer[]> variableModificationsSites, boolean decoy) {
        this.sequence = sequence;
        this.charge = charge;
        this.variableModifications = variableModifications;
        this.variableModificationsSites = variableModificationsSites;
        setTargetDecoy(decoy);
    }

    public void setTargetDecoy(boolean decoy) {
        if (decoy) {
            this.decoy = true;
        } else {
            target = true;
        }
    }

    public String getKey(String[] orderedModifications, SequenceMatchingPreferences sequenceMatchingPreferences) {
        if (variableModifications != null) {
            StringBuilder stringBuilder = new StringBuilder(sequence.length() + 4 * variableModifications.size());
            String matchingSequence = AminoAcid.getMatchingSequence(sequence, sequenceMatchingPreferences);
            stringBuilder.append(matchingSequence);
            for (int i = 0; i < orderedModifications.length; i++) {
                String modification = orderedModifications[i];
                Integer occurrence = variableModifications.get(modification);
                if (occurrence != null) {
                    stringBuilder.append(separatorModification).append(i).append(separatorOccurrence).append(occurrence);
                }
            }
            return stringBuilder.toString();
        }
        return sequence;
    }

    public String getSequence() {
        return sequence;
    }

    public int getCharge() {
        return charge;
    }

    public HashMap<String, Integer> getVariableModifications() {
        return variableModifications;
    }

    public HashMap<String, Integer[]> getVariableModificationsSites() {
        return variableModificationsSites;
    }

    public boolean isDecoy() {
        return decoy;
    }

    public boolean isTarget() {
        return target;
    }

    public Double getRawScore() {
        return rawScore;
    }

    public void setRawScore(Double rawScore) {
        this.rawScore = rawScore;
    }

    public Double geteValue() {
        return eValue;
    }

    public void seteValue(Double eValue) {
        this.eValue = eValue;
    }

}
