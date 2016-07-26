package org.kohsuke.xstich;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.sanselan.color.ColorCIELab;
import org.apache.sanselan.color.ColorConversions;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.kohsuke.xstich.ColorPalette.Entry;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.*;

/**
 *
 */
public class App {
    @Option(name="-p",usage="Color palette to use")
    public String paletteName = "dmc-floss";

    /**
     * Pretend as if these color codes were not a part of the color palette, and find the next best matching color.
     */
    @Option(name="-e",usage="Code of excluded colors, comma separated")
    public String exclude;

    /**
     * Two-path color exclusion. Assign colors once to produce a small color palette, then
     * exclude these colors from that palette and rerun the algorithm again. The net effect
     * is that the colors specified here gets re-mapped to other existing colors.
     *
     * Useful for removing colors that are only used in a few pixels.
     */
    @Option(name="-2e",usage="Code of excluded colors, comma separated")
    public String twoPhaseExclude;

    @Argument(required=true)
    public File input;

    @Option(name="-b",usage="Select Bayer matrix size from [2,3,4]")
    public int bayerSize = 4;


    public OrderedDitheringAlgorithm dither = new NearestColor();

    /**
     * Capture command line arguments so that the output have this information in comment.
     */
    private String cmdLine;

    @Option(name="-ctk",usage="Composite Thomas Knoll")
    public void useCompositeThomasKnoll(File mask) throws IOException {
        dither = new CompositeDither(
            new ThomasKnoll(new BayerMatrix(bayerSize)),
            new NearestColor(),
            ImageIO.read(mask)
        );
    }

    @Option(name="-tk",usage="Thomas Knoll")
    public void useThomasKnoll(boolean v) throws IOException {
        dither = new ThomasKnoll(new BayerMatrix(bayerSize));
    }

    public Collection<String> getExcludedColorCodes() {
        if (exclude==null)  return Collections.emptySet();
        return asList(exclude.split(","));
    }

    public static void main(String[] args) throws Exception {
        App app = new App();
        CmdLineParser p = new CmdLineParser(app);
        try {
            p.parseArgument(args);
            app.cmdLine = StringUtils.join(args," ");
            app.run();
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            p.printUsage(System.err);
        }
    }

    public void run() throws Exception {
        BufferedImage img = ImageIO.read(input);
        ColorPalette p = new ColorPalette(paletteName, getExcludedColorCodes());
        Result r = apply(img, p);

        if (twoPhaseExclude!=null) {
            // run the second phase
            List<String> excluded = asList(twoPhaseExclude.split(","));
            List<Entry> secondColors = new ArrayList<Entry>();
            for (Entry e : r.used.keySet()) {
                if (!excluded.contains(e.dmcCode))
                    secondColors.add(e);
            }

            r = apply(r.image,new ColorPalette(secondColors));
        }

        r.write();
    }

    /**
     * Result of the color assignment.
     */
    class Result {
        /**
         * Use of colors in the output image.
         */
        final Map<Entry,Use> used;
        /**
         * Output image.
         */
        final BufferedImage image;
        /**
         * Schematic html
         */
        final String html;

        public Result(String html, Map<Entry, Use> used, BufferedImage image) {
            this.html = html;
            this.used = used;
            this.image = image;
        }

        public void write() throws IOException {
            ImageIO.write(image,"PNG",new File(input.getPath()+"-out.png"));
            File txt = new File(input.getPath() + ".html");
            FileWriter w = new FileWriter(txt);
            w.write(html);
            w.close();
        }
    }

    /**
     * Reduce image to the given color palette and produce color assignment.
     */
    public Result apply(BufferedImage img, ColorPalette palette) throws IOException {
        Map<Entry,Use> used = new LinkedHashMap<Entry,Use>();

        BufferedImage out = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);

        String template = IOUtils.toString(App.class.getResourceAsStream("/output.html"));

        StringBuilder schematic = new StringBuilder();
        StringBuilder styles = new StringBuilder();
        for (int y=0; y<img.getHeight(); y++) {
            schematic.append("<tr>");
            for (int x=0; x<img.getWidth(); x++) {
                Color c = new Color(img.getRGB(x,y),true);

                if (c.getAlpha()<128) {
                    // treat this as transparent
                    schematic.append("<td>　</td>");
                    continue;
                }

                c = mix(c,c.getAlpha(), Color.WHITE,255-c.getAlpha());

                Entry e = dither.map(palette,c,x,y);
                out.setRGB(x,y,e.rgb.getRGB());

                Use v = used.get(e);
                if (v==null) {
                    used.put(e,v=new Use(e,used.size()));
                    v.formatCellStyle(styles);
                }
                v.pixels++;

                schematic.append("<td class=c" + v.index + ">").append(v.letter).append("</td>");
            }

            if (y==0) {
                schematic.append("<td rowspan=").append(img.getHeight()).append(" style=\"border:none\">◀</td>");
            }

            schematic.append("</tr>");
        }

        StringBuilder items = new StringBuilder();
        StringBuilder colors = new StringBuilder();
        ArrayList<Use> usedList = new ArrayList<Use>(used.values());
        Collections.sort(usedList);
        for (Use use : usedList) {
            items.append(String.format("<li index='%d'>", use.index));
            items.append(String.format("<div class=sample style='background-color:%s'></div>",use.toRGB()));
            items.append(String.format("<div class=letter>%c</div>", use.letter));
            items.append(String.format(" %4s %s (%d cnt)\n", use.color.dmcCode, use.color.name, use.pixels));
            items.append("</li>");

            // specify black or white text color for each cell
            styles.append(String.format("#schematic td.c%d { color:%s } #schematic.symbol td.c%d { color:transparent; }",
                    use.index,
                    use.color.hsv.V<=0.5?"#fff":"#000",
                    use.index));
            colors.append(use.color.dmcCode).append('\n');
        }

        template = template.replace("${items}",items);
        template = template.replace("${schematic}", schematic);
        template = template.replace("${styles}",styles);
        template = template.replace("${size}", String.format("%dw x %dh", img.getWidth(), img.getHeight()));
        template = template.replace("${width}", String.valueOf(img.getWidth()));
        template = template.replace("${cmdLine}",cmdLine);
        template = template.replace("${colors}",colors);

        return new Result(template,used,out);
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
