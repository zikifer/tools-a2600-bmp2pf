package com.zikworks.tools.a2600.bmp2pf.impl;

public abstract class AsymmetricalPlayfieldLineDataParser extends BasePlayfieldLineDataParser {
    private static final int IMAGE_WIDTH = 40;

    @Override
    public int getExpectedWidth() {
        return IMAGE_WIDTH;
    }
}
