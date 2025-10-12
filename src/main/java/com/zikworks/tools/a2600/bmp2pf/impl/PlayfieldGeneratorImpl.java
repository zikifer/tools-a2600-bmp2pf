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
    private final boolean excludeColor;
    private final int kernelLines;
    private final int collisionLines;
    private final int outputBufferLines;
    private final boolean separateCollisionFile;
    private final String outputSectionPrefix;
    private final PlayfieldLineDataParser parser;
    private final Map<PlayfieldOutputSection, List<String>> outputMap;
    private int lineCount = 0;

    public PlayfieldGeneratorImpl(PlayfieldGeneratorBuilder builder, PlayfieldLineDataParser parser) {
        this.inputFile = builder.getInputFile();
        this.outputFile = builder.getOutputFile();
        this.fullScale = builder.isFullScale();
        this.excludeColor = builder.isExcludeColor();
        this.kernelLines = builder.getKernelLines();
        this.collisionLines = builder.getCollisionLines();
        this.outputBufferLines = builder.getOutputBufferLines();
        this.separateCollisionFile = builder.isSeparateCollisionFile();
        this.outputSectionPrefix = builder.getOutputSectionPrefix();
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
                ? new WideBitmapImageReader(bufferedImage, kernelLines)
                : new NarrowBitmapImageReader(bufferedImage, kernelLines);

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
        if (collisionLines > 0 && (lineCount++ % collisionLines == 0)) {
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
    }

    /**
     * Write the output file.
     *
     * @throws IOException Error writing to file
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void writeOutputFile(int imageHeight) throws IOException {
        Path collisionFile = null;
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

                if (excludeColor && (section == PlayfieldOutputSection.PFColors)) {
                    continue;
                }

                writer.write(ALIGNMENT_BLOCK);
                writer.write(outputSectionPrefix + section.name() + System.lineSeparator());
                for (String dataLine : outputMap.get(section)) {
                    writer.write(dataLine + System.lineSeparator());
                }
            }

            List<String> data = outputMap.get(PlayfieldOutputSection.PFCollision);
            if (data != null) {
                if (separateCollisionFile) {
                    Path fileName = outputPath.getFileName();
                    String[] parts = fileName.toString().split("\\.");
                    String rootName = parts[0];
                    String ext = parts[parts.length - 1];
                    String newName = rootName + "_collision." + ext;
                    collisionFile = outputPath.resolveSibling(newName);
                    try (FileWriter collisionWriter = new FileWriter(collisionFile.toString(), false)) {
                        writeCollisionFile(collisionWriter, data);
                    }
                } else {
                    writeCollisionFile(writer, data);
                }
            }
        }

        System.out.println("\nWrote output file: " + outputFile);
        if (collisionFile != null) {
            System.out.println("Wrote collision file: " + collisionFile);
        }
    }

    private void writeCollisionFile(FileWriter writer, List<String> data) throws IOException {
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

    private List<String> getSectionDataFromOutputMap(PlayfieldOutputSection section) {
        return outputMap.computeIfAbsent(section, ign -> new LinkedList<>());
    }
}
