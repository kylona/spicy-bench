package benchmarks.asyncwhen.linearsearch;

import benchmarks.BenchmarkRunner;

import java.util.Random;

/**
 * @author <a href="http://shams.web.rice.edu/">Shams Imam</a> (shams@rice.edu)
 */
public class LinearSearchConfig {

    public static int NUM_FRAGMENTS = 400;
    public static int FRAGMENT_SIZE = 300;
    public static boolean debug = false;

    public static long[][] dataArray;
    public static long itemToFind;

    protected static void parseArgs(final String[] args) {
        int i = 0;
        while (i < args.length) {
            final String loopOptionKey = args[i];

            switch (loopOptionKey) {
                case "-nf":
                    i += 1;
                    NUM_FRAGMENTS = Integer.parseInt(args[i]);
                    break;
                case "-fs":
                    i += 1;
                    FRAGMENT_SIZE = Integer.parseInt(args[i]);
                    break;
                case "-debug":
                case "-verbose":
                    debug = true;
                    break;
            }

            i += 1;
        }

        initializeArray();
    }

    protected static void initializeArray() {
        dataArray = new long[NUM_FRAGMENTS][FRAGMENT_SIZE];

        final Random random = new Random(NUM_FRAGMENTS + FRAGMENT_SIZE);
        for (final long[] fragment : dataArray) {
            for (int j = 0; j < fragment.length; j++) {
                fragment[j] = random.nextLong();
            }
        }

        itemToFind = dataArray[3 * (NUM_FRAGMENTS / 4)][FRAGMENT_SIZE / 2];
    }

    protected static void printArgs() {
        System.out.printf(BenchmarkRunner.argOutputFormat, "Num Fragments", NUM_FRAGMENTS);
        System.out.printf(BenchmarkRunner.argOutputFormat, "Fragment Size", FRAGMENT_SIZE);
        System.out.printf(BenchmarkRunner.argOutputFormat, "debug", debug);
    }
}
