package no.uib.onyase.applications.engine.modules.precursor_handling;

import com.compomics.util.experiment.biology.ions.ElementaryIon;
import com.compomics.util.experiment.massspectrometry.SpectrumFactory;
import com.compomics.util.experiment.massspectrometry.indexes.PrecursorMap;
import java.io.IOException;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshallerException;

/**
 * This class processes the precursors present in an mgf file.
 *
 * @author Marc Vaudel
 */
public class PrecursorProcessor {

    /**
     * The spectrum factory.
     */
    private SpectrumFactory spectrumFactory = SpectrumFactory.getInstance();

    /**
     * The precursor map.
     */
    private PrecursorMap precursorMap;

    /**
     * The minimal mass that can be expected according to the search parameters
     * and the precursor m/z distribution.
     */
    private Double massMin;

    /**
     * The maximal mass that can be expected according to the search parameters
     * and the precursor m/z distribution.
     */
    private Double massMax;

    /**
     * Constructor. The spectrum file corresponding to the file to import must
     * be loaded in the spectrum factory.
     *
     * @param mgfFileName the name of the file to process
     * @param ms1Tolerance the ms1 m/z tolerance
     * @param ms1TolerancePpm a boolean indicating whether the ms1 m/z tolerance is in ppm
     * @param ms2Tolerance the ms2 m/z tolerance
     * @param ms2TolerancePpm a boolean indicating whether the ms2 m/z tolerance is in ppm
     * @param minCharge the minimal charge to consider
     * @param maxCharge the maximal charge to consider
     * @param minMz the minimal m/z to consider
     * @param maxMz the maximal m/z to consider
     *
     * @throws IOException exception thrown whenever an error occurred while
     * loading the spectra
     * @throws MzMLUnmarshallerException exception thrown whenever an error
     * occurred while reading an mzML file
     */
    public PrecursorProcessor(String mgfFileName, double ms1Tolerance, boolean ms1TolerancePpm, double ms2Tolerance, boolean ms2TolerancePpm, int minCharge, int maxCharge, double minMz, double maxMz) throws IOException, MzMLUnmarshallerException {
        importSpectra(mgfFileName, ms1Tolerance, ms1TolerancePpm, ms2Tolerance, ms2TolerancePpm, minCharge, maxCharge, minMz, maxMz);
    }

    /**
     * Imports the precursors of the given file according to the given search
     * parameters.
     *
     * @param fileName the name of the file to process
     * @param ms1Tolerance the ms1 m/z tolerance
     * @param ms1TolerancePpm a boolean indicating whether the ms1 m/z tolerance is in ppm
     * @param ms2Tolerance the ms2 m/z tolerance
     * @param ms2TolerancePpm a boolean indicating whether the ms2 m/z tolerance is in ppm
     * @param minCharge the minimal charge to consider
     * @param maxCharge the maximal charge to consider
     * @param minMz the minimal m/z to consider
     * @param maxMz the maximal m/z to consider
     *
     * @throws IOException exception thrown whenever an error occurred while
     * loading the spectra
     * @throws MzMLUnmarshallerException exception thrown whenever an error
     * occurred while reading an mzML file
     */
    private void importSpectra(String fileName, double ms1Tolerance, boolean ms1TolerancePpm, double ms2Tolerance, boolean ms2TolerancePpm, int minCharge, int maxCharge, double minMz, double maxMz) throws IOException, MzMLUnmarshallerException {

        precursorMap = new PrecursorMap(spectrumFactory.getPrecursorMap(fileName), ms2Tolerance, ms2TolerancePpm);

        Double mzMin = Math.max(minMz, precursorMap.getMinMz());
        Double mzMax;
        if (maxMz == 0.0) {
            mzMax = precursorMap.getMaxMz();
        } else {
            mzMax = Math.min(maxMz, precursorMap.getMaxMz());
        }
        if (ms1TolerancePpm) {
            mzMin *= (1 - ms1Tolerance / 1000000);
            mzMax *= (1 + ms1Tolerance / 1000000);
        } else {
            mzMin -= ms1Tolerance;
            mzMax += ms1Tolerance;
        }

        massMin = (mzMin * minCharge) - (minCharge * ElementaryIon.proton.getTheoreticMass());
        massMax = (mzMax * maxCharge) - (maxCharge * ElementaryIon.proton.getTheoreticMass());
    }

    /**
     * Returns the precursor map.
     *
     * @return the precursor map
     */
    public PrecursorMap getPrecursorMap() {
        return precursorMap;
    }

    /**
     * Returns the minimal mass expected.
     *
     * @return the minimal mass expected
     */
    public Double getMassMin() {
        return massMin;
    }

    /**
     * Returns the maximal mass expected.
     *
     * @return the maximal mass expected
     */
    public Double getMassMax() {
        return massMax;
    }

}
