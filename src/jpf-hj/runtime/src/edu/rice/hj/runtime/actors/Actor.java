/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.rice.hj.runtime.actors;

import edu.rice.hj.api.HjActor;
import edu.rice.hj.api.HjSuspendable;
import edu.rice.hj.api.SuspendableException;
import hj.runtime.wsh.Activity;
import java.util.LinkedList;
import hj.util.SyncLock;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 * @param <MessageType>
 */
public abstract class Actor<MessageType> implements HjActor<MessageType> {

    private final ActivityActor a_actor;
    private ACTOR_STATUS status;
    private HjSuspendable suspendable;
    private final LinkedList<MessageType> mailbox;
    private final SyncLock lock;

    public Actor() {
        a_actor = new ActivityActor();
        status = ACTOR_STATUS.NEW;
        mailbox = new LinkedList<>();
        lock = new SyncLock();
    }

    @Override
    public void start() {
        onPreStart();
        status = ACTOR_STATUS.STARTED;
        onPostStart();
        tryProcessMessage();
    }

    @Override
    public void exit() {
        onPreExit();
        status = ACTOR_STATUS.TERMINATED;
        onPostExit();
    }

    protected void handleThrowable(Throwable th) {

    }

    /*
     Specify code to be executed after actor has terminated
     */
    protected abstract void onPostExit();

    /*
     Specify code to be executed after actor has started
     */
    protected abstract void onPostStart();

    /*
     Specify code to be executed before actor is terminated
     */
    protected abstract void onPreExit();

    /*
     Specify code to be executed before actor is started
     */
    protected abstract void onPreStart();

    public boolean hasStarted() {
        if (status != ACTOR_STATUS.NEW) {
            return true;
        } else {
            return false;
        }
    }

    /*
     Sends a message to the actor. If the actor has not terminated then this
     methods will effectively add one message to the mailbox. 
     */
    @Override
    public void send(MessageType msg) {
        if (status != ACTOR_STATUS.TERMINATED) {
            lock.lock();
            try {
                mailbox.add(msg);
            } finally {
                lock.unlock();
            }
        }

    }

    @Override
    public void pause() {
        status = ACTOR_STATUS.PAUSED;
    }

    @Override
    public void resume() throws IllegalStateException {
        if (status == ACTOR_STATUS.PAUSED) {
            status = ACTOR_STATUS.STARTED;
        } else {
            throw new IllegalStateException();
        }
    }

    protected abstract void process(final MessageType msg);

    /*
     Try and process message from the mailbox
     */
    protected void tryProcessMessage() {
        suspendable = new HjSuspendable() {
            public void run() {
                while (status == ACTOR_STATUS.PAUSED
                        || status == ACTOR_STATUS.STARTED) {
                    while (status == ACTOR_STATUS.STARTED) {
                        lock.lock();
                        try {
                            if (mailbox.size() > 0) {
                                process(mailbox.removeFirst());
                            }
                        } finally {
                            lock.unlock();
                        }
                    }
                }
            }
        };
        a_actor.setSuspendable(suspendable);
        a_actor.start();
    }

    public enum ACTOR_STATUS {

        NEW, STARTED, PAUSED, TERMINATED;
    }

    private class ActivityActor extends Activity {

        private HjSuspendable suspendable;

        public ActivityActor() {
            super(false, null, null);
        }

        public void setSuspendable(HjSuspendable suspendable) {
            this.suspendable = suspendable;
        }

        @Override
        public void run() {
            try {
                try {
                    suspendable.run();
                } catch (SuspendableException ex) {
                    System.err.println("Caught Suspendable Exception");
                    ex.printStackTrace();
                }
            } finally {
                assert (lockReleased()) : "Unreleased Lock";
                stopFinish();
                super.unregisterFromPhasers();
            }
        }

    }

}
