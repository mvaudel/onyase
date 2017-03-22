# Onyase engine

The Onyase engine is a script using the [compomics-utilities](https://github.com/compomics/compomics-utilities) to conduct standard proteomic database searches. An *early beta* release is available [here](https://github.com/mvaudel/onyase/tree/master/release/Onyase-0.0.1.zip).

## Usage ##

The Onyase engine can be run in command line as detailed below.

```java
java -cp Onyase-X.Y.Z.jar no.uib.onyase.cli.engine.OnyaseEngineCLI [parameters]
```

**Parameters**

Standard parameters:

```
-spectra                Spectrum file (mgf format), mandatory.
-id_params				The identification parameters in a file (.par). Alternatively, IdentificationParametersCLI parameters can be passed directly. See below for more details.
-output					The folder where to write the output file, mandatory.
```

Advanced parameters:

```
-temp_folder			A folder for temporary file storage. Use only if you encounter problems with the default configuration.
-log					Folder where to write log files.
-threads				Number of threads to use for the processing, default: the number of cores.
```

** Identification Parameters **

The identification parameters are the ones of [compomics-utilities](https://github.com/compomics/compomics-utilities) and therefore by design compatible with [SearchGUI](https://github.com/compomics/searchgui) and [PeptideShaker](https://github.com/compomics/peptide-shaker). Identification parameter files are in the [json](https://en.wikipedia.org/wiki/JSON) format and can be created in the graphical user interface of [SearchGUI](https://github.com/compomics/searchgui) and [PeptideShaker](https://github.com/compomics/peptide-shaker), using the [IdentificationParametersCLI](https://github.com/compomics/compomics-utilities/wiki/IdentificationParametersCLI), or using third party tools. Alternatively, the parameters can be passed directly to OnyaseEngineCLI by using the command line arguments of the [IdentificationParametersCLI](https://github.com/compomics/compomics-utilities/wiki/IdentificationParametersCLI).

** Temporary files **

The [compomics-utilities](https://github.com/compomics/compomics-utilities) uses temporary files to store parameters and indexes. These files are stored next to original files or in your user folder. You can set a temporary folder path using the *temp_folder* option. When running multiple instances of Onyase, it is recommended to use this option, as well as the redirection of the error stream via the *log* option.

** Output format **

The output lists all peptide matches for every spectrum. It is a gzip compressed space separated text file where every line represents a peptide candidate. Every spectrum is referenced by its title in the mgf file. The title is encoded like a URL with UTF-8 encoding.

A parser for the output file is available in the [compomics-utilities](https://github.com/compomics/compomics-utilities/blob/master/src/main/java/com/compomics/util/experiment/io/identifications/idfilereaders/OnyaseIdfileReader.java). The output file can be imported in [PeptideShaker](https://github.com/compomics/peptide-shaker), and from there an [mzIdentML](http://www.psidev.info/mzidentml) file can be created.

Note that the output does not contain the peptide to protein mapping. Peptides can be mapped back to proteins using [PeptideMapper](https://github.com/compomics/compomics-utilities/wiki/PeptideMapper).

** Performance **

Onysase is mainly based on functions written for [PeptideShaker](https://github.com/compomics/peptide-shaker), wich is a different use case. They are therefore not optimized for database searches. I will try to improve the performance as time goes.

** Disclaimer **

Onyase is at a very early development stage. Please be critical, patient, and don't hesitate to [report issues](https://github.com/mvaudel/onyase/issues).

** Licensing **

The [compomics-utilities](https://github.com/compomics/compomics-utilities) library is licensed under the Apache-2.0 license.

Please note that some of the JAR files used by Onyase or compomics-utilities may not have the same license as Onyase itself. If you want to use any of these in a different context, make sure to obtain the original license for the JAR file in question.

Onyase is licensed under the [GNU General Public License v3.0](https://github.com/mvaudel/onyase/blob/master/LICENSE).

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program. If not, see <http://www.gnu.org/licenses/>.