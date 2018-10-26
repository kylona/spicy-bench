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
//import hj.applications.jgf.arrayview.crypt.*;
//import hj.applications.jgf.jgfutil.*; 
import java.lang.Integer; 

public class JGFCryptBenchSizeA { 

    public static void main(String argv[]){
        int nthreads = 1;

        if(argv.length != 0 )
            nthreads = Integer.parseInt(argv[0]);
        else
            System.out.println("The no of threads has not been specified, defaulting to 1");

        int niter = 1;
        for (int i=0; i<argv.length-1; i++)
            if (argv[i].startsWith("--iter")) {
                niter = Integer.parseInt(argv[i+1]);
            }
        System.out.println("Iterations="+niter);
        JGFInstrumentor.printHeader(2,0);

        for (int iter = 0; iter < niter; iter++) {
            JGFCryptBench cb = new JGFCryptBench(nthreads,iter); 
            cb.JGFrun(0);
        }
    }
}
