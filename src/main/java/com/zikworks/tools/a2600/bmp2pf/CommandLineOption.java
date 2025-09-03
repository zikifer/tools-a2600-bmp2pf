package com.zikworks.tools.a2600.bmp2pf;

import org.apache.commons.cli.Option;

/**
 * The various command line options
 */
public enum CommandLineOption {

    INPUT_FILE('f', "file", true, true, "BMP file to parse"),
    OUTPUT_FILE('o', "out", true, true, "Output file name"),
    BUFFER_OUTPUT('b', "buffer", false, true, "Add a buffer of empty rows to the output file"),
    FULL_SCALE('x', "full-scale", false, false, "Input BMP has each bit as 4 pixels wide"),
    SYMMETRICAL('s', "symmetrical", false, false, "Generate symmetrical playfield (default)"),
    ASYMMETRICAL('a', "asymmetrical", false, false, "Generate asymmetrical playfield"),
    MIRRORED('m', "mirrored", false, false, "When asymmetrical mirror PF registers"),
    REPEATED('r', "repeated", false, false, "When asymmetrical repeat PF registers");

    final Option option;
    CommandLineOption(char opt,
                      String longOption,
                      boolean isRequired,
                      boolean hasArg,
                      String description) {
        option = Option.builder(String.valueOf(opt))
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
