package f_series;
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
 *                                                                         *
 *      This version copyright (c) The University of Edinburgh, 1999.      *
 *                         All rights reserved.                            *
 *                                                                         *
 **************************************************************************/

import static edu.rice.hj.Module0.*;
import java.lang.Integer;
import java.util.List;
import java.util.ArrayList;

public class JGFSeriesBenchSizeC{ 

	public static void main(String argv[]){

		launchHabaneroApp(() -> {

			int nthreads = 1;
			int niter = 2;
			final List<Double> execTimes = new ArrayList<Double>(niter);
			for (int i=0; i<argv.length-1; i++)
				if (argv[i].startsWith("--iter")) {
					niter = Integer.parseInt(argv[i+1]);
				}
			System.out.println("Iterations="+niter);

			double totalTime = 0.0;
			java.text.DecimalFormat df = new java.text.DecimalFormat ("#.##");
			JGFInstrumentor.printHeader(2,2);
			for (int iter = 0; iter < niter; iter++) {
				final int iter1 = iter;
				double t1 = System.nanoTime()/1e6;
				finish (() -> {
					JGFSeriesBench se = new JGFSeriesBench(nthreads,iter1); 
					se.JGFrun(3);
				});
				double t2 = System.nanoTime()/1e6;
				System.out.println("time = " + df.format(t2-t1) + " msec");
				if (iter != 0)
					totalTime = totalTime + t2 - t1;
				execTimes.add(t2-t1);
			}
			System.out.println("Mean execution time = " + df.format(totalTime/(niter-1)) + " msec");
			new Mean("series", execTimes);
		});
	}
}
