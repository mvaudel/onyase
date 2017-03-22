# Onyase Engine Example

The following details how to run the example files available with the release. Don't hesitate to open an [issue](https://github.com/mvaudel/onyase/issues) should you encounter problems running the example.

0- Make sure that you have Java installed, version later than 1.6 and 64 bits if you are using a 64 bits computer. The latest version of Java can be found [here](http://java.sun.com/javase/downloads/index.jsp).

1- Download the example files for the Onyase engine [here](https://github.com/mvaudel/onyase/tree/master/example/example.zip). Unzip the file. You should have in your folder _qExactive01819.mgf_, _Test Onyase.par_, and _uniprot-human-reviewed-trypsin-november-2016_concatenated_target_decoy.fasta_. Note that these files are from the [CompOmics tutorial](https://compomics.com/bioinformatics-for-proteomics/) example.

2- Open a command line terminal. Navigate to the folder containing the unzipped engine. You should see the Onyase jar file in the folder.

3- Run the following command, replacing _exampleFolder_ by the path to the folder where the example files are unzipped, and X.Y.Z by the version number of the jar file you are using.

```
java -cp Onyase-X.Y.Z.jar no.uib.onyase.cli.engine.OnyaseEngineCLI -spectra "exampleFolder\\qExactive01819.mgf" -db "exampleFolder\\uniprot-human-reviewed-trypsin-november-2016_concatenated_target_decoy.fasta" -output "exampleFolder" -id_params "exampleFolder\\Test Onyase.par"
```

_Note that the -db option overwrites the database in the parameters file._

4- After completion, a file named _qExactive01819.psm_ is written in the _exampleFolder_.

___
[Back to the Onyase Engine](engine.md)