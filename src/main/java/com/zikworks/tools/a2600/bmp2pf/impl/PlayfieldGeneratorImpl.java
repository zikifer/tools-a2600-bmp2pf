package com.zikworks.tools.a2600.bmp2pf.impl;

import com.zikworks.tools.a2600.bmp2pf.BitmapImageReader;
import com.zikworks.tools.a2600.bmp2pf.PlayfieldGenerator;
import com.zikworks.tools.a2600.bmp2pf.PlayfieldGeneratorBuilder;
import com.zikworks.tools.a2600.bmp2pf.PlayfieldLineDataParser;
import com.zikworks.tools.a2600.bmp2pf.PlayfieldOutputSection;
import com.zikworks.tools.a2600.bmp2pf.Utilities;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * An abstract class the contains all the methods that are used regardless
 * of symmetrical or asymmetrical playfield generation.
 */
public class PlayfieldGeneratorImpl implements PlayfieldGenerator {
    private static final String DATA_LINE_PREFIX = "    .byte ";
    private static final String BYTE_PREFIX = DATA_LINE_PREFIX + "%";
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
    private final PlayfieldLineDataParser parser;
    private final Map<PlayfieldOutputSection, List<String>> outputMap;

    public PlayfieldGeneratorImpl(PlayfieldGeneratorBuilder builder, PlayfieldLineDataParser parser) {
        this.inputFile = builder.getInputFile();
        this.outputFile = builder.getOutputFile();
        this.fullScale = builder.isFullScale();
        this.outputBufferLines = builder.getOutputBufferLines();
        this.parser = parser;
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
        int expectedWidth = parser.getExpectedWidth();
        bitmapImageReader.checkWidth(expectedWidth);

        while (bitmapImageReader.hasNext()) {
            PlayfieldLineData lineData = bitmapImageReader.next();
            var parsedLine = parser.parseLineData(lineData);
            addParsedLineToOutputMap(parsedLine);
            addColorData(lineData);
            addCollisionData(lineData);
        }

        // Add any extra empty lines
        for (int i = 0; i < outputBufferLines; i++) {
            var bits = new ArrayList<>(IntStream.range(0, expectedWidth).mapToObj(ign -> Boolean.FALSE).toList());
            PlayfieldLineData lineData = new PlayfieldLineData().withBits(bits).withCollisions(bits);
            var parsedLine = parser.parseLineData(lineData);
            addParsedLineToOutputMap(parsedLine);
            addColorData(lineData);
            addCollisionData(lineData);
        }

        // Finally write output file
        writeOutputFile(bufferedImage.getHeight());
    }

    private void addParsedLineToOutputMap(Map<PlayfieldOutputSection, String> parsedLineData) {
        parsedLineData.forEach((section, data) -> {
            List<String> sectionData = getSectionDataFromOutputMap(section);
            sectionData.addFirst(BYTE_PREFIX + data);
        });
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

        List<String> sectionData = getSectionDataFromOutputMap(PlayfieldOutputSection.PFColors);
        sectionData.addFirst(line);
    }

    private void addCollisionData(PlayfieldLineData lineData) {
        List<Boolean> collisions = lineData.getCollisions();
        List<String> bytes = new ArrayList<>();
        int chunkSize = 8;
        for (int i = 0; i < collisions.size(); i += chunkSize) {
            int end = Math.min(i + chunkSize, collisions.size());
            var sublist = new ArrayList<>(collisions.subList(i, end));
            String byteData = "%" + Utilities.getByte(sublist);
            bytes.add(byteData);
        }

        String line = "   .byte " + String.join(", ", bytes);
        List<String> sectionData = getSectionDataFromOutputMap(PlayfieldOutputSection.PFCollision);
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
            for (PlayfieldOutputSection section : keySet) {
                if (section == PlayfieldOutputSection.PFCollision) {
                    continue;
                }

                writer.write(ALIGNMENT_BLOCK);
                writer.write(section.name() + System.lineSeparator());
                for (String dataLine : outputMap.get(section)) {
                    writer.write(dataLine + System.lineSeparator());
                }
            }

            List<String> data = outputMap.get(PlayfieldOutputSection.PFCollision);
            if (data != null) {
                writer.write(ALIGNMENT_BLOCK);
                int sectionCount = 0;
                int chunkSize = 8;
                for (int i = 0; i < data.size(); i += chunkSize, sectionCount++) {
                    int end = Math.min(i + chunkSize, data.size());
                    var sublist = new ArrayList<>(data.subList(i, end));
                    String sectionName = PlayfieldOutputSection.PFCollision.name() + sectionCount;
                    writer.write(sectionName + System.lineSeparator());
                    for (String dataLine : sublist) {
                        writer.write(dataLine + System.lineSeparator());
                    }
                }

                String sectionName = PlayfieldOutputSection.PFCollision.name();
                writer.write(System.lineSeparator());
                writer.write(sectionName + "_Lo" + System.lineSeparator());
                for (int i = 0; i < sectionCount; i++) {
                    String dataLine = DATA_LINE_PREFIX + "#<" + sectionName + i;
                    writer.write(dataLine + System.lineSeparator());
                }

                writer.write(System.lineSeparator());
                writer.write(sectionName + "_Hi" + System.lineSeparator());
                for (int i = 0; i < sectionCount; i++) {
                    String dataLine = DATA_LINE_PREFIX + "#>" + sectionName + i;
                    writer.write(dataLine + System.lineSeparator());
                }
            }
        }

        System.out.println("Wrote output file: " + outputFile);
    }

    private List<String> getSectionDataFromOutputMap(PlayfieldOutputSection section) {
        return outputMap.computeIfAbsent(section, ign -> new LinkedList<>());
    }
}
