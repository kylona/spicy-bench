/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package graphtests;

import static edu.rice.hj.Module0.finish;
import static edu.rice.hj.Module1.forAsync;
import edu.rice.hj.api.HjPoint;
import edu.rice.hj.api.HjSuspendable;
import edu.rice.hj.api.HjSuspendingProcedure;
import hj.runtime.region.point;
import hj.runtime.region.PointFactory;
import java.util.ArrayList;

/**
 *
 * @author Kris
 */
public class CryptTest {
    
    public void cipher_idea(final byte [] text1, int length1,final byte [] text2, int length2, final int [] key)
    {
        final int len = (length1 % 8 == 0) ? length1 / 8 : length1 / 8 + 1;
            
        finish(new HjSuspendable() {
                    @Override
                    public void run() {
                        ArrayList<HjPoint> points = new ArrayList<HjPoint>();
                        for(int i=0;i<len;i++){
                            points.add(PointFactory.point(i));
                        }

                        forAsync(points, new HjSuspendingProcedure<HjPoint>(){
                            @Override
                            public void apply(HjPoint p){
                                point po = (point) p;
                                 int i = po.get(0) * 8;
                                // moved into loop body for privatization
                                int ik;                     // Index into key array.
                                int x1, x2, x3, x4, t1, t2; // Four "16-bit" blocks, two temps.
                                int r;                      // Eight rounds of processing.

                                ik = 0;                 // Restart key index.
                                r = 8;                  // Eight rounds of processing.

                                // Load eight plain1 bytes as four 16-bit "unsigned" integers.
                                // Masking with 0xff prevents sign extension with cast to int.

                                // i1 is replaced by expression with i.
                                x1 = text1[i] & 0xff;          // Build 16-bit x1 from 2 bytes,
                                x1 |= (text1[i+1] & 0xff) << 8;  // assuming low-order byte first.
                                x2 = text1[i+2] & 0xff;
                                x2 |= (text1[i+3] & 0xff) << 8;
                                x3 = text1[i+4] & 0xff;
                                x3 |= (text1[i+5] & 0xff) << 8;
                                x4 = text1[i+6] & 0xff;
                                x4 |= (text1[i+7] & 0xff) << 8;

                                do {
                                    // 1) Multiply (modulo 0x10001), 1st text sub-block
                                    // with 1st key sub-block.

                                    x1 = (int) ((long) x1 * key[ik++] % 0x10001L & 0xffff);

                                    // 2) Add (modulo 0x10000), 2nd text sub-block
                                    // with 2nd key sub-block.

                                    x2 = x2 + key[ik++] & 0xffff;

                                    // 3) Add (modulo 0x10000), 3rd text sub-block
                                    // with 3rd key sub-block.

                                    x3 = x3 + key[ik++] & 0xffff;

                                    // 4) Multiply (modulo 0x10001), 4th text sub-block
                                    // with 4th key sub-block.

                                    x4 = (int) ((long) x4 * key[ik++] % 0x10001L & 0xffff);

                                    // 5) XOR results from steps 1 and 3.

                                    t2 = x1 ^ x3;

                                    // 6) XOR results from steps 2 and 4.
                                    // Included in step 8.

                                    // 7) Multiply (modulo 0x10001), result of step 5
                                    // with 5th key sub-block.

                                    t2 = (int) ((long) t2 * key[ik++] % 0x10001L & 0xffff);

                                    // 8) Add (modulo 0x10000), results of steps 6 and 7.

                                    t1 = t2 + (x2 ^ x4) & 0xffff;

                                    // 9) Multiply (modulo 0x10001), result of step 8
                                    // with 6th key sub-block.

                                    t1 = (int) ((long) t1 * key[ik++] % 0x10001L & 0xffff);

                                    // 10) Add (modulo 0x10000), results of steps 7 and 9.

                                    t2 = t1 + t2 & 0xffff;

                                    // 11) XOR results from steps 1 and 9.

                                    x1 ^= t1;

                                    // 14) XOR results from steps 4 and 10. (Out of order).

                                    x4 ^= t2;

                                    // 13) XOR results from steps 2 and 10. (Out of order).

                                    t2 ^= x2;

                                    // 12) XOR results from steps 3 and 9. (Out of order).

                                    x2 = x3 ^ t1;

                                    x3 = t2;        // Results of x2 and x3 now swapped.

                                } while(--r != 0);  // Repeats seven more rounds.

                                // Final output transform (4 steps).

                                // 1) Multiply (modulo 0x10001), 1st text-block
                                // with 1st key sub-block.

                                x1 = (int) ((long) x1 * key[ik++] % 0x10001L & 0xffff);

                                // 2) Add (modulo 0x10000), 2nd text sub-block
                                // with 2nd key sub-block. It says x3, but that is to undo swap
                                // of subblocks 2 and 3 in 8th processing round.

                                x3 = x3 + key[ik++] & 0xffff;

                                // 3) Add (modulo 0x10000), 3rd text sub-block
                                // with 3rd key sub-block. It says x2, but that is to undo swap
                                // of subblocks 2 and 3 in 8th processing round.

                                x2 = x2 + key[ik++] & 0xffff;

                                // 4) Multiply (modulo 0x10001), 4th text-block
                                // with 4th key sub-block.

                                x4 = (int) ((long) x4 * key[ik++] % 0x10001L & 0xffff);

                                // Repackage from 16-bit sub-blocks to 8-bit byte array text2.

                                // i2 is replaced by expression with i.
                                text2[i] = (byte) x1;
                                text2[i+1] = (byte) (x1 >>> 8);
                                text2[i+2] = (byte) x3;                // x3 and x2 are switched
                                text2[i+3] = (byte) (x3 >>> 8);        // only in name.
                                text2[i+4] = (byte) x2;
                                text2[i+5] = (byte) (x2 >>> 8);
                                text2[i+6] = (byte) x4;
                                text2[i+7] = (byte) (x4 >>> 8);
                            }
                        });
                    }
                    });
                }
}
