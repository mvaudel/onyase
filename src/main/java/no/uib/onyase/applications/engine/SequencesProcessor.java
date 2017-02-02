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
import com.compomics.util.experiment.identification.protein_sequences.ProteinSequenceIterator;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.experiment.identification.psm_scoring.psm_scores.HyperScore;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationSettings;
import com.compomics.util.experiment.identification.spectrum_annotation.SpecificAnnotationSettings;
import com.compomics.util.experiment.identification.spectrum_annotation.spectrum_annotators.PeptideSpectrumAnnotator;
import com.compomics.util.experiment.identification.spectrum_assumptions.PeptideAssumption;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.SpectrumFactory;
import com.compomics.util.experiment.massspectrometry.indexes.PrecursorMap;
import com.compomics.util.maps.MapMutex;
import com.compomics.util.preferences.DigestionPreferences;
import com.compomics.util.preferences.IdentificationParameters;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import no.uib.onyase.applications.engine.export.TextExporter;
import no.uib.onyase.applications.engine.modules.ModificationProfileIterator.ModificationProfile;
import no.uib.onyase.applications.engine.modules.peptide_modification_iterators.MultipleModificationsIterators;
import no.uib.onyase.applications.engine.modules.peptide_modification_iterators.OverlappingModificationsIterator;
import no.uib.onyase.applications.engine.modules.peptide_modification_iterators.SingleModificationIterator;

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
     * Map of the PSM scores.
     */
    private HashMap<String, HashMap<String, Integer>> scoresMap;
    /**
     * Mutex for the scores map.
     */
    private MapMutex<String> scoresMapMutex = new MapMutex<String>();
    /**
     * Map of the peptides inspected for each spectrum.
     */
    private HashMap<String, HashSet<String>> peptidesInspected;
    /**
     * The number of lines written to the file.
     */
    private int nLines;

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
     * @param maxSites the preferred number of sites to iterate for every PTM
     * @param allPsmsFile the file where to export the results
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading a file
     * @throws InterruptedException exception thrown if a threading issue
     * occurs.
     */
    public void iterateSequences(String spectrumFileName, PrecursorProcessor precursorProcessor, IdentificationParameters identificationParameters, int maxX, int nThreads, Double minMz, Double maxMz, HashMap<String, Integer> maxModifications, int maxSites, File allPsmsFile) throws IOException, InterruptedException {
        iterateSequences(spectrumFileName, precursorProcessor, null, identificationParameters, maxX, nThreads, minMz, maxMz, maxModifications, maxSites, allPsmsFile);
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
     * @param maxSites the preferred number of sites to iterate for every PTM
     * @param allPsmsFile the file where to export the results
     *
     * @throws InterruptedException exception thrown if a threading issue
     * occurs.
     * @throws IOException exception thrown whenever an error occurred while
     * reading a file
     */
    public void iterateSequences(String spectrumFileName, PrecursorProcessor precursorProcessor, String exclusionListFilePath, IdentificationParameters identificationParameters, int maxX, int nThreads, Double minMz, Double maxMz, HashMap<String, Integer> maxModifications, int maxSites, File allPsmsFile) throws InterruptedException, IOException {

        // Set up the writer to write the psms
        TextExporter textExporter = new TextExporter(allPsmsFile);
        textExporter.writeHeaders();

        // Initialize the maps
        ArrayList<String> spectrumTitles = spectrumFactory.getSpectrumTitles(spectrumFileName);
        scoresMap = new HashMap<String, HashMap<String, Integer>>(spectrumTitles.size());
        peptidesInspected = new HashMap<String, HashSet<String>>(spectrumTitles.size());
        for (String spectrumTitle : spectrumTitles) {
            scoresMap.put(spectrumTitle, new HashMap<String, Integer>(2));
            peptidesInspected.put(spectrumTitle, new HashSet<String>(2));
        }

        // Set progress counters
        waitingHandler.setSecondaryProgressCounterIndeterminate(false);
        waitingHandler.setMaxSecondaryProgressCounter(sequenceFactory.getNSequences());

        // Make a pool of sequence processors
        SequenceFactory.ProteinIterator proteinIterator = sequenceFactory.getProteinIterator(false);
        ArrayList<SequenceProcessor> sequenceProcessors = new ArrayList<SequenceProcessor>(nThreads);
        ExecutorService pool = Executors.newFixedThreadPool(nThreads);
        for (int i = 0; i < nThreads; i++) {
            SequenceProcessor sequenceProcessor = new SequenceProcessor(proteinIterator, spectrumFileName, precursorProcessor, exclusionListFilePath, identificationParameters, maxX, minMz, maxMz, maxModifications, maxSites, textExporter);
            sequenceProcessors.add(sequenceProcessor);
            pool.submit(sequenceProcessor);
        }

        // Execute
        pool.shutdown();
        if (!pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS)) {
            waitingHandler.appendReport("Search timed out.", true, true);
        }

        // Gather the information saved by every sequence processor
        waitingHandler.setSecondaryProgressCounterIndeterminate(true);
        nLines = 0;
        for (SequenceProcessor sequenceProcessor : sequenceProcessors) {
            nLines += sequenceProcessor.getnLines();
        }

        // Close writer
        textExporter.close();
    }

    /**
     * Returns a map of the scores for every peptide found for every spectrum.
     *
     * @return a map of the scores for every peptide found for every spectrum
     */
    public HashMap<String, HashMap<String, Integer>> getScoresMap() {
        return scoresMap;
    }

    /**
     * Returns the number of lines written to the file.
     *
     * @return the number of lines written to the file
     */
    public int getnLines() {
        return nLines;
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
         * The number of lines written to the result file.
         */
        private int threadNLines = 0;
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
         * The preferred number of modification sites to iterate per
         * modification.
         */
        private int maxSites;
        /**
         * The exporter where to write the results.
         */
        private TextExporter textExporter;

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
         * @param textExporter the text exporter used to write the results
         *
         * @throws IOException exception thrown whenever an error occurred while
         * reading the exclusion list file
         */
        public SequenceProcessor(SequenceFactory.ProteinIterator proteinIterator, String spectrumFileName, PrecursorProcessor precursorProcessor, String exclusionListFilePath, IdentificationParameters identificationParameters, int maxX, Double minMz, Double maxMz, HashMap<String, Integer> maxModifications, int maxSites, TextExporter textExporter) throws IOException {
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
            this.textExporter = textExporter;
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

                // Store information on the searched modifications
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
                ArrayList<String> orderedModificationsName = new ArrayList<String>(ptmSettings.getVariableModifications());
                Collections.sort(orderedModificationsName);
                ArrayList<String> orderedPeptideModificationsName = new ArrayList<String>(orderedModificationsName.size());

                // Get objects for modification iteration
                ArrayList<String> orderedModificationsNameList = new ArrayList<String>(variablePtms.keySet());
                Collections.sort(orderedModificationsNameList);
                String[] orderedModifications = orderedModificationsNameList.toArray(new String[orderedModificationsNameList.size()]);

                // Information from the spectrum processing
                Double massMin = precursorProcessor.getMassMin();
                Double massMax = precursorProcessor.getMassMax();
                PrecursorMap precursorMap = precursorProcessor.getPrecursorMap();

                // Settings for the annotation of spectra
                AnnotationSettings annotationSettings = identificationParameters.getAnnotationPreferences();

                // Sequence settings for the keys of the peptides
                SequenceMatchingPreferences sequenceMatchingPreferences = SequenceMatchingPreferences.getDefaultSequenceMatching();

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
                        String peptideKey = peptide.getMatchingKey(sequenceMatchingPreferences);
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

                                            // Get the spectrum title
                                            String spectrumTitle = precursorWithTitle.spectrumTitle;

                                            // See if the peptide has already been identified for this spectrum
                                            HashSet<String> peptidesInspectedForSpectrum = peptidesInspected.get(spectrumTitle);
                                            if (!peptidesInspectedForSpectrum.contains(peptideKey)) {

                                                // Get the scores map
                                                HashMap<String, Integer> spectrumScores = scoresMap.get(spectrumTitle);

                                                // See if we already have a score
                                                if (!spectrumScores.containsKey(peptideKey)) {

                                                    // Get the spectrum annotation
                                                    PeptideAssumption peptideAssumption = new PeptideAssumption(peptide, new Charge(Charge.PLUS, charge));
                                                    MSnSpectrum spectrum = (MSnSpectrum) spectrumFactory.getSpectrum(spectrumFileName, spectrumTitle);
                                                    SpecificAnnotationSettings specificAnnotationSettings = annotationSettings.getSpecificAnnotationPreferences(spectrumTitle, peptideAssumption, identificationParameters.getSequenceMatchingPreferences(), identificationParameters.getPtmScoringPreferences().getSequenceMatchingPreferences());
                                                    ArrayList<IonMatch> ionMatches = peptideSpectrumAnnotator.getSpectrumAnnotation(annotationSettings, specificAnnotationSettings, spectrum, peptide);

                                                    // Get the score
                                                    double score = hyperScore.getScore(peptide, spectrum, annotationSettings, specificAnnotationSettings, ionMatches);
                                                    int scoreBin = (int) score;

                                                    // Retain only positive scores
                                                    if (scoreBin > 0) {

                                                        // Save score to the e-value calculation map
                                                        scoresMapMutex.acquire(spectrumTitle);
                                                        spectrumScores.put(peptideKey, scoreBin);
                                                        scoresMapMutex.release(spectrumTitle);

                                                        // Write the assumption to the file
                                                        textExporter.writePeptide(spectrumFileName, spectrumTitle, peptide, score, charge);
                                                        threadNLines++;

                                                    } else {

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
                                                String modifiedPeptideKey = peptideDraft.getKey(orderedModifications, sequenceMatchingPreferences);

                                                // Get a precursor to see if the peptide was already inspected
                                                PrecursorMap.PrecursorWithTitle precursorWithTitle = precursorMatches.get(0);
                                                boolean newPeptide = true;

                                                // Get the spectrum title
                                                String spectrumTitle = precursorWithTitle.spectrumTitle;

                                                // See if the peptide has already been identified for this spectrum
                                                HashSet<String> peptidesInspectedForSpectrum = peptidesInspected.get(spectrumTitle);
                                                if (peptidesInspectedForSpectrum.contains(modifiedPeptideKey)) {
                                                    newPeptide = false;
                                                } else {

                                                    // Get the scores map for this spectrum
                                                    HashMap<String, Integer> spectrumScores = scoresMap.get(spectrumTitle);

                                                    // See if we already have a score
                                                    if (spectrumScores.containsKey(modifiedPeptideKey)) {
                                                        newPeptide = false;
                                                    }
                                                }

                                                // if new, write all possible peptides to the file
                                                if (newPeptide) {

                                                    // Create an iterator for the possible sites
                                                    PeptideModificationsIterator peptideModificationsIterator;
                                                    if (modificationOccurrence.size() == 1) {
                                                        String modificationName = modificationOccurrence.keySet().iterator().next();
                                                        Integer[] possibleSites = possibleModificationSites.get(modificationName);
                                                        Integer occurrence = modificationOccurrence.get(modificationName);
                                                        peptideModificationsIterator = new SingleModificationIterator(possibleSites, occurrence, modificationName, maxSites);
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
                                                            peptideModificationsIterator = new OverlappingModificationsIterator(modificationOccurrence, possibleModificationSites, orderedPeptideModificationsName, maxSites);
                                                        } else {
                                                            peptideModificationsIterator = new MultipleModificationsIterators(modificationOccurrence, possibleModificationSites, orderedPeptideModificationsName, maxSites);
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

                                                        // Iterate all precursor matches
                                                        for (PrecursorMap.PrecursorWithTitle precursorWithTitle2 : precursorMatches) {

                                                            // Get the spectrum title
                                                            spectrumTitle = precursorWithTitle2.spectrumTitle;

                                                            // Get the scores map
                                                            HashMap<String, Integer> spectrumScores = scoresMap.get(spectrumTitle);

                                                            // Get the spectrum annotation
                                                            PeptideAssumption peptideAssumption = new PeptideAssumption(modifiedPeptide, new Charge(Charge.PLUS, charge));
                                                            MSnSpectrum spectrum = (MSnSpectrum) spectrumFactory.getSpectrum(spectrumFileName, spectrumTitle);
                                                            SpecificAnnotationSettings specificAnnotationSettings = annotationSettings.getSpecificAnnotationPreferences(spectrumTitle, peptideAssumption, identificationParameters.getSequenceMatchingPreferences(), identificationParameters.getPtmScoringPreferences().getSequenceMatchingPreferences());
                                                            ArrayList<IonMatch> ionMatches = peptideSpectrumAnnotator.getSpectrumAnnotation(annotationSettings, specificAnnotationSettings, spectrum, modifiedPeptide);

                                                            // Get the score
                                                            double score = hyperScore.getScore(peptide, spectrum, annotationSettings, specificAnnotationSettings, ionMatches);
                                                            int scoreBin = (int) score;

                                                            // Retain only positive scores
                                                            if (scoreBin > 0) {

                                                                // Save score to the e-value calculation map
                                                                scoresMapMutex.acquire(spectrumTitle);
                                                                spectrumScores.put(peptideKey, scoreBin);
                                                                scoresMapMutex.release(spectrumTitle);

                                                                // Write the assumption to the file
                                                                textExporter.writePeptide(spectrumFileName, spectrumTitle, peptide, score, charge);
                                                                threadNLines++;

                                                            } else if (first) {

                                                                // Make sure that the list of inspected peptides for this spectrum does not get too long
                                                                if (peptidesInspectedForSpectrum.size() == 8192) {
                                                                    peptidesInspectedForSpectrum.clear();
                                                                }

                                                                // Add the peptide to the inspected peptides list
                                                                peptidesInspectedForSpectrum.add(peptideKey);

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

        /**
         * Returns the number of lines written to the file.
         *
         * @return the number of lines written to the file
         */
        public int getnLines() {
            return threadNLines;
        }
    }
}
