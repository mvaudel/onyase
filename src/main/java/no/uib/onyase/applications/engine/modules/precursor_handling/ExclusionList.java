package no.uib.onyase.applications.engine.modules.precursor_handling;

import com.compomics.util.experiment.mass_spectrometry.spectra.Precursor;
import com.compomics.util.experiment.mass_spectrometry.indexes.PrecursorMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * List of m/z that should be excluded from the search.
 *
 * @author Marc Vaudel
 */
public class ExclusionList {

    /**
     * Separator used to separate the m/z from the comment.
     */
    public static final String separator = "\t";
    /**
     * The exclusion list to use by default.
     */
    public static final String defaultListFilePath = "resources/ExcludedMz.txt";
    /**
     * Map of the precursors in this map.
     */
    private PrecursorMap precursorMap;
    /**
     * The maximal m/z to accept.
     */
    private double maxMz = Double.MAX_VALUE;
    /**
     * The minimal m/z to accept
     */
    private double minMz;

    /**
     * Constructor.
     *
     * @param precursorTolerance the precursor m/z tolerance
     * @param ppm boolean indicating whether the precursor tolerance is in ppm
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading the exclusion list
     */
    public ExclusionList(double precursorTolerance, boolean ppm) throws IOException {
        this(defaultListFilePath, precursorTolerance, ppm, 0.0, Double.MAX_VALUE);
    }

    /**
     * Constructor.
     *
     * @param precursorTolerance the precursor m/z tolerance
     * @param ppm boolean indicating whether the precursor tolerance is in ppm
     * @param minMz the minimal m/z to accept
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading the exclusion list
     */
    public ExclusionList(double precursorTolerance, boolean ppm, double minMz) throws IOException {
        this(defaultListFilePath, precursorTolerance, ppm, minMz, Double.MAX_VALUE);
    }

    /**
     * Constructor.
     *
     * @param precursorTolerance the precursor m/z tolerance
     * @param ppm boolean indicating whether the precursor tolerance is in ppm
     * @param minMz the minimal m/z to accept
     * @param maxMz the maximal m/z to accept
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading the exclusion list
     */
    public ExclusionList(double precursorTolerance, boolean ppm, double minMz, double maxMz) throws IOException {
        this(defaultListFilePath, precursorTolerance, ppm, minMz, maxMz);
    }

    /**
     * Constructor.
     *
     * @param exclusionListfilePath the path of the file containing the
     * exclusion list
     * @param precursorTolerance the precursor m/z tolerance
     * @param ppm boolean indicating whether the precursor tolerance is in ppm
     * @param minMz the minimal m/z to accept
     * @param maxMz the maximal m/z to accept
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading the exclusion list
     */
    public ExclusionList(String exclusionListfilePath, double precursorTolerance, boolean ppm, double minMz, double maxMz) throws IOException {
        this.minMz = minMz;
        this.maxMz = maxMz;
        importListFromFile(exclusionListfilePath, precursorTolerance, ppm);
    }

    /**
     * Imports the exclusion list from the given file.
     *
     * @param exclusionListFilePath the path of the file containing the
     * exclusion list
     * @param precursorTolerance the precursor m/z tolerance
     * @param ppm boolean indicating whether the precursor tolerance is in ppm
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading the exclusion list
     */
    private void importListFromFile(String exclusionListFilePath, double precursorTolerance, boolean ppm) throws IOException {

        File exclusionListFile = new File(exclusionListFilePath);
        
        try (BufferedReader br = new BufferedReader(new FileReader(exclusionListFile))) {
        
            HashMap<String, Precursor> precursors = new HashMap<>();
            ArrayList<Integer> possibleCharges = new ArrayList<>(0);

            String line;
            while ((line = br.readLine()) != null) {
                if (!line.equals("")) {
                    String[] split = line.split(separator);
                    Double mz = new Double(split[0]);
                    String name;
                    if (split.length > 1) {
                        name = split[1];
                    } else {
                        name = mz + "";
                    }
                    precursors.put(name, new Precursor(0, mz, possibleCharges));
                }
            }

            precursorMap = new PrecursorMap(precursors, precursorTolerance, ppm);

        }
    }

    /**
     * Returns a list containing the precursors matching the given m/z.
     *
     * @param referenceMz a mz to query
     *
     * @return a list containing the precursors matching the given m/z
     */
    public ArrayList<PrecursorMap.PrecursorWithTitle> getMatchingSpectra(double referenceMz) {
        return precursorMap.getMatchingSpectra(referenceMz);
    }

    /**
     * Returns a boolean indicating whether the given m/z should be excluded
     * according to the exclusion list.
     *
     * @param mz the m/z of interest
     *
     * @return a boolean indicating whether the given m/z should be excluded
     * according to the exclusion list
     */
    public boolean isExcluded(double mz) {
        if (mz < minMz || mz > maxMz) {
            return true;
        }
        ArrayList<PrecursorMap.PrecursorWithTitle> excludedPrecursors = getMatchingSpectra(mz);
        return !excludedPrecursors.isEmpty();
    }

}
