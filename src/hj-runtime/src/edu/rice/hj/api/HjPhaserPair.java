/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.rice.hj.api;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 */
public final class HjPhaserPair {

    public final HjPhaser phaser;
    public final HjPhaserMode mode;

    public HjPhaserPair(HjPhaser phaser, HjPhaserMode mode) {
        this.phaser = phaser;
        this.mode = mode;
    }

    public static HjPhaserPair PhaserPair(HjPhaser phaser, HjPhaserMode mode) {
        return new HjPhaserPair(phaser, mode);
    }
}
