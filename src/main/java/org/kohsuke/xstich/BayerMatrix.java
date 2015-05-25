package org.kohsuke.xstich;

/**
 * @author Kohsuke Kawaguchi
 */
public class BayerMatrix {
    private final int[] matrix;
    public final int size;

    public BayerMatrix(int size) {
        this.matrix = ALL[size];
        this.size = size;
    }

    /**
     * Return the index as [0,size*size) value.
     */
    public int asIndex(int x, int y) {
        int idx = (y%size)*size + (x%size);
        return matrix[idx]-1;
    }

    private static final int[] ONE = new int[] {
            1,
    };

    private static final int[] TWO = new int[] {
            1, 3,
            4, 2,
    };

    private static final int[] THREE = new int[] {
            3, 7, 4,
            6, 1, 9,
            2, 8, 5,
    };

    private static final int[] FOUR = new int[] {
            1, 9, 3, 11,
            13,5, 15,7,
            4, 12,2, 10,
            16,8, 14,6,
    };

    private static final int[][] ALL = new int[][] {null,ONE,TWO,THREE,FOUR};
}
