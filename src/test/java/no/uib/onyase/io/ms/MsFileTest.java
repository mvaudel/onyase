package no.uib.onyase.io.ms;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import junit.framework.Assert;
import junit.framework.TestCase;
import no.uib.onyase.model.SimpleSpectrum;

/**
 * Test for MS file writing and reading.
 *
 * @author Marc Vaudel
 */
public class MsFileTest extends TestCase {

    public static final int N_SPECTRA = 100;

    public void testParsing() throws IOException, DataFormatException {

        SimpleSpectrum[] spectra = getSpectra();

        File mzFile = new File("src/test/resources/mzFileTest");

        try (MsFileWriter writer = new MsFileWriter(mzFile)) {

            for (SimpleSpectrum simpleSpectrum : spectra) {

                writer.addSpectrum(simpleSpectrum);

            }
        }

        try (MsFileReader reader = new MsFileReader(mzFile)) {

            Assert.assertTrue(reader.titles.length == N_SPECTRA);

            Arrays.stream(spectra)
                    .forEach(spectrum -> checkSpectrum(spectrum, reader));

        }
    }

    private void checkSpectrum(SimpleSpectrum referenceSpectrum, MsFileReader reader) {

        SimpleSpectrum fileSpectrum = reader.get(referenceSpectrum.title);

        Assert.assertTrue(fileSpectrum != null);
        Assert.assertTrue(fileSpectrum.precursorMz == referenceSpectrum.precursorMz);
        Assert.assertTrue(fileSpectrum.title.equals(referenceSpectrum.title));
        Assert.assertTrue(Arrays.equals(fileSpectrum.mz, referenceSpectrum.mz));
        Assert.assertTrue(Arrays.equals(fileSpectrum.intensity, referenceSpectrum.intensity));

    }

    private SimpleSpectrum[] getSpectra() {

        SimpleSpectrum[] spectra = new SimpleSpectrum[N_SPECTRA];

        for (int i = 0; i < N_SPECTRA; i++) {

            String title = "Spectrum " + i;
            double precursorMz = 2.0 * i;

            double[] mz = new double[123];
            double[] intensity = new double[123];

            double mzFactor = 0.1 * i;
            double intensityFactor = 10.0 * (100 - i);

            for (int j = 0; j < 123; j++) {

                mz[j] = mzFactor * j;
                intensity[j] = intensityFactor * j * i;

            }

            SimpleSpectrum simpleSpectrum = new SimpleSpectrum(
                    title,
                    precursorMz,
                    mz,
                    intensity
            );

            spectra[i] = simpleSpectrum;

        }

        return spectra;

    }

}
