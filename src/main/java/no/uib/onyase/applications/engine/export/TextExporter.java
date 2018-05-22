package no.uib.onyase.applications.engine.export;

import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.io.identification.idfilereaders.OnyaseIdfileReader;
import com.compomics.util.experiment.mass_spectrometry.spectra.Precursor;
import com.compomics.util.experiment.mass_spectrometry.SpectrumFactory;
import com.compomics.util.waiting.WaitingHandler;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;
import no.uib.onyase.applications.engine.model.Psm;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshallerException;
import no.uib.onyase.applications.engine.modules.scoring.EValueEstimator;

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
    private final SpectrumFactory spectrumFactory = SpectrumFactory.getInstance();
    /**
     * A handler for the exceptions.
     */
    private final ExceptionHandler exceptionHandler;
    /**
     * A waiting handler providing feedback to the user and allowing canceling
     * the process.
     */
    private final WaitingHandler waitingHandler;

    /**
     * Constructor.
     *
     * @param waitingHandler a waiting handler providing feedback to the user
     * and allowing canceling the process
     * @param exceptionHandler a handler for the exceptions
     */
    public TextExporter(WaitingHandler waitingHandler, ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        this.waitingHandler = waitingHandler;
    }

    /**
     * Writes all the PSMs to a file. The export represents a table with spaces
     * as separator and every PSM is a line. Spectrum titles are encoded and the
     * table is gziped.
     *
     * @param spectrumFileName the name of the spectrum file
     * @param psmsMap the map of the PSMs
     * @param eValueEstimator an estimator for the e-value
     * @param destinationFile the destination file
     *
     * @throws IOException exception thrown if an error occurred while writing
     * the file
     * @throws MzMLUnmarshallerException exception thrown if an error occurred
     * while reading an mzML file
     */
    public void writePsms(String spectrumFileName, HashMap<String, HashMap<Long, Psm>> psmsMap, EValueEstimator eValueEstimator, File destinationFile) throws IOException, MzMLUnmarshallerException {

        // Set progress bars
        waitingHandler.setSecondaryProgressCounterIndeterminate(false);
        waitingHandler.setMaxSecondaryProgressCounter(psmsMap.size());

        // Setup the writer
        FileOutputStream fileStream = new FileOutputStream(destinationFile);
        GZIPOutputStream gzipStream = new GZIPOutputStream(fileStream);
        OutputStreamWriter encoder = new OutputStreamWriter(gzipStream, OnyaseIdfileReader.encoding);
        try (BufferedWriter bw = new BufferedWriter(encoder)) {

            // Write headers
            writeHeaders(bw);

            // Iterate all spectra
            for (Entry<String, HashMap<Long, Psm>> entry1 : psmsMap.entrySet()) {

                // Get the PSMs
                String spectrumTitle = entry1.getKey();
                HashMap<Long, Psm> spectrumPsms = entry1.getValue();

                // Iterate all Psms
                for (Psm psm : spectrumPsms.values()) {

                    // Write to output
                    writePsm(bw, spectrumFileName, spectrumTitle, psm, eValueEstimator);
                    
                }

                // Increase progress
                waitingHandler.increaseSecondaryProgressCounter();

            }

        }
    }

    private void writeHeaders(BufferedWriter bw) throws IOException {
        bw.write("Spectrum_Title" + OnyaseIdfileReader.separator + "mz" + OnyaseIdfileReader.separator + "rt" + OnyaseIdfileReader.separator + "Sequence" + OnyaseIdfileReader.separator + "Modifications" + OnyaseIdfileReader.separator + "Charge" + OnyaseIdfileReader.separator + "Score" + OnyaseIdfileReader.separator + "E-Value");
        bw.newLine();
    }

    private void writePsm(BufferedWriter bw, String spectrumFileName, String spectrumTitle, Psm psm, EValueEstimator eValueEstimator) throws IOException, MzMLUnmarshallerException {

        // Encode the spectrum title
        String encodedSpectrumTitle = URLEncoder.encode(spectrumTitle, OnyaseIdfileReader.encoding);

        // Get the precursor
        Precursor precursor = spectrumFactory.getPrecursor(spectrumFileName, spectrumTitle);

        // Get the peptide
        Peptide peptide = psm.getPeptide();

        // Get the e-value
        double score = psm.getScore();
        double eValue = eValueEstimator.getEValue(spectrumTitle, score);

        // Create the line for this PSM
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
        stringBuilder.append(psm.getCharge());
        stringBuilder.append(OnyaseIdfileReader.separator);
        stringBuilder.append(score);
        stringBuilder.append(OnyaseIdfileReader.separator);
        stringBuilder.append(eValue);
        stringBuilder.append(END_LINE);

        // Write to the file
        bw.write(stringBuilder.toString());
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

        String result = Arrays.stream(peptide.getVariableModifications())
                .map(modificationMatch -> String.join(Peptide.MODIFICATION_LOCALIZATION_SEPARATOR, modificationMatch.getModification(), Integer.toString(modificationMatch.getSite())))
                .collect(Collectors.joining(Peptide.MODIFICATION_SEPARATOR));

        return URLEncoder.encode(result, "utf-8");
    }
}
