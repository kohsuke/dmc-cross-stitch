package org.kohsuke.xstich;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.OptionDef;
import org.kohsuke.args4j.spi.OptionHandler;
import org.kohsuke.args4j.spi.Parameters;
import org.kohsuke.args4j.spi.Setter;

import java.awt.*;

/**
 * @author Kohsuke Kawaguchi
 */
public class RectangleHandler extends OptionHandler<Rectangle> {
    public RectangleHandler(CmdLineParser parser, OptionDef option, Setter<Rectangle> setter) {
        super(parser, option, setter);
    }

    public String getDefaultMetaVariable() {
        return "WxH+X+Y";
    }

    public int parseArguments(Parameters params) throws CmdLineException {
        String v = params.getParameter(0);
        String[] t = v.split("[x:+]");
        setter.addValue(new Rectangle(i(t[2]),i(t[3]),i(t[0]),i(t[1])));
        return 1;
    }

    private int i(String s) throws CmdLineException {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            throw new CmdLineException("Expected integer but got "+s);
        }
    }
}
