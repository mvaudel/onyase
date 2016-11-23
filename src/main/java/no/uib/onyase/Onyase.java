package no.uib.onyase;

/**
 * Main class.
 *
 * @author Marc Vaudel
 */
public class Onyase {

    private String mgfFilePath = "C:\\Projects\\PeptideShaker\\test files\\1 mgf\\qExactive01819.mgf";
    private String fastaFilePath = "C:\\Databases\\uniprot-human-reviewed-trypsin-april-2016_concatenated_target_decoy.fasta";
    private String parametersFilePath = "C:\\Users\\mvaudel\\.compomics\\identification_parameters\\tutorial new parameters.par";

    /**
     * The main method used to start PeptideShaker.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        Onyase instance;
        if (args.length > 0) {
            instance = new Onyase(args[0], args[1], args[2]);
        } else {
            instance = new Onyase();
        }
        System.out.println(instance.launch());
    }
    
    public Onyase(String mgfFilePath, String fastaFilePath, String parametersFilePath) {
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
    
    public Onyase() {
        this(null, null, null);
    }
    
    public int launch() {
        
        return 1;
    }
}
