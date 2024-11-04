package com.allendowney.thinkdast.utils;

import com.allendowney.thinkdast.constants.ResourcesConstants;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StopWordUtility {
    public Set<String> getStopWords() throws IOException {
        final String filePath = this.getFilePath();

        final Set<String> stopWords = new HashSet<>();
        parseWordsToSet(stopWords, filePath);

        return stopWords;
    }

    private void parseWordsToSet(Set<String> words, String filePath) throws IOException {
        try (var br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                words.addAll(Arrays.asList(line.split(", ")));
            }
        }
    }

    private String getFilePath() throws FileNotFoundException {
        final URL path = this.getClass().getClassLoader().getResource(ResourcesConstants.STOP_WORD_PATH);
        if (path == null) {
            throw new FileNotFoundException("Failed to find stop-words file at " + ResourcesConstants.STOP_WORD_PATH);
        }

        return URLDecoder.decode(path.getFile(), StandardCharsets.UTF_8);
    }
}
