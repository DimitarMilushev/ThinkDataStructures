package com.allendowney.thinkdast;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import com.allendowney.thinkdast.constants.CSSClassConstants;
import com.allendowney.thinkdast.exceptions.DuplicateLinkException;
import com.allendowney.thinkdast.exceptions.LinkNotFoundException;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class WikiPhilosophy {
    final static List<String> visited = new ArrayList<String>();
    final static WikiFetcher wf =  WikiFetcher.getInstance();
    private final static String WIKI_BASE_URL = "https://en.wikipedia.org";

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
    public static void main(String[] args) throws IOException, DuplicateLinkException {
        String destination = "https://en.wikipedia.org/wiki/Philosophy";
        String source = "https://en.wikipedia.org/wiki/Java_(programming_language)";

        testConjecture(destination, source, 20);
    }

    /**
     * Starts from given URL and follows first link until it finds the destination or exceeds the limit.
     *
     * @param destination
     * @param source
     * @throws IOException
     */
    public static void testConjecture(String destination, String source, int limit) throws DuplicateLinkException, IOException {
        String currentLink = source;
        int attempts = 0;
        Elements html;
        try {
            while (limit > attempts && !currentLink.equals(destination)) {
                if (visited.contains(currentLink)) {
                    throw new DuplicateLinkException("Link was already found.");
                }
                visited.add(currentLink);
                System.out.println(currentLink);
                html = wf.fetchWikipedia(currentLink);
                currentLink = getFirstValidLink(html);
                ++attempts;
            }
            if (!currentLink.equals(destination))
                throw new LinkNotFoundException("Failed to find the requested link");
            System.out.printf(String.format("Got to %s at %sth attempt.", currentLink, attempts));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Traverse the tree for a valid link and return it.
     * @param tree
     * @return the first valid link
     * @throws LinkNotFoundException if no links are found.
     */
    private static String getFirstValidLink(Elements tree) throws LinkNotFoundException {
        Deque<Element> traversed = new ArrayDeque<>();
        traversed.push(tree.first());

        Element node;
        Elements children;
        while (!traversed.isEmpty()) {
            node = traversed.pop();
            // Check if valid link
            if (linkIsValid(node)) {
                return WIKI_BASE_URL + getHref(node);
            }
            // Else continue traversing
            children = node.children();
            for (int i = children.size() - 1; i >= 0; --i) {
                traversed.push(children.get(i));
            }
        }
        throw new LinkNotFoundException("Couldn't find any links.");
    }

    /**
     * Follows a set of rules to determine if a link is considered valid:
     *  1. The link should be in the content text of the page, not in a sidebar or boxout.
     *  2. It should not be in italics or in parentheses.
     *  3. You should skip external links, links to the current page, and red links.
     *  4. In some versions, you should skip a link if the text starts with an uppercase letter.
     * @param link
     */
    private static boolean linkIsValid(Element element) {
        String href = getHref(element);
        if (href == null || href.isEmpty()) return false;

        if (isLinkPartOfContent(element)) return false; // #1
        if (isLinkAQuote(element)) return false; // Rule #2
        if (!isLinkSourceValid(href) || isRedLink(element) || isAnchorTag(element)) return false; // Rule #3
        if (Character.isUpperCase(element.text().charAt(0))) return false;// Rule #4

        return true;
    }
    private static String getHref(Element element) {
        String href = element.attr("href");
        if (href == null || href.isEmpty()) return null;
        // href tends not to have the base URL
        return href;
    }
    private static boolean isLinkPartOfContent(Element link) {
        return link.className().equals(CSSClassConstants.EMPTY_DIV_CLASS) ||
                link.className().equals(CSSClassConstants.BOXOUT_CLASS);
    }
    private static boolean isLinkAQuote(Element link) {
        // Checks if the element is inside brackets
        final String pattern = String.format("(\\().*(%s).*(\\))", "href=" + getHref(link));
        return (link.attr("font-style").equals("italic") ||
                Pattern.matches(pattern, link.parent().toString()));
    }
    private static boolean isLinkSourceValid(String link) {
        return link.startsWith("/wiki/"); // Different source
    }
    private static boolean isRedLink(Element link) {
        return link.attr("color").equals("#dd3333") || link.className().equals(".new");
    }
    private static boolean isAnchorTag(Element element) {
        return element.className().equals("reference") || element.attr("href").startsWith("#");
    }
}
