package no.uib.onyase.applications.engine.model;

import com.compomics.util.experiment.biology.Peptide;

/**
 * This class represents a peptide spectrum match.
 *
 * @author Marc Vaudel
 */
public class Psm {

    /**
     * The peptide.
     */
    private Peptide peptide;
    
    /**
     * The charge.
     */
    private int charge;

    /**
     * The score.
     */
    private double score;

    /**
     * Constructor.
     * 
     * @param peptide the peptide
     * @param charge the charge
     * @param score the score
     */
    public Psm(Peptide peptide, int charge, double score) {
        this.peptide = peptide;
        this.score = score;
        this.charge = charge;
    }

    /**
     * Returns the peptide.
     * 
     * @return the peptide
     */
    public Peptide getPeptide() {
        return peptide;
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
     * Returns the score.
     * 
     * @return the score.
     */
    public double getScore() {
        return score;
    }
}
