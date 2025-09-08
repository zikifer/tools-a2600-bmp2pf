package com.zikworks.tools.a2600.bmp2pf;

import com.zikworks.tools.a2600.bmp2pf.impl.PlayfieldLineData;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.IOException;
import java.util.Iterator;

public abstract class BitmapImageReader implements Iterator<PlayfieldLineData> {

    private final BufferedImage bufferedImage;
    private final ColorModel colorModel;
    private final int imageHeight;
    private int currentLine;

    public BitmapImageReader(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
        this.colorModel = bufferedImage.getColorModel();
        this.imageHeight = bufferedImage.getHeight();
        this.currentLine = 0;
    }

    public abstract void checkWidth(int expectedWidth) throws IOException;

    /**
     * Returns true if the iteration has more elements.
     *
     * @return true if there is another line to read; false otherwise
     */
    @Override
    public boolean hasNext() {
        return currentLine < imageHeight;
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return The playfield line data for the current line in the image
     */
    @Override
    public PlayfieldLineData next() {
        return readLine(currentLine++);
    }

    protected abstract PlayfieldLineData readLine(int line);

    protected int getImageWidth() {
        return bufferedImage.getWidth();
    }

    protected int getRGB(int x, int y) {
        return bufferedImage.getRGB(x, y);
    }

    protected boolean getBit(int rgb) {
        return colorModel.hasAlpha()
                ? (colorModel.getAlpha(rgb) != 0)
                : (rgb != -1);
    }

    protected int getNtscColor(int rgb) {
        Color color = new Color(rgb);
        return color.getBlue();
    }

    protected int getPalColor(int rgb) {
        Color color = new Color(rgb);
        return color.getGreen();
    }

    protected boolean isCollision(int rgb) {
        Color color = new Color(rgb);
        return color.getRed() > 7;
    }
}
