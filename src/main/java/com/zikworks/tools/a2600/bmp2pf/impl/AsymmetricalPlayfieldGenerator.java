package com.zikworks.tools.a2600.bmp2pf.impl;

import com.zikworks.tools.a2600.bmp2pf.PlayfieldGeneratorBuilder;

public abstract class AsymmetricalPlayfieldGenerator extends BasePlayfieldGenerator {

    private static final int IMAGE_WIDTH = 40;

    protected AsymmetricalPlayfieldGenerator(PlayfieldGeneratorBuilder builder) {
        super(builder, IMAGE_WIDTH);
    }
}
