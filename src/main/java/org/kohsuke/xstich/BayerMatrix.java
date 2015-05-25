package org.kohsuke.xstich;

/**
 * @author Kohsuke Kawaguchi
 */
public class BayerMatrix {
    public static final int[] ONE = new int[] {
            1,
    };

    public static final int[] TWO = new int[] {
            1, 3,
            4, 2,
    };

    public static final int[] THREE = new int[] {
            3, 7, 4,
            6, 1, 9,
            2, 8, 5,
    };

    public static final int[] FOUR = new int[] {
            1, 9, 3, 11,
            13,5, 15,7,
            4, 12,2, 10,
            16,8, 14,6,
    };

    public static final int[][] ALL = new int[][] {null,ONE,TWO,THREE,FOUR};
}
