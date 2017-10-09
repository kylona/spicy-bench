/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.rice.hj.api;

/**
 *
 * @author kristophermiles
 */
public interface HjRegion2D extends HjRegion {

    public int[] lowerLimits();

    public int toLinearIndex(int regionIndex0, int regionIndex1);

    public int toLinearIndex(int[] regionIndices);

    public int[] upperLimits();
}
