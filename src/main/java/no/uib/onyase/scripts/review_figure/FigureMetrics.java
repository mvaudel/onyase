package no.uib.onyase.scripts.review_figure;

import com.compomics.util.experiment.personalization.UrParameter;

/**
 * This class is used to store metrics for the peptide assumptions of the figure of the review.
 *
 * @author Marc Vaudel
 */
public class FigureMetrics implements UrParameter {
    
    /**
     * Boolean indicating whether the peptide can be mapped to a target sequence.
     */
    private boolean isTarget = false;
    
    /**
     * Boolean indicating whether the peptide can be mapped to a decoy sequence.
     */
    private boolean isDecoy = false;
    
    /**
     * The number of ions that can be matched in the spectrum.
     */
    private int nIons = 0;
    
    /**
     * Constructor.
     */
    public FigureMetrics() {
        
    }

    /**
     * Returns a boolean indicating whether the peptide can be mapped to a target sequence.
     * 
     * @return a boolean indicating whether the peptide can be mapped to a target sequence
     */
    public boolean isIsTarget() {
        return isTarget;
    }

    /**
     * Sets whether the peptide can be mapped to a target sequence.
     * @param isTarget a boolean indicating whether the peptide can be mapped to a target sequence
     */
    public void setIsTarget(boolean isTarget) {
        this.isTarget = isTarget;
    }

    /**
     * Returns a boolean indicating whether the peptide can be mapped to a decoy sequence.
     * 
     * @return a boolean indicating whether the peptide can be mapped to a decoy sequence
     */
    public boolean isIsDecoy() {
        return isDecoy;
    }

    /**
     * Sets whether the peptide can be mapped to a decoy sequence.
     * 
     * @param isDecoy a boolean indicating whether the peptide can be mapped to a decoy sequence
     */
    public void setIsDecoy(boolean isDecoy) {
        this.isDecoy = isDecoy;
    }

    /**
     * Returns the number of ions that can be matched in the spectrum.
     * 
     * @return the number of ions that can be matched in the spectrum
     */
    public int getnIons() {
        return nIons;
    }

    /**
     * Sets the number of ions that can be matched in the spectrum.
     * 
     * @param nIons the number of ions that can be matched in the spectrum
     */
    public void setnIons(int nIons) {
        this.nIons = nIons;
    }
    
    @Override
    public String getParameterKey() {
        return "Figure_Review";
    }

}
