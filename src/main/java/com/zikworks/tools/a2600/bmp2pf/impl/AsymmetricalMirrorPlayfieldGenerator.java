package com.zikworks.tools.a2600.bmp2pf.impl;

import com.zikworks.tools.a2600.bmp2pf.PlayfieldGeneratorBuilder;

import java.util.LinkedList;

public class AsymmetricalMirrorPlayfieldGenerator extends AsymmetricalPlayfieldGenerator {

    public AsymmetricalMirrorPlayfieldGenerator(PlayfieldGeneratorBuilder builder) {
        super(builder);
    }

    @Override
    protected void parseLineData(PlayfieldLineData lineData) {
        LinkedList<Boolean> bits = new LinkedList<>(lineData.getBits());

        // First 4 bits are PF0
        var pfData = popFromLineData(bits, 4);
        String pfByte = getHalfPFByte(pfData, true);
        addByteToSection("PF0DataA", pfByte);

        // Next 8 bits are PF1
        pfData = popFromLineData(bits, 8);
        pfByte = getFullPFByte(pfData, false);
        addByteToSection("PF1DataA", pfByte);

        // Next 8 bits are PF2
        pfData = popFromLineData(bits, 8);
        pfByte = getFullPFByte(pfData, true);
        addByteToSection("PF2DataA", pfByte);

        // Next 8 bits are PF2 again
        pfData = popFromLineData(bits, 8);
        pfByte = getFullPFByte(pfData, false);
        addByteToSection("PF2DataB", pfByte);

        // Next 8 bits are PF1 again
        pfData = popFromLineData(bits, 8);
        pfByte = getFullPFByte(pfData, true);
        addByteToSection("PF1DataB", pfByte);

        // Last 4 bits are PF0 again
        pfData = popFromLineData(bits, 4);
        pfByte = getHalfPFByte(pfData, false);
        addByteToSection("PF0DataB", pfByte);
    }
}
