package com.zikworks.tools.a2600.bmp2pf.impl;

import com.zikworks.tools.a2600.bmp2pf.PlayfieldGeneratorBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class SymmetricalPlayfieldGenerator extends BasePlayfieldGenerator {

    private static final int IMAGE_WIDTH = 20;

    public SymmetricalPlayfieldGenerator(PlayfieldGeneratorBuilder builder) {
        super(builder);
    }

    public void generate() throws IOException {
        // First open the input file, expecting a width of 20 pixels
        openInputFile(IMAGE_WIDTH);

        // Read each line from the file
        List<Boolean> lineData;
        do {
            // Read the next line as a list of bits
            lineData = getNextLine();
            if (!lineData.isEmpty()) {
                parseLineData(lineData);
            }
        } while (!lineData.isEmpty());

        // Add any extra empty lines
        for (int i = 0; i < super.getOutputBufferLines(); i++) {
            lineData = IntStream.range(0, IMAGE_WIDTH).mapToObj(ign -> Boolean.FALSE).toList();
            parseLineData(new ArrayList<>(lineData));
        }

        // Finally write output file
        writeOutputFile();
    }

    private void parseLineData(List<Boolean> lineData) {
        // First 4 bits are PF0
        var pf0Data = lineData.subList(0, 4);
        String pf0Byte = getPF0Byte(pf0Data);
        addByteToSection("PF0DataA", pf0Byte);

        // Next 8 bits are PF1
        var pf1Data = lineData.subList(4, 12);
        String pf1Byte = getPF1Byte(pf1Data);
        addByteToSection("PF1DataA", pf1Byte);

        // Last 8 bits are PF2
        var pf2Data = lineData.subList(12, 20);
        String pf2Byte = getPF2Byte(pf2Data);
        addByteToSection("PF2DataA", pf2Byte);
    }
}
