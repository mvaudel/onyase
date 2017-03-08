package no.uib.onyase.scripts.review_figure;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.zip.GZIPInputStream;

/**
 * Filters and counts the PSMs with a hyperscore of 0.
 *
 * @author Marc Vaudel
 */
public class AllPsmsFilter {

    /**
     * The main method used to start the script.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        AllPsmsFilter allPsmsFilter = new AllPsmsFilter();

        try {

            int[] conditions = {0,1,2,3,4,5,6,8,9,10,11,12,13,14,15,16,17};
            //int[] conditions = {0};

            for (int condition : conditions) {
                System.out.println("Filtering condition " + condition);
                allPsmsFilter.run(condition);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void run(int condition) throws IOException {

        File inFile = new File("C:\\Github\\onyase\\R\\resources\\all_psms_" + condition + ".psm.gz");
        InputStream fileStream = new FileInputStream(inFile);
        InputStream gzipStream = new GZIPInputStream(fileStream);
        Reader decoder = new InputStreamReader(gzipStream, "UTF-8");
        BufferedReader br = new BufferedReader(decoder);

        File outFile = new File("C:\\Github\\onyase\\R\\resources\\all_psms_" + condition + "_filtered.psm");

        int nZeros = 0;
        int nTotal = 0;

        try {

            BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));

            try {

                String line = br.readLine();
                bw.write(line);
                bw.newLine();

                while ((line = br.readLine()) != null) {

                    String[] lineSplit = line.split(" ");
                    Double hyperscore = new Double(lineSplit[6]);

                    if (hyperscore <= 0.0) {

                        nZeros++;

                    } else {

                        bw.write(line);
                        bw.newLine();

                    }

                    nTotal++;

                }

            } finally {

                bw.close();

            }

        } finally {

            br.close();

        }

        System.out.println(condition + " nZeros: " + nZeros);
        System.out.println(condition + " nTotal: " + nTotal);
    }
}
