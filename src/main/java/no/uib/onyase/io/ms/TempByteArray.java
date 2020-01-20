package no.uib.onyase.io.ms;

/**
 * Convenience class for storing an array buffer and a length.
 *
 * @author Marc Vaudel
 */
public class TempByteArray {

        public final byte[] array;
        public final int length;

        public TempByteArray(
                byte[] array,
                int length
        ) {

            this.array = array;
            this.length = length;

        }

}
