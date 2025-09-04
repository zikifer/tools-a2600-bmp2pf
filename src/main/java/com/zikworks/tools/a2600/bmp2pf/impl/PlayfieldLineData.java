package com.zikworks.tools.a2600.bmp2pf.impl;

import java.util.ArrayList;
import java.util.List;

public class PlayfieldLineData {
    private int ntscColor;
    private int palColor;
    private List<Boolean> bits = new ArrayList<>();

    public int getNtscColor() {
        return ntscColor;
    }

    public PlayfieldLineData withNtscColor(int ntscColor) {
        if (ntscColor > 0 && this.ntscColor == 0) {
            this.ntscColor = ntscColor;
        }
        return this;
    }

    public int getPalColor() {
        return palColor;
    }

    public PlayfieldLineData withPalColor(int palColor) {
        if (palColor > 0 && this.palColor == 0) {
            this.palColor = palColor;
        }
        return this;
    }

    public List<Boolean> getBits() {
        return bits;
    }

    @SuppressWarnings("UnusedReturnValue")
    public PlayfieldLineData withBit(boolean bit) {
        bits.add(bit);
        return this;
    }

    public PlayfieldLineData withBits(List<Boolean> bits) {
        this.bits = bits;
        return this;
    }
}
