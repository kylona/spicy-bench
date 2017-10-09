/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hj.lang;

import hj.util.SyncLock;

/**
 *
 * @author bchase
 */
public class Runtime {

    // There is only one global lock used for the isolation statement
    // Many of these methods are empty because the compiler uses these interfaces
    // but the global lock doesn't need them all
    private final static SyncLock lock = new SyncLock();
    private final static SyncLock IDlock = new SyncLock();
    private static boolean withinIsolated = true;
    private static int nextActivityID = 0;

    public static int getUniqueActivityID() {
        int ID;
        try {
            IDlock.lock();
            ID = nextActivityID;
            nextActivityID++;
            return ID;
        } finally {
            IDlock.unlock();
        }
    }
    
    public static boolean isWithinIsolated(){
        return withinIsolated;
    }

    public static boolean lockReleased() {
        return !lock.isHeldByCurrentThread();
    }

    public static SyncLock getLock() {
        return lock;
    }

    public static void startIsolation() {
        lock.lock();
        withinIsolated = false;
    }

    public static void stopIsolation() {
        lock.unlock();
        withinIsolated = true;
    }

}
