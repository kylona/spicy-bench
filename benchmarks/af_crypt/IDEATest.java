package af_crypt;
/**************************************************************************
 *                                                                         *
 *             Java Grande Forum Benchmark Suite - Version 2.0             *
 *                                                                         *
 *                            produced by                                  *
 *                                                                         *
 *                  Java Grande Benchmarking Project                       *
 *                                                                         *
 *                                at                                       *
 *                                                                         *
 *                Edinburgh Parallel Computing Centre                      *
 *                                                                         * 
 *                email: epcc-javagrande@epcc.ed.ac.uk                     *
 *                                                                         *
 *                  Original version of this code by                       *
 *                 Gabriel Zachmann (zach@igd.fhg.de)                      *
 *                                                                         *
 *      This version copyright (c) The University of Edinburgh, 1999.      *
 *                         All rights reserved.                            *
 *                                                                         *
 **************************************************************************/


/**
 * Class IDEATest
 *
 * This test performs IDEA encryption then decryption. IDEA stands
 * for International Data Encryption Algorithm. The test is based
 * on code presented in Applied Cryptography by Bruce Schnier,
 * which was based on code developed by Xuejia Lai and James L.
 * Massey.

 **/

//package hj.applications.jgf.arrayview.crypt;

import java.util.*;
import edu.rice.hj.api.SuspendableException;
import static edu.rice.hj.Module1.*;

class IDEATest
{

	// Declare class data. Byte buffer plain1 holds the original
	// data for encryption, crypt1 holds the encrypted data, and
	// plain2 holds the decrypted data, which should match plain1
	// byte for byte.

	int array_rows; 

	byte [] plain1_b;       // Buffer for plaintext data.
	byte [] crypt1_b;       // Buffer for encrypted data.
	byte [] plain2_b;       // Buffer for decrypted data.
	
	short [] userkey_b;     // Key for encryption/decryption.
	
	int [] Z_b;             // Encryption subkey (userkey derived).
	int [] DK_b;            // Decryption subkey (userkey derived).
	
	int nthreads;

	IDEATest(int nthreads) {
		this.nthreads = nthreads;
	}

	void Do(int iter) throws SuspendableException
	{
		// Start the stopwatch.       
		JGFInstrumentor.startTimer("Section2:Crypt(" + iter + "):Kernel"); 		

		cipher_idea(plain1_b, plain1_b.length, crypt1_b, crypt1_b.length, Z_b);     // Encrypt plain1.
		cipher_idea(crypt1_b, crypt1_b.length, plain2_b, plain2_b.length, DK_b);    // Decrypt.

		//cipher_idea(plain1_b, plain1_b.length, crypt1_b, crypt1_b.length, Z_b);     // Encrypt plain1.
		//cipher_idea(crypt1_b, crypt1_b.length, plain2_b, plain2_b.length, DK_b);    // Decrypt.

		// Stop the stopwatch.
		JGFInstrumentor.stopTimer("Section2:Crypt(" + iter + "):Kernel");

	}

	/*
	 * buildTestData
	 *
	 * Builds the data used for the test -- each time the test is run.
	 */

	void buildTestData()
	{
        plain1_b = new byte [array_rows];
        plain2_b = new byte [array_rows];
        crypt1_b = new byte [array_rows];

		// Create three byte arrays that will be used (and reused) for
		// encryption/decryption operations.

		
		Random rndnum = new Random(136506717L);  // Create random number generator.


		// Allocate three arrays to hold keys: userkey is the 128-bit key.
		// Z is the set of 16-bit encryption subkeys derived from userkey,
		// while DK is the set of 16-bit decryption subkeys also derived
		// from userkey. NOTE: The 16-bit values are stored here in
		// 32-bit int arrays so that the values may be used in calculations
		// as if they are unsigned. Each 64-bit block of plaintext goes
		// through eight processing rounds involving six of the subkeys
		// then a final output transform with four of the keys; (8 * 6)
		// + 4 = 52 subkeys.

		userkey_b = new short [8];  // User key has 8 16-bit shorts.
		//userkey = new arrayView (userkey_b, 0, [0:userkey_b.length-1]);
		
		Z_b = new int [52];         // Encryption subkey (user key derived).
		//Z = new arrayView (Z_b, 0, [0:Z_b.length-1]);
		DK_b = new int [52];        // Decryption subkey (user key derived).
		//DK = new arrayView (DK_b, 0, [0:DK_b.length-1]);

		// Generate user key randomly; eight 16-bit values in an array.

		for (int i = 0; i < 8; i++)
		{
			// Again, the random number function returns int. Converting
			// to a short type preserves the bit pattern in the lower 16
			// bits of the int and discards the rest.

			userkey_b[i] = (short) rndnum.nextInt();
		}

		// Compute encryption and decryption subkeys.

		calcEncryptKey();
		calcDecryptKey();

		// Fill plain1 with "text."
		for (int i = 0; i < array_rows; i++)
		{
			plain1_b[i] = (byte) i; 

			// Converting to a byte
			// type preserves the bit pattern in the lower 8 bits of the
			// int and discards the rest.
		}
	}

	/*
	 * calcEncryptKey
	 *
	 * Builds the 52 16-bit encryption subkeys Z[] from the user key and
	 * stores in 32-bit int array. The routing corrects an error in the
	 * source code in the Schnier book. Basically, the sense of the 7-
	 * and 9-bit shifts are reversed. It still works reversed, but would
	 * encrypted code would not decrypt with someone else's IDEA code.
	 */

	private void calcEncryptKey()
	{
		int j;                       // Utility variable.

		for (int i = 0; i < 52; i++) // Zero out the 52-int Z array.
			Z_b[i] = 0;

		for (int i = 0; i < 8; i++)  // First 8 subkeys are userkey itself.
		{
			Z_b[i] = userkey_b[i] & 0xffff;     // Convert "unsigned"
			// short to int.
		}

		// Each set of 8 subkeys thereafter is derived from left rotating
		// the whole 128-bit key 25 bits to left (once between each set of
		// eight keys and then before the last four). Instead of actually
		// rotating the whole key, this routine just grabs the 16 bits
		// that are 25 bits to the right of the corresponding subkey
		// eight positions below the current subkey. That 16-bit extent
		// straddles two array members, so bits are shifted left in one
		// member and right (with zero fill) in the other. For the last
		// two subkeys in any group of eight, those 16 bits start to
		// wrap around to the first two members of the previous eight.

		for (int i = 8; i < 52; i++)
		{
			j = i % 8;
			if (j < 6)
			{
				Z_b[i] = ((Z_b[i -7]>>>9) | (Z_b[i-6]<<7)) // Shift and combine.
						& 0xFFFF;                    // Just 16 bits.
				continue;                            // Next iteration.
			}

			if (j == 6)    // Wrap to beginning for second chunk.
			{
				Z_b[i] = ((Z_b[i -7]>>>9) | (Z_b[i-14]<<7))
						& 0xFFFF;
				continue;
			}

			// j == 7 so wrap to beginning for both chunks.

			Z_b[i] = ((Z_b[i -15]>>>9) | (Z_b[i-14]<<7))
					& 0xFFFF;
		}
	}

	/*
	 * calcDecryptKey
	 *
	 * Builds the 52 16-bit encryption subkeys DK[] from the encryption-
	 * subkeys Z[]. DK[] is a 32-bit int array holding 16-bit values as
	 * unsigned.
	 */

	private void calcDecryptKey()
	{
		int j, k;                 // Index counters.
		int t1, t2, t3;           // Temps to hold decrypt subkeys.

		t1 = inv(Z_b[0]);           // Multiplicative inverse (mod x10001).
		t2 = - Z_b[1] & 0xffff;     // Additive inverse, 2nd encrypt subkey.
		t3 = - Z_b[2] & 0xffff;     // Additive inverse, 3rd encrypt subkey.

		DK_b[51] = inv(Z_b[3]);       // Multiplicative inverse (mod x10001).
		DK_b[50] = t3;
		DK_b[49] = t2;
		DK_b[48] = t1;

		j = 47;                   // Indices into temp and encrypt arrays.
		k = 4;
		for (int i = 0; i < 7; i++)
		{
			t1 = Z_b[k++];
			DK_b[j--] = Z_b[k++];
			DK_b[j--] = t1;
			t1 = inv(Z_b[k++]);
			t2 = -Z_b[k++] & 0xffff;
			t3 = -Z_b[k++] & 0xffff;
			DK_b[j--] = inv(Z_b[k++]);
			DK_b[j--] = t2;
			DK_b[j--] = t3;
			DK_b[j--] = t1;
		}

		t1 = Z_b[k++];
		DK_b[j--] = Z_b[k++];
		DK_b[j--] = t1;
		t1 = inv(Z_b[k++]);
		t2 = -Z_b[k++] & 0xffff;
		t3 = -Z_b[k++] & 0xffff;
		DK_b[j--] = inv(Z_b[k++]);
		DK_b[j--] = t3;
		DK_b[j--] = t2;
		DK_b[j--] = t1;
	}

	/*
	 * cipher_idea
	 *
	 * IDEA encryption/decryption algorithm. It processes plaintext in
	 * 64-bit blocks, one at a time, breaking the block into four 16-bit
	 * unsigned subblocks. It goes through eight rounds of processing
	 * using 6 new subkeys each time, plus four for last step. The source
	 * text is in array text1, the destination text goes into array text2
	 * The routine represents 16-bit subblocks and subkeys as type int so
	 * that they can be treated more easily as unsigned. Multiplication
	 * modulo 0x10001 interprets a zero sub-block as 0x10000; it must to
	 * fit in 16 bits.
	 */

	//public void cipher_idea(final byte [.] text1, int length1, final byte [.] text2, int length2, final int [.] key)
	public void cipher_idea(final byte [] text1, int length1, final byte [] text2, int length2, final int [] key) throws SuspendableException
	{
		// int ik;                     // Index into key array.
		// int x1, x2, x3, x4, t1, t2; // Four "16-bit" blocks, two temps.
		// int r;                      // Eight rounds of processing.
		// int i1 = 0;                 // Index into first text array.
		// int i2 = 0;                 // Index into second text array.

		/* 1. Loop index normalization
before: for (int i = 0; i < text1.length; i += 8)
after:  for (int ni = 0; ni < text1.length / 8; ni++)
2. Divides iterration space ni by nproc
- Creating too many async threads may cause large overhead.

Question: multi-dimensional array is better for text1 and text2? */

		final int len = (length1 % 8 == 0) ?
				length1 / 8 : length1 / 8 + 1;
		//final int nproc = (nthreads<=0 ||nthreads>len)? len:nthreads;
		//finish forasync (point [proc]:[0:nproc-1]) {
		//int startI = (int)((long)len * proc / nproc);
		//int lastI = (int)((long)len * (proc+1) / nproc - 1);

		//for (int ni = startI; ni <= lastI; ni++)
		//{
		//finish forasync (point [ni]:[0:len-1]) {
		forAll(0, len-1, (ni_1) -> {
			//for(int ni_1=0; ni_1 < len; ni_1++) {
			final int ni = ni_1;
			int i = ni * 8;

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

			//}   // End for loop.
		});   // End forasync loop.
	}   // End routine.

	/*
	 * mul
	 *
	 * Performs multiplication, modulo (2**16)+1. This code is structured
	 * on the assumption that untaken branches are cheaper than taken
	 * branches, and that the compiler doesn't schedule branches.
	 * Java: Must work with 32-bit int and one 64-bit long to keep
	 * 16-bit values and their products "unsigned." The routine assumes
	 * that both a and b could fit in 16 bits even though they come in
	 * as 32-bit ints. Lots of "& 0xFFFF" masks here to keep things 16-bit.
	 * Also, because the routine stores mod (2**16)+1 results in a 2**16
	 * space, the result is truncated to zero whenever the result would
	 * zero, be 2**16. And if one of the multiplicands is 0, the result
	 * is not zero, but (2**16) + 1 minus the other multiplicand (sort
	 * of an additive inverse mod 0x10001).

	 * NOTE: The java conversion of this routine works correctly, but
	 * is half the speed of using Java's modulus division function (%)
	 * on the multiplication with a 16-bit masking of the result--running
	 * in the Symantec Caje IDE. So it's not called for now; the test
	 * uses Java % instead.
	 */

	private int mul(int a, int b) throws ArithmeticException
	{
		long p;             // Large enough to catch 16-bit multiply
		// without hitting sign bit.
		if (a != 0)
		{
			if(b != 0)
			{
				p = (long) a * b;
				b = (int) p & 0xFFFF;       // Lower 16 bits.
				a = (int) p >>> 16;         // Upper 16 bits.

				return (b - a + (b < a ? 1 : 0) & 0xFFFF);
			}
			else
				return ((1 - a) & 0xFFFF);  // If b = 0, then same as
			// 0x10001 - a.
		}
		else                                // If a = 0, then return
			return((1 - b) & 0xFFFF);       // same as 0x10001 - b.
	}

	/*
	 * inv
	 *
	 * Compute multiplicative inverse of x, modulo (2**16)+1 using
	 * extended Euclid's GCD (greatest common divisor) algorithm.
	 * It is unrolled twice to avoid swapping the meaning of
	 * the registers. And some subtracts are changed to adds.
	 * Java: Though it uses signed 32-bit ints, the interpretation
	 * of the bits within is strictly unsigned 16-bit.
	 */

	private int inv(int x)
	{
		int t0, t1;
		int q, y;

		if (x <= 1)             // Assumes positive x.
			return(x);          // 0 and 1 are self-inverse.

		t1 = 0x10001 / x;       // (2**16+1)/x; x is >= 2, so fits 16 bits.
		y = 0x10001 % x;
		if (y == 1)
			return((1 - t1) & 0xFFFF);

		t0 = 1;
		do {
			q = x / y;
			x = x % y;
			t0 += q * t1;
			if (x == 1) return(t0);
			q = y / x;
			y = y % x;
			t1 += q * t0;
		} while (y != 1);

		return((1 - t1) & 0xFFFF);
	}

	/*
	 * freeTestData
	 *
	 * Nulls arrays and forces garbage collection to free up memory.
	 */

	void freeTestData()
	{
		/*  // avoid nullable, this is X10
            plain1 = null;
            crypt1 = null;
            plain2 = null;
            userkey = null;
            Z = null;
            DK = null;
		 */
		System.gc();                // Force garbage collection.
	}


}