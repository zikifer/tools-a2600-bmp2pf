package com.zikworks.tools.a2600.bmp2pf;

import org.apache.commons.cli.Option;

/**
 * The various command line options
 */
public enum CommandLineOption {

    INPUT_FILE("f", "file", true, true, "BMP file to parse"),
    OUTPUT_FILE("o", "out", true, true, "Output file name"),
    BUFFER_OUTPUT("b", "buffer", false, true, "Add a buffer of empty rows to the output file"),
    FULL_SCALE("x", "full-scale", false, false, "Input BMP has each bit as 4 pixels wide"),
    SYMMETRICAL("s", "symmetrical", false, false, "Generate symmetrical playfield (default)"),
    ASYMMETRICAL("a", "asymmetrical", false, false, "Generate asymmetrical playfield"),
    MIRRORED("m", "mirrored", false, false, "When asymmetrical mirror PF registers"),
    REPEATED("r", "repeated", false, false, "When asymmetrical repeat PF registers"),
    KERNEL("k", "kernel", false, true, "Number of scan lines per kernel loop (default 1)"),
    NO_COLOR(null, "no-color", false, false, "Do not add color info to output file"),
    NO_COLLISION(null, "no-collision", false, false, "Do not add collision info to output file");

    final Option option;

    CommandLineOption(String opt,
                      String longOption,
                      boolean isRequired,
                      boolean hasArg,
                      String description) {
        option = Option.builder(opt)
                .longOpt(longOption)
                .required(isRequired)
                .hasArg(hasArg)
                .desc(description)
                .get();
    }

    public Option toOption() {
        return option;
    }
}
