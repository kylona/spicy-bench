/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package array;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public class ArrayMonitor {

    private final Pair<Set<Long>, Set<Long>>[] monitor;

    public ArrayMonitor(int size) {
        monitor = new Pair[size];
        for (int i = 0; i < size; i++) {
            Set<Long> readers = Collections.unmodifiableSet(new HashSet<>());
            Set<Long> writers = Collections.unmodifiableSet(new HashSet<>());
            monitor[i] = new Pair<>(readers, writers);
        }
    }

    private ArrayMonitor(Pair<Set<Long>, Set<Long>>[] monitor) {
        this.monitor = monitor;
    }

    public boolean isShared() {
        return detectCollision();
    }

    private boolean detectCollision() {
        for (Pair<Set<Long>, Set<Long>> sets : monitor) {
            Set<Long> readers = sets.getValue0();
            Set<Long> writers = sets.getValue1();
            if (setsOverlap(readers, writers)) {
                return true;
            }
        }
        return false;
    }

    private boolean setsOverlap(Set<Long> readers, Set<Long> writers) {
        if (writers.size() > 1) {
//            System.out.println("Number of writers: " + writers.size());
//            for (long writer : writers) {
//                System.out.println(writer);
//            }
            return true;
        }
        for (long reader : readers) {
            for (long writer : writers) {
                if (reader != writer) {
//                    System.out.println("reader: " + reader 
//                        + " writer: " + writer);
                    return true;
                }
            }
        }
        return false;
    }

    public ArrayMonitor addReader(int index, long threadID) {
//        System.out.println("Adding Reader on index: " + index +
//                " with id: " + threadID);
        if (index < monitor.length && index >= 0) {
            Set readers = monitor[index].getValue0();
            Set newReaders = new HashSet<>(readers);
            Set newWriters = new HashSet<>(monitor[index].getValue1());
            newReaders.add(threadID);
            Set<Long> finalReaders = Collections.unmodifiableSet(newReaders);
            Set<Long> finalWriters = Collections.unmodifiableSet(newWriters);
            Pair<Set<Long>, Set<Long>>[] newMonitor = new Pair[monitor.length];
            newMonitor[index] = new Pair<>(finalReaders, finalWriters);

            for (int i = 0; i < monitor.length; i++) {
                if (i != index) {
                    newReaders = new HashSet<>(monitor[i].getValue0());
                    finalReaders = Collections.unmodifiableSet(newReaders);
                    newWriters = new HashSet<>(monitor[i].getValue1());
                    finalWriters = Collections.unmodifiableSet(newWriters);
                    newMonitor[i] = new Pair<>(finalReaders, finalWriters);
                }
            }
            return new ArrayMonitor(newMonitor);
        } else {
            return null;
        }
    }

    public ArrayMonitor addWriter(int index, long threadID) {
//        System.out.println("Adding Writer on index: " + index +
//                " with id: " + threadID);
        if (index < monitor.length && index >= 0) {
            Set writers = monitor[index].getValue1();
            Set newReaders = new HashSet<>(monitor[index].getValue0());
            Set newWriters = new HashSet<>(writers);
            newWriters.add(threadID);
            Set<Long> finalReaders = Collections.unmodifiableSet(newReaders);
            Set<Long> finalWriters = Collections.unmodifiableSet(newWriters);
            Pair<Set<Long>, Set<Long>>[] newMonitor = new Pair[monitor.length];
            newMonitor[index] = new Pair<>(finalReaders, finalWriters);

            for (int i = 0; i < monitor.length; i++) {
                if (i != index) {
                    newReaders = new HashSet<>(monitor[i].getValue0());
                    finalReaders = Collections.unmodifiableSet(newReaders);
                    newWriters = new HashSet<>(monitor[i].getValue1());
                    finalWriters = Collections.unmodifiableSet(newWriters);
                    newMonitor[i] = new Pair<>(finalReaders, finalWriters);
                }
            }
            return new ArrayMonitor(newMonitor);
        } else {
            return null;
        }
    }

}
