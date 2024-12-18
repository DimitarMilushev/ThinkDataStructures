package com.allendowney.thinkdast;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import org.jsoup.select.Elements;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

/**
 * Represents a Redis-backed web search index.
 *
 */
public class JedisIndex {

	private Jedis jedis;

	/**
	 * Constructor.
	 *
	 * @param jedis
	 */
	public JedisIndex(Jedis jedis) {
		this.jedis = jedis;
	}

	/**
	 * Returns the Redis key for a given search term.
	 *
	 * @return Redis key.
	 */
	private String urlSetKey(String term) {
		return "URLSet:" + term;
	}

	/**
	 * Returns the Redis key for a URL's TermCounter.
	 *
	 * @return Redis key.
	 */
	private String termCounterKey(String url) {
		return "TermCounter:" + url;
	}

	/**
	 * Checks whether we have a TermCounter for a given URL.
	 *
	 * @param url
	 * @return
	 */
	public boolean isIndexed(String url) {
		String redisKey = termCounterKey(url);
		return jedis.exists(redisKey);
	}
	
	/**
	 * Adds a URL to the set associated with `term`.
	 * 
	 * @param term
	 * @param tc
	 */
	public void add(String term, TermCounter tc) {
		jedis.sadd(urlSetKey(term), tc.getLabel());
	}

	public void add(String term, TermCounter tc, Transaction transaction) {
		transaction.sadd(urlSetKey(term), tc.getLabel());
	}

	/**
	 * Updates the count of term for a URL.
	 * If it doesn't exist, it creates a new record.
	 *
	 */
	public void setTermCount(String term, TermCounter tc) {
		final String key = termCounterKey(tc.getLabel());
		final String value = tc.get(term).toString();

		jedis.hset(key, term, value);
	}

	public void setTermCount(String term, TermCounter tc, Transaction transaction) {
		final String key = termCounterKey(tc.getLabel());
		final String value = tc.get(term).toString();

		transaction.hset(key, term, value);
	}


	/**
	 * Looks up a search term and returns a set of URLs.
	 * 
	 * @param term
	 * @return Set of URLs.
	 */
	public Set<String> getURLs(String term) {
		return jedis.smembers(this.urlSetKey(term));
	}

    /**
	 * Looks up a term and returns a map from URL to count.
	 * 
	 * @param term
	 * @return Map from URL to count.
	 */
	public Map<String, Integer> getCounts(String term) {
		return this.getURLs(term).stream().collect(
				HashMap::new,
				(map, URL) -> map.putIfAbsent(URL, getCount(URL, term)),
				Map::putAll
		);
	}

	/**
	 * Optimizes requests to Redis by fetching all counts in a single transaction.
	 * @return
	 */
	public Map<String, Integer> getCountsTransactional(String term) {
		final List<String> urls = this.getURLs(term).stream().toList();
		Transaction tr = jedis.multi();

		for (String url : urls) {
			tr.hget(termCounterKey(url), term);
		}

		final List<Object> counts = tr.exec();
		if (counts.size() != urls.size()) {
			throw new RuntimeException("Term counts per URL don't match:\nURLs: " + urls.size() + "\nTerm Count: " + counts.size());
		}

		Map<String, Integer> countsPerUrl = new HashMap<>();
		int termCount;
		for (int i = 0; i < counts.size(); i++) {
			try {
				termCount = Integer.parseInt((String)counts.get(i));
			} catch (NumberFormatException ex) {
				termCount = 0;
			}
			countsPerUrl.put(urls.get(i), termCount);
		}

		return countsPerUrl;
	}

    /**
	 * Returns the number of times the given term appears at the given URL.
	 * 
	 * @param url The URL at which the term is counted.
	 * @param term Term to check counts.
	 * @return Number of occurrences of term.
	 */
	public Integer getCount(String url, String term)  {
		final String value = jedis.hget(termCounterKey(url), term);
        if (value == null) return 0;

		return Integer.valueOf(value);
	}

	/**
	 * Adds a page to the index.
	 *
	 * @param url         URL of the page.
	 * @param paragraphs  Collection of elements that should be indexed.
	 */
	public void indexPage(String url, Elements paragraphs) throws IOException {
		// make a TermCounter and count the terms in the paragraphs
		final TermCounter termCounter = new TermCounter(url);
		termCounter.processElements(paragraphs);

		// for each term in the TermCounter, add the TermCounter to the index
		Transaction transaction = jedis.multi();
		for (String term : termCounter.keySet()) {
			this.add(term, termCounter, transaction);
			this.setTermCount(term, termCounter, transaction);
		}
		transaction.exec();
	}

	/**
	 * Prints the contents of the index.
	 *
	 * Should be used for development and testing, not production.
	 */
	public void printIndex() {
		// loop through the search terms
		for (String term: termSet()) {
			System.out.println(term);

			// for each term, print the pages where it appears
			Set<String> urls = getURLs(term);
			for (String url: urls) {
				Integer count = getCount(url, term);
				System.out.println("    " + url + " " + count);
			}
		}
	}

	/**
	 * Returns the set of terms that have been indexed.
	 *
	 * Should be used for development and testing, not production.
	 *
	 * @return
	 */
	public Set<String> termSet() {
		Set<String> keys = urlSetKeys();
		Set<String> terms = new HashSet<String>();
		for (String key: keys) {
			String[] array = key.split(":");
			if (array.length < 2) {
				terms.add("");
			} else {
				terms.add(array[1]);
			}
		}
		return terms;
	}

	/**
	 * Returns URLSet keys for the terms that have been indexed.
	 *
	 * Should be used for development and testing, not production.
	 *
	 * @return
	 */
	public Set<String> urlSetKeys() {
		return jedis.keys("URLSet:*");
	}

	/**
	 * Returns TermCounter keys for the URLS that have been indexed.
	 *
	 * Should be used for development and testing, not production.
	 *
	 * @return
	 */
	public Set<String> termCounterKeys() {
		return jedis.keys("TermCounter:*");
	}

	/**
	 * Deletes all URLSet objects from the database.
	 *
	 * Should be used for development and testing, not production.
	 *
	 * @return
	 */
	public void deleteURLSets() {
		Set<String> keys = urlSetKeys();
		Transaction t = jedis.multi();
		for (String key: keys) {
			t.del(key);
		}
		t.exec();
	}

	/**
	 * Deletes all URLSet objects from the database.
	 *
	 * Should be used for development and testing, not production.
	 *
	 * @return
	 */
	public void deleteTermCounters() {
		Set<String> keys = termCounterKeys();
		Transaction t = jedis.multi();
		for (String key: keys) {
			t.del(key);
		}
		t.exec();
	}

	/**
	 * Deletes all keys from the database.
	 *
	 * Should be used for development and testing, not production.
	 *
	 * @return
	 */
	public void deleteAllKeys() {
		Set<String> keys = jedis.keys("*");
		Transaction t = jedis.multi();
		for (String key: keys) {
			t.del(key);
		}
		t.exec();
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Jedis jedis = JedisMaker.make();
		JedisIndex index = new JedisIndex(jedis);

//		index.deleteTermCounters();
//		index.deleteURLSets();
//		index.deleteAllKeys();
		loadIndex(index);

		Map<String, Integer> map = index.getCountsTransactional("input");
		for (Entry<String, Integer> entry: map.entrySet()) {
			System.out.println(entry);
		}
	}

	/**
	 * Stores two pages in the index for testing purposes.
	 *
	 * @return
	 * @throws IOException
	 */
	private static void loadIndex(JedisIndex index) throws IOException {
		WikiFetcher wf = WikiFetcher.getInstance();

		String url = "https://en.wikipedia.org/wiki/Java_(programming_language)";
		Elements paragraphs = wf.readWikipedia(url);
		index.indexPage(url, paragraphs);

		url = "https://en.wikipedia.org/wiki/Programming_language";
		paragraphs = wf.readWikipedia(url);
		index.indexPage(url, paragraphs);
	}
}
