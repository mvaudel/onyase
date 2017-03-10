package no.uib.onyase.scripts.review_figure;

import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.exceptions.exception_handlers.CommandLineExceptionHandler;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.protein_inference.PeptideMapper;
import com.compomics.util.experiment.identification.protein_inference.PeptideProteinMapping;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.preferences.PeptideVariantsPreferences;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

/**
 * This scripts extracts the best PSM from an Onyase result file.
 *
 * @author Marc Vaudel
 */
public class BestPsmFilter {

    private SequenceFactory sequenceFactory = SequenceFactory.getInstance();

    private PeptideMapper peptideMapper;

    private SequenceMatchingPreferences sequenceMatchingPreferences = SequenceMatchingPreferences.getDefaultSequenceMatching();

    /**
     * The main method used to start the script.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {

        try {

            BestPsmFilter bestPsmsFilter = new BestPsmFilter();

           //  int[] conditions = {0,5,6,8,9,10,11,12,13,14,15,16,17};
            int[] conditions = {13};

            for (int condition : conditions) {
                System.out.println("Filtering condition " + condition);
                bestPsmsFilter.run(condition);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public BestPsmFilter() throws IOException, ClassNotFoundException, InterruptedException, SQLException {
        System.out.println("Importing database");
        File databaseFile = new File("C:\\Databases\\uniprot-human-reviewed-trypsin-november-2016_concatenated_target_decoy.fasta");
        sequenceFactory.loadFastaFile(databaseFile);
        SearchParameters searchParameters = new SearchParameters();
        PeptideVariantsPreferences peptideVariantsPreferences = PeptideVariantsPreferences.getNoVariantPreferences();
        ExceptionHandler exceptionHandler = new CommandLineExceptionHandler();
        peptideMapper = sequenceFactory.getDefaultPeptideMapper(sequenceMatchingPreferences, searchParameters, peptideVariantsPreferences, null, exceptionHandler);
    }

    private void run(int condition) throws IOException, InterruptedException, SQLException, ClassNotFoundException {
        
        System.out.println("    Extracting best hits");

        File inFile = new File("C:\\Github\\onyase\\R\\resources\\all_psms_" + condition + ".psm.gz");
        InputStream fileStream = new FileInputStream(inFile);
        InputStream gzipStream = new GZIPInputStream(fileStream);
        Reader decoder = new InputStreamReader(gzipStream, "UTF-8");
        BufferedReader br = new BufferedReader(decoder);

        HashMap<String, BestHit> bestHitMap = new HashMap<String, BestHit>();
        String header;

        try {

            String line = br.readLine();
            header = line;

            while ((line = br.readLine()) != null) {

                String[] lineSplit = line.split(" ");
                String spectrumTitle = lineSplit[0];
                Double eValue = new Double(lineSplit[7]);

                BestHit bestHit = bestHitMap.get(spectrumTitle);

                if (bestHit == null || eValue < bestHit.getScore()) {

                    bestHit = new BestHit(eValue, line);
                    bestHitMap.put(spectrumTitle, bestHit);

                }

            }

        } finally {

            br.close();

        }
        
        System.out.println("    Mapping best hits");

        File outFile = new File("C:\\Github\\onyase\\R\\resources\\best_psms_" + condition + ".txt");

        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));

        try {

            bw.write(header);
            bw.newLine();

            for (String spectrumTitle : bestHitMap.keySet()) {

                BestHit bestHit = bestHitMap.get(spectrumTitle);

                String line = bestHit.getLine();
                String[] lineSplit = line.split(" ");

                String sequence = lineSplit[3];
                ArrayList<PeptideProteinMapping> proteinMapping = peptideMapper.getProteinMapping(sequence, sequenceMatchingPreferences);

                boolean decoy = false;
                boolean target = false;
                for (PeptideProteinMapping peptideProteinMapping : proteinMapping) {
                    String accession = peptideProteinMapping.getProteinAccession();
                    if (sequenceFactory.isDecoyAccession(accession)) {
                        decoy = true;
                    } else {
                        target = true;
                    }
                }
                if (decoy) {
                    lineSplit[8] = "1";
                } else {
                    lineSplit[8] = "0";
                }
                if (target) {
                    lineSplit[9] = "1";
                } else {
                    lineSplit[9] = "0";
                }
                bw.write(lineSplit[0]);
                for (int i = 1; i < lineSplit.length; i++) {
                    bw.write(' ');
                    bw.write(lineSplit[i]);
                }
                bw.newLine();

            }

        } finally {

            bw.close();

        }

    }

    /**
     * Class used to store the best hit details.
     */
    private class BestHit {

        /**
         * The score.
         */
        private double score;
        /**
         * The line.
         */
        private String line;

        /**
         * Constructor.
         *
         * @param score the score
         * @param line the line
         */
        public BestHit(double score, String line) {
            this.score = score;
            this.line = line;
        }

        /**
         * Returns the score of the best hit.
         *
         * @return the score of the best hit
         */
        public double getScore() {
            return score;
        }

        /**
         * Returns the line.
         *
         * @return the line
         */
        public String getLine() {
            return line;
        }

    }
}
