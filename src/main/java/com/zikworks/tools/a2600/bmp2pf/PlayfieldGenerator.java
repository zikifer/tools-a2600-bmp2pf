package com.zikworks.tools.a2600.bmp2pf;

import org.apache.commons.cli.CommandLine;

import java.io.IOException;

/**
 * Responsible for generating an ASM compatible playfield.
 */
public interface PlayfieldGenerator {

    /**
     * Generate the ASM output file from the input BMP file.
     *
     * @throws IOException I/O error during generation
     */
    void generate() throws IOException;

    /**
     * Create a new PlayfieldGeneratorBuilder.
     *
     * @param commandLine Command line used to start the application
     * @return A new PlayfieldGeneratorBuilder
     */
    static PlayfieldGeneratorBuilder builder(CommandLine commandLine) {
        return new PlayfieldGeneratorBuilder(commandLine);
    }
}
