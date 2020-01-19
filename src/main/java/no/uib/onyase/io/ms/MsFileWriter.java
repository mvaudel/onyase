package no.uib.onyase.io.ms;

import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.zip.Deflater;
import static no.uib.onyase.io.ms.MsFileUtils.magicNumber;
import static no.uib.onyase.io.ms.MsFileUtils.mergeArrays;

/**
 *
 * @author Marc Vaudel
 */
public class MsFileWriter implements AutoCloseable {

    private final RandomAccessFile raf;

    private final Deflater deflater = new Deflater();

    private double minMz = Double.MAX_VALUE;
    private double maxMz = 0.0;
    private long footerPosition = 0l;

    private final ArrayList<String> titles = new ArrayList<>();

    public MsFileWriter(
            File outputFile
    ) throws FileNotFoundException, IOException {

        raf = new RandomAccessFile(outputFile, "rw");
        raf.seek(magicNumber.length + 3 * Long.BYTES);

    }

    public void addSpectrum(
            Spectrum spectrum
    ) throws IOException {

        double precursorMz = spectrum.getPrecursor().getMz();
        double[] mzArray = spectrum.getOrderedMzValues();
        double[] intensityArray = Arrays.stream(mzArray)
                .map(mz -> spectrum.getPeakMap().get(mz).intensity)
                .toArray();
        int nPeaks = mzArray.length;

        ByteBuffer buffer = ByteBuffer.allocate((2 * nPeaks + 1) * Double.BYTES);

        buffer.putDouble(precursorMz);

        for (int i = 0; i < nPeaks; i++) {

            buffer.putDouble(mzArray[i]);
            buffer.putDouble(intensityArray[i]);

        }

        compressAndWrite(buffer.array());

        titles.add(spectrum.getSpectrumTitle());

        if (minMz > precursorMz) {

            minMz = precursorMz;

        }

        if (maxMz < precursorMz) {

            maxMz = precursorMz;

        }
    }

    private void compressAndWrite(
            byte[] uncompressedData
    ) throws IOException {

        byte[] compressedData = new byte[uncompressedData.length];

        int outputLength = compressedData.length;

        deflater.setInput(uncompressedData);
        int compressedByteLength = deflater.deflate(compressedData, 0, compressedData.length, Deflater.FULL_FLUSH);
        int compressedDataLength = compressedByteLength;

        while (compressedByteLength == outputLength) {

            byte[] output2 = new byte[outputLength];
            compressedByteLength = deflater.deflate(output2, 0, outputLength, Deflater.FULL_FLUSH);

            compressedData = mergeArrays(compressedData, output2, compressedByteLength);
            compressedDataLength += compressedByteLength;

        }

        ByteBuffer buffer = ByteBuffer.allocate(compressedDataLength + Integer.BYTES);
        buffer.putInt(compressedDataLength)
                .put(compressedData);
        byte[] arrayToWrite = buffer.array();

        raf.write(arrayToWrite, 0, arrayToWrite.length);

    }

    private void writeHeaderAndFooter() throws IOException {

        footerPosition = raf.getFilePointer();

        String titleString = titles.stream()
                .collect(Collectors.joining(MsFileUtils.titleSeparator));
        byte[] titleBytes = titleString.getBytes(MsFileUtils.encoding);

        compressAndWrite(titleBytes);

        raf.seek(0);

        ByteBuffer buffer = ByteBuffer.allocate(magicNumber.length + 3 * Long.BYTES);
        buffer.put(magicNumber)
                .putLong(footerPosition)
                .putDouble(minMz)
                .putDouble(maxMz);

        raf.write(buffer.array());

    }

    @Override
    public void close() throws Exception {

        writeHeaderAndFooter();

        deflater.end();
        raf.close();

    }

}
