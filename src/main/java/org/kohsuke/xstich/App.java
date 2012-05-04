package org.kohsuke.xstich;

import org.apache.sanselan.color.ColorCIELab;
import org.apache.sanselan.color.ColorConversions;
import org.kohsuke.xstich.ColorPalette.Entry;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Hello world!
 *
 */
public class App {
    private static class Use implements Comparable<Use> {
        int pixels;
        final Entry color;
        final char letter;

        private Use(Entry color, char letter) {
            this.color = color;
            this.letter = letter;
        }

        public int compareTo(Use that) {
            return that.pixels-this.pixels;
        }
        
        public String toRGB() {
            return String.format("rgb(%d,%d,%d)",color.rgb.getRed(), color.rgb.getGreen(), color.rgb.getBlue());
        }
    }
    
    public static void main(String[] args) throws Exception {
        ColorPalette dmc = new ColorPalette();
        Map<Entry,Use> used = new LinkedHashMap<Entry,Use>();
        
        BufferedImage img = ImageIO.read(new File(args[0]));
        BufferedImage out = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        
        StringBuilder schematic = new StringBuilder();
        schematic.append("<html><style>");
        schematic.append(".schematic TD { text-align:center; }\n");
        schematic.append("div.sample { display: inline-block; height:1em; width:1em; }\n");
        schematic.append("div.letter { display: inline-block; height:1em; width:1em; }\n");
        schematic.append("</style><body style='font-family:monospace'>");

        StringBuilder table = new StringBuilder();
        table.append("<table border=1 style='border-collapse:collapse' class=schematic>");
        for (int y=0; y<img.getHeight(); y++) {
            table.append("<tr>");
            for (int x=0; x<img.getWidth(); x++) {
                Color c = new Color(img.getRGB(x,y));
                c = mix(c,c.getAlpha(), Color.WHITE,255-c.getAlpha());

                Entry e = dmc.findNearest(c.getRGB()&0xFFFFFF);
                out.setRGB(x,y,e.rgb.getRGB());

                Use v = used.get(e);
                if (v==null)
                    used.put(e,v=new Use(e,SYMBOLS.charAt(used.size())));
                v.pixels++;

                table.append("<td>").append(v.letter).append("</td>");
            }
            
            table.append("</tr>");
        }
        table.append("</table>");


        schematic.append("<table><tr><td>");
        schematic.append(table);
        schematic.append("</td><td>");
        schematic.append("<div><ul>");
        ArrayList<Use> usedList = new ArrayList<Use>(used.values());
        Collections.sort(usedList);
        for (Use use : usedList) {
            schematic.append("<li>");
            schematic.append(String.format("<div class=sample style='background-color:%s'></div>",use.toRGB()));
            schematic.append(String.format("<div class=letter>%c</div>", use.letter));
            schematic.append(String.format(" %4d  DMC:%-4s %s\n", use.pixels, use.color.dmcCode, use.color.name));
            schematic.append("</li>");
        }
        schematic.append("</ul></div>");
        schematic.append("</td></tr></table>");
        schematic.append("</body></html>");

        ImageIO.write(out,"PNG",new File(args[0]+"-out.png"));
        File txt = new File(args[0] + ".html");
        FileWriter w = new FileWriter(txt);
        w.write(schematic.toString());
        w.close();
    }

    /**
     * Mix the two colors with the given weight.
     */
    private static Color mix(Color c1, int w1, Color c2, int w2) {
        return new Color(
                mix(c1.getRed(),w1,c2.getRed(),w2),
                mix(c1.getGreen(),w1,c2.getGreen(),w2),
                mix(c1.getBlue(),w1,c2.getBlue(),w2),
                mix(c1.getAlpha(),w1,c2.getAlpha(),w2));
    }
    
    private static int mix(int x, int w1, int y, int w2) {
        return (int)((x*((long)w1)+y*((long)w2))/(w1+w2));
    }

    private static ColorCIELab convertRGBtoCIELab(int rgb) {
        return ColorConversions.convertXYZtoCIELab(ColorConversions.convertRGBtoXYZ(rgb));
    }
    
    private static final String SYMBOLS = "　＋Ｅー＊＃・＜□◇＝｜×ＺＮ∥％";
}
