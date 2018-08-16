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
public interface HjMetrics {

    public abstract long totalWork();

    public abstract long criticalPathLength();

    public abstract double idealParallelism();
}
