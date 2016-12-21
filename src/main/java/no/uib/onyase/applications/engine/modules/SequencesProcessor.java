package no.uib.onyase.applications.engine.modules;

import no.uib.onyase.applications.engine.modules.precursor_handling.ExclusionList;
import no.uib.onyase.applications.engine.modules.precursor_handling.PrecursorProcessor;
import com.compomics.util.exceptions.ExceptionHandler;
import com.compomics.util.experiment.biology.ElementaryElement;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.biology.ions.ElementaryIon;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.protein_sequences.ProteinSequenceIterator;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.experiment.identification.psm_scoring.psm_scores.HyperScore;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationSettings;
import com.compomics.util.experiment.identification.spectrum_annotation.SpecificAnnotationSettings;
import com.compomics.util.experiment.identification.spectrum_annotation.spectrum_annotators.PeptideSpectrumAnnotator;
import com.compomics.util.experiment.identification.spectrum_assumptions.PeptideAssumption;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.Spectrum;
import com.compomics.util.experiment.massspectrometry.SpectrumFactory;
import com.compomics.util.experiment.massspectrometry.indexes.PrecursorMap;
import com.compomics.util.experiment.massspectrometry.indexes.PrecursorMap.PrecursorWithTitle;
import com.compomics.util.preferences.DigestionPreferences;
import com.compomics.util.preferences.IdentificationParameters;
import com.compomics.util.waiting.WaitingHandler;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import no.uib.onyase.applications.engine.modules.ModificationProfileIterator.ModificationProfile;
import no.uib.onyase.applications.engine.modules.peptide_modification_iterators.MultipleModificationsIterators;
import no.uib.onyase.applications.engine.modules.peptide_modification_iterators.OverlappingModificationsIterator;
import no.uib.onyase.applications.engine.modules.peptide_modification_iterators.SingleModificationIterator;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshallerException;

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
     * @param removeZeros boolean indicating whether the peptide assumptions of
     * score zero should be removed
     * @param nThreads the number of threads to use
     * @param minMz the minimal m/z to consider
     * @param maxMz the maximal m/z to consider
     *
     * @return a map of all PSMs indexed by spectrum and peptide key
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading a file
     * @throws InterruptedException exception thrown if a threading issue
     * occurs.
     */
    public HashMap<String, HashMap<String, PeptideAssumption>> iterateSequences(String spectrumFileName, PrecursorProcessor precursorProcessor, IdentificationParameters identificationParameters, int maxX, boolean removeZeros, int nThreads, Double minMz, Double maxMz) throws IOException, InterruptedException {
        return iterateSequences(spectrumFileName, precursorProcessor, null, identificationParameters, maxX, removeZeros, nThreads, minMz, maxMz);
    }

    /**
     * Iterates all sequences and returns all PSMs found in a map indexed by
     * spectrum title and peptide key.
     *
     * @param spectrumFileName the name of the file to process
     * @param precursorProcessor the precursor processor
     * @param exclusionListFilePath path of the exclusion list to use
     * @param identificationParameters the identification parameters to sue
     * @param maxX the maximal number of Xs to allow in a peptide sequence
     * @param removeZeros boolean indicating whether the peptide assumptions of
     * score zero should be removed
     * @param nThreads the number of threads to use
     * @param minMz the minimal m/z to consider
     * @param maxMz the maximal m/z to consider
     *
     * @return a map of all PSMs indexed by spectrum and peptide key
     *
     * @throws InterruptedException exception thrown if a threading issue
     * occurs.
     * @throws IOException exception thrown whenever an error occurred while
     * reading a file
     */
    public HashMap<String, HashMap<String, PeptideAssumption>> iterateSequences(String spectrumFileName, PrecursorProcessor precursorProcessor, String exclusionListFilePath, IdentificationParameters identificationParameters, int maxX, boolean removeZeros, int nThreads, Double minMz, Double maxMz) throws InterruptedException, IOException {

        // Iterate all protein sequences in the factory and get the possible PSMs
        waitingHandler.setSecondaryProgressCounterIndeterminate(false);
        waitingHandler.setMaxSecondaryProgressCounter(sequenceFactory.getNSequences());
        SequenceFactory.ProteinIterator proteinIterator = sequenceFactory.getProteinIterator(false);
        ArrayList<SequenceProcessor> sequenceProcessors = new ArrayList<SequenceProcessor>(nThreads);
        ExecutorService pool = Executors.newFixedThreadPool(nThreads);
        for (int i = 0; i < nThreads; i++) {
            SequenceProcessor sequenceProcessor = new SequenceProcessor(proteinIterator, spectrumFileName, precursorProcessor, exclusionListFilePath, identificationParameters, maxX, removeZeros, minMz, maxMz);
            sequenceProcessors.add(sequenceProcessor);
            pool.submit(sequenceProcessor);
        }
        pool.shutdown();
        if (!pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS)) {
            waitingHandler.appendReport("Search timed out.", true, true);
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
         * Map of the keys of inspected peptides indexed by spectrum.
         */
        private HashMap<String, HashSet<String>> inspectedPeptides = new HashMap<String, HashSet<String>>();
        /**
         * An iterator for the possible modification profiles.
         */
        private ModificationProfileIterator modificationProfileIterator = new ModificationProfileIterator();
        /**
         * Map of modifications with overlapping targets.
         */
        private HashMap<String, HashSet<String>> overlappingModifications;
        /**
         * Boolean indicating whether the peptide assumptions of score zero
         * should be removed.
         */
        private boolean removeZeros;
        /**
         * A list of excluded m/z.
         */
        private ExclusionList exclusionList;

        /**
         * Constructor.
         *
         * @param proteinIterator the protein iterator to use to iterate the
         * different proteins
         * @param spectrumFileName the name of the spectrum file
         * @param precursorProcessor the precursor processor for this file
         * @param exclusionListFilePath path of the exclusion list to use
         * @param identificationParameters the identification parameters to use
         * @param maxX the maximal number of Xs to allow in a peptide
         * @param removeZeros boolean indicating whether the peptide assumptions
         * of score zero should be removed
         * @param minMz the minimal m/z to consider
         * @param maxMz the maximal m/z to consider
         *
         * @throws IOException exception thrown whenever an error occurred while
         * reading the exclusion list file
         */
        public SequenceProcessor(SequenceFactory.ProteinIterator proteinIterator, String spectrumFileName, PrecursorProcessor precursorProcessor, String exclusionListFilePath, IdentificationParameters identificationParameters, int maxX, boolean removeZeros, Double minMz, Double maxMz) throws IOException {
            this.proteinIterator = proteinIterator;
            this.spectrumFileName = spectrumFileName;
            this.precursorProcessor = precursorProcessor;
            this.identificationParameters = identificationParameters;
            SearchParameters searchParameters = identificationParameters.getSearchParameters();
            this.removeZeros = removeZeros;
            if (exclusionListFilePath != null) {
                exclusionList = new ExclusionList(exclusionListFilePath, searchParameters.getPrecursorAccuracy(), searchParameters.isPrecursorAccuracyTypePpm(), minMz, maxMz);
            } else {
                exclusionList = new ExclusionList(searchParameters.getPrecursorAccuracy(), searchParameters.isPrecursorAccuracyTypePpm(), minMz, maxMz);
            }
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
                int nVariableModifications = ptmSettings.getVariableModifications().size();
                HashMap<String, PTM> variablePtms = new HashMap<String, PTM>(nVariableModifications);
                HashMap<String, Double> variablePtmMasses = new HashMap<String, Double>(nVariableModifications);
                overlappingModifications = new HashMap<String, HashSet<String>>(nVariableModifications);
                for (String ptmName : ptmSettings.getVariableModifications()) {
                    PTM ptm = ptmFactory.getPTM(ptmName);
                    variablePtms.put(ptmName, ptm);
                    variablePtmMasses.put(ptmName, ptm.getMass());
                    if (ptm.getType() == PTM.MODAA) {
                        for (String ptmName2 : ptmSettings.getVariableModifications()) {
                            if (!ptmName.equals(ptmName2)) {
                                PTM ptm2 = ptmFactory.getPTM(ptmName2);
                                if (ptm2.getType() == PTM.MODAA) {
                                    HashSet<Character> aas1 = ptm.getPattern().getAminoAcidsAtTargetSet();
                                    for (Character aa2 : ptm2.getPattern().getAminoAcidsAtTarget()) {
                                        if (aas1.contains(aa2)) {
                                            HashSet<String> conflicts = overlappingModifications.get(ptmName);
                                            if (conflicts == null) {
                                                conflicts = new HashSet<String>(1);
                                                overlappingModifications.put(ptmName, conflicts);
                                            }
                                            conflicts.add(ptmName2);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Get objects for modification iteration
                ArrayList<String> orderedModificationsName = new ArrayList<String>(variablePtms.keySet());
                Collections.sort(orderedModificationsName);
                ArrayList<String> orderedPeptideModificationsName = new ArrayList<String>(variablePtms.size());
                HashMap<String, ArrayList<Integer>> possibleModificationSites = new HashMap<String, ArrayList<Integer>>(1);
                HashMap<String, Integer> possibleModificationOccurence = new HashMap<String, Integer>(1);
                ArrayList<ModificationMatch> modificationMatches = new ArrayList<ModificationMatch>(1);

                // Information from the spectrum processing
                Double massMin = precursorProcessor.getMassMin();
                Double massMax = precursorProcessor.getMassMax();
                PrecursorMap precursorMap = precursorProcessor.getPrecursorMap();

                // Settings needed for the scoring
                AnnotationSettings annotationSettings = identificationParameters.getAnnotationPreferences().clone();

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

                                // See if the precursor is in the exclusion list
                                if (!exclusionList.isExcluded(mz)) {

                                    // get the precursors at this m/z
                                    ArrayList<PrecursorMap.PrecursorWithTitle> precursorMatches = precursorMap.getMatchingSpectra(mz);

                                    // For every match, estimate the PSM score if not done previsouly
                                    for (PrecursorMap.PrecursorWithTitle precursorWithTitle : precursorMatches) {
                                        createPeptideAssumption(precursorWithTitle, peptideKey, peptide, charge, annotationSettings, removeZeros);
                                    }
                                }

                                // See if the peptide can be modified
                                possibleModificationSites.clear();
                                possibleModificationOccurence.clear();
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

                                        // See if the modified mass yields to any precursor match
                                        double modifedMass = mass + modificationProfile.getMass();
                                        mz = modifedMass / charge;

                                        // See if the precursor is in the exclusion list
                                        if (!exclusionList.isExcluded(mz)) {

                                            // get the precursors at this m/z
                                            ArrayList<PrecursorMap.PrecursorWithTitle> precursorMatches = precursorMap.getMatchingSpectra(mz);

                                            if (!precursorMatches.isEmpty()) {

                                                // Get the number of modifications
                                                HashMap<String, Integer> modificationOccurrence = modificationProfile.getModificationOccurence();

                                                // Create an iterator for the possible sites
                                                PeptideModificationsIterator peptideModificationsIterator;
                                                if (modificationOccurrence.size() == 1) {
                                                    String modificationName = modificationOccurrence.keySet().iterator().next();
                                                    ArrayList<Integer> possibleSites = possibleModificationSites.get(modificationName);
                                                    Integer occurrence = modificationOccurrence.get(modificationName);
                                                    peptideModificationsIterator = new SingleModificationIterator(possibleSites, occurrence, modificationName);
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
                                                    for (String modification : orderedModificationsName) {
                                                        if (modificationOccurrence.keySet().contains(modification)) {
                                                            orderedPeptideModificationsName.add(modification);
                                                        }
                                                    }
                                                    if (overlap) {
                                                        peptideModificationsIterator = new OverlappingModificationsIterator(modificationProfile, possibleModificationSites, orderedPeptideModificationsName);
                                                    } else {
                                                        peptideModificationsIterator = new MultipleModificationsIterators(modificationProfile, possibleModificationSites, orderedPeptideModificationsName);
                                                    }
                                                }

                                                // Iterate all possible sites
                                                while (peptideModificationsIterator.hasNext()) {

                                                    // Create a modified peptide
                                                    modificationMatches.clear();
                                                    HashMap<String, ArrayList<Integer>> modificationSitesMap = peptideModificationsIterator.next();
                                                    for (String modificationName : modificationSitesMap.keySet()) {
                                                        ArrayList<Integer> sites = modificationSitesMap.get(modificationName);
                                                        for (Integer site : sites) {
                                                            ModificationMatch modificationMatch = new ModificationMatch(modificationName, true, site);
                                                            modificationMatches.add(modificationMatch);
                                                        }
                                                    }
                                                    Peptide modifiedPeptide = new Peptide(peptide.getSequence(), modificationMatches);
                                                    String modifiedPeptideKey = modifiedPeptide.getKey();

                                                    // For every match, estimate the PSM score if not done previsouly
                                                    for (PrecursorMap.PrecursorWithTitle precursorWithTitle : precursorMatches) {
                                                        createPeptideAssumption(precursorWithTitle, modifiedPeptideKey, modifiedPeptide, charge, annotationSettings, removeZeros);
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

        /**
         * Returns a map of all PSMs found indexed by spectrum and peptide key.
         *
         * @return a map of all PSMs found
         */
        public HashMap<String, HashMap<String, PeptideAssumption>> getPsmMap() {
            return psmMap;
        }

        /**
         * Creates the peptide assumption for the given input and estimates the
         * score.
         *
         * @param precursorWithTitle the precursor with title for this spectrum
         * @param peptideKey the key for the peptide
         * @param peptide the peptide
         * @param charge the charge
         * @param annotationSettings the annotation settings
         * @param removeZeros boolean indicating whether the peptide assumptions
         * of score zero should be removed
         *
         * @throws InterruptedException
         * @throws ClassNotFoundException
         * @throws SQLException
         * @throws IOException exception thrown whenever an error occurred while
         * reading the file
         * @throws MzMLUnmarshallerException exception thrown whenever an error
         * occurred while parsing the mzML file
         * @throws InterruptedException exception thrown whenever a threading
         * issue occurred while mapping potential modification sites
         * @throws ClassNotFoundException exception thrown whenever an error
         * occurred while deserializing an object from the ProteinTree
         * @throws SQLException exception thrown whenever an error occurred
         * while interacting with the ProteinTree
         */
        private void createPeptideAssumption(PrecursorWithTitle precursorWithTitle, String peptideKey, Peptide peptide, int charge, AnnotationSettings annotationSettings, boolean removeZero) throws IOException, MzMLUnmarshallerException, InterruptedException, ClassNotFoundException, SQLException {

            String spectrumTitle = precursorWithTitle.spectrumTitle;
            HashMap<String, PeptideAssumption> spectrumMatches = psmMap.get(spectrumTitle);
            HashSet<String> inspectedPeptidesForSpectrum;
            if (spectrumMatches == null) {
                spectrumMatches = new HashMap<String, PeptideAssumption>();
                psmMap.put(spectrumTitle, spectrumMatches);
                inspectedPeptidesForSpectrum = new HashSet<String>();
                inspectedPeptides.put(spectrumTitle, inspectedPeptidesForSpectrum);
            } else {
                inspectedPeptidesForSpectrum = inspectedPeptides.get(spectrumTitle);
            }

            // If the PSM was not scored already, estimate the score
            if (!inspectedPeptidesForSpectrum.contains(peptideKey)) {
                inspectedPeptidesForSpectrum.add(peptideKey);
                String spectrumKey = Spectrum.getSpectrumKey(spectrumFileName, spectrumTitle);
                PeptideAssumption peptideAssumption = new PeptideAssumption(peptide, new Charge(Charge.PLUS, charge));
                MSnSpectrum spectrum = (MSnSpectrum) spectrumFactory.getSpectrum(spectrumFileName, spectrumTitle);
                SpecificAnnotationSettings specificAnnotationSettings = annotationSettings.getSpecificAnnotationPreferences(spectrumKey, peptideAssumption, identificationParameters.getSequenceMatchingPreferences(), identificationParameters.getPtmScoringPreferences().getSequenceMatchingPreferences());
                Double score = hyperScore.getScore(peptide, spectrum, annotationSettings, specificAnnotationSettings, peptideSpectrumAnnotator);
                if (!removeZero || score > 0) {
                    peptideAssumption.setRawScore(score);
                    peptideAssumption.setScore(score);

                    // Save the PSM in the map
                    spectrumMatches.put(peptideKey, peptideAssumption);
                }
            }
        }
    }
}
