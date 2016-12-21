package no.uib.onyase.applications.engine.modules.precursor_handling;

import com.compomics.util.experiment.biology.ions.ElementaryIon;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
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
     * @param searchParameters the search parameters
     * @param minMz the minimal m/z to consider
     * @param maxMz the maximal m/z to consider
     *
     * @throws IOException exception thrown whenever an error occurred while
     * loading the spectra
     * @throws MzMLUnmarshallerException exception thrown whenever an error
     * occurred while reading an mzML file
     */
    public PrecursorProcessor(String mgfFileName, SearchParameters searchParameters, Double minMz, Double maxMz) throws IOException, MzMLUnmarshallerException {
        importSpectra(mgfFileName, searchParameters, minMz, maxMz);
    }

    /**
     * Imports the precursors of the given file according to the given search
     * parameters.
     *
     * @param fileName the name of the file to process
     * @param searchParameters the search parameters
     * @param minMz the minimal m/z to consider
     * @param maxMz the maximal m/z to consider
     *
     * @throws IOException exception thrown whenever an error occurred while
     * loading the spectra
     * @throws MzMLUnmarshallerException exception thrown whenever an error
     * occurred while reading an mzML file
     */
    private void importSpectra(String fileName, SearchParameters searchParameters, Double minMz, Double maxMz) throws IOException, MzMLUnmarshallerException {

        precursorMap = new PrecursorMap(spectrumFactory.getPrecursorMap(fileName), searchParameters.getPrecursorAccuracy(), searchParameters.isPrecursorAccuracyTypePpm());

        int minCharge = searchParameters.getMinChargeSearched().value;
        int maxCharge = searchParameters.getMaxChargeSearched().value;

        Double mzMin;
        if (minMz != null) {
            mzMin = Math.max(minMz, precursorMap.getMinMz());
        } else {
            mzMin = precursorMap.getMinMz();
        }
        Double mzMax;
        if (maxMz != null) {
            mzMax = Math.min(maxMz, precursorMap.getMaxMz());
        } else {
            mzMax = precursorMap.getMaxMz();
        }
        if (searchParameters.isPrecursorAccuracyTypePpm()) {
            mzMin *= (1 - searchParameters.getPrecursorAccuracy() / 1000000);
            mzMax *= (1 + searchParameters.getPrecursorAccuracy() / 1000000);
        } else {
            mzMin -= searchParameters.getPrecursorAccuracy();
            mzMax += searchParameters.getPrecursorAccuracy();
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
