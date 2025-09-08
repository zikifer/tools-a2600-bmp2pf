package com.zikworks.tools.a2600.bmp2pf.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SymmetricalMirrorPlayfieldLineDataParser extends SymmetricalPlayfieldLineDataParser {

    /**
     * Since there are only 20 bits we need to double the collision data to 40.
     * Additional bits are reversed.  PlayfieldLineData is updated in-place.
     *
     * @param lineData Parsed line data
     */
    protected void updateCollisionData(PlayfieldLineData lineData) {
        List<Boolean> collisions = lineData.getCollisions();
        List<Boolean> reverse = new ArrayList<>(collisions);
        Collections.reverse(reverse);
        collisions.addAll(reverse);
    }
}
