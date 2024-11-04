package com.allendowney.thinkdast.constants;

import java.io.File;

public interface ResourcesConstants {
    String WIKI_PATH = String.join(File.separator, "resources", "en", "wikipedia", "org", "wiki");

    String STOP_WORD_PATH = String.join(File.separator, "resources", "stop-words.txt");
}
