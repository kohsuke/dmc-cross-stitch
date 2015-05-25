package org.kohsuke.xstich;

import org.kohsuke.xstich.ColorPalette.Entry;

import java.awt.*;

/**
 * Represents the ordered dithering algorithm, which maps the source color into
 * one of the entries from the palette.
 *
 * <p>
 * The algorithm is local, meaning it doesn't consult colors of neighboring pixels.
 *
 * @author Kohsuke Kawaguchi
 */
public interface OrderedDitheringAlgorithm {
    /**
     * @param c
     *      Color of the pixel to be mapped.
     * @param x
     *      Location of the pixel to be mapped.
     * @param y
     *      Location of the pixel to be mapped.
     */
    Entry map(ColorPalette palette, Color c, int x, int y);
}
