package no.uib.onyase.applications.engine.model;

/**
 * This class is used to store metrics for the peptide assumptions of the figure of the review.
 *
 * @author Marc Vaudel
 */
public class FigureMetrics {
    
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
