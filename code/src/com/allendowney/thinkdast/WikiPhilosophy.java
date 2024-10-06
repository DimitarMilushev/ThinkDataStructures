package com.allendowney.thinkdast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import static com.allendowney.thinkdast.constants.WikiConstants.*;

public class WikiPhilosophy {
    private final static String WIKI_PHILOSOPHY_URL = WIKI_ORIGIN_EN + WIKI_PHILOSOPHY;
    final static List<String> visited = new ArrayList<String>();
    final static WikiFetcher wf = WikiFetcher.getInstance();

    /**
     * Tests a conjecture about Wikipedia and Philosophy.
     *
     * https://en.wikipedia.org/wiki/Wikipedia:Getting_to_Philosophy
     *
     * 1. Clicking on the first non-parenthesized, non-italicized link
     * 2. Ignoring external links, links to the current page, or red links
     * 3. Stopping when reaching "Philosophy", a page with no links or a page
     *    that does not exist, or when a loop occurs
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        String destination = "https://en.wikipedia.org/wiki/Philosophy";
        String source = "https://en.wikipedia.org/wiki/Java_(programming_language)";

        testConjecture(destination, source, 10);
    }

    /**
     * Starts from given URL and follows first link until it finds the destination or exceeds the limit.
     *
     * @param destination
     * @param source
     * @throws IOException
     */
    public static void testConjecture(String destination, String source, int limit) throws IOException {
        WikiParser parser;
        Elements content;
        Element linkElement;
        String parsedLink;

        int pageHops = 0;
        while (!destination.equals(source) && limit >= pageHops) {
            ++pageHops;

            System.out.println(source + " #" + pageHops);
            content = wf.fetchWikipedia(source);
            parser = new WikiParser(content);

            linkElement = parser.findFirstLink();
            if (linkElement == null) {
                throw new RuntimeException("No links were found on " + source);
            }

            parsedLink = parseWikiLink(linkElement);
            if (parsedLink.equals(WIKI_PHILOSOPHY_URL)) {
                System.out.println("Reached " + WIKI_PHILOSOPHY_URL + " after " + pageHops + " tries." );
                return;
            }

            if (visited.contains(parsedLink)) {
                throw new RuntimeException(parsedLink + " is already visited.");
            }

            visited.add(parsedLink);
            source = parsedLink;
        }

        System.out.println("Failed to reach " + WIKI_PHILOSOPHY_URL);
    }

    private static String parseWikiLink(Element el) {
        String link = el.attr("href");
        if (!link.startsWith(WIKI_ORIGIN_EN)) {
            link = WIKI_ORIGIN_EN + link;
        }

        return link;
    }
}
