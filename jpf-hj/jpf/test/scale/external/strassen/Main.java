package scale.external.strassen;

import java.util.List;
import static edu.rice.hj.Module0.*;
import java.util.ArrayList;

/**
 * The driving benchmark class to test the linked matrix using strassens method
 * @author Matthew Johnston
 */
public class Main {

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {

		launchHabaneroApp(() -> {
			int[][] A, B;

			int range = 1000;
			//int n = 4096; //or 1024;
			int n = 32;
			int a = 64;

			// Build the arrays

			A = new int[n][n];
			B = new int[n][n];

			// Fill the arrays with data
			for (int x = 0; x < n; x++) {
				for (int y = 0; y < n; y++) {
					A[x][y] = (int) (Math.random() * range + 1);
					B[x][y] = (int) (Math.random() * range + 1);
				}
			}


			System.out.println("Matrix Size: " + n);
			// =================================================================
			// Standard Test
			// =================================================================
			Matrix p = new Matrix(A, A.length);
			Matrix q = new Matrix(B, B.length);


			// =================================================================
			// Hybrid Test
			// =================================================================
			int numIters = 31;
			final List<Double> execTimes = new ArrayList<Double>(numIters);
			java.text.DecimalFormat df = new java.text.DecimalFormat ("#.##");
			double totalTime = 0.0;
			for (int i = 0; i < numIters; i++) {
				double t1 = System.nanoTime()/1e6;

				finish(() -> {
					Matrix.hybrid(p, q, a);
				});    	

				double t2 = System.nanoTime()/1e6;
				System.out.println("time = " + df.format(t2-t1) + " msec");
				if (i != 0)
					totalTime += (t2-t1);
				execTimes.add(t2-t1);
				System.gc(); System.gc(); System.gc(); System.gc();// Make a call to the garbage collector. Maybe it'll pick something up for once
			}
			new Mean("strassen", execTimes);
			System.out.println("Mean execution time = " + df.format(totalTime/(numIters-1)) + " msec");
		});
	}
}
