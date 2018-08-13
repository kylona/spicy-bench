package edu.rice.hj;

import edu.rice.hj.api.HjActor;
import edu.rice.hj.api.HjCallable;
import edu.rice.hj.api.HjProcedure;
import edu.rice.hj.api.HjRunnable;
import edu.rice.hj.runtime.actors.Actor;
import hj.lang.Runtime;
import hj.util.Pair;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public class Module2 extends Module1 {

    public static void isolated(HjRunnable runnable) {
        hj.lang.Runtime.startIsolation();
        runnable.run();
        hj.lang.Runtime.stopIsolation();
    }

    public static void isolated(Object participant1, HjRunnable runnable) {
        hj.lang.Runtime.startIsolation();
        runnable.run();
        hj.lang.Runtime.stopIsolation();
    }

    public static void isolated(Object participant1, Object participant2, HjRunnable runnable) {
        hj.lang.Runtime.startIsolation();
        runnable.run();
        hj.lang.Runtime.stopIsolation();
    }

    public static void isolated(Object[] participants, HjRunnable runnable) {
        hj.lang.Runtime.startIsolation();
        runnable.run();
        hj.lang.Runtime.stopIsolation();
    }

    public static void isolated(Object participant1, Object participant2, Object participant3, HjRunnable runnable) {
        hj.lang.Runtime.startIsolation();
        runnable.run();
        hj.lang.Runtime.stopIsolation();
    }

    public static <V> V isolatedWithReturn(HjCallable<V> callable) {
        Runtime.startIsolation();
        final V v = callable.call();
        Runtime.stopIsolation();
        return v;
    }

    public static <V> V isolatedWithReturn(Object participant1, HjCallable<V> callable) {
        Runtime.startIsolation();
        final V v = callable.call();
        Runtime.stopIsolation();
        return v;
    }

    public static <V> V isolatedWithReturn(Object participant1, Object participant2, HjCallable<V> callable) {
        Runtime.startIsolation();
        final V v = callable.call();
        Runtime.stopIsolation();
        return v;
    }

    public static <V> V isolatedWithReturn(Object participant1, Object participant2, Object participant3, HjCallable<V> callable) {
        Runtime.startIsolation();
        final V v = callable.call();
        Runtime.stopIsolation();
        return v;
    }

    public static <V> V isolatedWithReturn(Object[] participants, HjCallable<V> callable) {
        Runtime.startIsolation();
        final V v = callable.call();
        Runtime.stopIsolation();
        return v;
    }

    public static Object readMode(Object wrapee) {
        System.out.println("Object modes not supported in verification library.");
        return wrapee;
    }

    public static Object writeMode(Object wrapee) {
        System.out.println("Object modes not supported in verification library.");
        return wrapee;
    }

    public static <MsgType> HjActor<MsgType> newActor(HjProcedure<Pair<HjActor<MsgType>, MsgType>> processor) {
        return new Actor<MsgType>() {

            @Override
            protected void onPostExit() {

            }

            @Override
            protected void onPostStart() {

            }

            @Override
            protected void onPreExit() {

            }

            @Override
            protected void onPreStart() {

            }

            @Override
            protected void process(MsgType msg) {
                processor.apply((Pair<HjActor<MsgType>, MsgType>) msg);
            }
        };
    }
}
