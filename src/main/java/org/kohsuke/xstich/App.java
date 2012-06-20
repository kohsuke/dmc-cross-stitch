package org.kohsuke.xstich;

import org.apache.commons.io.IOUtils;
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
        final int index;

        private Use(Entry color, int index) {
            this.color = color;
            this.letter = SYMBOLS.charAt(index);
            this.index = index;
        }

        public int compareTo(Use that) {
            return that.pixels-this.pixels;
        }
        
        public String toRGB() {
            return String.format("rgb(%d,%d,%d)",color.rgb.getRed(), color.rgb.getGreen(), color.rgb.getBlue());
        }

        private static final String SYMBOLS = "　＋Ｅー＊＃・＜□◇＝｜×ＺＮ∥％";

        public void formatCellStyle(StringBuilder o) {
            o.append(String.format("#schematic.color TD.c%d { background-color:%s; }\n", index, toRGB()));
        }
    }
    
    public static void main(String[] args) throws Exception {
        ColorPalette dmc = new ColorPalette();
        Map<Entry,Use> used = new LinkedHashMap<Entry,Use>();
        
        BufferedImage img = ImageIO.read(new File(args[0]));
        BufferedImage out = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);

        String template = IOUtils.toString(App.class.getResourceAsStream("/output.html"));

        StringBuilder schematic = new StringBuilder();
        StringBuilder styles = new StringBuilder();
        for (int y=0; y<img.getHeight(); y++) {
            schematic.append("<tr>");
            for (int x=0; x<img.getWidth(); x++) {
                Color c = new Color(img.getRGB(x,y));
                c = mix(c,c.getAlpha(), Color.WHITE,255-c.getAlpha());

                Entry e = dmc.findNearest(c.getRGB()&0xFFFFFF);
                out.setRGB(x,y,e.rgb.getRGB());

                Use v = used.get(e);
                if (v==null) {
                    used.put(e,v=new Use(e,used.size()));
                    v.formatCellStyle(styles);
                }
                v.pixels++;

                schematic.append("<td class=c" + v.index + ">").append(v.letter).append("</td>");
            }
            
            schematic.append("</tr>");
        }
        template = template.replace("${schematic}", schematic).replace("${styles}",styles);
        template = template.replace("${size}", String.format("%dw x %dh", img.getWidth(), img.getHeight()));


        StringBuilder items = new StringBuilder();
        ArrayList<Use> usedList = new ArrayList<Use>(used.values());
        Collections.sort(usedList);
        for (Use use : usedList) {
            items.append("<li>");
            items.append(String.format("<div class=sample style='background-color:%s'></div>",use.toRGB()));
            items.append(String.format("<div class=letter>%c</div>", use.letter));
            items.append(String.format(" DMC:%4s %s (%d cnt)\n", use.color.dmcCode, use.color.name, use.pixels));
            items.append("</li>");
        }
        template = template.replace("${items}",items);

        ImageIO.write(out,"PNG",new File(args[0]+"-out.png"));
        File txt = new File(args[0] + ".html");
        FileWriter w = new FileWriter(txt);
        w.write(template);
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
}
