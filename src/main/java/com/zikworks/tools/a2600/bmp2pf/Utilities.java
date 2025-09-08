package com.zikworks.tools.a2600.bmp2pf;

import java.util.List;
import java.util.stream.Collectors;

public class Utilities {

    /**
     * Convert a list of bits to a byte string.
     *
     * @param bits List of bits
     * @return String of 0's and 1's in the same order as the bits in the list
     */
    public static String getByte(List<Boolean> bits) {
        while (bits.size() < 8) {
            bits.add(false);
        }
        return bits.stream().map(Utilities::toStr).collect(Collectors.joining(""));
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
