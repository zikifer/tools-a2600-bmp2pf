package com.zikworks.tools.a2600.bmp2pf.impl;

import java.util.ArrayList;
import java.util.List;

public class SymmetricalRepeatPlayfieldLineDataParser extends SymmetricalPlayfieldLineDataParser {

    /**
     * Since there are only 20 bits we need to double the collision data to 40.
     * Additional bits are repeated.  PlayfieldLineData is updated in-place.
     *
     * @param lineData Parsed line data
     */
    @Override
    protected void updateCollisionData(PlayfieldLineData lineData) {
        List<Boolean> collisions = lineData.getCollisions();
        collisions.addAll(new ArrayList<>(collisions));
    }
}
