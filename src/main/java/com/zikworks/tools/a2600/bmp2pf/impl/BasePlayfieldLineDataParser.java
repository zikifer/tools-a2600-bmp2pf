package com.zikworks.tools.a2600.bmp2pf.impl;

import com.zikworks.tools.a2600.bmp2pf.PlayfieldLineDataParser;
import com.zikworks.tools.a2600.bmp2pf.Utilities;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class BasePlayfieldLineDataParser implements PlayfieldLineDataParser {

    /**
     * Remove a certain number of booleans from the head of the list and return them.
     *
     * @param lineData List to manipulate
     * @param count    Number of elements to remove and return
     * @return A list of the removed elements
     */
    protected static List<Boolean> popFromLineData(LinkedList<Boolean> lineData, int count) {
        return IntStream.range(0, count)
                .mapToObj(ign -> lineData.removeFirst())
                .collect(Collectors.toList());
    }

    /**
     * Convert a list of bits to string for use with a PF register.
     *
     * @param bits    Bits to convert
     * @param reverse Whether to reverse the bits
     * @return Byte data to be written to PF register
     */
    protected static String getPFByte(List<Boolean> bits, boolean reverse) {
        if (reverse) {
            Collections.reverse(bits);
        }
        return Utilities.getByte(bits);
    }
}
