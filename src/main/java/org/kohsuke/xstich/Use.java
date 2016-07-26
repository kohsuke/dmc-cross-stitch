package org.kohsuke.xstich;

import org.kohsuke.xstich.ColorPalette.Entry;

import java.util.Map;
import java.util.TreeMap;

/**
 * Use of a palette entry in a specific image.
 *
 * @author Kohsuke Kawaguchi
 */
class Use implements Comparable<Use> {
    /**
     * Number of pixesl in this color.
     */
    int pixels;
    /**
     * Entry in the color palette.
     */
    final Entry color;
    /**
     * Symbol we use in the schematics.
     */
    final char letter;
    /**
     * Unique sequence number assigned to {@link Use}s
     */
    final int index;

    /**
     * Used with {@linkplain App#tileFill the tile fill mode} to remember break downs of tiles we've used.
     */
    final Map<String,Integer> tiles = new TreeMap<String, Integer>();

    Use(Entry color, int index) {
        this.color = color;
        this.letter = (SYMBOLS.length()>index) ? SYMBOLS.charAt(index) : ' ';
        this.index = index;
    }

    /**
     * Sort by the dominant color first.
     */
    public int compareTo(Use that) {
        return that.pixels-this.pixels;
    }

    public String toRGB() {
        return String.format("rgb(%d,%d,%d)",color.rgb.getRed(), color.rgb.getGreen(), color.rgb.getBlue());
    }

    private static final String SYMBOLS = "＋Ｅー＊＃・＜□◇＝｜×ＺＮ∥％●○◎／ABX￥CDFHJKLMPRSTU";

    public void formatCellStyle(StringBuilder o) {
        o.append(String.format("#schematic.color TD.c%d { background-color:%s; }\n", index, toRGB()));
        o.append(String.format("#schematic.c%d TD.c%d { color:white; background-color: red; }\n", index, index));
    }

    public void incrementTile(String name) {
        Integer c = tiles.get(name);
        tiles.put(name, c==null ? 1 : c+1 );
    }
}
