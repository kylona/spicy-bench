/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.rice.hj.api;

import hj.runtime.wsh.Activity;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public interface HjPhaser {

    public void doNext();

    public void doWait();

    public void drop();

    public HjPhaserMode getPhaserMode();

    public int getSigPhase();

    public int getWaitPhase();

    public HjPhaserPair inMode(HjPhaserMode phaserMode);

    public void signal();
}
