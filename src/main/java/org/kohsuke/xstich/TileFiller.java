package org.kohsuke.xstich;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * @author Kohsuke Kawaguchi
 */
public class TileFiller {
    @Argument(required=true)
    public File input;

    /**
     * Region to fill
     */
    @Option(name="-sz",handler=RectangleHandler.class)
    public Rectangle area;

    public static void main(String[] args) throws Exception {
        TileFiller app = new TileFiller();
        CmdLineParser p = new CmdLineParser(app);
        try {
            p.parseArgument(args);
            app.run();
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            p.printUsage(System.err);
        }
    }

    public void run() throws Exception {
        BufferedImage img = ImageIO.read(input);

    }
}
