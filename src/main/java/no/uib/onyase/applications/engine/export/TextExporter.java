package no.uib.onyase.applications.engine.export;

import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.io.identifications.idfilereaders.OnyaseIdfileReader;
import com.compomics.util.experiment.massspectrometry.Precursor;
import com.compomics.util.experiment.massspectrometry.SpectrumFactory;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshallerException;

/**
 * This text exporter exports PSMs on the fly.
 *
 * @author Marc Vaudel
 */
public class TextExporter {

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

    public TextExporter(File allHits) throws IOException {
        if (allHits != null) {
            bwAll = new BufferedWriter(new FileWriter(allHits));
        } else {
            bwAll = null;
        }
    }

    public void writeHeaders() throws IOException {
        if (bwAll != null) {
            bwAll.write("Spectrum_Title" + OnyaseIdfileReader.separator + "mz" + OnyaseIdfileReader.separator + "rt" + OnyaseIdfileReader.separator + "Sequence" + OnyaseIdfileReader.separator + "Modifications" + OnyaseIdfileReader.separator + "Charge" + OnyaseIdfileReader.separator + "HyperScore" + OnyaseIdfileReader.separator + "E-Value" + OnyaseIdfileReader.separator + "Decoy" + OnyaseIdfileReader.separator + "Target");
            bwAll.newLine();
        }
    }

    public void writePeptide(String mgfFileName, String spectrumTitle, Peptide peptide, double score, int charge) throws IOException, MzMLUnmarshallerException {

        // Encode the spectrum title
        String encodedSpectrumTitle = URLEncoder.encode(spectrumTitle, "utf-8");

        // Get the precursor
        Precursor precursor = spectrumFactory.getPrecursor(mgfFileName, spectrumTitle);

        // Create the line for this peptide
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(encodedSpectrumTitle);
        stringBuilder.append(OnyaseIdfileReader.separator);
        stringBuilder.append(precursor.getMz());
        stringBuilder.append(OnyaseIdfileReader.separator);
        stringBuilder.append(precursor.getRtInMinutes());
        stringBuilder.append(OnyaseIdfileReader.separator);
        stringBuilder.append(peptide.getSequence());
        stringBuilder.append(OnyaseIdfileReader.separator);
        String modificationsAsString = getModifications(peptide);
        stringBuilder.append(modificationsAsString);
        stringBuilder.append(OnyaseIdfileReader.separator);
        stringBuilder.append(charge);
        stringBuilder.append(OnyaseIdfileReader.separator);
        stringBuilder.append(score);
        stringBuilder.append(END_LINE);

        // Write to the file
        bwAll.write(stringBuilder.toString());
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

    /**
     * Closes open connections to files.
     *
     * @throws IOException exception thrown whenever an error occurred while
     * closing the connection to aF file.
     */
    public void close() throws IOException {
        if (bwAll != null) {
            bwAll.close();
        }
    }
}
