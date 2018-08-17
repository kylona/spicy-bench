package array;

import array.ArrayMonitor;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public class ArrayMonitorTest {

    @Test
    public void doubleReader() {
        ArrayMonitor monitor = new ArrayMonitor(1);
        monitor = monitor.addReader(0, 0);
        monitor = monitor.addReader(0, 1);
        assertFalse(monitor.isShared());
    }

    @Test
    public void readerWriterRace() {
        ArrayMonitor monitor = new ArrayMonitor(1);
        monitor = monitor.addReader(0, 0);
        monitor = monitor.addWriter(0, 1);
        assertTrue(monitor.isShared());
    }

    @Test
    public void readerWriterNoRace() {
        ArrayMonitor monitor = new ArrayMonitor(2);
        monitor = monitor.addReader(0, 0);
        monitor = monitor.addWriter(1, 1);
        assertFalse(monitor.isShared());
    }

    @Test
    public void sameReaderWriter() {
        ArrayMonitor monitor = new ArrayMonitor(1);
        monitor = monitor.addReader(0, 0);
        monitor = monitor.addWriter(0, 0);
        assertFalse(monitor.isShared());
    }

    @Test
    public void readerWriterManyRace() {
        final int SIZE = 10;
        ArrayMonitor monitor = new ArrayMonitor(SIZE);
        for (int i = 0; i < SIZE; i++) {
            monitor = monitor.addReader(i, 0);
            monitor = monitor.addWriter(i, 1);
        }
        assertTrue(monitor.isShared());
    }

    @Test
    public void yetAnotherTest() {
        final int SIZE = 10;
        ArrayMonitor monitor = new ArrayMonitor(SIZE);
        for (int i = 0; i < SIZE / 2; i++) {
            monitor = monitor.addWriter(i, 0);
        }
        for (int i = SIZE / 2; i < SIZE; i++) {
            monitor = monitor.addWriter(i, 1);
        }
        assertFalse(monitor.isShared());
    }

}
