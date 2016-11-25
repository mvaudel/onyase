package no.uib.onyase.scripts;

import com.compomics.util.experiment.biology.ElementaryElement;
import com.compomics.util.experiment.biology.EnzymeFactory;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.biology.ions.ElementaryIon;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.peptide_fragmentation.PeptideFragmentationModel;
import com.compomics.util.experiment.identification.protein_sequences.ProteinSequenceIterator;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.experiment.identification.psm_scoring.psm_scores.FastXcorr;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationSettings;
import com.compomics.util.experiment.identification.spectrum_annotation.SpecificAnnotationSettings;
import com.compomics.util.experiment.identification.spectrum_annotation.spectrum_annotators.PeptideSpectrumAnnotator;
import com.compomics.util.experiment.identification.spectrum_assumptions.PeptideAssumption;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Precursor;
import com.compomics.util.experiment.massspectrometry.Spectrum;
import com.compomics.util.experiment.massspectrometry.SpectrumFactory;
import com.compomics.util.experiment.massspectrometry.indexes.PrecursorMap;
import com.compomics.util.preferences.DigestionPreferences;
import com.compomics.util.preferences.IdentificationParameters;
import com.compomics.util.waiting.Duration;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshallerException;

/**
 *
 * @author Marc Vaudel
 */
public class ReviewFigure {

    private String mgfFilePath = "C:\\Projects\\PeptideShaker\\test files\\1 mgf\\qExactive01819.mgf";
    private String fastaFilePath = "C:\\Databases\\uniprot-human-reviewed-trypsin-november-2016_concatenated_target_decoy.fasta";
    private String parametersFilePath = "C:\\Projects\\PeptideShaker\\test files\\tutorial.par";

    public final static String SEPARATOR = "\t";

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
        ReviewFigure instance;
        if (args.length > 0) {
            instance = new ReviewFigure(args[0], args[1], args[2]);
        } else {
            instance = new ReviewFigure();
        }
        try {
            instance.launch();
            System.out.println(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(0);
        }
    }

    public ReviewFigure(String mgfFilePath, String fastaFilePath, String parametersFilePath) {
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

    public ReviewFigure() {
        this(null, null, null);
    }

    public void launch() throws IOException, ClassNotFoundException, SQLException, MzMLUnmarshallerException, InterruptedException {

        Duration duration = new Duration();
        duration.start();

        initializeFactories();

        File mgfFile = new File(mgfFilePath);

        spectrumFactory.addSpectra(mgfFile);
        sequenceFactory.loadFastaFile(new File(fastaFilePath));

        IdentificationParameters identificationParameters = IdentificationParameters.getIdentificationParameters(new File(parametersFilePath));
        SearchParameters searchParameters = identificationParameters.getSearchParameters();
        DigestionPreferences digestionPreferences = searchParameters.getDigestionPreferences();
        // digestionPreferences.setSpecificity("Trypsin", DigestionPreferences.Specificity.semiSpecific);
        // digestionPreferences.setCleavagePreference(DigestionPreferences.CleavagePreference.unSpecific);
        searchParameters.setPrecursorAccuracy(10.0);
        searchParameters.setPrecursorAccuracyType(SearchParameters.MassAccuracyType.PPM);
        int minCharge = searchParameters.getMinChargeSearched().value;
        int maxCharge = searchParameters.getMaxChargeSearched().value;

        String fileName = mgfFile.getName();
        PrecursorMap precursorMap = new PrecursorMap(spectrumFactory.getPrecursorMap(fileName), searchParameters.getPrecursorAccuracy(), searchParameters.isPrecursorAccuracyTypePpm());
        Double mzMin = precursorMap.getMinMz();
        Double mzMax = precursorMap.getMaxMz();
        Double massMin = (mzMin * minCharge) - (minCharge * ElementaryIon.proton.getTheoreticMass());
        Double massMax = (mzMax * maxCharge) - (maxCharge * ElementaryIon.proton.getTheoreticMass());
        if (searchParameters.isPrecursorAccuracyTypePpm()) {
            massMin *= (1 - searchParameters.getPrecursorAccuracy() / 1000000);
            massMax *= (1 + searchParameters.getPrecursorAccuracy() / 1000000);
        } else {
            massMin -= searchParameters.getPrecursorAccuracy();
            massMax += searchParameters.getPrecursorAccuracy();
        }

        PeptideSpectrumAnnotator peptideSpectrumAnnotator = new PeptideSpectrumAnnotator();

        FastXcorr fastXcorr = new FastXcorr(PeptideFragmentationModel.uniform);

        ProteinSequenceIterator proteinSequenceIterator = new ProteinSequenceIterator(searchParameters.getPtmSettings().getFixedModifications());

        int lastProgress = 0;
        int cpt = 0;
        SequenceFactory.ProteinIterator pi = sequenceFactory.getProteinIterator(false);
        long nMatches = 0, nSequences = 0;

        HashMap<String, ArrayList<Double>> ms1DeviationMap = new HashMap<String, ArrayList<Double>>();
        HashMap<String, ArrayList<Double>> scoresMap = new HashMap<String, ArrayList<Double>>();

        while (pi.hasNext()) {
            Protein protein = pi.getNextProtein();
            //Protein protein = sequenceFactory.getProtein("P11021");
            String sequence = protein.getSequence();
            ArrayList<Peptide> peptides = proteinSequenceIterator.getPeptides(sequence, digestionPreferences, massMin, massMax);
            nSequences += peptides.size();

            for (Peptide peptide : peptides) {

                String peptideKey = peptide.getSequence();
                Double peptideMass = peptide.getMass();
                for (int charge = minCharge; charge <= maxCharge; charge++) {
                    PeptideAssumption peptideAssumption = new PeptideAssumption(peptide, new Charge(Charge.PLUS, charge));
                    double mz = (charge * ElementaryIon.proton.getTheoreticMass() + peptideMass) / charge;
                    ArrayList<PrecursorMap.PrecursorWithTitle> matches = precursorMap.getMatchingSpectra(mz);
                    nMatches += matches.size();
                    for (PrecursorMap.PrecursorWithTitle precursorWithTitle : matches) {
                        String spectrumTitle = precursorWithTitle.spectrumTitle;
                        Precursor precursor = precursorWithTitle.precursor;
                        String spectrumKey = Spectrum.getSpectrumKey(fileName, spectrumTitle);
                        MSnSpectrum spectrum = (MSnSpectrum) spectrumFactory.getSpectrum(fileName, spectrumTitle);
                        AnnotationSettings annotationSettings = identificationParameters.getAnnotationPreferences();
                        SpecificAnnotationSettings specificAnnotationSettings = annotationSettings.getSpecificAnnotationPreferences(spectrumKey, peptideAssumption, identificationParameters.getSequenceMatchingPreferences(), identificationParameters.getPtmScoringPreferences().getSequenceMatchingPreferences());
                        Double score = fastXcorr.getScore(peptide, spectrum, annotationSettings, specificAnnotationSettings, peptideSpectrumAnnotator);

                        ArrayList<Double> spectrumScores = scoresMap.get(spectrumTitle);
                        if (spectrumScores == null) {
                            spectrumScores = new ArrayList<Double>();
                            scoresMap.put(spectrumTitle, spectrumScores);
                        }
                        spectrumScores.add(score);

                        Double ms1Deviation;
                        if (searchParameters.isPrecursorAccuracyTypePpm()) {
                            ms1Deviation = 1000000 * (mz - precursor.getMz()) / precursor.getMz();
                        } else {
                            ms1Deviation = mz - precursor.getMz();
                        }
                        ArrayList<Double> spectrumDeviations = ms1DeviationMap.get(spectrumTitle);
                        if (spectrumDeviations == null) {
                            spectrumDeviations = new ArrayList<Double>();
                            ms1DeviationMap.put(spectrumTitle, spectrumDeviations);
                        }
                        spectrumDeviations.add(ms1Deviation);
                    }
                }
            }

            cpt++;
            //System.out.println(protein.getAccession() + ": " + peptides.size());
            int progress = 100 * cpt / sequenceFactory.getNSequences();
            if (progress > lastProgress) {
                System.out.println("Progress: " + progress + "%");
                lastProgress = progress;
            }
        }

        duration.end();
        System.out.println("Proteins: " + sequenceFactory.getNSequences() + ", Peptides: " + nSequences + ", Spectra: " + spectrumFactory.getNSpectra() + ", Matches: " + nMatches);
        System.out.println("Processing time: " + duration);

        File reportFile = new File("C:\\Github\\onyase\\R\\report.txt");
        BufferedWriter bw = new BufferedWriter(new FileWriter(reportFile));
        bw.write("Proteins" + SEPARATOR + sequenceFactory.getNSequences());
        bw.newLine();
        bw.write("Peptides" + SEPARATOR + nSequences);
        bw.newLine();
        bw.write("Spectra" + SEPARATOR + spectrumFactory.getNSpectra());
        bw.newLine();
        bw.write("Matches" + SEPARATOR + nMatches);
        bw.close();

        File matchesFile = new File("C:\\Github\\onyase\\R\\matches.txt");
        bw = new BufferedWriter(new FileWriter(matchesFile));
        for (String spectrumTitle : spectrumFactory.getSpectrumTitles(fileName)) {
            bw.write(spectrumTitle);
            bw.write(SEPARATOR);
            ArrayList<Double> mzDeviations = ms1DeviationMap.get(spectrumTitle);
            if (mzDeviations == null) {
                bw.write(SEPARATOR);
            } else {
                StringBuilder mzDeviationsTxt = new StringBuilder();
                for (Double mzDeviation : mzDeviations) {
                    if (mzDeviationsTxt.length() > 0) {
                        mzDeviationsTxt.append(",");
                    }
                    mzDeviationsTxt.append(mzDeviation);
                }
                bw.write(mzDeviationsTxt.toString());
                bw.write(SEPARATOR);
                ArrayList<Double> scores = scoresMap.get(spectrumTitle);
                StringBuilder scoresTxt = new StringBuilder();
                for (Double score : scores) {
                    if (scoresTxt.length() > 0) {
                        scoresTxt.append(",");
                    }
                    scoresTxt.append(score);
                }
                bw.write(scoresTxt.toString());
            }
            bw.newLine();
        }
        bw.close();

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
