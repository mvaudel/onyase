package no.uib.onyase.applications.engine.parameters;

import com.compomics.util.experiment.identification.spectrum_annotation.SimplePeptideAnnotator;
import com.compomics.util.io.json.JsonMarshaller;
import com.compomics.util.preferences.DigestionPreferences;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import no.uib.onyase.applications.engine.modules.scoring.PsmScore;

/**
 * The Onyase engine parameters
 *
 * @author Marc Vaudel
 */
public class EngineParameters {

    /**
     * The current version of the parameters.
     */
    public final static String currentVersion = "0.0.2";
    /**
     * The version used to create the parameters.
     */
    public final String version = currentVersion;
    /**
     * The name of the parameter set.
     */
    private String name;
    /**
     * The score to use.
     */
    private PsmScore psmScore;
    /**
     * The maximal number of Xs allowed in a peptide.
     */
    private int maxX;
    /**
     * The names of the fixed modifications.
     */
    private ArrayList<String> fixedModifications;
    /**
     * The names of the variable modifications.
     */
    private String[] variableModifications;
    /**
     * Map of the maximal number of modifications per peptide.
     */
    private HashMap<String, Integer> maxModifications;
    /**
     * The preferred number of modification sites to inspect for every peptide.
     */
    private int maxSites;
    /**
     * The MS1 m/z tolerance.
     */
    private double ms1Tolerance;
    /**
     * Boolean indicating whether the MS1 tolerance is in ppm.
     */
    private boolean ms1TolerancePpm;
    /**
     * The MS2 m/z tolerance.
     */
    private double ms2Tolerance;
    /**
     * Boolean indicating whether the MS2 tolerance is in ppm.
     */
    private boolean ms2TolerancePpm;
    /**
     * The MS1 minimal m/z to consider.
     */
    private double ms1MinMz;
    /**
     * The MS1 maximal m/z to consider.
     */
    private double ms1MaxMz = Double.MAX_VALUE;
    /**
     * The MS2 intensity threshold.
     */
    private double ms2IntensityThreshold;
    /**
     * Boolean indicating whether the charge in the mgf file should be used.
     */
    private boolean useMgfCharge;
    /**
     * The minimal charge to consider.
     */
    private int minCharge;
    /**
     * The maximal charge to consider.
     */
    private int maxCharge;
    /**
     * The minimal isotopic correction to conduct.
     */
    private int minIsotopicCorrection;
    /**
     * The maximal isotopic correction to conduct.
     */
    private int maxIsotopicCorrection;
    /**
     * The digestion preferences.
     */
    private DigestionPreferences digestionPreferences;
    /**
     * The dominant ion series.
     */
    private SimplePeptideAnnotator.IonSeries dominantSeries;
    /**
     * The spectrum annotation settings.
     */
    private SpectrumAnnotationSettings spectrumAnnotationSettings;

    /**
     * Constructor.
     */
    public EngineParameters() {

    }

    /**
     * Returns the score to use.
     * 
     * @return the score to use
     */
    public PsmScore getPsmScore() {
        return psmScore;
    }

    /**
     * Sets the score to use.
     * 
     * @param psmScore the score to use
     */
    public void setPsmScore(PsmScore psmScore) {
        this.psmScore = psmScore;
    }

    /**
     * Returns the maximal number of Xs allowed in a peptide.
     * 
     * @return the maximal number of Xs allowed in a peptide
     */
    public int getMaxX() {
        return maxX;
    }

    /**
     * Sets the maximal number of Xs allowed in a peptide.
     * 
     * @param maxX the maximal number of Xs allowed in a peptide
     */
    public void setMaxX(int maxX) {
        this.maxX = maxX;
    }

    /**
     * Returns the name of the fixed modifications.
     * 
     * @return the name of the fixed modifications
     */
    public ArrayList<String> getFixedModifications() {
        return fixedModifications;
    }

    /**
     * Sets the name of the fixed modifications.
     * 
     * @param fixedModifications the name of the fixed modifications
     */
    public void setFixedModifications(ArrayList<String> fixedModifications) {
        this.fixedModifications = fixedModifications;
    }

    /**
     * Returns the name of the variable modifications.
     * 
     * @return the name of the variable modifications
     */
    public String[] getVariableModifications() {
        return variableModifications;
    }

    /**
     * Sets the name of the variable modifications.
     * 
     * @param variableModifications the name of the variable modifications
     */
    public void setVariableModifications(String[] variableModifications) {
        this.variableModifications = variableModifications;
    }

    /**
     * Returns the maximal number of modifications allowed per peptide in a indexed by modification name.
     * 
     * @return the maximal number of modifications allowed per peptide 
     */
    public HashMap<String, Integer> getMaxModifications() {
        return maxModifications;
    }

    /**
     * Sets the maximal number of modifications allowed per peptide in a indexed by modification name.
     * 
     * @param maxModifications the maximal number of modifications allowed per peptide 
     */
    public void setMaxModifications(HashMap<String, Integer> maxModifications) {
        this.maxModifications = maxModifications;
    }

    /**
     * Returns the preferred maximal number of modification sites to test per peptide.
     * 
     * @return the preferred maximal number of modification sites to test per peptide
     */
    public int getMaxSites() {
        return maxSites;
    }

    /**
     * Sets the preferred maximal number of modification sites to test per peptide.
     * 
     * @param maxSites the preferred maximal number of modification sites to test per peptide
     */
    public void setMaxSites(int maxSites) {
        this.maxSites = maxSites;
    }

    /**
     * Returns the MS1 m/z tolerance.
     * 
     * @return the MS1 m/z tolerance
     */
    public double getMs1Tolerance() {
        return ms1Tolerance;
    }

    /**
     * Sets the MS1 m/z tolerance.
     * 
     * @param ms1Tolerance the MS1 m/z tolerance
     */
    public void setMs1Tolerance(double ms1Tolerance) {
        this.ms1Tolerance = ms1Tolerance;
    }

    /**
     * Returns a boolean indicating whether the MS1 tolerance is in ppm.
     * 
     * @return a boolean indicating whether the MS1 tolerance is in ppm
     */
    public boolean isMs1TolerancePpm() {
        return ms1TolerancePpm;
    }

    /**
     * Sets a boolean indicating whether the MS1 tolerance is in ppm.
     * 
     * @param ms1TolerancePpm a boolean indicating whether the MS1 tolerance is in ppm
     */
    public void setMs1TolerancePpm(boolean ms1TolerancePpm) {
        this.ms1TolerancePpm = ms1TolerancePpm;
    }

    /**
     * Returns the MS2 m/z tolerance.
     * 
     * @return the MS2 m/z tolerance
     */
    public double getMs2Tolerance() {
        return ms2Tolerance;
    }

    /**
     * Sets the MS2 m/z tolerance.
     * 
     * @param ms2Tolerance the MS2 m/z tolerance
     */
    public void setMs2Tolerance(double ms2Tolerance) {
        this.ms2Tolerance = ms2Tolerance;
    }

    /**
     * Returns a boolean indicating whether the MS2 tolerance is in ppm.
     * 
     * @return a boolean indicating whether the MS2 tolerance is in ppm
     */
    public boolean isMs2TolerancePpm() {
        return ms2TolerancePpm;
    }

    /**
     * Sets a boolean indicating whether the MS2 tolerance is in ppm.
     * 
     * @param ms2TolerancePpm a boolean indicating whether the MS2 tolerance is in ppm
     */
    public void setMs2TolerancePpm(boolean ms2TolerancePpm) {
        this.ms2TolerancePpm = ms2TolerancePpm;
    }

    /**
     * Returns the maximal MS1 m/z to consider.
     * 
     * @return the maximal MS1 m/z to consider
     */
    public double getMs1MaxMz() {
        return ms1MaxMz;
    }

    /**
     * Sets the maximal MS1 m/z to consider.
     * 
     * @param ms1MaxMz the maximal MS1 m/z to consider
     */
    public void setMs1MaxMz(double ms1MaxMz) {
        this.ms1MaxMz = ms1MaxMz;
    }

    /**
     * Returns the minimal MS1 m/z to consider.
     * 
     * @return the minimal MS1 m/z to consider
     */
    public double getMs1MinMz() {
        return ms1MinMz;
    }

    /**
     * Sets the minimal MS1 m/z to consider.
     * 
     * @param ms1MinMz the minimal MS1 m/z to consider
     */
    public void setMs1MinMz(double ms1MinMz) {
        this.ms1MinMz = ms1MinMz;
    }

    /**
     * Returns the MS2 intensity threshold.
     * 
     * @return the MS2 intensity threshold
     */
    public double getMs2IntensityThreshold() {
        return ms2IntensityThreshold;
    }

    /**
     * Sets the MS2 intensity threshold.
     * 
     * @param ms2IntensityThreshold the MS2 intensity threshold
     */
    public void setMs2IntensityThreshold(double ms2IntensityThreshold) {
        this.ms2IntensityThreshold = ms2IntensityThreshold;
    }

    /**
     * Returns a boolean indicating whether the charge in the mgf file should be used.
     * 
     * @return a boolean indicating whether the charge in the mgf file should be used
     */
    public boolean isUseMgfCharge() {
        return useMgfCharge;
    }

    /**
     * Sets a boolean indicating whether the charge in the mgf file should be used.
     * 
     * @param useMgfCharge a boolean indicating whether the charge in the mgf file should be used
     */
    public void setUseMgfCharge(boolean useMgfCharge) {
        this.useMgfCharge = useMgfCharge;
    }

    /**
     * Returns the minimal charge to consider.
     * 
     * @return the minimal charge to consider
     */
    public int getMinCharge() {
        return minCharge;
    }

    /**
     * Sets the minimal charge to consider.
     * 
     * @param minCharge the minimal charge to consider
     */
    public void setMinCharge(int minCharge) {
        this.minCharge = minCharge;
    }

    /**
     * Returns the maximal charge to consider.
     * 
     * @return the maximal charge to consider
     */
    public int getMaxCharge() {
        return maxCharge;
    }

    /**
     * Sets the maximal charge to consider.
     * 
     * @param maxCharge the maximal charge to consider
     */
    public void setMaxCharge(int maxCharge) {
        this.maxCharge = maxCharge;
    }

    /**
     * Returns the minimal isotope to consider.
     * 
     * @return the minimal isotope to consider
     */
    public int getMinIsotopicCorrection() {
        return minIsotopicCorrection;
    }

    /**
     * Sets the minimal isotope to consider.
     * 
     * @param minIsotopicCorrection the minimal isotope to consider
     */
    public void setMinIsotopicCorrection(int minIsotopicCorrection) {
        this.minIsotopicCorrection = minIsotopicCorrection;
    }

    /**
     * Returns the maximal isotope to consider.
     * 
     * @return the maximal isotope to consider
     */
    public int getMaxIsotopicCorrection() {
        return maxIsotopicCorrection;
    }

    /**
     * Sets the maximal isotope to consider.
     * 
     * @param maxIsotopicCorrection the maximal isotope to consider
     */
    public void setMaxIsotopicCorrection(int maxIsotopicCorrection) {
        this.maxIsotopicCorrection = maxIsotopicCorrection;
    }

    /**
     * Returns the digestion preferences.
     * 
     * @return the digestion preferences
     */
    public DigestionPreferences getDigestionPreferences() {
        return digestionPreferences;
    }

    /**
     * Sets the digestion preferences.
     * 
     * @param digestionPreferences the digestion preferences
     */
    public void setDigestionPreferences(DigestionPreferences digestionPreferences) {
        this.digestionPreferences = digestionPreferences;
    }

    /**
     * Returns the dominant ion series.
     * 
     * @return the dominant ion series
     */
    public SimplePeptideAnnotator.IonSeries getDominantSeries() {
        return dominantSeries;
    }

    /**
     * Sets the dominant ion series
     * 
     * @param dominantSeries the dominant ion series
     */
    public void setDominantSeries(SimplePeptideAnnotator.IonSeries dominantSeries) {
        this.dominantSeries = dominantSeries;
    }

    /**
     * Returns the name.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     * 
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the spectrum annotation settings.
     * 
     * @return the spectrum annotation settings
     */
    public SpectrumAnnotationSettings getSpectrumAnnotationSettings() {
        return spectrumAnnotationSettings;
    }

    /**
     * Sets the spectrum annotation settings.
     * 
     * @param spectrumAnnotationSettings the spectrum annotation settings
     */
    public void setSpectrumAnnotationSettings(SpectrumAnnotationSettings spectrumAnnotationSettings) {
        this.spectrumAnnotationSettings = spectrumAnnotationSettings;
    }

    /**
     * Loads the parameters from a file.
     *
     * @param parametersFile the file
     *
     * @return the parameters
     *
     * @throws IOException if an IOException occurs
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     */
    public static EngineParameters getIdentificationParameters(File parametersFile) throws IOException, ClassNotFoundException {

        // Read json file
        JsonMarshaller jsonMarshaller = new JsonMarshaller();
        return (EngineParameters) jsonMarshaller.fromJson(EngineParameters.class, parametersFile);
    }

    /**
     * Saves the parameters to a json Ffile.
     *
     * @param parameters the parameters
     * @param parametersFile the file
     *
     * @throws IOException if an IOException occurs
     */
    public static void saveParameters(EngineParameters parameters, File parametersFile) throws IOException {

        JsonMarshaller jsonMarshaller = new JsonMarshaller();
        jsonMarshaller.saveObjectToJson(parameters, parametersFile);
    }

}
