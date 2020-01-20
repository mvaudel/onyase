package no.uib.onyase.io.ms;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import static no.uib.onyase.io.ms.MsFileUtils.encoding;
import no.uib.onyase.model.SimpleSpectrum;

/**
 * Reader for an ms file.
 *
 * @author Marc Vaudel
 */
public class MsFileReader implements AutoCloseable {

    public final double minMz;
    public final double maxMz;
    private final String[] titles;
    private final HashMap<String, Integer> indexMap;
    private final HashMap<String, Integer> compressedLengthMap;
    private final HashMap<String, Double> precrursorMzMap;
    private final HashMap<String, Integer> nPeaksMap;
    /**
     * The random access file.
     */
    private final RandomAccessFile raf;
    /**
     * The channel to the file.
     */
    private final FileChannel fc;
    private final MappedByteBuffer mappedByteBuffer;

    public MsFileReader(File file) throws FileNotFoundException, IOException, DataFormatException {

        raf = new RandomAccessFile(file, "rw");

        byte[] fileMagicNumber = new byte[MsFileUtils.magicNumber.length];
        raf.read(fileMagicNumber);

        if (!Arrays.equals(MsFileUtils.magicNumber, fileMagicNumber)) {

            throw new IOException("File format of " + file + " not supported.");

        }

        long footerPosition = raf.readLong();

        minMz = raf.readDouble();
        maxMz = raf.readDouble();

        raf.seek(footerPosition);
        int length = raf.readInt();
        int uncompressedLength = raf.readInt();

        byte[] compressedTitles = new byte[length];
        raf.read(compressedTitles);

        byte[] titlesByteArray = uncompress(compressedTitles, uncompressedLength);
        String titlesString = new String(titlesByteArray, 0, titlesByteArray.length, encoding);

        titles = titlesString.split(MsFileUtils.titleSeparator);
        indexMap = new HashMap<>(titles.length);
        compressedLengthMap = new HashMap<>(titles.length);
        precrursorMzMap = new HashMap<>(titles.length);
        nPeaksMap = new HashMap<>(titles.length);

        fc = raf.getChannel();

        long size = footerPosition - MsFileWriter.HEADER_LENGTH;

        if (size > Integer.MAX_VALUE) {

            throw new IOException("Buffer exceeds max size.");

        }

        mappedByteBuffer = fc.map(FileChannel.MapMode.READ_ONLY, MsFileWriter.HEADER_LENGTH, size);

        int index = 0;

        for (String title : titles) {

            indexMap.put(title, index);

            int compressedLength = mappedByteBuffer.getInt(index);
            index += Integer.BYTES;

            double precursorMz = mappedByteBuffer.getDouble(index);
            index += Double.BYTES;

            int nPeaks = mappedByteBuffer.getInt(index);
            index += Integer.BYTES;

            indexMap.put(title, index);
            compressedLengthMap.put(title, compressedLength);
            precrursorMzMap.put(title, precursorMz);
            nPeaksMap.put(title, nPeaks);

            index += compressedLength;

        }
    }

    public double getPrecursorMz(String spectrumTitle) {

        return precrursorMzMap.get(spectrumTitle);

    }

    public SimpleSpectrum get(String spectrumTitle) {

        try {

            double precursorMz = precrursorMzMap.get(spectrumTitle);
            int nPeaks = nPeaksMap.get(spectrumTitle);
            int index = indexMap.get(spectrumTitle);
            int compressedLength = compressedLengthMap.get(spectrumTitle);
            int uncompressedLength = nPeaks * 2 * Double.BYTES;

            byte[] compressedSpectrum = new byte[compressedLength];
            mappedByteBuffer.position(index);
            mappedByteBuffer.get(compressedSpectrum, 0, compressedLength);

            byte[] uncompressedSpectrum = uncompress(compressedSpectrum, uncompressedLength);
            ByteBuffer byteBuffer = ByteBuffer.wrap(uncompressedSpectrum);

            double[] mz = new double[nPeaks];
            double[] intensity = new double[nPeaks];

            for (int i = 0; i < nPeaks; i++) {

                mz[i] = byteBuffer.getDouble();
                intensity[i] = byteBuffer.getDouble();

            }

            return new SimpleSpectrum(spectrumTitle, precursorMz, mz, intensity);

        } catch (DataFormatException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] uncompress(byte[] compressedByteArray, int uncompressedLength) throws DataFormatException {

        byte[] uncompressedByteAray = new byte[uncompressedLength];

        Inflater inflater = new Inflater(true);

        inflater.setInput(compressedByteArray);
        int bytesUncompressed = inflater.inflate(uncompressedByteAray);

        if (bytesUncompressed == 0) {

            throw new IllegalArgumentException("Missing input or dictionary.");

        } else if (bytesUncompressed != uncompressedLength) {

//                String debug = new String(uncompressedByteAray, 0, uncompressedByteAray.length, encoding);
            throw new IllegalArgumentException("Unexpected number of bytes uncompressed " + bytesUncompressed + " (expected: " + uncompressedLength + ")");

        }

        return uncompressedByteAray;

    }

    @Override
    public void close() throws Exception {

        MsFileUtils.closeBuffer(mappedByteBuffer);

        raf.close();

    }

}
