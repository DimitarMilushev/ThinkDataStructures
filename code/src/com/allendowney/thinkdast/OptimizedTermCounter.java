package com.allendowney.thinkdast;

import com.allendowney.thinkdast.utils.StopWordUtility;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class OptimizedTermCounter extends TermCounter {
    final Set<String> stopWords;
    public OptimizedTermCounter(String label) throws IOException {
        super(label);
        this.stopWords = new StopWordUtility().getStopWords();
    }

    @Override
    public void processText(String text) {
        String[] array = text.replaceAll("\\pP", " ").
                toLowerCase().
                split("\\s+");

        for (String term : array) {
            // filter out stop words.
            if (this.stopWords.contains(term)) continue;

            incrementTermCount(term);
        }
    }
}
