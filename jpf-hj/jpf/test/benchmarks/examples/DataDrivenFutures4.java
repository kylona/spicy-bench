package benchmarks.examples;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *

 package test.lib.examples;

 import edu.rice.hj.api.HjFuture;
 import static edu.rice.hj.Module1.*;

 /**
 * Example to verify use of metrics with DDFs.
 *
 * @author Shams Imam (shams@rice.edu)
 *
 public class DataDrivenFutures4 {

 /**
 * <p>main.</p>
 *
 * @param args an array of {@link java.lang.String} objects.
 *
 public static void main(final String[] args) {
 launchHabaneroApp(() -> {
 finish(() -> {
 final HjFuture<Integer> A = future(() -> {
 return 1;
 });
 final HjFuture<Integer> B = futureAwait(A, () -> {
 return 1 + A.get();
 });
 final HjFuture<Integer> C = futureAwait(A, () -> {
 return 1 + A.get();
 });
 final HjFuture<Integer> D = futureAwait(B, C, () -> {
 return 1 + Math.max(B.get(), C.get());
 });
 final HjFuture<Integer> E = futureAwait(C, () -> {
 return 1 + C.get();
 });
 final HjFuture<Integer> F = futureAwait(D, E, () -> {
 final int res = 1 + Math.max(D.get(), E.get());
 System.out.println("Res = " + res);
 return res;
 });
 });
 });
 }

 }
 */
