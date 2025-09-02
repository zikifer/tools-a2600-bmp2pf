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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class BasePlayfieldGenerator implements PlayfieldGenerator {
    private static final String BYTE_PREFIX = "    .byte %";
    private static final int WHITE = 0x00FFFFFF;

    private static final String OUTPUT_FILE_START = "PLAYFIELD_HEIGHT = ";
    private static final String ALIGNMENT_BLOCK = """
            
                if >. != >[.+(PLAYFIELD_HEIGHT)]
                    align 256
                endif
            
            """;

    private final String inputFile;
    private final String outputFile;
    private final int outputBufferLines;
    private BufferedImage bufferedImage;
    private ColorModel colorModel;
    private int imageHeight;
    private int currentLine;
    private Map<String, List<String>> outputMap;

    protected BasePlayfieldGenerator(PlayfieldGeneratorBuilder builder) {
        this.inputFile = builder.getInputFile();
        this.outputFile = builder.getOutputFile();
        this.outputBufferLines = builder.getOutputBufferLines();
    }

    protected int getOutputBufferLines() {
        return outputBufferLines;
    }

    protected void openInputFile(int requiredWidth) throws IOException {
        bufferedImage = ImageIO.read(new File(inputFile));
        if (bufferedImage.getWidth() != requiredWidth) {
            throw new IOException("Invalid file format, required image width=" + requiredWidth);
        }

        colorModel = bufferedImage.getColorModel();
        imageHeight = bufferedImage.getHeight();
        currentLine = 0;
        outputMap = new LinkedHashMap<>();
        System.out.println("Reading input file: " + inputFile);
    }

    protected List<Boolean> getNextLine() {
        if (currentLine >= imageHeight) {
            return Collections.emptyList();
        }

        List<Boolean> lineData = new ArrayList<>();
        int width = bufferedImage.getWidth();
        for (int i = 0; i < width; i++) {
            int argb = bufferedImage.getRGB(i, currentLine);
            boolean bit = colorModel.hasAlpha()
                    ? (colorModel.getAlpha(argb) != 0)
                    : (argb != -1);
            lineData.add(bit);
        }

        currentLine++;
        return lineData;
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
