package com.zikworks.tools.a2600.bmp2pf.impl;

import com.zikworks.tools.a2600.bmp2pf.PlayfieldGenerator;
import com.zikworks.tools.a2600.bmp2pf.PlayfieldGeneratorBuilder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * An abstract class the contains all the methods that are used regardless
 * of symmetrical or asymmetrical playfield generation.
 */
public abstract class BasePlayfieldGenerator implements PlayfieldGenerator {
    private static final String BYTE_PREFIX = "    .byte %";
    private static final String OUTPUT_FILE_START = "PLAYFIELD_HEIGHT = ";
    private static final String ALIGNMENT_BLOCK = """
            
                if >. != >[.+(PLAYFIELD_HEIGHT)]
                    align 256
                endif
            
            """;

    private final String inputFile;
    private final String outputFile;
    private final boolean fullScale;
    private final int outputBufferLines;
    private BufferedImage bufferedImage;
    private ColorModel colorModel;
    private int imageHeight;
    private int currentLine;
    private Map<String, List<String>> outputMap;

    protected BasePlayfieldGenerator(PlayfieldGeneratorBuilder builder) {
        this.inputFile = builder.getInputFile();
        this.outputFile = builder.getOutputFile();
        this.fullScale = builder.isFullScale();
        this.outputBufferLines = builder.getOutputBufferLines();
    }

    /**
     * Get the number of empty lines to add to the end of the playfield.
     *
     * @return Number of empty lines
     */
    protected int getOutputBufferLines() {
        return outputBufferLines;
    }

    /**
     * Open the input file.
     *
     * @param requiredWidth Required width of the BMP
     * @throws IOException Error opening the file or invalid width
     */
    protected void openInputFile(int requiredWidth) throws IOException {
        bufferedImage = ImageIO.read(new File(inputFile));
        int expectedWidth = fullScale ? (requiredWidth * 4) : requiredWidth;
        if (bufferedImage.getWidth() != expectedWidth) {
            throw new IOException("Invalid file format, required image width=" + expectedWidth);
        }

        colorModel = bufferedImage.getColorModel();
        imageHeight = bufferedImage.getHeight();
        currentLine = 0;
        outputMap = new LinkedHashMap<>();
        System.out.println("Reading input file: " + inputFile);
    }

    /**
     * Get the next line of the input file.  Length of the output list
     * matches the width of the input file.
     *
     * @return A list of bits where true means the bit is set and false not set
     */
    protected LinkedList<Boolean> getNextLine() {
        if (currentLine >= imageHeight) {
            return new LinkedList<>();
        }

        LinkedList<Boolean> lineData = new LinkedList<>();
        int width = bufferedImage.getWidth();
        int inc = fullScale ? 4 : 1;
        for (int x = 0; x < width; x += inc) {
            boolean bit = fullScale ? getWidePixelBit(x) : getNarrowPixelBit(x);
            lineData.add(bit);
        }

        currentLine++;
        return lineData;
    }

    /**
     * When in fullScale mode (-x option) a bit is represented by 4 continuous pixels.
     *
     * @param x X index
     * @return True if the pixel is set; false otherwise
     */
    private boolean getWidePixelBit(int x) {
        int sum = IntStream.range(x, x + 4)
                .mapToObj(this::getNarrowPixelBit)
                .map(BasePlayfieldGenerator::toInt)
                .mapToInt(Integer::intValue)
                .sum();
        return sum > 1;
    }

    /**
     * When in normal mode a bit is represented by a single pixel.
     *
     * @param x X index
     * @return True if the pixel is set; false otherwise
     */
    private boolean getNarrowPixelBit(int x) {
        int argb = bufferedImage.getRGB(x, currentLine);
        return colorModel.hasAlpha()
                ? (colorModel.getAlpha(argb) != 0)
                : (argb != -1);
    }

    /**
     * Remove a certain number of booleans from the head of the list and return them.
     *
     * @param lineData List to manipulate
     * @param count    Number of elements to remove and return
     * @return A list of the removed elements
     */
    protected List<Boolean> popFromLineData(LinkedList<Boolean> lineData, int count) {
        return IntStream.range(0, count)
                .mapToObj(ign -> lineData.removeFirst())
                .collect(Collectors.toList());
    }

    /**
     * Save a byte to the given section of the output file.
     *
     * @param sectionName Section name for the byte
     * @param data Byte data
     */
    protected void addByteToSection(String sectionName, String data) {
        List<String> sectionData = outputMap.computeIfAbsent(sectionName, ign -> new LinkedList<>());
        sectionData.addFirst(BYTE_PREFIX + data);
    }

    /**
     * Write the output file.
     *
     * @throws IOException Error writing to file
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected void writeOutputFile() throws IOException {
        Path outputPath = Path.of(outputFile).toAbsolutePath().normalize();
        outputPath.getParent().toFile().mkdirs();

        try (FileWriter writer = new FileWriter(outputPath.toString(), false)) {
            // Write the header portion
            int length = imageHeight + outputBufferLines;
            writer.write(OUTPUT_FILE_START + length + System.lineSeparator());
            writer.write(ALIGNMENT_BLOCK);

            // Write each segment
            var keySet = outputMap.keySet();
            for (String name : keySet) {
                writer.write(name + System.lineSeparator());
                for (String dataLine : outputMap.get(name)) {
                    writer.write(dataLine + System.lineSeparator());
                }

                writer.write(ALIGNMENT_BLOCK);
            }
        }

        System.out.println("Wrote output file: " + outputFile);
    }

    /**
     * Convert a list of bits to string for use with PF0.
     * PF0 only uses the upper 4 bits, and are written in reverse order.
     *
     * @param bits Bits to convert
     * @return Byte data to be written to PF0
     */
    protected static String getPF0Byte(List<Boolean> bits) {
        Collections.reverse(bits);
        return getByte(bits) + "0000";
    }

    /**
     * Convert a list of bits to string for use with PF1.
     * PF1 bits are written in order.
     *
     * @param bits Bits to convert
     * @return Byte data to be written to PF1
     */
    protected static String getPF1Byte(List<Boolean> bits) {
        return getByte(bits);
    }

    /**
     * Convert a list of bits to string for use with PF2.
     * PF2 bits are written in reverse order.
     *
     * @param bits Bits to convert
     * @return Byte data to be written to PF2
     */
    protected static String getPF2Byte(List<Boolean> bits) {
        Collections.reverse(bits);
        return getByte(bits);
    }

    /**
     * Convert a list of bits to a byte string.
     *
     * @param bits List of bits
     * @return String of 0's and 1's in the same order as the bits in the list
     */
    private static String getByte(List<Boolean> bits) {
        return bits.stream().map(BasePlayfieldGenerator::toStr).collect(Collectors.joining(""));
    }

    /**
     * Convert a boolean to an integer.
     *
     * @param bit Bit to convert
     * @return The value 1 if passed true; otherwise 0
     */
    private static int toInt(boolean bit) {
        return bit ? 1 : 0;
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
