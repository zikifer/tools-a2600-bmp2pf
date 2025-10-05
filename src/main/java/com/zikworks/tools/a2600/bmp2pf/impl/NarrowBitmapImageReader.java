package com.zikworks.tools.a2600.bmp2pf.impl;

import com.zikworks.tools.a2600.bmp2pf.BitmapImageReader;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * A bitmap image reader that uses 1 pixel per bit.
 */
public class NarrowBitmapImageReader extends BitmapImageReader {

    public NarrowBitmapImageReader(BufferedImage bufferedImage, int kernelLines) {
        super(bufferedImage, kernelLines);
    }

    @Override
    public void checkWidth(int expectedWidth) throws IOException {
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
        int width = getImageWidth();
        for (int x = 0; x < width; x++) {
            int rgb = getRGB(x, line);
            boolean bit = getBit(rgb);
            int ntsc = bit ? getNtscColor(rgb) : 0;
            int pal = bit ? getPalColor(rgb) : 0;
            boolean collide = isCollision(rgb);

            playfieldLineData.withNtscColor(ntsc).withPalColor(pal).withBit(bit).withCollide(collide);
        }

        return playfieldLineData;
    }
}
