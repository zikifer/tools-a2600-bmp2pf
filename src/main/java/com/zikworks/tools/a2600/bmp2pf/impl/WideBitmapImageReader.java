package com.zikworks.tools.a2600.bmp2pf.impl;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * A bitmap image reader that uses 4 pixels per bit.
 */
public class WideBitmapImageReader extends BitmapImageReader {

    public WideBitmapImageReader(BufferedImage bufferedImage) {
        super(bufferedImage);
    }

    @Override
    public void checkWidth(int expectedWidth) throws IOException {
        expectedWidth *= 4;
        if (getImageWidth() != expectedWidth) {
            throw new IOException("Invalid file format, required image width=" + expectedWidth);
        }
    }

    /**
     * Read a line from the image.
     *
     * @param line Line number to read
     * @return Playfield line data
     */
    @Override
    protected PlayfieldLineData readLine(int line) {
        PlayfieldLineData playfieldLineData = new PlayfieldLineData();

        // Since there are 4 pixels per bit we need to do some extra calculations

        // Set the bit based on the number of pixels in the group of 4
        int widePixel = 0;

        // Set the NTSC and PAL colors based on the first non-black pixel in the group
        int wideNtsc = 0;
        int widePal = 0;

        int width = getImageWidth();
        for (int x = 0; x < width; x++) {
            int rgb = getRGB(x, line);
            boolean bit = getBit(rgb);
            int ntsc = bit ? getNtscColor(rgb) : 0;
            int pal = bit ? getPalColor(rgb) : 0;

            widePixel += (bit) ? 1 : 0;
            if (wideNtsc == 0) {
                wideNtsc = ntsc;
            }
            if (widePal == 0) {
                widePal = pal;
            }

            // Every 4 pixels add to the line data
            if (x % 4 == 3) {
                playfieldLineData
                        .withNtscColor(wideNtsc)
                        .withPalColor(widePal)
                        .withBit(widePixel > 1);

                widePixel = 0;
                wideNtsc = 0;
                widePal = 0;
            }
        }

        return playfieldLineData;
    }
}
