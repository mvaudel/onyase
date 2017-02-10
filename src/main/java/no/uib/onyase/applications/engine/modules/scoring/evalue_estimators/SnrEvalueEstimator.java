package no.uib.onyase.applications.engine.modules.scoring.evalue_estimators;

import no.uib.onyase.applications.engine.modules.scoring.EValueEstimator;

/**
 * E-value estimator for the SNR score.
 *
 * @author Marc Vaudel
 */
public class SnrEvalueEstimator implements EValueEstimator {

    @Override
    public double getEValue(String spectrumTitle, double score) {
        return -score;
    }

}
