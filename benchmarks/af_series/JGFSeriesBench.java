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

public class JGFSeriesBench extends SeriesTest implements JGFSection2{ 

	private int size; 
	int iter;

	public JGFSeriesBench(int nthreads,int iter) {
		super(nthreads);
		this.iter = iter;
	}

	public void JGFsetsize(int size){
		this.size = size;
	}

	public void JGFinitialise(){
		switch (size) {
		case 0: array_rows = 10000; break;
		case 1: array_rows = 100000; break;
		case 2: array_rows = 1000000; break;
		case 3: array_rows = 100; break;
		default: throw new Error();
		}
		buildTestData();
	}

	public void JGFkernel(){
		Do(iter); 
	}

	public void JGFvalidate(){
		double[][] ref = {{2.8729524964837996, 0.0},
				{1.1161046676147888, -1.8819691893398025},
				{0.34429060398168704, -1.1645642623320958},
				{0.15238898702519288, -0.8143461113044298}};

		for (int i = 0; i < 4; i++){
			for (int j = 0; j < 2; j++){
				double error = Math.abs(testArray[j] - ref[i][j]);
				if (error > 1.0e-12 ){
					System.out.println("Validation failed for coefficient " + j + "," + i);
					System.out.println("Computed value = " + testArray[i]);
					System.out.println("Reference value = " + ref[i][j]);
				}
			}
		}
	}

	public void JGFtidyup(){
		freeTestData(); 
	}  



	public void JGFrun(int size){

		JGFInstrumentor.addTimer("Section2:Series(" + iter + "):Kernel", "coefficients",size);
		JGFsetsize(size); 
		JGFinitialise(); 
		JGFkernel(); 
		JGFvalidate(); 
		JGFtidyup(); 


		JGFInstrumentor.addOpsToTimer("Section2:Series(" + iter + "):Kernel", (double) (array_rows * 2 - 1));
		JGFInstrumentor.printTimer("Section2:Series(" + iter + "):Kernel"); 
	}
}
