import static permission.PermissionChecks.*;
/*
   A translation of: antidep1-orig-yes.c
   Originally produced at the Lawrence Livermore National Laboratory
   Written by Chunhua Liao, Pei-Hung Lin, Joshua Asplund,
   Markus Schordan, and Ian Karlin

   Translated at Brigham Young University by Kyle Storey

 */
import static edu.rice.hj.Module2.launchHabaneroApp;
import static edu.rice.hj.Module2.forAll;
import edu.rice.hj.api.*;

public class Jacobi2dParallelNo {
    static int i;
    static int len = 1000;
    static int[] a = new int[len];
    public static void init_array(int n, double A[][], double B[][]) throws SuspendableException {
        //int i;
        //int j;
        {
            int c1;
            if (n >= 1) {
                forAll(0, n-1, new HjSuspendingProcedure<Integer>() {
                    public void apply(Integer c1) throws SuspendableException {
                        for (int c2 = 0; c2 <= n + -1; c2++) {
                            acquireR(c1);
                            acquireW(A[c1], c2);
                            A[c1][c2] = (((double )c1) * (c2 + 2) + 2) / n;
                            releaseW(A[c1], c2);
                            acquireW(B[c1], c2);
                            B[c1][c2] = (((double )c1) * (c2 + 3) + 3) / n;
                            releaseW(B[c1], c2);
                            releaseR(c1);
                        }
                    }
                });
            }
        }
    }
    public static void print_array(int n, double A[][]) {
        int i;
        int j;
        for (i = 0; i < n; i++)
            for (j = 0; j < n; j++) {
                System.out.printf("%.2f ", A[i][j]);
                if ((i * n + j) % 20 == 0)
                    System.out.print("\n");
            }
            System.out.print("\n");
    }

    static int c0;
    public static void kernel_jacobi_2d_imper(int tsteps, int n, double A[][], double B[][]) throws SuspendableException {
        //int t;
        //int i;
        //int j;

        //#pragma scop
        {
            int c2;
            int c1;
            for (c2 = 1; c2 <= 498; c2++) {
                B[1][c2] = 0.2 * (A[1][c2] + A[1][c2 - 1] + A[1][1 + c2] + A[1 + 1][c2] + A[1 - 1][c2]);
            }
            for (c0 = 2; c0 <= 525; c0++) {
                if (c0 <= 28) {
                    if ((2 * c0 + 1) % 3 == 0) {
                        for (c2 = ((2 * c0 + 1) * 3 < 0?-(-(2 * c0 + 1) / 3) : ((3 < 0?(-(2 * c0 + 1) + - 3 - 1) / - 3 : (2 * c0 + 1 + 3 - 1) / 3))); c2 <= (((2 * c0 + 1492) * 3 < 0?((3 < 0?-((-(2 * c0 + 1492) + 3 + 1) / 3) : -((-(2 * c0 + 1492) + 3 - 1) / 3))) : (2 * c0 + 1492) / 3)); c2++) {
                            B[1][(-2 * c0 + 3 * c2 + 2) / 3] = 0.2 * (A[1][(-2 * c0 + 3 * c2 + 2) / 3] + A[1][(-2 * c0 + 3 * c2 + 2) / 3 - 1] + A[1][1 + (-2 * c0 + 3 * c2 + 2) / 3] + A[1 + 1][(-2 * c0 + 3 * c2 + 2) / 3] + A[1 - 1][(-2 * c0 + 3 * c2 + 2) / 3]);
                        }
                    }
                }
                final int private_c2_init = c2;
                final int c0_init = c0;
                int start = ((((2 * c0 + 2) * 3 < 0?-(-(2 * c0 + 2) / 3) : ((3 < 0?(-(2 * c0 + 2) + - 3 - 1) / - 3 : (2 * c0 + 2 + 3 - 1) / 3)))) > c0 + -9?(((2 * c0 + 2) * 3 < 0?-(-(2 * c0 + 2) / 3) : ((3 < 0?(-(2 * c0 + 2) + - 3 - 1) / - 3 : (2 * c0 + 2 + 3 - 1) / 3)))) : c0 + -9);
                int end = (((((2 * c0 + 498) * 3 < 0?((3 < 0?-((-(2 * c0 + 498) + 3 + 1) / 3) : -((-(2 * c0 + 498) + 3 - 1) / 3))) : (2 * c0 + 498) / 3)) < c0?(((2 * c0 + 498) * 3 < 0?((3 < 0?-((-(2 * c0 + 498) + 3 + 1) / 3) : -((-(2 * c0 + 498) + 3 - 1) / 3))) : (2 * c0 + 498) / 3)) : c0));
                forAll(start, end, new HjSuspendingProcedure<Integer>() {
                        public void apply(Integer c1) throws SuspendableException {
                            int c2 = private_c2_init;
                            B[-2 * c0 + 3 * c1][1] = 0.2 * (A[-2 * c0 + 3 * c1][1] + A[-2 * c0 + 3 * c1][1 - 1] + A[-2 * c0 + 3 * c1][1 + 1] + A[1 + (-2 * c0 + 3 * c1)][1] + A[-2 * c0 + 3 * c1 - 1][1]);
                            for (c2 = 2 * c0 + -2 * c1 + 2; c2 <= 2 * c0 + -2 * c1 + 498; c2++) {
                                A[-2 * c0 + 3 * c1 + -1][-2 * c0 + 2 * c1 + c2 + -1] = B[-2 * c0 + 3 * c1 + -1][-2 * c0 + 2 * c1 + c2 + -1];
                                B[-2 * c0 + 3 * c1][-2 * c0 + 2 * c1 + c2] = 0.2 * (A[-2 * c0 + 3 * c1][-2 * c0 + 2 * c1 + c2] + A[-2 * c0 + 3 * c1][-2 * c0 + 2 * c1 + c2 - 1] + A[-2 * c0 + 3 * c1][1 + (-2 * c0 + 2 * c1 + c2)] + A[1 + (-2 * c0 + 3 * c1)][-2 * c0 + 2 * c1 + c2] + A[-2 * c0 + 3 * c1 - 1][-2 * c0 + 2 * c1 + c2]);
                            }
                            A[-2 * c0 + 3 * c1 + -1][498] = B[-2 * c0 + 3 * c1 + -1][498];
                            }
                        });
                if (c0 >= 499) {
                    if ((2 * c0 + 1) % 3 == 0) {
                        for (c2 = ((2 * c0 + -992) * 3 < 0?-(-(2 * c0 + -992) / 3) : ((3 < 0?(-(2 * c0 + -992) + - 3 - 1) / - 3 : (2 * c0 + -992 + 3 - 1) / 3))); c2 <= (((2 * c0 + 499) * 3 < 0?((3 < 0?-((-(2 * c0 + 499) + 3 + 1) / 3) : -((-(2 * c0 + 499) + 3 - 1) / 3))) : (2 * c0 + 499) / 3)); c2++) {
                            A[498][(-2 * c0 + 3 * c2 + 995) / 3] = B[498][(-2 * c0 + 3 * c2 + 995) / 3];
                        }
                    }
                }
            }
            for (c2 = 20; c2 <= 517; c2++) {
                A[498][c2 + -19] = B[498][c2 + -19];
            }
        }

        //#pragma endscop

    }
    public static void main(String[] args) throws SuspendableException {
        launchHabaneroApp(new HjSuspendable() {

                @Override
                public void run() throws SuspendableException {

                    /* Retrieve problem size. */
                    int n = 500;
                    int tsteps = 10;
                    /* Variable declaration/allocation. */
                    double A[][] = new double[500+0][500+0]; 
                    double B[][] = new double[500+0][500+0]; 
                    /* Initialize array(s). */
                    init_array(n, A, B);
                    /* Start timer. */
                    Timer.start();
                    /* Run kernel. */
                    kernel_jacobi_2d_imper(tsteps,n, A, B);
                    /* Stop and print timer. */
                    Timer.stop();
                    Timer.print();
                    /* Prevent dead-code elimination. All live-out data must be printed
                       by the function call in argument. */
                    if (args.length > 42 && !args[0].equals(""))
                        print_array(n, A);

                }
            });
    }

	private static class Timer {
	    private static long start_time = 0;
	    private static long stop_time = 0;
	    public static void start() {
		start_time = System.currentTimeMillis();
	    }
	    public static void stop() {
		stop_time = System.currentTimeMillis();
	    }

	    public static void print() {
		System.out.println(stop_time - start_time);
	    }
	}
}
