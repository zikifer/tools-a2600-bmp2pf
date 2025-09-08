package com.zikworks.tools.a2600.bmp2pf.impl;

import com.zikworks.tools.a2600.bmp2pf.PlayfieldLineDataParser;

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
     * Convert a list of bits to string for use with a half PF register (PF0).
     *
     * @param bits    Bits to convert
     * @param reverse Whether to reverse the bits
     * @return Byte data to be written to PF register
     */
    protected static String getHalfPFByte(List<Boolean> bits, boolean reverse) {
        if (reverse) {
            Collections.reverse(bits);
        }
        return getByte(bits) + "0000";
    }

    /**
     * Convert a list of bits to string for use with a full PF register (PF1/2).
     *
     * @param bits    Bits to convert
     * @param reverse Whether to reverse the bits
     * @return Byte data to be written to PF register
     */
    protected static String getFullPFByte(List<Boolean> bits, boolean reverse) {
        if (reverse) {
            Collections.reverse(bits);
        }
        return getByte(bits);
    }

    /**
     * Convert a list of bits to a byte string.
     *
     * @param bits List of bits
     * @return String of 0's and 1's in the same order as the bits in the list
     */
    private static String getByte(List<Boolean> bits) {
        return bits.stream().map(BasePlayfieldLineDataParser::toStr).collect(Collectors.joining(""));
    }

    /**
     * Convert a boolean to a string.
     *
     * @param bit Bit to convert
     * @return "1" if passed true; "0" if passed false
     */
    private static String toStr(boolean bit) {
        return bit ? "1" : "0";
    }
}
