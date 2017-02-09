package no.uib.onyase.applications.engine.model;

import com.compomics.util.experiment.biology.Peptide;

/**
 * This class represents a peptide spectrum match.
 *
 * @author Marc Vaudel
 */
public class Psm {

    private Peptide peptide;
    
    private int charge;

    private double score;

    public Psm(Peptide peptide, int charge, double score) {
        this.peptide = peptide;
        this.score = score;
    }

    public Peptide getPeptide() {
        return peptide;
    }

    public int getCharge() {
        return charge;
    }

    public double getScore() {
        return score;
    }
}
