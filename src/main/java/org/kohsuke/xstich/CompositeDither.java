package org.kohsuke.xstich;

import org.kohsuke.xstich.ColorPalette.Entry;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Applies different dither algorithms to different region.
 *
 * @author Kohsuke Kawaguchi
 */
public class CompositeDither implements OrderedDitheringAlgorithm {
    private final OrderedDitheringAlgorithm lhs, rhs;
    private final BufferedImage map;

    public CompositeDither(OrderedDitheringAlgorithm lhs, OrderedDitheringAlgorithm rhs, BufferedImage map) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.map = map;
    }

    public Entry map(ColorPalette palette, Color c, int x, int y) {
        Color m = new Color(map.getRGB(x,y),true);
        return (m.getAlpha()>127 ? lhs : rhs).map(palette,c,x,y);
    }
}
