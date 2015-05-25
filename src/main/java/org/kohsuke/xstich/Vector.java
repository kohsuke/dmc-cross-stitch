package org.kohsuke.xstich;

import java.awt.*;

/**
 * Vector in RGB color space.
 *
 * @author Kohsuke Kawaguchi
 */
public class Vector {
    public final int r,g,b;

    public Vector(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public Vector(Color c) {
        this(c.getRed(), c.getGreen(), c.getBlue());
    }

    public Vector multiply(double d) {
        return new Vector((int)(r*d), (int)(g*d), (int)(b*d));
    }

    public Vector plus(Vector that) {
        return new Vector(
                this.r+that.r,
                this.g+that.g,
                this.b+that.b);
    }

    public Vector minus(Vector that) {
        return new Vector(
                this.r-that.r,
                this.g-that.g,
                this.b-that.b);
    }

    /**
     * Maps {@link Vector} to {@link Color} by clipping values in the [0,256) range.
     */
    public Color toColor() {
        return new Color(clip(r),clip(g),clip(b));
    }

    private int clip(int v) {
        if (v<0)    return 0;
        if (v>255)  return 255;
        return v;
    }

    public static final Vector ZERO = new Vector(0,0,0);
}
