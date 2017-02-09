package no.uib.onyase.applications.engine.modules.scoring;

/**
 * Interface for the e-value estimators of the different scores.
 *
 * @author Marc Vaudel
 */
public interface EValueEstimator {

    
    /**
     * Returns the e-value corresponding to the given score for the given
     * spectrum.
     *
     * @param spectrumTitle the title of the spectrum
     * @param score the score
     *
     * @return the corresponding e-value
     */
    public double getEValue(String spectrumTitle, double score);
}
