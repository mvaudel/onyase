package no.uib.onyase.scripts;

import com.compomics.util.experiment.biology.ElementaryElement;
import com.compomics.util.experiment.biology.EnzymeFactory;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.biology.ions.ElementaryIon;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
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
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshallerException;

/**
 *
 * @author Marc Vaudel
 */
public class ReviewFigure {

    private String mgfFilePath = "C:\\Projects\\PeptideShaker\\test files\\1 mgf\\qExactive01819.mgf";
    private String fastaFilePath = "C:\\Databases\\uniprot-human-reviewed-trypsin-november-2016_concatenated_target_decoy.fasta";
    private String parametersFilePath = "C:\\Projects\\PeptideShaker\\test files\\tutorial.par";

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

        initializeFactories();

        File mgfFile = new File(mgfFilePath);

        spectrumFactory.addSpectra(mgfFile);
        sequenceFactory.loadFastaFile(new File(fastaFilePath));

        IdentificationParameters identificationParameters = IdentificationParameters.getIdentificationParameters(new File(parametersFilePath));
        SearchParameters searchParameters = identificationParameters.getSearchParameters();
        DigestionPreferences digestionPreferences = searchParameters.getDigestionPreferences();
        // digestionPreferences.setSpecificity("Trypsin", DigestionPreferences.Specificity.semiSpecific);
        // digestionPreferences.setCleavagePreference(DigestionPreferences.CleavagePreference.unSpecific);
        searchParameters.setPrecursorAccuracy(0.02);
        searchParameters.setPrecursorAccuracyType(SearchParameters.MassAccuracyType.DA);
        int minCharge = searchParameters.getMinChargeSearched().value;
        int maxCharge = searchParameters.getMaxChargeSearched().value;

        String fileName = mgfFile.getName();
        PrecursorMap precursorMap = new PrecursorMap(spectrumFactory.getPrecursorMap(fileName), searchParameters.getFragmentIonAccuracy(), searchParameters.isPrecursorAccuracyTypePpm());
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
        
        FastXcorr fastXcorr = new FastXcorr();

        ProteinSequenceIterator proteinSequenceIterator = new ProteinSequenceIterator(searchParameters.getPtmSettings().getFixedModifications());

        int lastProgress = 0;
        int cpt = 0;
        SequenceFactory.ProteinIterator pi = sequenceFactory.getProteinIterator(false);
        long nMatches = 0, nSequences = 0;
        while (pi.hasNext()) {
            Protein protein = pi.getNextProtein();
          //Protein protein = sequenceFactory.getProtein("P11021");
            String sequence = protein.getSequence();
            ArrayList<Peptide> peptides = proteinSequenceIterator.getPeptides(sequence, digestionPreferences, massMin, massMax);
            nSequences += peptides.size();

            for (Peptide peptide : peptides) {
                if (peptide.getSequence().equals("LYGSAGPPPTGEEDTAEKDEL")) {
                    int debug = 1;
                }
                Double peptideMass = peptide.getMass();
                for (int charge = minCharge; charge <= maxCharge; charge++) {
                    PeptideAssumption peptideAssumption = new PeptideAssumption(peptide, new Charge(Charge.PLUS, charge));
                    double mz = (charge * ElementaryIon.proton.getTheoreticMass() + peptideMass) / charge;
                    ArrayList<PrecursorMap.PrecursorWithTitle> matches = precursorMap.getMatchingSpectra(mz);
                    nMatches += matches.size();
                    for (PrecursorMap.PrecursorWithTitle precursorWithTitle : matches) {
                        String spectrumTitle = precursorWithTitle.spectrumTitle;
                        String spectrumKey = Spectrum.getSpectrumKey(fileName, spectrumTitle);
                        MSnSpectrum spectrum = (MSnSpectrum) spectrumFactory.getSpectrum(fileName, spectrumTitle);
                        AnnotationSettings annotationSettings = identificationParameters.getAnnotationPreferences();
                        SpecificAnnotationSettings specificAnnotationSettings = annotationSettings.getSpecificAnnotationPreferences(spectrumKey, peptideAssumption, identificationParameters.getSequenceMatchingPreferences(), identificationParameters.getPtmScoringPreferences().getSequenceMatchingPreferences());
                        Double score = fastXcorr.getScore(peptide, spectrum, annotationSettings, specificAnnotationSettings, peptideSpectrumAnnotator);
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
        
        System.out.println("Peptides: " + nSequences + ", Matches: " + nMatches);

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
