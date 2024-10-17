package com.allendowney.thinkdast;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jsoup.select.Elements;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import redis.clients.jedis.Jedis;

/**
 * @author downey
 *
 */
public class JedisIndexTest {

	private static String url1, url2;
	private Jedis jedis;
	private JedisIndex index;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		jedis = JedisMaker.make();
		index = new JedisIndex(jedis);

		index.deleteAllKeys();
		index.deleteURLSets();
		index.deleteTermCounters();

		loadIndex(index);
	}

	/**
	 * Loads the index with two pages read from files.
	 *
	 * @return
	 * @throws IOException
	 */
	private static void loadIndex(JedisIndex index) throws IOException {
		WikiFetcher wf = WikiFetcher.getInstance();

		url1 = "https://en.wikipedia.org/wiki/Java_(programming_language)";
		Elements paragraphs = wf.readWikipedia(url1);
		index.indexPage(url1, paragraphs);

		url2 = "https://en.wikipedia.org/wiki/Programming_language";
		paragraphs = wf.readWikipedia(url2);
		index.indexPage(url2, paragraphs);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		jedis.close();
	}


	@Test
	public void testGetUrlsWhenEmpty() {
		final String[] expected = new String[0];

		final String[] actual = index.getURLs("SOME_RANDM_URL").toArray(String[]::new);

		assertArrayEquals(expected, actual);
	}

	@Test
	public void testGetUrls() {
        final Set<String> expected = new HashSet<>(List.of(new String[]{url1, url2}));

		final Set<String> actuals = index.getURLs("the");

		assertEquals(expected.size(), actuals.size());
		for (var actual : actuals) {
			assertTrue(expected.contains(actual));
		}
	}

	/**
	 * Test method for {@link JedisIndex#getCounts(java.lang.String)}.
	 */
	@Test
	public void testGetCounts() {
		Map<String, Integer> map = index.getCounts("the");
		assertThat(map.get(url1), is(339));
		assertThat(map.get(url2), is(264));
	}

	@Test
	public void testGetCountShouldReturnZeroIfTermIsMissing() {
		final Integer expected = 0;

		final Integer actual = index.getCount(url1, "SOME_RANDOM_STRING_12312123");

		assertEquals(expected, actual);
	}


}
