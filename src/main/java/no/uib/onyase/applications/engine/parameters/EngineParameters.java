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

    public final static String currentVersion = "0.0.2";

    public final String version = currentVersion;

    private String name;

    private PsmScore psmScore;

    private int maxX;

    private ArrayList<String> fixedModifications;
    private String[] variableModifications;
    private HashMap<String, Integer> maxModifications;
    private int maxSites;

    private double ms1Tolerance;
    private boolean ms1TolerancePpm;
    private double ms2Tolerance;
    private boolean ms2TolerancePpm;
    private double ms1MinMz;
    private double ms1MaxMz;
    private double ms2IntensityThreshold;

    private boolean useMgfCharge;
    private int minCharge;
    private int maxCharge;

    private int minIsotopicCorrection;
    private int maxIsotopicCorrection;

    private DigestionPreferences digestionPreferences;

    private SimplePeptideAnnotator.IonSeries dominantSeries;

    public EngineParameters() {

    }

    public PsmScore getPsmScore() {
        return psmScore;
    }

    public void setPsmScore(PsmScore psmScore) {
        this.psmScore = psmScore;
    }

    public int getMaxX() {
        return maxX;
    }

    public void setMaxX(int maxX) {
        this.maxX = maxX;
    }

    public ArrayList<String> getFixedModifications() {
        return fixedModifications;
    }

    public void setFixedModifications(ArrayList<String> fixedModifications) {
        this.fixedModifications = fixedModifications;
    }

    public String[] getVariableModifications() {
        return variableModifications;
    }

    public void setVariableModifications(String[] variableModifications) {
        this.variableModifications = variableModifications;
    }

    public HashMap<String, Integer> getMaxModifications() {
        return maxModifications;
    }

    public void setMaxModifications(HashMap<String, Integer> maxModifications) {
        this.maxModifications = maxModifications;
    }

    public int getMaxSites() {
        return maxSites;
    }

    public void setMaxSites(int maxSites) {
        this.maxSites = maxSites;
    }

    public double getMs1Tolerance() {
        return ms1Tolerance;
    }

    public void setMs1Tolerance(double ms1Tolerance) {
        this.ms1Tolerance = ms1Tolerance;
    }

    public boolean isMs1TolerancePpm() {
        return ms1TolerancePpm;
    }

    public void setMs1TolerancePpm(boolean ms1TolerancePpm) {
        this.ms1TolerancePpm = ms1TolerancePpm;
    }

    public double getMs2Tolerance() {
        return ms2Tolerance;
    }

    public void setMs2Tolerance(double ms2Tolerance) {
        this.ms2Tolerance = ms2Tolerance;
    }

    public boolean isMs2TolerancePpm() {
        return ms2TolerancePpm;
    }

    public void setMs2TolerancePpm(boolean ms2TolerancePpm) {
        this.ms2TolerancePpm = ms2TolerancePpm;
    }

    public double getMs1MaxMz() {
        return ms1MaxMz;
    }

    public void setMs1MaxMz(double ms1MaxMz) {
        this.ms1MaxMz = ms1MaxMz;
    }

    public double getMs1MinMz() {
        return ms1MinMz;
    }

    public void setMs1MinMz(double ms1MinMz) {
        this.ms1MinMz = ms1MinMz;
    }

    public double getMs2IntensityThreshold() {
        return ms2IntensityThreshold;
    }

    public void setMs2IntensityThreshold(double ms2IntensityThreshold) {
        this.ms2IntensityThreshold = ms2IntensityThreshold;
    }

    public boolean isUseMgfCharge() {
        return useMgfCharge;
    }

    public void setUseMgfCharge(boolean useMgfCharge) {
        this.useMgfCharge = useMgfCharge;
    }

    public int getMinCharge() {
        return minCharge;
    }

    public void setMinCharge(int minCharge) {
        this.minCharge = minCharge;
    }

    public int getMaxCharge() {
        return maxCharge;
    }

    public void setMaxCharge(int maxCharge) {
        this.maxCharge = maxCharge;
    }

    public int getMinIsotopicCorrection() {
        return minIsotopicCorrection;
    }

    public void setMinIsotopicCorrection(int minIsotopicCorrection) {
        this.minIsotopicCorrection = minIsotopicCorrection;
    }

    public int getMaxIsotopicCorrection() {
        return maxIsotopicCorrection;
    }

    public void setMaxIsotopicCorrection(int maxIsotopicCorrection) {
        this.maxIsotopicCorrection = maxIsotopicCorrection;
    }

    public DigestionPreferences getDigestionPreferences() {
        return digestionPreferences;
    }

    public void setDigestionPreferences(DigestionPreferences digestionPreferences) {
        this.digestionPreferences = digestionPreferences;
    }

    public SimplePeptideAnnotator.IonSeries getDominantSeries() {
        return dominantSeries;
    }

    public void setDominantSeries(SimplePeptideAnnotator.IonSeries dominantSeries) {
        this.dominantSeries = dominantSeries;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
