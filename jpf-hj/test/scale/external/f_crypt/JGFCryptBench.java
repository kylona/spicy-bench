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


import edu.rice.hj.api.SuspendableException;

public class JGFCryptBench extends IDEATest implements JGFSection2{ 


    private int size; 


    //private int datasizes_b[]={3000000,20000000,50000000};

    //private int datasizes_b[]={300,2000,5000};   

    private int datasizes_b[]={300,1000,1500};    //This works 


    int iter;


    public JGFCryptBench(int nthreads, int iter) {

        super(nthreads);

        this.iter = iter;

    }


    public void JGFsetsize(int size){

        this.size = size;

    }


    public void JGFinitialise(){

        //datasizes = new arrayView (datasizes_b, 0, [0:datasizes_b.length-1]);

        array_rows = datasizes_b[size];

        buildTestData();

    }


    public void JGFkernel() {

	try {
		Do(iter); 
	} catch (SuspendableException e) {
		e.printStackTrace();
	}

    }


    public void JGFvalidate(){

        boolean error;


        error = false; 

        for (int i = 0; i < array_rows; i++){

            error = (plain1_b [i] != plain2_b [i]); 

            if (error){

                System.out.println("Validation failed");

                System.out.println("Original Byte " + i + " = " + plain1_b[i]); 

                System.out.println("Encrypted Byte " + i + " = " + crypt1_b[i]); 

                System.out.println("Decrypted Byte " + i + " = " + plain2_b[i]); 

                //break;

            }

        }

    }



    public void JGFtidyup(){

        freeTestData(); 

    }  




    public void JGFrun(int size){



        JGFInstrumentor.addTimer("Section2:Crypt(" + iter + "):Kernel", "Kbyte",size);


        JGFsetsize(size); 

        JGFinitialise(); 

        JGFkernel(); 

        JGFvalidate(); 

        JGFtidyup(); 



        JGFInstrumentor.addOpsToTimer("Section2:Crypt(" + iter + "):Kernel", (2*array_rows)/1000.); 

        JGFInstrumentor.printTimer("Section2:Crypt(" + iter + "):Kernel"); 

    }

}
