package benchmarks.examples;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *

 package test.lib.examples;

 import edu.rice.hj.api.HjPhaser;
 import edu.rice.hj.api.HjPhaserPair;
 import java.util.Arrays;
 import java.util.List;

 import static edu.rice.hj.Module1.*;
 import static edu.rice.hj.api.HjPhaserMode.SIG;
 import static edu.rice.hj.api.HjPhaserMode.SIG_WAIT;
 import static edu.rice.hj.api.HjPhaserMode.WAIT;

 /**
 * <p>Phaser3Asyncs class.</p>
 *
 * @author Vivek Sarkar (vsarkar@rice.edu)
 *
 public class Phaser3Asyncs {

 /**
 * <p>main.</p>
 *
 * @param args an array of {@link java.lang.String} objects.
 *
 public static void main(final String[] args) {
 launchHabaneroApp(() -> {
 finish(() -> {
 final HjPhaser ph1 = newPhaser(SIG_WAIT);
 final HjPhaser ph2 = newPhaser(SIG_WAIT);
 final HjPhaser ph3 = newPhaser(SIG_WAIT);

 final List<HjPhaserPair> phList1 = Arrays.asList(
 ph1.inMode(SIG),
 ph2.inMode(WAIT));
 final List<HjPhaserPair> phList2 = Arrays.asList(
 ph2.inMode(SIG),
 ph1.inMode(WAIT),
 ph3.inMode(WAIT));
 final List<HjPhaserPair> phList3 = Arrays.asList(
 ph3.inMode(SIG),
 ph2.inMode(WAIT));

 asyncPhased(phList1, () -> {
 // Phase 0
 next();
 // Phase 1
 });

 asyncPhased(phList2, () -> {
 // Phase 0
 next();
 // Phase 1
 });

 asyncPhased(phList3, () -> {
 // Phase 0
 next();
 // Phase 1
 });

 });
 });
 }
 }
 */
