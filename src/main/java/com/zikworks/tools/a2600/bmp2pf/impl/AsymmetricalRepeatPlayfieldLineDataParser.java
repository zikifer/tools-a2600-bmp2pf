package com.zikworks.tools.a2600.bmp2pf.impl;

import com.zikworks.tools.a2600.bmp2pf.PlayfieldOutputSection;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public class AsymmetricalRepeatPlayfieldLineDataParser extends AsymmetricalPlayfieldLineDataParser {

    @Override
    public Map<PlayfieldOutputSection, String> parseLineData(PlayfieldLineData lineData) {
        LinkedList<Boolean> bits = new LinkedList<>(lineData.getBits());
        Map<PlayfieldOutputSection, String> result = new LinkedHashMap<>();

        // First 4 bits are PF0
        var pfData = popFromLineData(bits, 4);
        String pfByte = getHalfPFByte(pfData, true);
        result.put(PlayfieldOutputSection.PF0DataA, pfByte);

        // Next 8 bits are PF1
        pfData = popFromLineData(bits, 8);
        pfByte = getFullPFByte(pfData, false);
        result.put(PlayfieldOutputSection.PF1DataA, pfByte);

        // Next 8 bits are PF2
        pfData = popFromLineData(bits, 8);
        pfByte = getFullPFByte(pfData, true);
        result.put(PlayfieldOutputSection.PF2DataA, pfByte);

        // Next 4 bits are PF0 again
        pfData = popFromLineData(bits, 4);
        pfByte = getHalfPFByte(pfData, true);
        result.put(PlayfieldOutputSection.PF0DataB, pfByte);

        // Next 8 bits are PF1 again
        pfData = popFromLineData(bits, 8);
        pfByte = getFullPFByte(pfData, false);
        result.put(PlayfieldOutputSection.PF1DataB, pfByte);

        // Last 8 bits are PF2 again
        pfData = popFromLineData(bits, 8);
        pfByte = getFullPFByte(pfData, true);
        result.put(PlayfieldOutputSection.PF2DataB, pfByte);

        return result;
    }
}
