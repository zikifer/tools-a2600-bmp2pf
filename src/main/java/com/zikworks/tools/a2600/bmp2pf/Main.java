package com.zikworks.tools.a2600.bmp2pf;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.help.HelpFormatter;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        Options options = new Options()
                .addOption(CommandLineOption.INPUT_FILE.toOption())
                .addOption(CommandLineOption.OUTPUT_FILE.toOption())
                .addOption(CommandLineOption.BUFFER_OUTPUT.toOption())
                .addOption(CommandLineOption.SYMMETRICAL.toOption())
                .addOption(CommandLineOption.ASYMMETRICAL.toOption())
                .addOption(CommandLineOption.MIRRORED.toOption())
                .addOption(CommandLineOption.REPEATED.toOption());

        CommandLineParser commandLineParser = new DefaultParser();

        try {
            CommandLine commandLine = commandLineParser.parse(options, args);
            PlayfieldGenerator generator = PlayfieldGenerator.builder(commandLine).build();
            generator.generate();
            System.out.println("Done.");
        } catch (ParseException ex) {
            System.err.println(ex.getMessage());
            showHelp(options);
        } catch (IOException ex) {
            System.err.println("Failed to convert BMP file: " + ex.getMessage());
        }
    }

    private static void showHelp(Options options) {
        HelpFormatter formatter = HelpFormatter.builder().setShowSince(false).get();
        try {
            formatter.printOptions(options);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
