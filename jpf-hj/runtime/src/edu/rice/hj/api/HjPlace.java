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
public abstract interface HjPlace {

    public abstract int id();

    public abstract HjPlace place(int paramInt);

    public abstract int numPlaces();

    public abstract HjPlace nextPlace();

    public abstract HjPlace prevPlace();
}
