package benchmarks.examples;

import static edu.rice.hj.Module1.launchHabaneroApp;
import static edu.rice.hj.Module1.finish;
import static edu.rice.hj.Module1.future;
import edu.rice.hj.api.HjFuture;
import edu.rice.hj.api.HjSuspendable;
import edu.rice.hj.api.HjSuspendingCallable;

/**
 * <p>
 * BinaryTrees class.</p>
 *
 * @author Vivek Sarkar (vsarkar@rice.edu)
 *
 */
public class BinaryTrees {

    private final static int minDepth = 2;

    /**
     * <p>
     * main.</p>
     *
     * @param args an array of {@link String} objects.
     *
     */
    public static void main(final String[] args) {
        launchHabaneroApp(new HjSuspendable() {
            @Override
            public void run() {
                final int n = 0;

                final int maxDepth = (minDepth + 2 > n) ? minDepth + 2 : n;
                final int stretchDepth = maxDepth + 1;

                finish(new HjSuspendable() {
                    @Override
                    public void run() {
                        performComputation(maxDepth, stretchDepth);
                    }
                });
            }
        });
    }

    private static void performComputation(final int maxDepth, final int stretchDepth) {
        int check = (TreeNode.bottomUpTree(0, stretchDepth)).itemCheck();
        System.out.println("stretch tree of depth " + stretchDepth + "\t check: " + check);

        final TreeNode longLivedTree = TreeNode.bottomUpTree(0, maxDepth);

        for (int depth = minDepth; depth <= maxDepth; depth += 2) {
            final int iterations = 1 << (maxDepth - depth + minDepth);
            check = 0;

            for (int i = 1; i <= iterations; i++) {
                check += (TreeNode.bottomUpTree(i, depth)).itemCheck();
                check += (TreeNode.bottomUpTree(-i, depth)).itemCheck();
            }
            System.out.println((iterations * 2) + "\t trees of depth " + depth + "\t check: " + check);
        }
        System.out.println("long lived tree of depth " + maxDepth + "\t check: " + longLivedTree.itemCheck());
    }

    private static class TreeNode {

        private final HjFuture<TreeNode> left;
        private final HjFuture<TreeNode> right;

        private final int item;

        TreeNode(final int item) {
            this(null, null, item);
        }

        TreeNode(final HjFuture<TreeNode> left, final HjFuture<TreeNode> right, final int item) {
            this.left = left;
            this.right = right;
            this.item = item;
        }

        private static TreeNode bottomUpTree(final int item, final int depth) {

            final int finalItem = item;
            final int finalDepth = depth;

            if (depth > 0) {
                final HjFuture<TreeNode> LNode = future(new HjSuspendingCallable() {
                    public Object call() {
                        return bottomUpTree(2 * finalItem - 1, finalDepth - 1);
                    }
                });
                final HjFuture<TreeNode> RNode = future(new HjSuspendingCallable() {

                    public Object call() {
                        return bottomUpTree(2 * finalItem, finalDepth - 1);
                    }
                });
                return new TreeNode(LNode, RNode, item);
            } else {
                return new TreeNode(item);
            }
        }

        private int itemCheck() {
            // if necessary deallocate here
            if (left == null) {
                return item;
            } else {
                final TreeNode leftNode = left.get();
                final TreeNode rightNode = right.get();
                return item + leftNode.itemCheck() - rightNode.itemCheck();
            }
        }
    }

}
