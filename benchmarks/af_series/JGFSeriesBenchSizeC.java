package af_series;

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


//import series.*; 
//import jgfutil.*; 

import java.lang.Integer; 
import static edu.rice.hj.Module0.launchHabaneroApp; 

public class JGFSeriesBenchSizeC{ 

	public static void main(String argv[]){

		launchHabaneroApp(() -> {
			int nthreads = 1;

			if(argv.length != 0 )
				nthreads = Integer.parseInt(argv[0]);
			else
				System.out.println("The no of threads has not been specified, defaulting to 1");

			int niter = 1; //3;
			for (int i=0; i<argv.length-1; i++)
				if (argv[i].startsWith("--iter")) {
					niter = Integer.parseInt(argv[i+1]);
				}
			System.out.println("Iterations="+niter);

			JGFInstrumentor.printHeader(2,2);
			for (int iter = 0; iter < niter; iter++) {
				JGFSeriesBench se = new JGFSeriesBench(nthreads,iter); 
				se.JGFrun(3);
			}
		});
	}
}
