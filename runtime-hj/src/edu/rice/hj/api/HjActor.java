/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.rice.hj.api;

/**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 * @param <MessageType>
 */
public interface HjActor<MessageType> {

    void exit();

    void pause();

    void resume();

    void send(MessageType msg);

    void start();
}
