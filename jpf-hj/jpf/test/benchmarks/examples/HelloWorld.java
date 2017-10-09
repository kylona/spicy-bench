package benchmarks.examples;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *

 package test.lib.examples;

 import static edu.rice.hj.Module1.finish;
 import static edu.rice.hj.Module1.launchHabaneroApp;
 import edu.rice.hj.runtime.actors.Actor;

 /**
 *
 * @author Peter Anderson <anderson.peter@byu.edu>
 *
 public class HelloWorld {

 public static void main(final String[] args) {
 launchHabaneroApp(() -> {
 finish(() -> {
 EchoActor actor = new EchoActor();
 actor.start(); // don’t forget to start the actor
 actor.send("Hello"); // asynchronous send (returns immediately)
 actor.send("World");
 actor.send(EchoActor.STOP_MSG);
 });
 });
 }
    
 private static class EchoActor extends Actor<Object> {
 static final Object STOP_MSG = new Object();
 private int messageCount = 0;

 @Override
 protected void process(final Object msg) {
 if (msg.equals(STOP_MSG)){
 System.out.println("Message-" + messageCount + ": terminating.");
 exit();
 } else {
 messageCount += 1;
 System.out.println("Message-" + messageCount + ": " + msg);
 }
 }
 }
 }

 */
