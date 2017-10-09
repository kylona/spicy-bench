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
public interface HjProcedure<T> {

    public void apply(T arg);
}
