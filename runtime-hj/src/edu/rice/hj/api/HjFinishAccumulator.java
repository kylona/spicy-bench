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
public abstract interface HjFinishAccumulator {

    public abstract void put(Number paramNumber);

    public abstract void put(int paramInt);

    public abstract void put(double paramDouble);

    public abstract Number get();

    public abstract Class getType();

    public abstract HjOperator getOperator();
}
