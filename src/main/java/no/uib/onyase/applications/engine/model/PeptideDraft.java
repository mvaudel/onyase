package no.uib.onyase.applications.engine.model;

import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
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
    private final String sequence;
    /**
     * The charge.
     */
    private final int charge;
    /**
     * The occurrence of variable modifications indexed by modification name.
     */
    private final HashMap<String, Integer> variableModifications;
    /**
     * The possible modification sites for every modification.
     */
    private final HashMap<String, Integer[]> variableModificationsSites;
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
     */
    public PeptideDraft(String sequence, int charge, HashMap<String, Integer> variableModifications, HashMap<String, Integer[]> variableModificationsSites) {
        this.sequence = sequence;
        this.charge = charge;
        this.variableModifications = variableModifications;
        this.variableModificationsSites = variableModificationsSites;
    }

    /**
     * Returns the key of this peptide draft.
     * 
     * @param orderedModifications a list of all possible ordered modifications
     * @param sequenceMatchingPreferences the sequence matching preferences
     * 
     * @return the key of this peptide draft
     */
    public long getKey(String[] orderedModifications, SequenceMatchingParameters sequenceMatchingPreferences) {
    
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
            
            return ExperimentObject.asLong(stringBuilder.toString());
        
        }
        
        return ExperimentObject.asLong(sequence);
    
    }

    /**
     * Returns the sequence of the peptide draft.
     * 
     * @return the sequence of the peptide draft
     */
    public String getSequence() {
        return sequence;
    }

    /**
     * Returns the charge.
     * 
     * @return the charge
     */
    public int getCharge() {
        return charge;
    }

    /**
     * Returns the variable modifications.
     * 
     * @return the variable modifications
     */
    public HashMap<String, Integer> getVariableModifications() {
        return variableModifications;
    }

    /**
     * Returns the variable modification sites.
     * 
     * @return the variable modification sites
     */
    public HashMap<String, Integer[]> getVariableModificationsSites() {
        return variableModificationsSites;
    }

    /**
     * Returns the raw score.
     * 
     * @return the raw score
     */
    public Double getRawScore() {
        return rawScore;
    }

    /**
     * Sets the raw score.
     * 
     * @param rawScore the raw score
     */
    public void setRawScore(Double rawScore) {
        this.rawScore = rawScore;
    }

    /**
     * Returns the e-value.
     * 
     * @return the e-value
     */
    public Double geteValue() {
        return eValue;
    }

    /**
     * Sets the e-value.
     * 
     * @param eValue the e-value
     */
    public void seteValue(Double eValue) {
        this.eValue = eValue;
    }

}
