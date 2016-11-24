package no.uib.onyase.scripts;

import com.compomics.util.experiment.biology.EnzymeFactory;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.experiment.massspectrometry.Spectrum;
import com.compomics.util.experiment.massspectrometry.SpectrumFactory;
import com.compomics.util.protein.Header;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import org.ehcache.sizeof.SizeOf;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshallerException;

/**
 *
 * @author Marc Vaudel
 */
public class SizeEstimation {

    private String mgfFilePath = "C:\\Projects\\PeptideShaker\\test files\\1 mgf\\qExactive01819.mgf";
    private String fastaFilePath = "C:\\Databases\\uniprot-human-reviewed-trypsin-april-2016_concatenated_target_decoy.fasta";
    private String parametersFilePath = "C:\\Users\\mvaudel\\.compomics\\identification_parameters\\tutorial new parameters.par";

    private SpectrumFactory spectrumFactory;
    private SequenceFactory sequenceFactory;
    private EnzymeFactory enzymeFactory;
    private PTMFactory ptmFactory;

    /**
     * The main method used to start PeptideShaker.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        SizeEstimation instance;
        if (args.length > 0) {
            instance = new SizeEstimation(args[0], args[1], args[2]);
        } else {
            instance = new SizeEstimation();
        }
        try {
            instance.launch();
            System.out.println(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(0);
        }
    }

    public SizeEstimation(String mgfFilePath, String fastaFilePath, String parametersFilePath) {
        if (mgfFilePath != null) {
            this.mgfFilePath = mgfFilePath;
        }
        if (fastaFilePath != null) {
            this.fastaFilePath = fastaFilePath;
        }
        if (parametersFilePath != null) {
            this.parametersFilePath = parametersFilePath;
        }
    }

    public SizeEstimation() {
        this(null, null, null);
    }

    public void launch() throws IOException, ClassNotFoundException, SQLException, MzMLUnmarshallerException, InterruptedException {

        initializeFactories();

        spectrumFactory.addSpectra(new File(mgfFilePath));
        sequenceFactory.loadFastaFile(new File(fastaFilePath));
        
        
        
        SizeOf sizeOf = SizeOf.newInstance();
        
        long sum = 0;
        ArrayList<Long> spectrumSizes = new ArrayList<Long>(spectrumFactory.getNSpectra());
        for (String fileName : spectrumFactory.getMgfFileNames()) {
            for (String spectrumTitle : spectrumFactory.getSpectrumTitles(fileName)) {
                Spectrum spectrum = spectrumFactory.getSpectrum(fileName, spectrumTitle);
                
                Long spectrumSize = sizeOf.sizeOf(spectrum);
                spectrumSizes.add(spectrumSize);
                sum += spectrumSize;
            }
        }
        
        System.out.println("Total size: " + sum);
        long average = sum / spectrumFactory.getNSpectra();
        System.out.println("Average size: " + average);
        Collections.sort(spectrumSizes);
        int index = spectrumSizes.size() / 2;
        System.out.println("Median spectrum size: " + spectrumSizes.get(index));
        index = 95 * spectrumSizes.size() / 100;
        System.out.println("95% spectrum size: " + spectrumSizes.get(index));
        
        System.out.println(spectrumFactory.getNSpectra() + " spectra.");
        System.out.println("spectrum factory size: " + sizeOf.sizeOf(spectrumFactory));
        
        ArrayList<Long> proteinSizes = new ArrayList<Long>(sequenceFactory.getAccessions().size());
        ArrayList<Long> headerSizes = new ArrayList<Long>(sequenceFactory.getAccessions().size());
        for (String accession : sequenceFactory.getAccessions()) {
            Protein protein = sequenceFactory.getProtein(accession);
            long size = sizeOf.sizeOf(protein);
            proteinSizes.add(size);
            Header header = sequenceFactory.getHeader(accession);
            size  = sizeOf.sizeOf(header);
            headerSizes.add(size);
        }
        
        index = proteinSizes.size() / 2;
        System.out.println("Median protein size: " + proteinSizes.get(index));
        System.out.println("Median header size: " + headerSizes.get(index));
        index = 95 * proteinSizes.size() / 100;
        System.out.println("95% protein size: " + proteinSizes.get(index));
        System.out.println("95% header size: " + headerSizes.get(index));
        
        spectrumFactory.closeFiles();
        sequenceFactory.closeFile();
    }

    private void initializeFactories() {
        spectrumFactory = SpectrumFactory.getInstance();
        sequenceFactory = SequenceFactory.getInstance();
        enzymeFactory = EnzymeFactory.getInstance();
        ptmFactory = PTMFactory.getInstance();
    }

}
