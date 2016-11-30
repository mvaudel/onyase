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
import com.compomics.util.experiment.identification.psm_scoring.psm_scores.HyperScore;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationSettings;
import com.compomics.util.experiment.identification.spectrum_annotation.NeutralLossesMap;
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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
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
    private String psReportFilePath = "C:\\Github\\onyase\\Resources\\Score_test.txt";

    public final static String SEPARATOR = " ";

    private SpectrumFactory spectrumFactory;
    private SequenceFactory sequenceFactory;
    private EnzymeFactory enzymeFactory;
    private PTMFactory ptmFactory;

    private HashMap<String, String> psIds;
    private HashMap<String, String> psScore;
    private HashMap<String, String> psConfidence;

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
        loadPsData();

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

        AnnotationSettings annotationSettings = identificationParameters.getAnnotationPreferences();
        annotationSettings.setIntensityLimit(0.5);
        annotationSettings.clearNeutralLosses();

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

        HyperScore hyperScore = new HyperScore(PeptideFragmentationModel.uniform);

        ProteinSequenceIterator proteinSequenceIterator = new ProteinSequenceIterator(searchParameters.getPtmSettings().getFixedModifications());

        int lastProgress = 0;
        int cpt = 0;
        int totalProgress = sequenceFactory.getNSequences() + spectrumFactory.getNSpectra();
        SequenceFactory.ProteinIterator pi = sequenceFactory.getProteinIterator(false);
        long nMatches = 0, nSequences = 0;

        HashMap<String, Integer> nMatchesMap = new HashMap<String, Integer>();
        ArrayList<String> spectrumTitlesExport = new ArrayList<String>();
        ArrayList<PeptideAssumption> peptidesExport = new ArrayList<PeptideAssumption>();
        ArrayList<Double> ms1Deviations = new ArrayList<Double>();
        ArrayList<Double> scores = new ArrayList<Double>();
        ArrayList<String> categories = new ArrayList<String>();
        HashMap<String, Double> newScores = new HashMap<String, Double>();
        HashMap<String, Double> newEValues = new HashMap<String, Double>();
        HashMap<String, ArrayList<Double>> hyperScores = new HashMap<String, ArrayList<Double>>(spectrumFactory.getNSpectra());

        while (pi.hasNext()) {
            Protein protein = pi.getNextProtein();
            String accession = protein.getAccession();
            String category;
            if (sequenceFactory.isDecoyAccession(accession)) {
                category = "Decoy";
            } else {
                category = "Target";
            }
            //Protein protein = sequenceFactory.getProtein("P11021");
            String sequence = protein.getSequence();
            ArrayList<Peptide> peptides = proteinSequenceIterator.getPeptides(sequence, digestionPreferences, massMin, massMax);
            nSequences += peptides.size();

            for (Peptide peptide : peptides) {

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
                        SpecificAnnotationSettings specificAnnotationSettings = annotationSettings.getSpecificAnnotationPreferences(spectrumKey, peptideAssumption, identificationParameters.getSequenceMatchingPreferences(), identificationParameters.getPtmScoringPreferences().getSequenceMatchingPreferences());
                        Double score = hyperScore.getScore(peptide, spectrum, annotationSettings, specificAnnotationSettings, peptideSpectrumAnnotator);

                        if (score > 0) {

                            spectrumTitlesExport.add(spectrumTitle);

                            scores.add(score);

                            peptidesExport.add(peptideAssumption);

                            ArrayList<Double> spectrumScores = hyperScores.get(spectrumTitle);
                            if (spectrumScores == null) {
                                spectrumScores = new ArrayList<Double>();
                                hyperScores.put(spectrumTitle, spectrumScores);
                            }
                            spectrumScores.add(score);

                            Double ms1Deviation;
                            if (searchParameters.isPrecursorAccuracyTypePpm()) {
                                ms1Deviation = 1000000 * (mz - precursor.getMz()) / precursor.getMz();
                            } else {
                                ms1Deviation = mz - precursor.getMz();
                            }
                            ms1Deviations.add(ms1Deviation);
                            Integer matchesForSpectrum = nMatchesMap.get(spectrumTitle);
                            if (matchesForSpectrum == null) {
                                nMatchesMap.put(spectrumTitle, 1);
                            } else {
                                nMatchesMap.put(spectrumTitle, matchesForSpectrum + 1);
                            }
                            categories.add(category);

                            String psPeptide = psIds.get(spectrumTitle);
                            if (psPeptide != null && psPeptide.equals(peptide.getSequence())) {
                                newScores.put(spectrumTitle, score);
                            }
                        }
                    }
                }
            }

            cpt++;
            //System.out.println(protein.getAccession() + ": " + peptides.size());
            int progress = 100 * cpt / totalProgress;
            if (progress > lastProgress) {
                System.out.println("Searching: " + progress + "%");
                lastProgress = progress;
            }
        }

        HashMap<String, HashMap<Double, Double>> eValuesMap = new HashMap<String, HashMap<Double, Double>>(hyperScores.size());
        ArrayList<String> missingValues = new ArrayList<String>(spectrumFactory.getNSpectra());
        for (String spectrumTitle : hyperScores.keySet()) {
            ArrayList<Double> spectrumHyperScores = hyperScores.get(spectrumTitle);
            boolean scored = false;
            if (spectrumHyperScores.size() > 1) {
                boolean debug = spectrumTitle.contains("7074.7074");
                HashMap<Double, Double> eValues = hyperScore.getEValueHistogram(spectrumHyperScores, debug);
                if (eValues != null) {
                    scored = true;
                    eValuesMap.put(spectrumTitle, eValues);

                    Double newScore = newScores.get(spectrumTitle);
                    if (newScore != null) {
                        Double eValue = eValues.get(newScore);
                        newEValues.put(spectrumTitle, eValue);
                    }
                    cpt++;
                    int progress = 100 * cpt / totalProgress;
                    if (progress > lastProgress) {
                        System.out.println("Scoring: " + progress + "%");
                        lastProgress = progress;
                    }
                }
            }
            if (!scored) {
                missingValues.add(spectrumTitle);
            }
        }

        Double aDefault = hyperScore.getMendianA();
        Double bDefault = hyperScore.getMendianB();
        for (String spectrumTitle : missingValues) {
            ArrayList<Double> spectrumHyperScores = hyperScores.get(spectrumTitle);
            HashMap<Double, Double> eValues = hyperScore.getInterpolation(spectrumHyperScores, aDefault, bDefault);
            if (eValues != null) {
                eValuesMap.put(spectrumTitle, eValues);

                Double newScore = newScores.get(spectrumTitle);
                if (newScore != null) {
                    Double eValue = eValues.get(newScore);
                    newEValues.put(spectrumTitle, eValue);
                }
                cpt++;
                int progress = 100 * cpt / totalProgress;
                if (progress > lastProgress) {
                    System.out.println("Scoring: " + progress + "%");
                    lastProgress = progress;
                }
            }
        }

        hyperScore.close();

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
        bw.write("Spectrum_Tilte" + SEPARATOR + "MS1_deviation" + SEPARATOR + "MS2_Score" + SEPARATOR + "eValue" + SEPARATOR + "Category");
        bw.newLine();
        for (int i = 0; i < spectrumTitlesExport.size(); i++) {
            Double ms1Deviation = ms1Deviations.get(i);
            Double score = scores.get(i);
            String category = categories.get(i);
            String spectrumTitle = spectrumTitlesExport.get(i);
            Double eValue = null;
            HashMap<Double, Double> spectrumEValueMap = eValuesMap.get(spectrumTitle);
            if (spectrumEValueMap != null) {
                eValue = spectrumEValueMap.get(score);
            }
            spectrumTitle = URLEncoder.encode(spectrumTitle, "utf-8");
            bw.write(spectrumTitle + SEPARATOR + ms1Deviation + SEPARATOR + score + SEPARATOR + eValue + SEPARATOR + category);
            bw.newLine();
        }
        bw.close();

        File nMatchesFile = new File("C:\\Github\\onyase\\R\\nMatches.txt");
        bw = new BufferedWriter(new FileWriter(nMatchesFile));
        bw.write("Title" + SEPARATOR + "Retention Time" + SEPARATOR + "m/z" + SEPARATOR + "nMatches");
        bw.newLine();
        for (String spectrumTitle : spectrumFactory.getSpectrumTitles(fileName)) {
            Precursor precursor = spectrumFactory.getPrecursor(fileName, spectrumTitle);
            Integer matchesForSpectrum = nMatchesMap.get(spectrumTitle);
            if (matchesForSpectrum == null) {
                matchesForSpectrum = 0;
            }
            spectrumTitle = URLEncoder.encode(spectrumTitle, "utf-8");
            bw.write(spectrumTitle + SEPARATOR + precursor.getMz() + SEPARATOR + precursor.getRtInMinutes() + SEPARATOR + matchesForSpectrum);
            bw.newLine();
        }
        bw.close();

        File scoresComparisonFile = new File("C:\\Github\\onyase\\R\\scoresComparison.txt");
        bw = new BufferedWriter(new FileWriter(scoresComparisonFile));
        bw.write("Title" + SEPARATOR + "Sequence" + SEPARATOR + "PS score" + SEPARATOR + "PS confidence" + SEPARATOR + "New score" + SEPARATOR + "New eValue");
        bw.newLine();
        for (String spectrumTitle : spectrumFactory.getSpectrumTitles(fileName)) {
            String sequence = psIds.get(spectrumTitle);
            if (sequence != null) {
                String score = psScore.get(spectrumTitle);
                String confidence = psConfidence.get(spectrumTitle);
                Double newScore = newScores.get(spectrumTitle);
                Double eValue = newEValues.get(spectrumTitle);
                spectrumTitle = URLEncoder.encode(spectrumTitle, "utf-8");
                bw.write(spectrumTitle + SEPARATOR + sequence + SEPARATOR + score + SEPARATOR + confidence + SEPARATOR + newScore + SEPARATOR + eValue);
                bw.newLine();
            }
        }
        bw.close();

        File exportFile = new File("C:\\Users\\mvaudel\\Desktop\\test\\test onyase\\no_PTM\\testExample.psm");
        bw = new BufferedWriter(new FileWriter(exportFile));
        bw.write("# Version: 0.0.1");
        bw.newLine();
        bw.write("# Spectra: " + mgfFilePath);
        bw.newLine();
        bw.write("# Fasta: " + fastaFilePath);
        bw.newLine();
        bw.write("# Parameters: " + parametersFilePath);
        bw.newLine();
        bw.write("# ");
        bw.newLine();
        bw.write("# Spectrum_Tilte" + SEPARATOR + "Sequence" + SEPARATOR + "Charge" + SEPARATOR + "MS2_Score" + SEPARATOR + "eValue");
        bw.newLine();
        String lastSpectrum = "";
        for (int i = 0; i < spectrumTitlesExport.size(); i++) {
            Double score = scores.get(i);
            String spectrumTitle = spectrumTitlesExport.get(i);
            Double eValue = null;
            HashMap<Double, Double> spectrumEValueMap = eValuesMap.get(spectrumTitle);
            if (spectrumEValueMap != null) {
                eValue = spectrumEValueMap.get(score);
            }
            if (eValue != null) {
                PeptideAssumption peptideAssumption = peptidesExport.get(i);
                Peptide peptide = peptideAssumption.getPeptide();
                spectrumTitle = URLEncoder.encode(spectrumTitle, "utf-8");
                if (!spectrumTitle.equals(lastSpectrum)) {
                    bw.write(spectrumTitle);
                    lastSpectrum = spectrumTitle;
                }
                bw.write(SEPARATOR + peptide.getSequence() + SEPARATOR + peptideAssumption.getIdentificationCharge().value + SEPARATOR + score + SEPARATOR + eValue);
                bw.newLine();
            }
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

    private void loadPsData() throws IOException {

        psIds = new HashMap<String, String>();
        psScore = new HashMap<String, String>();
        psConfidence = new HashMap<String, String>();

        File psReportFile = new File(psReportFilePath);
        BufferedReader br = new BufferedReader(new FileReader(psReportFile));
        br.readLine();
        String line;
        while ((line = br.readLine()) != null) {
            String[] split = line.split("\t");
            String title = split[2];
            String sequence = split[1];
            String score = split[3];
            String confidence = split[4];
            String[] scoreSplit = score.split("\\(");
            score = scoreSplit[1];
            scoreSplit = score.split("\\)");
            score = scoreSplit[0];
            psIds.put(title, sequence);
            psScore.put(title, score);
            psConfidence.put(title, confidence);
        }

    }
}
