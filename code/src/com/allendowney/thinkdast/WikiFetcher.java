package com.allendowney.thinkdast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import com.allendowney.thinkdast.constants.ResourcesConstants;
import com.allendowney.thinkdast.utils.ResourcesUtility;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class WikiFetcher {
	private static WikiFetcher instance;
	private final ResourcesUtility resourcesUtility;
	private long lastRequestTime = -1;
	private static final long minInterval = 1000;
	public static WikiFetcher getInstance() {
		if (instance == null) {
			instance = new WikiFetcher();
		}
		return instance;
	}
	private WikiFetcher() {
		this.resourcesUtility = new ResourcesUtility();
	}

	/**
	 * Fetches and parses a URL string, returning a list of paragraph elements.
	 *
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public Elements fetchWikipedia(String url) throws IOException {
		sleepIfNeeded();

		// download and parse the document
		Connection conn = Jsoup.connect(url);
		Document doc = conn.get();
		this.resourcesUtility.downloadWikiPage(doc);

		// select the content text and pull out the paragraphs.
		Element content = doc.getElementById("mw-content-text");

		// avoid selecting paragraphs from sidebars and boxouts
		this.clearSidebarsAndBoxouts(content);

		return content.select("p"); // Remove sidebars
	}

	private void clearSidebarsAndBoxouts(Element DOM) {
		DOM.select(".sidebar, .infobox").remove();
	}
	/**
	 * Reads the contents of a Wikipedia page from src/resources.
	 *
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public Elements readWikipedia(String url) throws IOException {
		URL realURL = new URL(url);
		this.resourcesUtility.ensureWikiDir();

		// assemble the file name
		String slash = File.separator;
		String filename = ResourcesConstants.WIKI_PATH + slash + this.resourcesUtility.getWikiPageFileNameFromURL(realURL);

		// read the file
		InputStream stream = WikiFetcher.class.getClassLoader().getResourceAsStream(filename);
		Document doc = Jsoup.parse(stream, "UTF-8", filename);

		// parse the contents of the file
		Element content = doc.getElementById("mw-content-text");
		return content.select("p");
	}

	/**
	 * Rate limits by waiting at least the minimum interval between requests.
	 */
	private void sleepIfNeeded() {
		if (lastRequestTime != -1) {
			long currentTime = System.currentTimeMillis();
			long nextRequestTime = lastRequestTime + minInterval;
			if (currentTime < nextRequestTime) {
				try {
					//System.out.println("Sleeping until " + nextRequestTime);
					Thread.sleep(nextRequestTime - currentTime);
				} catch (InterruptedException e) {
					System.err.println("Warning: sleep interrupted in fetchWikipedia.");
				}
			}
		}
		lastRequestTime = System.currentTimeMillis();
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		WikiFetcher wf = WikiFetcher.getInstance();
		String url = "https://en.wikipedia.org/wiki/Java_(programming_language)";
		wf.fetchWikipedia(url);

		Elements paragraphs = wf.readWikipedia(url);

		for (Element paragraph: paragraphs) {
			System.out.println(paragraph);
		}
	}
}