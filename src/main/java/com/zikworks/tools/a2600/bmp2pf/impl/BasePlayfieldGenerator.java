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

    protected int getOutputBufferLines() {
        return outputBufferLines;
    }

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

    private boolean getWidePixelBit(int x) {
        int sum = IntStream.range(x, x + 4)
                .mapToObj(this::getNarrowPixelBit)
                .map(val -> val ? 1 : 0)
                .mapToInt(Integer::intValue)
                .sum();
        return sum > 1;
    }

    private boolean getNarrowPixelBit(int x) {
        int argb = bufferedImage.getRGB(x, currentLine);
        return colorModel.hasAlpha()
                ? (colorModel.getAlpha(argb) != 0)
                : (argb != -1);
    }

    protected List<Boolean> popFromLineData(LinkedList<Boolean> lineData, int count) {
        return IntStream.range(0, count)
                .mapToObj(ign -> lineData.removeFirst())
                .collect(Collectors.toList());
    }

    protected void addByteToSection(String sectionName, String data) {
        List<String> sectionData = outputMap.computeIfAbsent(sectionName, ign -> new LinkedList<>());
        sectionData.addFirst(BYTE_PREFIX + data);
    }

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

    protected static String getPF0Byte(List<Boolean> bits) {
        Collections.reverse(bits);
        return getByte(bits) + "0000";
    }

    protected static String getPF1Byte(List<Boolean> bits) {
        return getByte(bits);
    }

    protected static String getPF2Byte(List<Boolean> bits) {
        Collections.reverse(bits);
        return getByte(bits);
    }

    private static String getByte(List<Boolean> bits) {
        return bits.stream().map(BasePlayfieldGenerator::toStr).collect(Collectors.joining(""));
    }

    private static String toStr(boolean bit) {
        return bit ? "1" : "0";
    }
}
