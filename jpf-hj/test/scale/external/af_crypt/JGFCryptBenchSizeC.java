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

 *                                                                         *

 *      This version copyright (c) The University of Edinburgh, 1999.      *

 *                         All rights reserved.                            *

 *                                                                         *

 **************************************************************************/



import static edu.rice.hj.Module0.launchHabaneroApp; 


public class JGFCryptBenchSizeC { 


    public static void main(String argv[]){


        launchHabaneroApp(() -> {


            final int nthreads = 1;

            final int niter = 1;

            System.out.println("Iterations="+niter);

            JGFInstrumentor.printHeader(2,2);


            for (int iter = 0; iter < niter; iter++) {

                JGFCryptBench cb = new JGFCryptBench(nthreads,iter); 

                cb.JGFrun(1);            

            }    

        });

    }

}
