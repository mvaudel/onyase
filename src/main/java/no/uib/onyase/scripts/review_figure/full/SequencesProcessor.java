package no.uib.onyase.scripts.review_figure.full;

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
import com.compomics.util.experiment.identification.protein_sequences.ProteinSequenceIterator;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.experiment.identification.psm_scoring.psm_scores.HyperScore;
import com.compomics.util.experiment.identification.spectrum_annotation.spectrum_annotators.PeptideSpectrumAnnotator;
import com.compomics.util.experiment.massspectrometry.SpectrumFactory;
import com.compomics.util.experiment.massspectrometry.indexes.PrecursorMap;
import com.compomics.util.preferences.DigestionPreferences;
import com.compomics.util.preferences.IdentificationParameters;
import com.compomics.util.waiting.WaitingHandler;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
     * @param minMz the minimal m/z to consider
     * @param maxMz the maximal m/z to consider
     * @param maxModifications the maximal number of modifications
     *
     * @return a map of all PSMs indexed by spectrum and peptide key
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading a file
     * @throws InterruptedException exception thrown if a threading issue
     * occurs.
     */
    public HashMap<String, HashMap<String, PeptideDraft>> iterateSequences(String spectrumFileName, PrecursorProcessor precursorProcessor, IdentificationParameters identificationParameters, int maxX, int nThreads, Double minMz, Double maxMz, HashMap<String, Integer> maxModifications) throws IOException, InterruptedException {
        return iterateSequences(spectrumFileName, precursorProcessor, null, identificationParameters, maxX, nThreads, minMz, maxMz, maxModifications);
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
     * @param nThreads the number of threads to use
     * @param minMz the minimal m/z to consider
     * @param maxMz the maximal m/z to consider
     * @param maxModifications the maximal number of modifications
     *
     * @return a map of all PSMs indexed by spectrum and peptide key
     *
     * @throws InterruptedException exception thrown if a threading issue
     * occurs.
     * @throws IOException exception thrown whenever an error occurred while
     * reading a file
     */
    public HashMap<String, HashMap<String, PeptideDraft>> iterateSequences(String spectrumFileName, PrecursorProcessor precursorProcessor, String exclusionListFilePath, IdentificationParameters identificationParameters, int maxX, int nThreads, Double minMz, Double maxMz, HashMap<String, Integer> maxModifications) throws InterruptedException, IOException {

        // Iterate all protein sequences in the factory and get the possible PSMs
        waitingHandler.setSecondaryProgressCounterIndeterminate(false);
        waitingHandler.setMaxSecondaryProgressCounter(sequenceFactory.getNSequences());
        SequenceFactory.ProteinIterator proteinIterator = sequenceFactory.getProteinIterator(false);
        ArrayList<SequenceProcessor> sequenceProcessors = new ArrayList<SequenceProcessor>(nThreads);
        ExecutorService pool = Executors.newFixedThreadPool(nThreads);
        for (int i = 0; i < nThreads; i++) {
            SequenceProcessor sequenceProcessor = new SequenceProcessor(proteinIterator, spectrumFileName, precursorProcessor, exclusionListFilePath, identificationParameters, maxX, minMz, maxMz, maxModifications);
            sequenceProcessors.add(sequenceProcessor);
            pool.submit(sequenceProcessor);
        }
        pool.shutdown();
        if (!pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS)) {
            waitingHandler.appendReport("Search timed out.", true, true);
        }

        // Gather all PSMs
        waitingHandler.setSecondaryProgressCounterIndeterminate(true);
        HashMap<String, HashMap<String, PeptideDraft>> psmMap = sequenceProcessors.get(0).getPsmMap();
        for (int i = 1; i < sequenceProcessors.size(); i++) {
            HashMap<String, HashMap<String, PeptideDraft>> tempMap = sequenceProcessors.get(i).getPsmMap();
            for (String spectrumTitle : tempMap.keySet()) {
                HashMap<String, PeptideDraft> newAssumptions = tempMap.get(spectrumTitle);
                HashMap<String, PeptideDraft> currentAssumptions = psmMap.get(spectrumTitle);
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
        private HashMap<String, HashMap<String, PeptideDraft>> psmMap = new HashMap<String, HashMap<String, PeptideDraft>>();
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
         * A list of excluded m/z.
         */
        private ExclusionList exclusionList;
        /**
         * The maximal number of modifications
         */
        private HashMap<String, Integer> maxModifications;

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
         * @param maxModifications the maximal number of modifications
         *
         * @throws IOException exception thrown whenever an error occurred while
         * reading the exclusion list file
         */
        public SequenceProcessor(SequenceFactory.ProteinIterator proteinIterator, String spectrumFileName, PrecursorProcessor precursorProcessor, String exclusionListFilePath, IdentificationParameters identificationParameters, int maxX, Double minMz, Double maxMz, HashMap<String, Integer> maxModifications) throws IOException {
            this.proteinIterator = proteinIterator;
            this.spectrumFileName = spectrumFileName;
            this.precursorProcessor = precursorProcessor;
            this.identificationParameters = identificationParameters;
            SearchParameters searchParameters = identificationParameters.getSearchParameters();
            if (exclusionListFilePath != null) {
                exclusionList = new ExclusionList(exclusionListFilePath, searchParameters.getPrecursorAccuracy(), searchParameters.isPrecursorAccuracyTypePpm(), minMz, maxMz);
            } else {
                exclusionList = new ExclusionList(searchParameters.getPrecursorAccuracy(), searchParameters.isPrecursorAccuracyTypePpm(), minMz, maxMz);
            }
            proteinSequenceIterator = new ProteinSequenceIterator(identificationParameters.getSearchParameters().getPtmSettings().getFixedModifications(), maxX);
            if (maxModifications != null) {
                this.maxModifications = new HashMap<String, Integer>(maxModifications);
            } else {
                this.maxModifications = new HashMap<String, Integer>(0);
            }
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
                ArrayList<String> orderedModificationsNameList = new ArrayList<String>(variablePtms.keySet());
                Collections.sort(orderedModificationsNameList);
                String[] orderedModifications = orderedModificationsNameList.toArray(new String[orderedModificationsNameList.size()]);

                // Information from the spectrum processing
                Double massMin = precursorProcessor.getMassMin();
                Double massMax = precursorProcessor.getMassMax();
                PrecursorMap precursorMap = precursorProcessor.getPrecursorMap();

                // Iterate the proteins and store the possible PSMs
                while (proteinIterator.hasNext()) {

                    // Get a protein and find all possible peptides
                    Protein protein = proteinIterator.getNextProtein();
                    String sequence = protein.getSequence();
                    ArrayList<ProteinSequenceIterator.PeptideWithPosition> peptides = proteinSequenceIterator.getPeptides(sequence, digestionPreferences, massMin, massMax);

                    boolean isDecoy = sequenceFactory.isDecoyAccession(protein.getAccession());

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

                                    if (!precursorMatches.isEmpty()) {

                                        // Iterate all precursor matches
                                        for (PrecursorMap.PrecursorWithTitle precursorWithTitle : precursorMatches) {

                                            // Get the matches for this precursor
                                            String spectrumTitle = precursorWithTitle.spectrumTitle;
                                            HashMap<String, PeptideDraft> peptideDrafts = psmMap.get(spectrumTitle);

                                            if (peptideDrafts == null) {
                                                // No matches yet, add new map
                                                peptideDrafts = new HashMap<String, PeptideDraft>();
                                                psmMap.put(spectrumTitle, peptideDrafts);

                                                // Create PeptideDraft and add it to the map
                                                PeptideDraft peptideDraft = new PeptideDraft(peptide.getSequence(), charge, null, null, isDecoy);
                                                peptideDrafts.put(peptideKey, peptideDraft);
                                            } else {
                                                // Matches found, see if this one was already found
                                                PeptideDraft previousDraft = peptideDrafts.get(peptideKey);
                                                if (previousDraft == null) {
                                                    // Create PeptideDraft and add it to the map
                                                    PeptideDraft peptideDraft = new PeptideDraft(peptide.getSequence(), charge, null, null, isDecoy);
                                                    peptideDrafts.put(peptideKey, peptideDraft);
                                                } else {
                                                    // Update the match
                                                    previousDraft.setTargetDecoy(isDecoy);
                                                }
                                            }
                                        }
                                    }
                                }

                                // See if the peptide can be modified
                                HashMap<String, Integer[]> possibleModificationSites = new HashMap<String, Integer[]>(1);
                                HashMap<String, Integer> possibleModificationOccurence = new HashMap<String, Integer>(1);
                                for (PTM ptm : variablePtms.values()) {
                                    ArrayList<Integer> ptmSites = peptide.getPotentialModificationSitesNoCombination(ptm, protein.getSequence(), indexOnProtein);
                                    if (!ptmSites.isEmpty()) {
                                        String ptmName = ptm.getName();
                                        possibleModificationSites.put(ptmName, ptmSites.toArray(new Integer[ptmSites.size()]));
                                        Integer maximalOccurrence = maxModifications.get(ptmName);
                                        if (maximalOccurrence == null) {
                                            maximalOccurrence = ptmSites.size();
                                        } else {
                                            maximalOccurrence = Math.min(ptmSites.size(), maximalOccurrence);
                                        }
                                        possibleModificationOccurence.put(ptmName, maximalOccurrence);
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
                                                HashMap<String, Integer> modificationOccurrence = new HashMap<String, Integer>(modificationProfile.getModificationOccurence());

                                                // Create PeptideDraft
                                                PeptideDraft peptideDraft = new PeptideDraft(peptide.getSequence(), charge, modificationOccurrence, possibleModificationSites, isDecoy);

                                                // Compute a key for this peptide to see if it was already found
                                                String modifiedPeptideKey = peptideDraft.getKey(orderedModifications);

                                                // Iterate all precursor matches
                                                for (PrecursorMap.PrecursorWithTitle precursorWithTitle : precursorMatches) {

                                                    // Get the matches for this precursor
                                                    String spectrumTitle = precursorWithTitle.spectrumTitle;
                                                    HashMap<String, PeptideDraft> peptideDrafts = psmMap.get(spectrumTitle);

                                                    if (peptideDrafts == null) {
                                                        // No matches yet, add new map
                                                        peptideDrafts = new HashMap<String, PeptideDraft>();
                                                        peptideDrafts.put(modifiedPeptideKey, peptideDraft);
                                                        psmMap.put(spectrumTitle, peptideDrafts);
                                                    } else {
                                                        // Matches found, see if this one was already found
                                                        PeptideDraft previousDraft = peptideDrafts.get(modifiedPeptideKey);
                                                        if (previousDraft == null) {
                                                            // Add the match to the list of matches for this precursor
                                                            peptideDrafts.put(modifiedPeptideKey, peptideDraft);
                                                        } else {
                                                            // Update the match
                                                            previousDraft.setTargetDecoy(isDecoy);
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

        /**
         * Returns a map of all PSMs found indexed by spectrum and peptide key.
         *
         * @return a map of all PSMs found
         */
        public HashMap<String, HashMap<String, PeptideDraft>> getPsmMap() {
            return psmMap;
        }
    }

}
