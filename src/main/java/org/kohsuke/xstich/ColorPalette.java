package org.kohsuke.xstich;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.sanselan.color.ColorCIELab;
import org.apache.sanselan.color.ColorConversions;

import java.awt.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Color palette, which consists of a fixed number of {@link Entry}s that represent available colors.
 *
 * @author Kohsuke Kawaguchi
 */
public class ColorPalette {
    /**
     * A color in a palette.
     */
    public static class Entry {
        /**
         * Color name by the vendor.
         */
        String name;
        Color rgb;
        ColorCIELab cie;
        String dmcCode;

        /**
         * Computes CIE color distance.
         */
        public double distance(ColorCIELab that) {
            return sq(this.cie.L-that.L)
                +  sq(this.cie.a-that.a)
                +  sq(this.cie.b-that.b);
        }

        public double getLumacity() {
            return 0.299*rgb.getRed() + 0.587*rgb.getGreen() + 0.114*rgb.getBlue();
        }
        
        private double sq(double d) {
            return d*d;
        }
    }
    
    final List<Entry> entries = new ArrayList<Entry>();
    
    public ColorPalette(String name, Collection<String> exclusions) throws IOException {
        CSVReader csv = new CSVReader(new InputStreamReader(getClass().getResourceAsStream("/"+ name +".csv")));
        csv.readNext(); // first line is caption
        
        while (true) {
            String[] line = csv.readNext();
            if (line==null)     return;
            
            Entry e = new Entry();
            e.dmcCode = line[0].trim();
            e.name = line[1];
            e.rgb = new Color(n(line[2]),n(line[3]),n(line[4]));
            e.cie = convertRGBtoCIELab(e.rgb.getRGB());
            if (!exclusions.contains(e.dmcCode))
                entries.add(e);
        }
    }
    
    private int n(String s) {
        return Integer.parseInt(s);
    }

    public Entry findNearest(Color c) {
        return findNearest(c.getRGB()&0xFFFFFF);
    }

    public Entry findNearest(int rgb) {
        ColorCIELab cie = convertRGBtoCIELab(rgb);
        double best=Double.MAX_VALUE;
        Entry nearest=null;
        
        for (Entry e : entries) {
            double d = e.distance(cie);
            if (d<best) {
                best = d;
                nearest = e;
            }
        }
        return nearest;
    }

    private static ColorCIELab convertRGBtoCIELab(int rgb) {
        return ColorConversions.convertXYZtoCIELab(ColorConversions.convertRGBtoXYZ(rgb));
    }
}
