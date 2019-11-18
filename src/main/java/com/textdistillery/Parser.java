package com.textdistillery;

import opennlp.tools.tokenize.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Parser {

    public static String[] tokenize(String text) {
        SimpleTokenizer tokenizer = SimpleTokenizer.INSTANCE;
        return tokenizer.tokenize(text);
    }

    public static String detokenize(String[] tokens) {
        StringBuilder builder = new StringBuilder();
        for (String token: tokens) {
            builder.append(token);
            builder.append(" ");
        }
        return builder.toString();
    }

    public static String readFile(String filename) throws IOException {
        File file = new File(Config.RESOURCE + "io/" + filename);
        return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
    }

    public static void writeFile(String output, String filename) throws IOException {
        File file = new File(Config.RESOURCE + "io/" + filename);
        FileUtils.writeStringToFile(file, output, StandardCharsets.UTF_8);
    }
}
