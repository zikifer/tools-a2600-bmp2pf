package com.zikworks.tools.a2600.bmp2pf.impl;

import com.zikworks.tools.a2600.bmp2pf.PlayfieldGenerator;
import com.zikworks.tools.a2600.bmp2pf.PlayfieldGeneratorBuilder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
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
    private final int expectedWidth;
    private final Map<String, List<String>> outputMap;

    protected BasePlayfieldGenerator(PlayfieldGeneratorBuilder builder, int expectedWidth) {
        this.inputFile = builder.getInputFile();
        this.outputFile = builder.getOutputFile();
        this.fullScale = builder.isFullScale();
        this.outputBufferLines = builder.getOutputBufferLines();
        this.expectedWidth = expectedWidth;
        this.outputMap = new LinkedHashMap<>();
    }

    /**
     * Generate the ASM output file from the input BMP file.
     *
     * @throws IOException I/O error during generation
     */
    public void generate() throws IOException {
        // Open image and create reader
        BufferedImage bufferedImage = ImageIO.read(new File(inputFile));
        BitmapImageReader bitmapImageReader = fullScale
                ? new WideBitmapImageReader(bufferedImage)
                : new NarrowBitmapImageReader(bufferedImage);

        System.out.println("Reading input file: " + inputFile);

        // Verify input file is correct width
        bitmapImageReader.checkWidth(expectedWidth);

        while (bitmapImageReader.hasNext()) {
            PlayfieldLineData lineData = bitmapImageReader.next();
            parseLineData(lineData);
            addColorData(lineData);
        }

        // Add any extra empty lines
        for (int i = 0; i < outputBufferLines; i++) {
            var bits = IntStream.range(0, expectedWidth).mapToObj(ign -> Boolean.FALSE).toList();
            PlayfieldLineData lineData = new PlayfieldLineData().withBits(bits);
            parseLineData(lineData);
            addColorData(lineData);
        }

        // Finally write output file
        writeOutputFile(bufferedImage.getHeight());
    }

    protected abstract void parseLineData(PlayfieldLineData lineData);

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
     * @param data        Byte data
     */
    protected void addByteToSection(String sectionName, String data) {
        List<String> sectionData = outputMap.computeIfAbsent(sectionName, ign -> new LinkedList<>());
        sectionData.addFirst(BYTE_PREFIX + data);
    }

    /**
     * Add the color data to the output map.
     *
     * @param lineData Playfield line data
     */
    private void addColorData(PlayfieldLineData lineData) {
        String ntsc = Integer.toHexString(lineData.getNtscColor());
        if (ntsc.length() == 1) {
            ntsc = "0" + ntsc;
        }

        String pal = Integer.toHexString(lineData.getPalColor());
        if (pal.length() == 1) {
            pal = "0" + pal;
        }

        String line = String.format("   .byte $%s ; $%s", ntsc, pal);

        String name = "PFColors     ; (NTSC : PAL)";
        List<String> sectionData = outputMap.computeIfAbsent(name, ign -> new LinkedList<>());
        sectionData.addFirst(line);
    }

    /**
     * Write the output file.
     *
     * @throws IOException Error writing to file
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void writeOutputFile(int imageHeight) throws IOException {
        Path outputPath = Path.of(outputFile).toAbsolutePath().normalize();
        outputPath.getParent().toFile().mkdirs();

        try (FileWriter writer = new FileWriter(outputPath.toString(), false)) {
            // Write the header portion
            int length = imageHeight + outputBufferLines;
            writer.write(OUTPUT_FILE_START + length + System.lineSeparator());

            // Write each segment
            var keySet = outputMap.keySet();
            for (String name : keySet) {
                writer.write(ALIGNMENT_BLOCK);

                writer.write(name + System.lineSeparator());
                for (String dataLine : outputMap.get(name)) {
                    writer.write(dataLine + System.lineSeparator());
                }
            }
        }

        System.out.println("Wrote output file: " + outputFile);
    }

    /**
     * Convert a list of bits to string for use with a half PF register (PF0).
     *
     * @param bits    Bits to convert
     * @param reverse Whether to reverse the bits
     * @return Byte data to be written to PF register
     */
    protected static String getHalfPFByte(List<Boolean> bits, boolean reverse) {
        if (reverse) {
            Collections.reverse(bits);
        }
        return getByte(bits) + "0000";
    }

    /**
     * Convert a list of bits to string for use with a full PF register (PF1/2).
     *
     * @param bits    Bits to convert
     * @param reverse Whether to reverse the bits
     * @return Byte data to be written to PF register
     */
    protected static String getFullPFByte(List<Boolean> bits, boolean reverse) {
        if (reverse) {
            Collections.reverse(bits);
        }
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
     * Convert a boolean to a string.
     *
     * @param bit Bit to convert
     * @return "1" if passed true; "0" if passed false
     */
    private static String toStr(boolean bit) {
        return bit ? "1" : "0";
    }
}
