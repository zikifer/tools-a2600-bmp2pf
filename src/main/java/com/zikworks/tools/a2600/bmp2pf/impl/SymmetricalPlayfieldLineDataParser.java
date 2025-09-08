package com.zikworks.tools.a2600.bmp2pf.impl;

import com.zikworks.tools.a2600.bmp2pf.PlayfieldOutputSection;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

public abstract class SymmetricalPlayfieldLineDataParser extends BasePlayfieldLineDataParser {
    private static final int IMAGE_WIDTH = 20;

    @Override
    public int getExpectedWidth() {
        return IMAGE_WIDTH;
    }

    @Override
    public Map<PlayfieldOutputSection, String> parseLineData(PlayfieldLineData lineData) {
        LinkedList<Boolean> bits = new LinkedList<>(lineData.getBits());
        Map<PlayfieldOutputSection, String> result = new LinkedHashMap<>();

        // First 4 bits are PF0
        var pfData = popFromLineData(bits, 4);
        String pfByte = getPFByte(pfData, true);
        result.put(PlayfieldOutputSection.PF0DataA, pfByte);

        // Next 8 bits are PF1
        pfData = popFromLineData(bits, 8);
        pfByte = getPFByte(pfData, false);
        result.put(PlayfieldOutputSection.PF1DataA, pfByte);

        // Last 8 bits are PF2
        pfData = popFromLineData(bits, 8);
        pfByte = getPFByte(pfData, true);
        result.put(PlayfieldOutputSection.PF2DataA, pfByte);

        // Since there are only 20 bits we need to double the collision data to 40
        updateCollisionData(lineData);

        return result;
    }

    /**
     * Since there are only 20 bits we need to double the collision data to 40.
     *
     * @param lineData Parsed line data
     */
    abstract protected void updateCollisionData(PlayfieldLineData lineData);
}
