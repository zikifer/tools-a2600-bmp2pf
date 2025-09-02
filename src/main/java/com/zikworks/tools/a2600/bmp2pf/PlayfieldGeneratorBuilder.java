package com.zikworks.tools.a2600.bmp2pf;

import com.zikworks.tools.a2600.bmp2pf.impl.SymmetricalPlayfieldGenerator;
import org.apache.commons.cli.CommandLine;

public class PlayfieldGeneratorBuilder {

    public enum PlayfieldRegistersMode {
        REPEAT,
        MIRROR
    }

    private enum GeneratorMode {
        SYMMETRICAL,
        ASYMMETRICAL
    }

    private final String inputFile;
    private final String outputFile;
    private final int outputBufferLines;
    private GeneratorMode generatorMode = GeneratorMode.SYMMETRICAL;
    private PlayfieldRegistersMode playfieldRegistersMode = PlayfieldRegistersMode.MIRROR;

    PlayfieldGeneratorBuilder(CommandLine commandLine) {
        this.inputFile = commandLine.getOptionValue(CommandLineOption.INPUT_FILE.toOption());
        this.outputFile = commandLine.getOptionValue(CommandLineOption.OUTPUT_FILE.toOption());
        this.outputBufferLines = commandLine.hasOption(CommandLineOption.BUFFER_OUTPUT.toOption())
                ? Integer.parseInt(commandLine.getOptionValue(CommandLineOption.BUFFER_OUTPUT.toOption()))
                : 0;

        if (commandLine.hasOption(CommandLineOption.ASYMMETRICAL.toOption())) {
            this.generatorMode = GeneratorMode.ASYMMETRICAL;
        }
        if (commandLine.hasOption(CommandLineOption.REPEATED.toOption())) {
            this.playfieldRegistersMode = PlayfieldRegistersMode.REPEAT;
        }
    }

    public String getInputFile() {
        return inputFile;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public int getOutputBufferLines() {
        return outputBufferLines;
    }

    public PlayfieldRegistersMode getPlayfieldRegistersMode() {
        return playfieldRegistersMode;
    }

    public PlayfieldGenerator build() {
        if (generatorMode == GeneratorMode.ASYMMETRICAL) {
            throw new IllegalArgumentException("Asymmetrical mode not yet implemented");
        }

        return new SymmetricalPlayfieldGenerator(this);
    }
}
