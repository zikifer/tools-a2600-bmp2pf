package com.zikworks.tools.a2600.bmp2pf;

import org.apache.commons.cli.CommandLine;

import java.io.IOException;

public interface PlayfieldGenerator {

    void generate() throws IOException;

    static PlayfieldGeneratorBuilder builder(CommandLine commandLine) {
        return new PlayfieldGeneratorBuilder(commandLine);
    }
}
