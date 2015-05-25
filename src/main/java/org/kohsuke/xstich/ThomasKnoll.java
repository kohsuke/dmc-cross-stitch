package org.kohsuke.xstich;

import org.kohsuke.xstich.ColorPalette.Entry;

import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;

/**
 * @author Kohsuke Kawaguchi
 * @see <a href="http://bisqwit.iki.fi/story/howto/dither/jy/">reference</a>
 */
public class ThomasKnoll implements OrderedDitheringAlgorithm {
    /**
     * How much of the error should be propagated? [0,1] with 0 being no dithering and
     * 1 being lot of dithering
     */
    private final double threshold = 0.5;

    private final BayerMatrix bayer;

    public ThomasKnoll(BayerMatrix bayer) {
        this.bayer = bayer;
    }

    public Entry map(ColorPalette palette, Color c, int x, int y) {
        Vector in = new Vector(c);

        Entry[] candidates = new Entry[bayer.size*bayer.size];

        Vector err = Vector.ZERO;
        for (int i=0; i<candidates.length; i++) {
            Color attempt = in.plus(err.multiply(threshold)).toColor();
            Entry e = candidates[i] = palette.findNearest(attempt);

            // the value we picked is different from the input, so the next probe should be compensated
            // into the other direction.
            err = err.plus(in).minus(new Vector(e.rgb));
        }

        Arrays.sort(candidates,new Comparator<Entry>() {
            public int compare(Entry a, Entry b) {
                double d = a.getLumacity() - b.getLumacity();
                if (d<0)    return -1;
                if (d>0)    return 1;
                return 0;
            }
        });

        // select the target color
        return candidates[bayer.asIndex(x,y)];
    }
}
