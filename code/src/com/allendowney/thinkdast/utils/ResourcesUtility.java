package com.allendowney.thinkdast.utils;

import com.allendowney.thinkdast.constants.ResourcesConstants;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import javax.print.Doc;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public class ResourcesUtility {
    private static final String srcPath = String.join(File.separator, System.getProperty("user.dir"),  "src");
    public void ensureWikiDir() throws IOException {
        final Path srcPath = Path.of(ResourcesUtility.srcPath);
        final Path wikiPath = Path.of(srcPath +File.separator + ResourcesConstants.WIKI_PATH);

        if (wikiPath.toFile().mkdirs()) {
            System.out.println(wikiPath + " created.");
        }
    }

    public void downloadWikiPage(Document page) throws IOException {
        ensureWikiDir();

        final String pageName = this.getWikiPageFileNameFromURL(new URL(page.location()));
        final String dest = ResourcesUtility.srcPath + File.separator + ResourcesConstants.WIKI_PATH + File.separator + pageName;
        final File file = Files.createFile(Path.of(dest)).toFile();

        try (FileWriter fw = new FileWriter(file)) {
            fw.write(page.html());
        }
    }

    public String getWikiPageFileNameFromURL(URL url) {
        return Arrays
                .stream(url.getPath().split("/"))
                .toList()
                .getLast()
                .replace(" ", "_");
    }
}
