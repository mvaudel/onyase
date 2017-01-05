package no.uib.onyase.utils;

import java.util.Set;
import java.util.concurrent.Semaphore;

/**
 * A simple thread safe iterator for the spectrum names. The iterator returns
 * null when it is done.
 *
 * @author Marc Vaudel
 */
public class TitlesIterator {

    /**
     * The array to iterate.
     */
    private final String[] array;
    /**
     * The current index of the iterator.
     */
    private int index = 0;
    /**
     * Mutex to manage simultaneous calls to next().
     */
    private final Semaphore mutex = new Semaphore(1);

    /**
     * Constructor.
     *
     * @param values a set of values to iterate
     */
    public TitlesIterator(Set<String> values) {
        array = values.toArray(new String[values.size()]);
    }

    /**
     * Returns the next value in the iterator. Null if the end was reached.
     *
     * @return the next value
     *
     * @throws InterruptedException exception thrown if a threading error
     * occurred.
     */
    public String next() throws InterruptedException {
        mutex.acquire();
        String result;
        if (index < array.length) {
            result = array[index];
            index++;
        } else {
            result = null;
        }
        mutex.release();
        return result;
    }
}
