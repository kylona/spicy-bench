package benchmarks.asyncwhen.linearsearch;

import edu.rice.hj.api.HjFuture;
import edu.rice.hj.api.SuspendableException;
import benchmarks.Benchmark;
import benchmarks.BenchmarkRunner;

import java.io.IOException;

import static edu.rice.hj.Module1.*;
import edu.rice.hj.api.HjSuspendable;
import edu.rice.hj.api.HjSuspendingCallable;

/**
 * @author <a href="http://shams.web.rice.edu/">Shams Imam</a> (shams@rice.edu)
 */
public class LinearSearchAsyncSimpleBenchmark extends Benchmark {

    private LinearSearchAsyncSimpleBenchmark() {
        super();
    }

    public static void main(final String[] args) {
        BenchmarkRunner.runBenchmark(args, new LinearSearchAsyncSimpleBenchmark());
    }

    @Override
    public void initialize(final String[] args) throws IOException {
        LinearSearchConfig.parseArgs(args);
    }

    @Override
    public void printArgInfo() {
        LinearSearchConfig.printArgs();
    }

    @Override
    public void runIteration() throws SuspendableException, SuspendableException {
        final long[][] dataArray = LinearSearchConfig.dataArray;
        final long itemToFind = LinearSearchConfig.itemToFind;
        finish(new HjSuspendable() {
            public void run() throws SuspendableException {
                final boolean result = findElement(dataArray, itemToFind);
                System.out.printf(BenchmarkRunner.argOutputFormat, "Item found", result);
            }
        });
    }

    private boolean findElement(final long[][] dataArray, final long itemToFind) throws SuspendableException {

        @SuppressWarnings("unchecked")
        final HjFuture<Boolean>[] futures = new HjFuture[dataArray.length];

        for (int i = 0; i < dataArray.length; i++) {
            final long[] loopFragment = dataArray[i];
            futures[i] = future(new HjSuspendingCallable<Boolean>() {
                @Override
                public Boolean call() {
                    boolean result = false;

                    for (int j = 0; j < loopFragment.length; j++) {
                        if (loopFragment[j] == itemToFind) {
                            result = true;
                            break;
                        }
                    }

                    return result;
                }

            });
        }

        waitAll(futures);

        return isAnyTrue(futures);
    }

    private boolean isAnyTrue(final HjFuture<Boolean>[] futures) {
        boolean itemFound = false;

        for (final HjFuture<Boolean> future : futures) {
            if (future.get()) {
                itemFound = true;
                break;
            }
        }

        return itemFound;
    }

    @Override
    public void cleanupIteration(final boolean lastIteration, final double execTimeMillis) {
        // do nothing
    }
}
