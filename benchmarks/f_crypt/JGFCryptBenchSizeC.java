package f_crypt;
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

//package hj.applications.jgf.arrayview;
//import hj.applications.jgf.arrayview.crypt.JGFCryptBench;
//import hj.applications.jgf.jgfutil.*; 
import java.lang.Integer; 
import static edu.rice.hj.Module0.*;

public class JGFCryptBenchSizeC { 

	public static void main(String argv[]){

		launchHabaneroApp(() -> {
			int nthreads = 1;

			int niter = 1;
			for (int i=0; i<argv.length-1; i++)
				if (argv[i].startsWith("--iter")) {
					niter = Integer.parseInt(argv[i+1]);
				}
			System.out.println("Iterations="+niter);
			JGFInstrumentor.printHeader(2,2);

			double totalTime = 0.0;
			java.text.DecimalFormat df = new java.text.DecimalFormat ("#.##");
			for (int iter = 0; iter < niter; iter++) {

				double t1 = System.nanoTime()/1e6;
				JGFCryptBench cb = new JGFCryptBench(nthreads,iter); 

				cb.JGFrun(2);
				double t2 = System.nanoTime()/1e6;
				System.out.println("time = " + df.format(t2-t1) + " msec");
				if (iter != 0)
					totalTime = totalTime + t2 - t1;
			}
			System.out.println("Mean execution time = " + df.format(totalTime/(niter-1)) + " msec");

		});
	}
}
