package com.zikworks.tools.a2600.bmp2pf;

import com.zikworks.tools.a2600.bmp2pf.impl.PlayfieldLineData;

import java.util.Map;

public interface PlayfieldLineDataParser {

    /**
     * Get the expected width of this parser.
     *
     * @return Expected width
     */
    int getExpectedWidth();

    /**
     * Parse line data into its individual Playfield Register parts.
     *
     * @param lineData Playfield line data to parse
     * @return Map of output section and byte value for that section
     */
    Map<PlayfieldOutputSection, String> parseLineData(PlayfieldLineData lineData);
}
