package no.uib.onyase.scripts.review_figure.full;

/**
 * This class is used to store metrics for the peptide assumptions of the figure of the review.
 *
 * @author Marc Vaudel
 */
public class FigureMetrics {
    
    /**
     * Boolean indicating whether the peptide can be mapped to a target sequence.
     */
    private boolean isTarget = false;
    
    /**
     * Boolean indicating whether the peptide can be mapped to a decoy sequence.
     */
    private boolean isDecoy = false;
    
    /**
     * The score.
     */
    private int score = 0;
    /**
     * The number of isoforms for this peptide.
     */
    private int nHits;
    
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
     * Returns the rounded score.
     * 
     * @return the rounded score
     */
    public int getScore() {
        return score;
    }

    /**
     * Sets the rounded score.
     * 
     * @param score the rounded score
     */
    public void setScore(int score) {
        this.score = score;
    }

    /**
     * Returns the number of isoforms for this peptide.
     * 
     * @return the number of isoforms for this peptide
     */
    public int getnHits() {
        return nHits;
    }

    /**
     * Sets the number of isoforms for this peptide.
     * 
     * @param nHits the number of isoforms for this peptide
     */
    public void setnHits(int nHits) {
        this.nHits = nHits;
    }


}
