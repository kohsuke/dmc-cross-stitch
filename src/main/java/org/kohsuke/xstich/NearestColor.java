package org.kohsuke.xstich;

import org.kohsuke.xstich.ColorPalette.Entry;

import java.awt.*;

/**
 * Undithered version that just finds the nearest color.
 *
 * @author Kohsuke Kawaguchi
 */
public class NearestColor implements OrderedDitheringAlgorithm {
    public Entry map(ColorPalette palette, Color c, int x, int y) {
        return palette.findNearest(c);
    }
}
