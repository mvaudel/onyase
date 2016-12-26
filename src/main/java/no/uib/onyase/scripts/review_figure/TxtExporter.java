package no.uib.onyase.scripts.review_figure;

import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.spectrum_assumptions.PeptideAssumption;
import com.compomics.util.experiment.io.identifications.idfilereaders.OnyaseIdfileReader;
import com.compomics.util.experiment.massspectrometry.Precursor;
import com.compomics.util.experiment.massspectrometry.SpectrumFactory;
import com.compomics.util.preferences.IdentificationParameters;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshallerException;

/**
 * This text exporter exports PSMs on the fly.
 *
 * @author Marc Vaudel
 */
public class TxtExporter {

    /**
     * The end of line separator.
     */
    public static final String END_LINE = System.getProperty("line.separator");
    /**
     * The spectrum factory.
     */
    private SpectrumFactory spectrumFactory = SpectrumFactory.getInstance();
    /**
     * The writer to use to write all hits.
     */
    private final BufferedWriter bwAll;
    /**
     * The writer to use to write the best hits only.
     */
    private final BufferedWriter bwBest;
    /**
     * Boolean indicating whether the precursor tolerance is in ppm.
     */
    private boolean ppm;
    /**
     * The minimal isotope to consider.
     */
    private int minIsotope;
    /**
     * The maximal isotope to consider.
     */
    private int maxIsotope;

    public TxtExporter(File bestHits, File allHits, IdentificationParameters identificationParameters) throws IOException {

        if (bestHits != null) {
            bwBest = new BufferedWriter(new FileWriter(bestHits));
        } else {
            bwBest = null;
        }
        if (allHits != null) {
            bwAll = new BufferedWriter(new FileWriter(allHits));
        } else {
            bwAll = null;
        }

        ppm = identificationParameters.getSearchParameters().isPrecursorAccuracyTypePpm();
        minIsotope = identificationParameters.getSearchParameters().getMinIsotopicCorrection();
        maxIsotope = identificationParameters.getSearchParameters().getMaxIsotopicCorrection();
    }

    public void writeHeaders() throws IOException {
        if (bwBest != null) {
            bwBest.write("Spectrum_Title" + OnyaseIdfileReader.separator + "mz" + OnyaseIdfileReader.separator + "mz_deviation" + OnyaseIdfileReader.separator + "rt" + OnyaseIdfileReader.separator + "Sequence" + OnyaseIdfileReader.separator + "Modifications" + OnyaseIdfileReader.separator + "Charge" + OnyaseIdfileReader.separator + "HyperScore" + OnyaseIdfileReader.separator + "E-Value" + OnyaseIdfileReader.separator + "Decoy" + OnyaseIdfileReader.separator + "Target");
            bwBest.newLine();
        }
        if (bwAll != null) {
            bwAll.write("Spectrum_Title" + OnyaseIdfileReader.separator + "mz" + OnyaseIdfileReader.separator + "mz_deviation" + OnyaseIdfileReader.separator + "rt" + OnyaseIdfileReader.separator + "Sequence" + OnyaseIdfileReader.separator + "Modifications" + OnyaseIdfileReader.separator + "Charge" + OnyaseIdfileReader.separator + "HyperScore" + OnyaseIdfileReader.separator + "E-Value" + OnyaseIdfileReader.separator + "Decoy" + OnyaseIdfileReader.separator + "Target");
            bwAll.newLine();
        }
    }

    public void writeAssumptions(String mgfFileName, String spectrumTitle, ArrayList<PeptideAssumption> peptideAssumptions) throws IOException, MzMLUnmarshallerException {

        // Encode the spectrum title
        String encodedSpectrumTitle = URLEncoder.encode(spectrumTitle, "utf-8");

        // Get the precursor
        Precursor precursor = spectrumFactory.getPrecursor(mgfFileName, spectrumTitle);

        // Iterate all assumptions and retain the best assumption
        Double bestEvalue = null;
        PeptideAssumption bestPeptideAssumption = null;
        FigureMetrics figureMetrics = new FigureMetrics();
        boolean first = true;
        for (PeptideAssumption peptideAssumption : peptideAssumptions) {
            Double eValue = peptideAssumption.getScore();
            if (bestEvalue == null || eValue < bestEvalue) {
                bestPeptideAssumption = peptideAssumption;
                bestEvalue = eValue;
            }

            // Export the assumption if needed
            if (bwAll != null) {

                figureMetrics = (FigureMetrics) peptideAssumption.getUrParam(figureMetrics);
                String assumptionLine = getAssumptionLine(encodedSpectrumTitle, first, precursor, peptideAssumption, figureMetrics);
                bwAll.write(assumptionLine);
                first = false;
            }
        }

        // Export the best assumption
        if (bwBest != null) {
            figureMetrics = (FigureMetrics) bestPeptideAssumption.getUrParam(figureMetrics);
            String assumptionLine = getAssumptionLine(encodedSpectrumTitle, true, precursor, bestPeptideAssumption, figureMetrics);
            bwBest.write(assumptionLine);
        }
    }

    private String getAssumptionLine(String encodedSpectrumTitle, boolean writeTitle, Precursor precursor, PeptideAssumption peptideAssumption, FigureMetrics figureMetrics) throws UnsupportedEncodingException {

        StringBuilder stringBuilder = new StringBuilder();

        if (writeTitle) {
            stringBuilder.append(encodedSpectrumTitle);
        }
        stringBuilder.append(OnyaseIdfileReader.separator);
        stringBuilder.append(precursor.getMz());
        stringBuilder.append(OnyaseIdfileReader.separator);
        stringBuilder.append(peptideAssumption.getDeltaMass(precursor.getMz(), ppm, minIsotope, maxIsotope));
        stringBuilder.append(OnyaseIdfileReader.separator);
        stringBuilder.append(precursor.getRtInMinutes());
        stringBuilder.append(OnyaseIdfileReader.separator);
        Peptide peptide = peptideAssumption.getPeptide();
        stringBuilder.append(peptide.getSequence());
        stringBuilder.append(OnyaseIdfileReader.separator);
        String modificationsAsString = getModifications(peptide);
        stringBuilder.append(modificationsAsString);
        stringBuilder.append(OnyaseIdfileReader.separator);
        stringBuilder.append(peptideAssumption.getIdentificationCharge().value);
        stringBuilder.append(OnyaseIdfileReader.separator);
        stringBuilder.append(peptideAssumption.getRawScore());
        stringBuilder.append(OnyaseIdfileReader.separator);
        stringBuilder.append(peptideAssumption.getScore());
        if (figureMetrics.isIsDecoy() && figureMetrics.isIsTarget()) {
            stringBuilder.append(OnyaseIdfileReader.separator);
            stringBuilder.append(0.5);
            stringBuilder.append(OnyaseIdfileReader.separator);
            stringBuilder.append(0.5);
        } else if (figureMetrics.isIsDecoy()) {
            stringBuilder.append(OnyaseIdfileReader.separator);
            stringBuilder.append(1);
            stringBuilder.append(OnyaseIdfileReader.separator);
            stringBuilder.append(0);
        } else {
            stringBuilder.append(OnyaseIdfileReader.separator);
            stringBuilder.append(0);
            stringBuilder.append(OnyaseIdfileReader.separator);
            stringBuilder.append(1);
        }
        stringBuilder.append(END_LINE);
        return stringBuilder.toString();
    }

    /**
     * Returns the variable modifications of the given peptide in an utf-8
     * encoded String in the form: modificationName1 +
     * Peptide.MODIFICATION_LOCALIZATION_SEPARATOR + modificationSite1 +
     * Peptide.MODIFICATION_SEPARATOR + modificationName2 +
     * Peptide.MODIFICATION_LOCALIZATION_SEPARATOR + modificationSite2.
     *
     * @param peptide the peptide of interest
     *
     * @return the variable modifications in a string
     *
     * @throws UnsupportedEncodingException exception thrown if the
     * modifications could not be encoded
     */
    private String getModifications(Peptide peptide) throws UnsupportedEncodingException {
        if (peptide.getModificationMatches() == null) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (ModificationMatch modificationMatch : peptide.getModificationMatches()) {
            if (modificationMatch.isVariable()) {
                if (stringBuilder.length() > 0) {
                    stringBuilder.append(Peptide.MODIFICATION_SEPARATOR);
                }
                stringBuilder.append(modificationMatch.getTheoreticPtm()).append(Peptide.MODIFICATION_LOCALIZATION_SEPARATOR).append(modificationMatch.getModificationSite());
            }
        }
        String result = URLEncoder.encode(stringBuilder.toString(), "utf-8");
        return result;
    }

    public void close() throws IOException {
        if (bwAll != null) {
            bwAll.close();
        }
        if (bwBest != null) {
            bwBest.close();
        }
    }
}
