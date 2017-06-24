package org.kohsuke.xstich;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.sanselan.color.ColorCIELab;
import org.apache.sanselan.color.ColorConversions;
import org.apache.sanselan.color.ColorHSV;

import java.awt.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static org.apache.sanselan.color.ColorConversions.convertRGBtoHSV;

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
        ColorHSV hsv;
        String dmcCode;

        /**
         * Computes CIE color distance.
         */
        public double distance(ColorCIELab that) {
            // CIE 76
            /*
            return sq(this.cie.L-that.L)
                +  sq(this.cie.a-that.a)
                +  sq(this.cie.b-that.b);
            */

            // CIE 94
            ColorCIELab x = cie;
            ColorCIELab y = that;

            double k_L = 1.0;
            double k1 = 0.045;
            double k2 = 0.015;

            double c1 = Math.sqrt(sq(x.a)+sq(x.b));
            double c2 = Math.sqrt(sq(y.a)+sq(y.b));
            double delta_L = x.L - y.L;
            double delta_a = x.a - y.a;
            double delta_b = x.b - y.b;
            double delta_C = c1-c2;
            double delta_H = Math.sqrt(sq(delta_a)+sq(delta_b)-sq(delta_C));
            double S_L = 1;
            double S_C = 1.0 + k1*c1;
            double S_H = 1.0 + k2*c1;

            double k_C= 1.0, k_H=1.0; // ???

            return Math.sqrt(sq(delta_L/(k_L*S_L)) + sq(delta_C/(k_C*S_C)) + sq(delta_H/(k_H*S_H)));
        }

        public double getLumacity() {
            return 0.299*rgb.getRed() + 0.587*rgb.getGreen() + 0.114*rgb.getBlue();
        }
        
        private double sq(double d) {
            return d*d;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry entry = (Entry) o;
            return Objects.equals(rgb, entry.rgb);
        }

        @Override
        public int hashCode() {
            return Objects.hash(rgb);
        }
    }
    
    final List<Entry> entries = new ArrayList<Entry>();

    public ColorPalette(Collection<Entry> entries) throws IOException {
        this.entries.addAll(entries);
    }

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
            e.hsv = convertRGBtoHSV(e.rgb.getRGB());
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
