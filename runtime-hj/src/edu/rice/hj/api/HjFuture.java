/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.rice.hj.api;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 * @param <V>
 */
public interface HjFuture<V> {

    public boolean cancel();

    public boolean failed();

    public V get();

    public boolean resolved();

    public V safeGet();
}
