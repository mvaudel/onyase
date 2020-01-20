package no.uib.onyase.io.ms;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import no.uib.onyase.utils.IoUtils;

/**
 * Utils to store ms files.
 *
 * @author Marc Vaudel
 */
public class MsFileUtils {
    
    /**
     * Default encoding, cf the second rule.
     */
    public static final String encoding = "UTF-8";
    
    public static final String titleSeparator = "\t";
    
    public static final byte[] magicNumber = getMagicNumber();

    public static byte[] getMagicNumber() {

        try {

            String magicName = "MsFile.1";
            return magicName.getBytes(encoding);

        } catch (UnsupportedEncodingException e) {

            throw new RuntimeException(e);

        }
    }
    
    /**
     * Simple method to merge two byte arrays.
     *
     * @param array1 First byte array.
     * @param array2 Second byte array.
     * @param len2 The length of the second array to copy
     *
     * @return A concatenation of the first and the second arrays.
     */
    public static byte[] mergeArrays(
            byte[] array1, 
            byte[] array2,
            int len2
    ) {
        
        byte[] result = new byte[array1.length + len2];
        
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, len2);
        
        return result;
    
    }

    /**
     * Attempts at closing a buffer to avoid memory issues. Taken from
     * https://stackoverflow.com/questions/2972986/how-to-unmap-a-file-from-memory-mapped-using-filechannel-in-java.
     *
     * @param buffer the buffer to close
     */
    public static void closeBuffer(MappedByteBuffer buffer) {

        if (buffer == null || !buffer.isDirect()) {
            return;
        }

        try {
            
            Method cleaner = buffer.getClass().getMethod("cleaner");
            cleaner.setAccessible(true);
            Method clean = Class.forName("sun.misc.Cleaner").getMethod("clean");
            clean.setAccessible(true);
            clean.invoke(cleaner.invoke(buffer));

        } catch (Exception ex) {
        }
    }

}
