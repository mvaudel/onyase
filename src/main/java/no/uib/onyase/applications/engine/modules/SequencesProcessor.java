package no.uib.onyase.applications.engine.modules;

import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.experiment.biology.ElementaryElement;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.biology.ions.ElementaryIon;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.matches.SpectrumMatch;
import com.compomics.util.experiment.identification.protein_sequences.ProteinSequenceIterator;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.experiment.identification.psm_scoring.psm_scores.HyperScore;
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
import com.compomics.util.preferences.SequenceMatchingPreferences;
import com.compomics.util.waiting.WaitingHandler;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import no.uib.onyase.applications.engine.modules.ModificationProfileIterator.ModificationProfile;

/**
 * The sequences processor runs multiple sequences iterators on the database in
 * the sequence factory.
 *
 * @author Marc Vaudel
 */
public class SequencesProcessor {

    /**
     * The sequence factory.
     */
    private SequenceFactory sequenceFactory = SequenceFactory.getInstance();
    /**
     * The spectrum factory.
     */
    private SpectrumFactory spectrumFactory = SpectrumFactory.getInstance();
    /**
     * The modifications factory.
     */
    private PTMFactory ptmFactory = PTMFactory.getInstance();
    /**
     * A handler for the exceptions.
     */
    private ExceptionHandler exceptionHandler;
    /**
     * A waiting handler providing feedback to the user and allowing canceling
     * the process.
     */
    private WaitingHandler waitingHandler;

    /**
     * Constructor.
     *
     * @param waitingHandler a waiting handler providing feedback to the user
     * and allowing canceling the process
     * @param exceptionHandler a handler for the exceptions
     */
    public SequencesProcessor(WaitingHandler waitingHandler, ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
        this.waitingHandler = waitingHandler;
    }

    /**
     * Iterates all sequences and returns all PSMs found in a map indexed by
     * spectrum title and peptide key.
     *
     * @param spectrumFileName the name of the file to process
     * @param precursorProcessor the precursor processor
     * @param identificationParameters the identification parameters to sue
     * @param maxX the maximal number of Xs to allow in a peptide sequence
     * @param nThreads the number of threads to use
     *
     * @return a map of all PSMs indexed by spectrum and peptide key
     *
     * @throws FileNotFoundException exception thrown if the fasta file is not
     * found
     * @throws InterruptedException exception thrown if a threading issue
     * occurs.
     */
    public HashMap<String, HashMap<String, PeptideAssumption>> iterateSequences(String spectrumFileName, PrecursorProcessor precursorProcessor, IdentificationParameters identificationParameters, int maxX, int nThreads) throws FileNotFoundException, InterruptedException {

        // Iterate all protein sequences in the factory and get the possible PSMs
        waitingHandler.setSecondaryProgressCounterIndeterminate(false);
        waitingHandler.setMaxSecondaryProgressCounter(sequenceFactory.getNSequences());
        SequenceFactory.ProteinIterator proteinIterator = sequenceFactory.getProteinIterator(false);
        ArrayList<SequenceProcessor> sequenceProcessors = new ArrayList<SequenceProcessor>(nThreads);
        ExecutorService pool = Executors.newFixedThreadPool(nThreads);
        for (int i = 0; i < nThreads; i++) {
            SequenceProcessor sequenceProcessor = new SequenceProcessor(proteinIterator, spectrumFileName, precursorProcessor, identificationParameters, maxX);
            sequenceProcessors.add(sequenceProcessor);
            pool.submit(sequenceProcessor);
        }
        pool.shutdown();
        if (!pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS)) {
            waitingHandler.appendReport("Mapping tags timed out. Please contact the developers.", true, true);
        }

        // Gather all PSMs
        waitingHandler.setSecondaryProgressCounterIndeterminate(true);
        HashMap<String, HashMap<String, PeptideAssumption>> psmMap = sequenceProcessors.get(0).getPsmMap();
        for (int i = 1; i < sequenceProcessors.size(); i++) {
            HashMap<String, HashMap<String, PeptideAssumption>> tempMap = sequenceProcessors.get(i).getPsmMap();
            for (String spectrumTitle : tempMap.keySet()) {
                HashMap<String, PeptideAssumption> newAssumptions = tempMap.get(spectrumTitle);
                HashMap<String, PeptideAssumption> currentAssumptions = psmMap.get(spectrumTitle);
                if (currentAssumptions != null) {
                    currentAssumptions.putAll(newAssumptions);
                } else {
                    psmMap.put(spectrumTitle, newAssumptions);
                }
            }
        }

        return psmMap;
    }

    /**
     * Private runnable to process a sequence.
     */
    private class SequenceProcessor implements Runnable {

        /**
         * The protein iterator.
         */
        private SequenceFactory.ProteinIterator proteinIterator;
        /**
         * The identification parameters
         */
        private IdentificationParameters identificationParameters;
        /**
         * The sequence iterator.
         */
        private ProteinSequenceIterator proteinSequenceIterator;
        /**
         * The precursor map to use.
         */
        private PrecursorProcessor precursorProcessor;
        /**
         * The name of the spectrum file to compare the sequences to.
         */
        private String spectrumFileName;
        /**
         * The score to use.
         */
        private HyperScore hyperScore = new HyperScore();
        /**
         * A spectrum annotator.
         */
        private PeptideSpectrumAnnotator peptideSpectrumAnnotator = new PeptideSpectrumAnnotator();
        /**
         * All peptide assumptions indexed by spectrum and by peptide key.
         */
        private HashMap<String, HashMap<String, PeptideAssumption>> psmMap = new HashMap<String, HashMap<String, PeptideAssumption>>();
        /**
         * An iterator for the possible modification profiles.
         */
        private ModificationProfileIterator modificationProfileIterator = new ModificationProfileIterator();

        /**
         * Constructor.
         *
         * @param proteinIterator the protein iterator to use to iterate the
         * different proteins
         * @param spectrumFileName the name of the spectrum file
         * @param precursorProcessor the precursor processor for this file
         * @param identificationParameters the identification parameters to use
         * @param maxX the maximal number of Xs to allow in a peptide
         */
        public SequenceProcessor(SequenceFactory.ProteinIterator proteinIterator, String spectrumFileName, PrecursorProcessor precursorProcessor, IdentificationParameters identificationParameters, int maxX) {
            this.proteinIterator = proteinIterator;
            this.spectrumFileName = spectrumFileName;
            this.precursorProcessor = precursorProcessor;
            this.identificationParameters = identificationParameters;
            proteinSequenceIterator = new ProteinSequenceIterator(identificationParameters.getSearchParameters().getPtmSettings().getFixedModifications(), maxX);
        }

        @Override
        public void run() {

            try {

                // The search settings
                SearchParameters searchParameters = identificationParameters.getSearchParameters();
                DigestionPreferences digestionPreferences = searchParameters.getDigestionPreferences();
                int minCharge = searchParameters.getMinChargeSearched().value;
                int maxCharge = searchParameters.getMaxChargeSearched().value;
                int minIsotope = searchParameters.getMinIsotopicCorrection();
                int maxIsotope = searchParameters.getMaxIsotopicCorrection();
                PtmSettings ptmSettings = searchParameters.getPtmSettings();
                HashMap<String, PTM> variablePtms = new HashMap<String, PTM>(ptmSettings.getVariableModifications().size());
                HashMap<String, Double> variablePtmMasses = new HashMap<String, Double>(ptmSettings.getVariableModifications().size());
                for (String ptmName : ptmSettings.getVariableModifications()) {
                    PTM ptm = ptmFactory.getPTM(ptmName);
                    variablePtms.put(ptmName, ptm);
                    variablePtmMasses.put(ptmName, ptm.getMass());
                }

                // Information from the spectrum processing
                Double massMin = precursorProcessor.getMassMin();
                Double massMax = precursorProcessor.getMassMax();
                PrecursorMap precursorMap = precursorProcessor.getPrecursorMap();

                // Settings needed for the scoring
                AnnotationSettings annotationSettings = identificationParameters.getAnnotationPreferences();

                // Iterate the proteins and store the possible PSMs
                while (proteinIterator.hasNext()) {

                    // Get a protein and find all possible peptides
                    Protein protein = proteinIterator.getNextProtein();
                    String sequence = protein.getSequence();
                    ArrayList<ProteinSequenceIterator.PeptideWithPosition> peptides = proteinSequenceIterator.getPeptides(sequence, digestionPreferences, massMin, massMax);

                    // Iterate all peptides
                    for (ProteinSequenceIterator.PeptideWithPosition peptideWithPosition : peptides) {

                        Peptide peptide = peptideWithPosition.getPeptide();
                        String peptideKey = peptide.getKey();
                        Double peptideMass = peptide.getMass();
                        int indexOnProtein = peptideWithPosition.getPosition();

                        // Iterate posible charges
                        for (int charge = minCharge; charge <= maxCharge; charge++) {

                            double protonContribution = charge * ElementaryIon.proton.getTheoreticMass();

                            // Iterate possible isotopes
                            for (int isotope = minIsotope; isotope <= maxIsotope; isotope++) {

                                // See if we have a precursor for this m/z
                                double mass;
                                if (isotope == 0) {
                                    mass = protonContribution + peptideMass;
                                } else {
                                    double isotopeContribution = isotope * ElementaryElement.neutron.getMass();
                                    mass = protonContribution + isotopeContribution + peptideMass;
                                }
                                double mz = mass / charge;
                                ArrayList<PrecursorMap.PrecursorWithTitle> precursorMatches = precursorMap.getMatchingSpectra(mz);

                                // For every match, estimate the PSM score if not done previsouly
                                for (PrecursorMap.PrecursorWithTitle precursorWithTitle : precursorMatches) {

                                    String spectrumTitle = precursorWithTitle.spectrumTitle;
                                    HashMap<String, PeptideAssumption> spectrumMatches = psmMap.get(spectrumTitle);
                                    if (spectrumMatches == null) {
                                        spectrumMatches = new HashMap<String, PeptideAssumption>();
                                        psmMap.put(spectrumTitle, spectrumMatches);
                                    }

                                    // If the PSM was not scored already, estimate the score
                                    if (!spectrumMatches.containsKey(peptideKey)) {
                                        String spectrumKey = Spectrum.getSpectrumKey(spectrumFileName, spectrumTitle);
                                        PeptideAssumption peptideAssumption = new PeptideAssumption(peptide, new Charge(Charge.PLUS, charge));
                                        MSnSpectrum spectrum = (MSnSpectrum) spectrumFactory.getSpectrum(spectrumFileName, spectrumTitle);
                                        SpecificAnnotationSettings specificAnnotationSettings = annotationSettings.getSpecificAnnotationPreferences(spectrumKey, peptideAssumption, identificationParameters.getSequenceMatchingPreferences(), identificationParameters.getPtmScoringPreferences().getSequenceMatchingPreferences());
                                        Double score = hyperScore.getScore(peptide, spectrum, annotationSettings, specificAnnotationSettings, peptideSpectrumAnnotator);
                                        peptideAssumption.setRawScore(score);
                                        peptideAssumption.setScore(score);

                                        // Save the PSM in the map
                                        spectrumMatches.put(peptideKey, peptideAssumption);
                                    }
                                }

                                // See if the peptide can be modified
                                HashMap<String, ArrayList<Integer>> possibleModificationSites = new HashMap<String, ArrayList<Integer>>(0);
                                HashMap<String, Integer> possibleModificationOccurence = new HashMap<String, Integer>(0);
                                for (PTM ptm : variablePtms.values()) {
                                    ArrayList<Integer> ptmSites = peptide.getPotentialModificationSitesNoCombination(ptm, protein.getSequence(), indexOnProtein);
                                    if (!ptmSites.isEmpty()) {
                                        possibleModificationSites.put(ptm.getName(), ptmSites);
                                        possibleModificationOccurence.put(ptm.getName(), ptmSites.size());
                                    }
                                }

                                if (!possibleModificationOccurence.isEmpty()) {

                                    // Get the possible modification combinations    
                                    ArrayList<ModificationProfile> modificationProfiles = modificationProfileIterator.getPossibleModificationProfiles(possibleModificationOccurence, variablePtmMasses);

                                    // See if the modification profiles yield matches among the precursors
                                    for (ModificationProfile modificationProfile : modificationProfiles) {
                                        double modifedMass = mass + modificationProfile.getMass();
                                        mz = modifedMass / charge;
                                        precursorMatches = precursorMap.getMatchingSpectra(mz);
                                    }
                                }
                            }
                        }
                    }
                    if (waitingHandler.isRunCanceled()) {
                        return;
                    } else {
                        waitingHandler.increaseSecondaryProgressCounter();
                    }
                }
            } catch (Exception e) {
                if (!waitingHandler.isRunCanceled()) {
                    exceptionHandler.catchException(e);
                    waitingHandler.setRunCanceled();
                }
            }
        }

        /**
         * Returns a map of all PSMs found indexed by spectrum and peptide key.
         *
         * @return a map of all PSMs found
         */
        public HashMap<String, HashMap<String, PeptideAssumption>> getPsmMap() {
            return psmMap;
        }
    }
}
