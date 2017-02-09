package no.uib.onyase.applications.engine.modules.scoring;

/**
 * Enum of the implemented scores.
 *
 * @author Marc Vaudel
 */
public enum ImplementedScore {

    hyperscore(0,"Hyperscore", "Implementation of the hyperscore similar to X!Tandem."), 
    snrScore(1,"SnrScore", "Score based on the signal to noise ratio of the intensities of annotated peaks.");

    /**
     * The index of the option.
     */
    public final int index;
    /**
     * The name of the option.
     */
    public final String name;
    /**
     * The description of the option.
     */
    public final String description;
    
    /**
     * Constructor.
     * 
     * @param index the index of the option
     * @param name the name of the option
     * @param description the description of the option
     */
    private ImplementedScore(int index, String name, String description) {
        this.index = index;
        this.name = name;
        this.description = description;
    }
    
    @Override
    public String toString() {
        return name;
    }

}
