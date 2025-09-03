package com.zikworks.tools.a2600.bmp2pf;

import com.zikworks.tools.a2600.bmp2pf.impl.SymmetricalPlayfieldGenerator;
import org.apache.commons.cli.CommandLine;

/**
 * Help build a PlayfieldGenerator.
 */
public class PlayfieldGeneratorBuilder {

    /**
     * Whether the input/output is used with a symmetrical playfield
     * or an asymmetrical one.  Determines the width of the input file.
     */
    private enum GeneratorMode {
        SYMMETRICAL,
        ASYMMETRICAL
    }

    /**
     * When generating an asymmetrical playfield controls whether the
     * PF registers are in repeat or mirror mode.
     */
    public enum PlayfieldRegistersMode {
        REPEAT,
        MIRROR
    }

    private final String inputFile;
    private final String outputFile;
    private final boolean fullScale;
    private final int outputBufferLines;
    private GeneratorMode generatorMode = GeneratorMode.SYMMETRICAL;
    private PlayfieldRegistersMode playfieldRegistersMode = PlayfieldRegistersMode.REPEAT;

    PlayfieldGeneratorBuilder(CommandLine commandLine) {
        this.inputFile = commandLine.getOptionValue(CommandLineOption.INPUT_FILE.toOption());
        this.outputFile = commandLine.getOptionValue(CommandLineOption.OUTPUT_FILE.toOption());
        this.fullScale = commandLine.hasOption(CommandLineOption.FULL_SCALE.toOption());
        this.outputBufferLines = commandLine.hasOption(CommandLineOption.BUFFER_OUTPUT.toOption())
                ? Integer.parseInt(commandLine.getOptionValue(CommandLineOption.BUFFER_OUTPUT.toOption()))
                : 0;

        if (commandLine.hasOption(CommandLineOption.ASYMMETRICAL.toOption())) {
            this.generatorMode = GeneratorMode.ASYMMETRICAL;
        }
        if (commandLine.hasOption(CommandLineOption.MIRRORED.toOption())) {
            this.playfieldRegistersMode = PlayfieldRegistersMode.MIRROR;
        }
    }

    public String getInputFile() {
        return inputFile;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public boolean isFullScale() {
        return fullScale;
    }

    public int getOutputBufferLines() {
        return outputBufferLines;
    }

    public PlayfieldRegistersMode getPlayfieldRegistersMode() {
        return playfieldRegistersMode;
    }

    /**
     * Build a new PlayfieldGenerator based on the current GeneratorMode.
     *
     * @return A new PlayfieldGenerator
     */
    public PlayfieldGenerator build() {
        if (generatorMode == GeneratorMode.ASYMMETRICAL) {
            throw new IllegalArgumentException("Asymmetrical mode not yet implemented");
        }

        return new SymmetricalPlayfieldGenerator(this);
    }
}
