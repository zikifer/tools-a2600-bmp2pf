package com.zikworks.tools.a2600.bmp2pf.impl;

import com.zikworks.tools.a2600.bmp2pf.PlayfieldGeneratorBuilder;

import java.util.LinkedList;

public class SymmetricalPlayfieldGenerator extends BasePlayfieldGenerator {

    private static final int IMAGE_WIDTH = 20;

    public SymmetricalPlayfieldGenerator(PlayfieldGeneratorBuilder builder) {
        super(builder, IMAGE_WIDTH);
    }

    protected void parseLineData(PlayfieldLineData lineData) {
        LinkedList<Boolean> bits = new LinkedList<>(lineData.getBits());

        // First 4 bits are PF0
        var pf0Data = popFromLineData(bits, 4);
        String pf0Byte = getPF0Byte(pf0Data);
        addByteToSection("PF0DataA", pf0Byte);

        // Next 8 bits are PF1
        var pf1Data = popFromLineData(bits, 8);
        String pf1Byte = getPF1Byte(pf1Data);
        addByteToSection("PF1DataA", pf1Byte);

        // Last 8 bits are PF2
        var pf2Data = popFromLineData(bits, 8);
        String pf2Byte = getPF2Byte(pf2Data);
        addByteToSection("PF2DataA", pf2Byte);
    }
}
