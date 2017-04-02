package no.uib.onyase.applications.engine;

import no.uib.onyase.applications.engine.model.PeptideDraft;
import no.uib.onyase.applications.engine.modules.precursor_handling.ExclusionList;
import no.uib.onyase.applications.engine.modules.precursor_handling.PrecursorProcessor;
import no.uib.onyase.applications.engine.modules.*;
import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.experiment.biology.ElementaryElement;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.biology.ions.ElementaryIon;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.experiment.identification.protein_sequences.digestion.IteratorFactory;
import com.compomics.util.experiment.identification.protein_sequences.digestion.PeptideWithPosition;
import com.compomics.util.experiment.identification.protein_sequences.digestion.SequenceIterator;
import com.compomics.util.experiment.identification.psm_scoring.psm_scores.HyperScore;
import com.compomics.util.experiment.identification.psm_scoring.psm_scores.SnrScore;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationSettings;
import com.compomics.util.experiment.identification.spectrum_annotation.SimplePeptideAnnotator;
import com.compomics.util.experiment.identification.spectrum_annotation.SpecificAnnotationSettings;
import com.compomics.util.experiment.identification.spectrum_annotation.simple_annotators.FragmentAnnotator;
import com.compomics.util.experiment.identification.spectrum_assumptions.PeptideAssumption;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Spectrum;
import com.compomics.util.experiment.massspectrometry.SpectrumFactory;
import com.compomics.util.experiment.massspectrometry.indexes.PrecursorMap;
import com.compomics.util.experiment.massspectrometry.indexes.SpectrumIndex;
import com.compomics.util.maps.MapMutex;
import com.compomics.util.preferences.DigestionPreferences;
import com.compomics.util.preferences.IdentificationParameters;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import com.compomics.util.waiting.WaitingHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import no.uib.onyase.applications.engine.model.Psm;
import no.uib.onyase.applications.engine.modules.ModificationProfileIterator.ModificationProfile;
import no.uib.onyase.applications.engine.modules.peptide_modification_iterators.MultipleModificationsIterators;
import no.uib.onyase.applications.engine.modules.peptide_modification_iterators.OverlappingModificationsIterator;
import no.uib.onyase.applications.engine.modules.peptide_modification_iterators.SingleModificationIterator;
import no.uib.onyase.applications.engine.modules.scoring.PsmScore;
import no.uib.onyase.applications.engine.parameters.EngineParameters;
import org.apache.commons.math.MathException;

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
     * Map of the PSMs found.
     */
    private HashMap<String, HashMap<String, Psm>> psmMap;
    /**
     * Mutex for the scores map.
     */
    private MapMutex<String> scoresMapMutex = new MapMutex<String>();
    /**
     * Map of the peptides inspected for each spectrum.
     */
    private HashMap<String, HashSet<String>> peptidesInspected;

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
     * @param exclusionListFilePath path of the exclusion list to use
     * @param engineParameters the engine parameters
     * @param nThreads the number of threads to use
     *
     * @throws InterruptedException exception thrown if a threading issue
     * occurs.
     * @throws IOException exception thrown whenever an error occurred while
     * reading a file
     */
    public void iterateSequences(String spectrumFileName, PrecursorProcessor precursorProcessor, String exclusionListFilePath, EngineParameters engineParameters, int nThreads) throws InterruptedException, IOException {

        // Make sure that modifications are sorted alphabetically
        Arrays.sort(engineParameters.getVariableModifications());

        // Initialize the maps
        ArrayList<String> spectrumTitles = spectrumFactory.getSpectrumTitles(spectrumFileName);
        psmMap = new HashMap<String, HashMap<String, Psm>>(spectrumTitles.size());
        peptidesInspected = new HashMap<String, HashSet<String>>(spectrumTitles.size());
        for (String spectrumTitle : spectrumTitles) {
            psmMap.put(spectrumTitle, new HashMap<String, Psm>(2));
            peptidesInspected.put(spectrumTitle, new HashSet<String>(2));
        }

        // Set progress counters
        waitingHandler.setSecondaryProgressCounterIndeterminate(false);
        waitingHandler.setMaxSecondaryProgressCounter(sequenceFactory.getNSequences());

        // Make a pool of sequence processors
        SequenceFactory.ProteinIterator proteinIterator = sequenceFactory.getProteinIterator(false);
        ExecutorService pool = Executors.newFixedThreadPool(nThreads);
        for (int i = 0; i < nThreads; i++) {
            SequenceProcessor sequenceProcessor = new SequenceProcessor(proteinIterator, spectrumFileName, precursorProcessor, exclusionListFilePath, engineParameters);
            pool.submit(sequenceProcessor);
        }

        // Execute
        pool.shutdown();
        if (!pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS)) {
            waitingHandler.appendReport("Search timed out.", true, true);
        }

        // Gather the information saved by every sequence processor
        waitingHandler.setSecondaryProgressCounterIndeterminate(true);
    }

    /**
     * Returns a map of the PSMs for every peptide found for every spectrum.
     *
     * @return a map of the PSMs for every peptide found for every spectrum
     */
    public HashMap<String, HashMap<String, Psm>> getPsms() {
        return psmMap;
    }

    /**
     * Returns the index of a spectrum.
     *
     * @param spectrum the spectrum of interest
     * @param intensityThreshold the snp intensity threshold to use
     * @param mzTolerance the m/z tolerance to use
     * @param isPpm boolean indicating whether the m/z tolerance is in ppm
     *
     * @return the spectrum index
     *
     * @throws MathException exception thrown if an error occurred while
     * estimating the intensity threshold
     * @throws InterruptedException exception thrown if a thread was interrupted
     */
    private SpectrumIndex getSpectrumIndex(Spectrum spectrum, double intensityThreshold, double mzTolerance, boolean isPpm) throws MathException, InterruptedException {

        // See whether the index was previously stored
        SpectrumIndex spectrumIndex = new SpectrumIndex();
        spectrumIndex = (SpectrumIndex) spectrum.getUrParam(spectrumIndex);

        // Create new index
        if (spectrumIndex == null) {
            double intensityLimit = spectrum.getIntensityLimit(AnnotationSettings.IntensityThresholdType.snp, intensityThreshold);
            spectrumIndex = new SpectrumIndex(spectrum.getPeakMap(), intensityLimit, mzTolerance, isPpm);
            spectrum.addUrParam(spectrumIndex);
        }

        return spectrumIndex;
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
         * The engine parameters
         */
        private EngineParameters engineParameters;
        /**
         * The sequence iterator factory.
         */
        private IteratorFactory iteratorFactory;
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
        private PsmScore psmScore;
        /**
         * The object used to estimate the hyperscore.
         */
        private HyperScore hyperScoreEstimator;
        /**
         * The object used to estimate the SNR score.
         */
        private SnrScore snrScoreEstimator;
        /**
         * An iterator for the possible modification profiles.
         */
        private ModificationProfileIterator modificationProfileIterator = new ModificationProfileIterator();
        /**
         * Map of modifications with overlapping targets.
         */
        private HashMap<String, HashSet<String>> overlappingModifications;
        /**
         * A list of excluded m/z.
         */
        private ExclusionList exclusionList;
        /**
         * Cache for the mass contribution of protons at different charges.
         */
        private double[] protonContributionCache;
        /**
         * Cache for the mass contribution of neutrons at different isotopes.
         */
        private double[] neutronContributionCache;

        /**
         * Constructor.
         *
         * @param proteinIterator the protein iterator to use to iterate the
         * different proteins
         * @param spectrumFileName the name of the spectrum file
         * @param precursorProcessor the precursor processor for this file
         * @param exclusionListFilePath path of the exclusion list to use
         * @param engineParameters the parameters to use
         *
         * @throws IOException exception thrown whenever an error occurred while
         * reading the exclusion list file
         */
        public SequenceProcessor(SequenceFactory.ProteinIterator proteinIterator, String spectrumFileName, PrecursorProcessor precursorProcessor, String exclusionListFilePath, EngineParameters engineParameters) throws IOException {

            this.engineParameters = engineParameters;
            this.proteinIterator = proteinIterator;
            this.spectrumFileName = spectrumFileName;
            this.precursorProcessor = precursorProcessor;
            this.psmScore = engineParameters.getPsmScore();
            if (exclusionListFilePath != null) {
                exclusionList = new ExclusionList(exclusionListFilePath, engineParameters.getMs1Tolerance(), engineParameters.isMs1TolerancePpm(), engineParameters.getMs1MinMz(), engineParameters.getMs1MaxMz());
            } else {
                exclusionList = new ExclusionList(engineParameters.getMs1Tolerance(), engineParameters.isMs1TolerancePpm(), engineParameters.getMs1MinMz(), engineParameters.getMs1MaxMz());
            }
            iteratorFactory = new IteratorFactory(engineParameters.getFixedModifications(), engineParameters.getMaxX());
            switch (psmScore) {
                case snrScore:
                    snrScoreEstimator = new SnrScore();
                    break;
                case hyperscore:
                    hyperScoreEstimator = new HyperScore();
                    break;
                default:
                    throw new UnsupportedOperationException("Score " + psmScore + " not implemented.");
            }
        }

        @Override
        public void run() {

            try {

                // The search settings
                DigestionPreferences digestionPreferences = engineParameters.getDigestionPreferences();
                int minCharge = engineParameters.getMinCharge();
                int maxCharge = engineParameters.getMaxCharge();
                int minIsotope = engineParameters.getMinIsotopicCorrection();
                int maxIsotope = engineParameters.getMaxIsotopicCorrection();

                // Cache for the proton and isotope contribution
                protonContributionCache = new double[maxCharge - minCharge + 1];
                for (int charge = minCharge; charge <= maxCharge; charge++) {
                    protonContributionCache[charge - minCharge] = ElementaryIon.getProtonMassMultiple(charge);
                }
                neutronContributionCache = new double[maxIsotope - minIsotope + 1];
                for (int isotope = minIsotope; isotope <= maxIsotope; isotope++) {
                    neutronContributionCache[isotope] = isotope * ElementaryElement.getNeutronMassMultiple(isotope);
                }

                // Store information on the searched modifications
                HashMap<String, Integer> maxModificationsMap = engineParameters.getMaxModifications();
                String[] variableModificationsNames = engineParameters.getVariableModifications();
                int nVariableModifications = variableModificationsNames.length;
                PTM[] variableModifications = new PTM[nVariableModifications];
                HashMap<String, Double> variableModificationsMasses = new HashMap<String, Double>(nVariableModifications);
                int[] maxModifications = new int[nVariableModifications];
                overlappingModifications = new HashMap<String, HashSet<String>>(nVariableModifications);
                for (int i = 0; i < nVariableModifications; i++) {
                    String modificationName = variableModificationsNames[i];
                    PTM modification = ptmFactory.getPTM(modificationName);
                    variableModifications[i] = modification;
                    variableModificationsMasses.put(modificationName, modification.getMass());
                    if (modification.getType() == PTM.MODAA) {
                        for (int j = 0; j < nVariableModifications; j++) {
                            if (i != j) {
                                String modification2Name = variableModificationsNames[j];
                                PTM modification2 = ptmFactory.getPTM(modification2Name);
                                if (modification2.getType() == PTM.MODAA) {
                                    HashSet<Character> aas1 = modification.getPattern().getAminoAcidsAtTargetSet();
                                    for (Character aa2 : modification2.getPattern().getAminoAcidsAtTarget()) {
                                        if (aas1.contains(aa2)) {
                                            HashSet<String> conflicts = overlappingModifications.get(modificationName);
                                            if (conflicts == null) {
                                                conflicts = new HashSet<String>(1);
                                                overlappingModifications.put(modificationName, conflicts);
                                            }
                                            conflicts.add(modification2Name);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Integer value = maxModificationsMap.get(modificationName);
                    if (value != null) {
                        maxModifications[i] = value;
                    }
                }
                ArrayList<String> orderedPeptideModificationsName = new ArrayList<String>(nVariableModifications);

                // Information from the spectrum processing
                Double massMin = precursorProcessor.getMassMin();
                Double massMax = precursorProcessor.getMassMax();
                PrecursorMap precursorMap = precursorProcessor.getPrecursorMap();

                // Sequence settings for the keys of the peptides
                SequenceMatchingPreferences sequenceMatchingPreferences = SequenceMatchingPreferences.getDefaultSequenceMatching();

                // Iterate the proteins and store the possible PSMs
                while (proteinIterator.hasNext()) {

                    // Get a protein and find all possible peptides
                    Protein protein = proteinIterator.getNextProtein();
                    String sequence = protein.getSequence();
                    SequenceIterator sequenceIterator = iteratorFactory.getSequenceIterator(sequence, digestionPreferences, massMin, massMax);

                    // Iterate all peptides
                    PeptideWithPosition peptideWithPosition;
                    while ((peptideWithPosition = sequenceIterator.getNextPeptide()) != null) {

                        Peptide peptide = peptideWithPosition.getPeptide();
                        String peptideKey = peptide.getMatchingKey(sequenceMatchingPreferences);
                        Double peptideMass = peptide.getMass();
                        int indexOnProtein = peptideWithPosition.getPosition();
                        FragmentAnnotator fragmentAnnotator = new FragmentAnnotator(peptide, engineParameters.getDominantSeries());

                        // Iterate posible charges
                        for (int charge = minCharge; charge <= maxCharge; charge++) {

                            // Get the mass contribution of the proton
                            double protonContribution = protonContributionCache[charge - minCharge];

                            // Iterate possible isotopes
                            for (int isotope = minIsotope; isotope <= maxIsotope; isotope++) {

                                // See if we have a precursor for this m/z
                                double neutronContribution = neutronContributionCache[isotope - minIsotope];
                                double mass = peptideMass + protonContribution + neutronContribution;
                                double mz = mass / charge;

                                // See if the precursor is in the exclusion list
                                if (!exclusionList.isExcluded(mz)) {

                                    // get the precursors at this m/z
                                    ArrayList<PrecursorMap.PrecursorWithTitle> precursorMatches = precursorMap.getMatchingSpectra(mz);

                                    if (!precursorMatches.isEmpty()) {

                                        // Iterate all precursor matches
                                        for (PrecursorMap.PrecursorWithTitle precursorWithTitle : precursorMatches) {

                                            // Get the spectrum title
                                            String spectrumTitle = precursorWithTitle.spectrumTitle;

                                            // See if the peptide has already been identified for this spectrum
                                            HashSet<String> peptidesInspectedForSpectrum = peptidesInspected.get(spectrumTitle);
                                            if (!peptidesInspectedForSpectrum.contains(peptideKey)) {

                                                // Get the PSMs map for this spectrum
                                                HashMap<String, Psm> spectrumPsms = psmMap.get(spectrumTitle);

                                                // See if we already have a score
                                                if (!spectrumPsms.containsKey(peptideKey)) {

                                                    // Get the spectrum annotation
                                                    MSnSpectrum spectrum = (MSnSpectrum) spectrumFactory.getSpectrum(spectrumFileName, spectrumTitle);
                                                    SpectrumIndex spectrumIndex = getSpectrumIndex(spectrum, engineParameters.getMs2IntensityThreshold(), engineParameters.getMs2Tolerance(), engineParameters.isMs2TolerancePpm());
                                                    ArrayList<IonMatch> ionMatches = fragmentAnnotator.getIonMatches(spectrumIndex, charge);

                                                    // Retain only matches yielding fragment ions
                                                    boolean retainedPSM = false;
                                                    if (!ionMatches.isEmpty()) {

                                                        // Get the score
                                                        double score;
                                                        switch (psmScore) {
                                                            case hyperscore:
                                                                score = hyperScoreEstimator.getScore(peptide, charge, spectrum, ionMatches);
                                                                break;
                                                            case snrScore:
                                                                score = snrScoreEstimator.getScore(peptide, spectrum, ionMatches);
                                                                break;
                                                            default:
                                                                throw new UnsupportedOperationException("Score " + psmScore + " not implemented.");
                                                        }

                                                        // Retain only PSMs with a score
                                                        if (score > 0) {

                                                            // Create a PSM
                                                            Psm psm = new Psm(peptide, charge, score);

                                                            // Save PSM
                                                            scoresMapMutex.acquire(spectrumTitle);
                                                            spectrumPsms.put(peptideKey, psm);
                                                            scoresMapMutex.release(spectrumTitle);
                                                            retainedPSM = true;

                                                        }
                                                    }

                                                    // Keep track of the inspected PSMs
                                                    if (!retainedPSM) {

                                                        // Make sure that the list of inspected peptides does not get too long
                                                        if (peptidesInspectedForSpectrum.size() == 8192) {
                                                            peptidesInspectedForSpectrum.clear();
                                                        }

                                                        // Add the peptide to the inspected peptides list
                                                        peptidesInspectedForSpectrum.add(peptideKey);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // See if the peptide can be modified
                                HashMap<String, Integer[]> possibleModificationSites = new HashMap<String, Integer[]>(1);
                                HashMap<String, Integer> possibleModificationOccurence = new HashMap<String, Integer>(1);
                                for (int i = 0; i < nVariableModifications; i++) {
                                    PTM modification = variableModifications[i];
                                    ArrayList<Integer> ptmSites = peptide.getPotentialModificationSitesNoCombination(modification, protein.getSequence(), indexOnProtein);
                                    if (!ptmSites.isEmpty()) {
                                        String ptmName = modification.getName();
                                        possibleModificationSites.put(ptmName, ptmSites.toArray(new Integer[ptmSites.size()]));
                                        int maximalOccurrence = maxModifications[i];
                                        if (maximalOccurrence == 0) {
                                            maximalOccurrence = ptmSites.size();
                                        } else {
                                            maximalOccurrence = Math.min(ptmSites.size(), maximalOccurrence);
                                        }
                                        possibleModificationOccurence.put(ptmName, maximalOccurrence);
                                    }
                                }

                                if (!possibleModificationOccurence.isEmpty()) {

                                    // Get the possible modification combinations     
                                    ArrayList<ModificationProfile> modificationProfiles = modificationProfileIterator.getPossibleModificationProfiles(possibleModificationOccurence, variableModificationsMasses);

                                    // See if the modification profiles yield matches among the precursors
                                    for (ModificationProfile modificationProfile : modificationProfiles) {

                                        // See if the modified mass yields to any precursor match
                                        double modifedMass = mass + modificationProfile.getMass();
                                        mz = modifedMass / charge;

                                        // See if the precursor is in the exclusion list
                                        if (!exclusionList.isExcluded(mz)) {

                                            // get the precursors at this m/z
                                            ArrayList<PrecursorMap.PrecursorWithTitle> precursorMatches = precursorMap.getMatchingSpectra(mz);

                                            if (!precursorMatches.isEmpty()) {

                                                // Get the number of modifications
                                                HashMap<String, Integer> modificationOccurrence = new HashMap<String, Integer>(modificationProfile.getModificationOccurence());

                                                // Create PeptideDraft
                                                PeptideDraft peptideDraft = new PeptideDraft(peptide.getSequence(), charge, modificationOccurrence, possibleModificationSites);

                                                // Compute a key for this peptide to see if it was already inspected
                                                String genericModifiedPeptideKey = peptideDraft.getKey(variableModificationsNames, sequenceMatchingPreferences);

                                                // Get a precursor to see if the peptide was already inspected
                                                PrecursorMap.PrecursorWithTitle precursorWithTitle = precursorMatches.get(0);
                                                boolean newPeptide = true;

                                                // Get the spectrum title
                                                String spectrumTitle = precursorWithTitle.spectrumTitle;

                                                // See if the peptide has already been identified for this spectrum
                                                HashSet<String> peptidesInspectedForSpectrum = peptidesInspected.get(spectrumTitle);
                                                if (peptidesInspectedForSpectrum.contains(genericModifiedPeptideKey)) {
                                                    newPeptide = false;
                                                }

                                                // if new, iterate possible modification sites
                                                if (newPeptide) {

                                                    // Create an iterator for the possible sites
                                                    PeptideModificationsIterator peptideModificationsIterator;
                                                    if (modificationOccurrence.size() == 1) {
                                                        String modificationName = modificationOccurrence.keySet().iterator().next();
                                                        Integer[] possibleSites = possibleModificationSites.get(modificationName);
                                                        Integer occurrence = modificationOccurrence.get(modificationName);
                                                        peptideModificationsIterator = new SingleModificationIterator(possibleSites, occurrence, modificationName, engineParameters.getMaxSites());
                                                    } else {
                                                        boolean overlap = false;
                                                        for (String modification1 : modificationOccurrence.keySet()) {
                                                            HashSet<String> potentialConflicts = overlappingModifications.get(modification1);
                                                            if (potentialConflicts != null) {
                                                                for (String modification2 : modificationOccurrence.keySet()) {
                                                                    if (potentialConflicts.contains(modification2)) {
                                                                        overlap = true;
                                                                        break;
                                                                    }
                                                                }
                                                            }
                                                            if (overlap) {
                                                                break;
                                                            }
                                                        }

                                                        orderedPeptideModificationsName.clear();
                                                        for (String modification : variableModificationsNames) {
                                                            if (modificationOccurrence.get(modification) != null) {
                                                                orderedPeptideModificationsName.add(modification);
                                                            }
                                                        }
                                                        if (overlap) {
                                                            peptideModificationsIterator = new OverlappingModificationsIterator(modificationOccurrence, possibleModificationSites, orderedPeptideModificationsName, engineParameters.getMaxSites());
                                                        } else {
                                                            peptideModificationsIterator = new MultipleModificationsIterators(modificationOccurrence, possibleModificationSites, orderedPeptideModificationsName, engineParameters.getMaxSites());
                                                        }
                                                    }

                                                    // Go through all possibilities
                                                    boolean first = true;
                                                    while (peptideModificationsIterator.hasNext()) {

                                                        // Create a modified peptide
                                                        HashMap<String, int[]> modificationSitesMap = peptideModificationsIterator.next();
                                                        ArrayList<ModificationMatch> modificationMatches = new ArrayList<ModificationMatch>();
                                                        for (String modificationName : modificationSitesMap.keySet()) {
                                                            int[] sites = modificationSitesMap.get(modificationName);
                                                            for (Integer site : sites) {
                                                                ModificationMatch modificationMatch = new ModificationMatch(modificationName, true, site);
                                                                modificationMatch.setConfident(true);
                                                                modificationMatches.add(modificationMatch);
                                                            }
                                                        }
                                                        Peptide modifiedPeptide = new Peptide(peptideDraft.getSequence(), modificationMatches);
                                                        FragmentAnnotator modifiedFragmentAnnotator = new FragmentAnnotator(peptide, engineParameters.getDominantSeries());

                                                        // Iterate all precursor matches
                                                        for (PrecursorMap.PrecursorWithTitle precursorWithTitle2 : precursorMatches) {

                                                            // Get the spectrum title
                                                            spectrumTitle = precursorWithTitle2.spectrumTitle;

                                                            // Get the PSM map
                                                            HashMap<String, Psm> spectrumPsms = psmMap.get(spectrumTitle);

                                                            // Get the spectrum annotation
                                                            MSnSpectrum spectrum = (MSnSpectrum) spectrumFactory.getSpectrum(spectrumFileName, spectrumTitle);
                                                            SpectrumIndex spectrumIndex = getSpectrumIndex(spectrum, engineParameters.getMs2IntensityThreshold(), engineParameters.getMs2Tolerance(), engineParameters.isMs2TolerancePpm());
                                                            ArrayList<IonMatch> ionMatches = modifiedFragmentAnnotator.getIonMatches(spectrumIndex, charge);

                                                            // Retain only peptides that yield fragment ions
                                                            if (!ionMatches.isEmpty()) {

                                                                // Get the score
                                                                double score;
                                                                switch (psmScore) {
                                                                    case hyperscore:
                                                                        score = hyperScoreEstimator.getScore(peptide, charge, spectrum, ionMatches);
                                                                        break;
                                                                    case snrScore:
                                                                        score = snrScoreEstimator.getScore(peptide, spectrum, ionMatches);
                                                                        break;
                                                                    default:
                                                                        throw new UnsupportedOperationException("Score " + psmScore + " not implemented.");
                                                                }

                                                                // Retain only PSMs with a score
                                                                if (score > 0) {

                                                                    // Create a PSM
                                                                    Psm psm = new Psm(modifiedPeptide, charge, score);

                                                                    // Save PSM
                                                                    String modifiedPeptideKey = modifiedPeptide.getKey();
                                                                    scoresMapMutex.acquire(spectrumTitle);
                                                                    spectrumPsms.put(modifiedPeptideKey, psm);
                                                                    scoresMapMutex.release(spectrumTitle);
                                                                }
                                                            }

                                                            // Keep track of the inspected PSMs
                                                            if (first) {

                                                                // Make sure that the list of inspected peptides for this spectrum does not get too long
                                                                if (peptidesInspectedForSpectrum.size() == 8192) {
                                                                    peptidesInspectedForSpectrum.clear();
                                                                }

                                                                // Add the peptide to the inspected peptides list
                                                                peptidesInspectedForSpectrum.add(genericModifiedPeptideKey);

                                                                // Avoid repeating this for all isoforms
                                                                first = false;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
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
    }
}
