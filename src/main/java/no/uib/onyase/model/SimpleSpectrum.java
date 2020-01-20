package no.uib.onyase.model;

/**
 *
 * @author Marc Vaudel
 */
public class SimpleSpectrum {
    
    public final String title;
    public final double precursorMz;
    public final double[] mz;
    public final double[] intensity;
    
    public SimpleSpectrum(
            String title,
            double precursorMz,
            double[] mz,
            double[] intensity
    ) {
        this.title = title;
        this.precursorMz = precursorMz;
        this.mz = mz;
        this.intensity = intensity;
    }

}
