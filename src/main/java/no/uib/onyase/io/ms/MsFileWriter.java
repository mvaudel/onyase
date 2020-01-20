package no.uib.onyase.io.ms;

import com.compomics.util.experiment.mass_spectrometry.spectra.Peak;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.zip.Deflater;
import static no.uib.onyase.io.ms.MsFileUtils.magicNumber;
import static no.uib.onyase.io.ms.MsFileUtils.mergeArrays;
import no.uib.onyase.model.SimpleSpectrum;

/**
 * Writer for an ms file.
 *
 * @author Marc Vaudel
 */
public class MsFileWriter implements AutoCloseable {

    public static final int HEADER_LENGTH = magicNumber.length + Long.BYTES + 2 * Double.BYTES;

    private final RandomAccessFile raf;

    private final Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION, true);

    private double minMz = Double.MAX_VALUE;
    private double maxMz = 0.0;

    private final ArrayList<String> titles = new ArrayList<>();

    public MsFileWriter(
            File outputFile
    ) throws FileNotFoundException, IOException {

        raf = new RandomAccessFile(outputFile, "rw");
        raf.seek(HEADER_LENGTH);

    }

    public void addSpectrum(
            SimpleSpectrum spectrum
    ) throws IOException {
        
        int nPeaks = spectrum.mz.length;

        ByteBuffer buffer = ByteBuffer.allocate((2 * nPeaks) * Double.BYTES);

        for (int i = 0; i < nPeaks; i++) {

            buffer.putDouble(spectrum.mz[i]);
            buffer.putDouble(spectrum.intensity[i]);

        }

        TempByteArray compressedData = compress(buffer.array());

        buffer = ByteBuffer.allocate(compressedData.length + Double.BYTES + 2 * Integer.BYTES);
        buffer.putInt(compressedData.length)
                .putDouble(spectrum.precursorMz)
                .putInt(nPeaks)
                .put(compressedData.array, 0, compressedData.length);
        byte[] arrayToWrite = buffer.array();

        raf.write(arrayToWrite, 0, arrayToWrite.length);

        titles.add(spectrum.title);

        if (minMz > spectrum.precursorMz) {

            minMz = spectrum.precursorMz;

        }

        if (maxMz < spectrum.precursorMz) {

            maxMz = spectrum.precursorMz;

        }
    }

    private void compressAndWrite(
            byte[] uncompressedData
    ) throws IOException {

        TempByteArray compressedData = compress(uncompressedData);

        raf.writeInt(compressedData.length);
        raf.writeInt(uncompressedData.length);
        raf.write(compressedData.array, 0, compressedData.length);

    }

    private TempByteArray compress(
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

        return new TempByteArray(compressedData, compressedDataLength);

    }

    private void writeHeaderAndFooter() throws IOException {

        long footerPosition = raf.getFilePointer();

        String titleString = titles.stream()
                .collect(Collectors.joining(MsFileUtils.titleSeparator));
        byte[] titleBytes = titleString.getBytes(MsFileUtils.encoding);

        compressAndWrite(titleBytes);

        raf.seek(0);

        ByteBuffer buffer = ByteBuffer.allocate(HEADER_LENGTH);
        buffer.put(magicNumber)
                .putLong(footerPosition)
                .putDouble(minMz)
                .putDouble(maxMz);

        raf.write(buffer.array());

    }

    @Override
    public void close() throws IOException {

        writeHeaderAndFooter();

        deflater.end();
        raf.close();

    }

}
