package com.zikworks.tools.a2600.bmp2pf;

import com.zikworks.tools.a2600.bmp2pf.impl.AsymmetricalMirrorPlayfieldLineDataParser;
import com.zikworks.tools.a2600.bmp2pf.impl.AsymmetricalRepeatPlayfieldLineDataParser;
import com.zikworks.tools.a2600.bmp2pf.impl.PlayfieldGeneratorImpl;
import com.zikworks.tools.a2600.bmp2pf.impl.SymmetricalMirrorPlayfieldLineDataParser;
import com.zikworks.tools.a2600.bmp2pf.impl.SymmetricalRepeatPlayfieldLineDataParser;
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
    private enum PlayfieldRegistersMode {
        REPEAT,
        MIRROR
    }

    private final String inputFile;
    private final String outputFile;
    private final boolean fullScale;
    private final boolean excludeColor;
    private final int kernelLines;
    private final int collisionLines;
    private final int outputBufferLines;
    private final boolean separateCollisionFile;
    private final String outputSectionPrefix;
    private GeneratorMode generatorMode = GeneratorMode.SYMMETRICAL;
    private PlayfieldRegistersMode playfieldRegistersMode = PlayfieldRegistersMode.REPEAT;

    PlayfieldGeneratorBuilder(CommandLine commandLine) {
        this.inputFile = commandLine.getOptionValue(CommandLineOption.INPUT_FILE.toOption());
        this.outputFile = commandLine.getOptionValue(CommandLineOption.OUTPUT_FILE.toOption());
        this.fullScale = commandLine.hasOption(CommandLineOption.FULL_SCALE.toOption());
        this.excludeColor = commandLine.hasOption(CommandLineOption.NO_COLOR.toOption());
        this.separateCollisionFile = commandLine.hasOption(CommandLineOption.SEPARATE_COLLISION.toOption());
        this.kernelLines = commandLine.hasOption(CommandLineOption.KERNEL.toOption())
                ? Integer.parseInt(commandLine.getOptionValue(CommandLineOption.KERNEL.toOption()))
                : 1;
        this.outputBufferLines = commandLine.hasOption(CommandLineOption.BUFFER_OUTPUT.toOption())
                ? Integer.parseInt(commandLine.getOptionValue(CommandLineOption.BUFFER_OUTPUT.toOption()))
                : 0;

        this.outputSectionPrefix = commandLine.hasOption(CommandLineOption.OUTPUT_SECTION_PREFIX.toOption())
                ? commandLine.getOptionValue(CommandLineOption.OUTPUT_SECTION_PREFIX.toOption())
                : "";

        if (commandLine.hasOption(CommandLineOption.NO_COLLISION.toOption())) {
            collisionLines = 0;
        } else {
            if (commandLine.hasOption(CommandLineOption.COLLISION_RESOLUTION.toOption())) {
                collisionLines = Math.max(
                        Integer.parseInt(commandLine.getOptionValue(CommandLineOption.COLLISION_RESOLUTION.toOption())),
                        kernelLines);
            } else {
                collisionLines = kernelLines;
            }
        }

        if (commandLine.hasOption(CommandLineOption.ASYMMETRICAL.toOption())) {
            this.generatorMode = GeneratorMode.ASYMMETRICAL;
        } else {
            // Default to mirrored mode
            this.playfieldRegistersMode = PlayfieldRegistersMode.MIRROR;
        }

        if (commandLine.hasOption(CommandLineOption.MIRRORED.toOption())) {
            this.playfieldRegistersMode = PlayfieldRegistersMode.MIRROR;
        }

        System.out.println("Running with options:");
        System.out.println(" - Input File: " + inputFile);
        System.out.println(" - Output File: " + outputFile);
        System.out.println(" - Scaled? " + fullScale);
        System.out.println(" - Number of scan lines per kernel loop: " + kernelLines);
        System.out.println(" - Number of scan lines per collision line: " + collisionLines);
        System.out.println(" - Output Buffer Lines: " + outputBufferLines);

        String mode = generatorMode == GeneratorMode.SYMMETRICAL
                ? " - Mode: " + generatorMode
                : String.format(" - Mode: %s (%s)", generatorMode, playfieldRegistersMode);
        System.out.println(mode);
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

    public boolean isExcludeColor() {
        return excludeColor;
    }

    public int getKernelLines() {
        return kernelLines;
    }

    public int getCollisionLines() {
        return collisionLines;
    }

    public int getOutputBufferLines() {
        return outputBufferLines;
    }

    public boolean isSeparateCollisionFile() {
        return separateCollisionFile;
    }

    public String getOutputSectionPrefix() {
        return outputSectionPrefix;
    }

    /**
     * Build a new PlayfieldGenerator based on the current GeneratorMode.
     *
     * @return A new PlayfieldGenerator
     */
    public PlayfieldGenerator build() {
        PlayfieldLineDataParser parser;

        if (generatorMode == GeneratorMode.ASYMMETRICAL) {
            if (playfieldRegistersMode == PlayfieldRegistersMode.REPEAT) {
                parser = new AsymmetricalRepeatPlayfieldLineDataParser();
            } else {
                parser = new AsymmetricalMirrorPlayfieldLineDataParser();
            }
        } else {
            if (playfieldRegistersMode == PlayfieldRegistersMode.REPEAT) {
                parser = new SymmetricalRepeatPlayfieldLineDataParser();
            } else {
                parser = new SymmetricalMirrorPlayfieldLineDataParser();
            }
        }

        return new PlayfieldGeneratorImpl(this, parser);
    }
}
