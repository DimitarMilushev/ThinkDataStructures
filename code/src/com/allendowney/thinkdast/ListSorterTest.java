package com.allendowney.thinkdast;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

/**
 * @author downey
 *
 */
public class ListSorterTest {

	private ListSorter<Integer> sorter;
	private Comparator<Integer> comparator;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {

		comparator = new Comparator<Integer>() {
			@Override
			public int compare(Integer elt1, Integer elt2) {
				return elt1.compareTo(elt2);
			}
		};
		
		sorter = new ListSorter<Integer>();
	}

	/**
	 * Test method for {@link ListSorter#insertionSort(java.util.List, java.util.Comparator)}.
	 */
	@Test
	public void testInsertionSort() {
		List<Integer> list = new ArrayList<Integer>(Arrays.asList(3, 5, 1, 4, 2));
		sorter.insertionSort(list, comparator);
		isSorted(list);
	}

	/**
	 * @param list 
	 * 
	 */
	private void isSorted(List<Integer> list) {
		assertThat(list.size(), is(5));
		assertThat(list.get(0), is(1));
		assertThat(list.get(1), is(2));
		assertThat(list.get(2), is(3));
		assertThat(list.get(3), is(4));
		assertThat(list.get(4), is(5));
	}

	/**
	 * Test method for {@link mergeSortInPlace(java.util.List, java.util.Comparator)}.
	 */
	@Test
	public void testMergeSortInPlace() {
		List<Integer> list = new ArrayList<Integer>(Arrays.asList(3, 5, 1, 4, 2));
		sorter.mergeSortInPlace(list, comparator);
		isSorted(list);
	}

	/**
	 * Test method for {@link mergeSort(java.util.List, java.util.Comparator)}.
	 */
	@Test
	public void testMergeSort() {
		List<Integer> list = new ArrayList<Integer>(Arrays.asList(3, 5, 1, 4, 2));
		List<Integer> sorted = sorter.mergeSort(list, comparator);
		isSorted(sorted);
	}

	/**
	 * Test method for {@link heapSort(java.util.List, java.util.Comparator)}.
	 */
	@Test
	public void testHeapSort() {
		List<Integer> list = new ArrayList<Integer>(Arrays.asList(3, 5, 1, 4, 2));
		sorter.heapSort(list, comparator);
		isSorted(list);
	}

	/**
	 * Test method for {@link topK(int, java.util.List, java.util.Comparator)}.
	 */
	@Test
	public void testTopK() {
		List<Integer> list = new ArrayList<Integer>(Arrays.asList(6, 3, 5, 8, 1, 4, 2, 7));

		List<Integer> res = sorter.topK(4, list, comparator);
		assertThat(res.size(), is(4));
		assertThat(res.get(0), is(5));
		assertThat(res.get(1), is(6));
		assertThat(res.get(2), is(7));
		assertThat(res.get(3), is(8));
	}

	@Test
	public void occurrenceTable() {
		List<Integer> list = new ArrayList<>(Arrays.asList(7, 23, 7, 5, 23, 12));

		int[] actual = sorter.getOccurrenceTable(list);
		assertThat(actual.length, is(24));
		assertThat(actual[7], is(2));
		assertThat(actual[5], is(1));
		assertThat(actual[12], is(1));
		assertThat(actual[23], is(2));
	}

	@Test
	public void countingSort() {
		List<Integer> list = new ArrayList<Integer>(Arrays.asList(3, 5, 1, 4, 2));
		sorter.countingSort(list);
		isSorted(list);
	}

	@Test
	public void countingSortChar() {
		List<Character> list = new ArrayList<>(Arrays.asList('a', '3', '5', 'c', '1', '7'));
		sorter.countingSortChar(list);

		assertThat(list.size(), is(6));
		assertThat(list.get(0), is('1'));
		assertThat(list.get(1), is('3'));
		assertThat(list.get(2), is('5'));
		assertThat(list.get(3), is('7'));
		assertThat(list.get(4), is('a'));
		assertThat(list.get(5), is('c'));
	}

	@Test
	public void radixSortNumericStrings() {
		List<String> list = new ArrayList<>(Arrays.asList("122", "431", "565", "22", "1", "47", "787"));

		sorter.radixSort(list);

		assertThat(list.size(), is(7));
		assertThat(list.get(0), is("1"));
		assertThat(list.get(1), is("22"));
		assertThat(list.get(2), is("47"));
		assertThat(list.get(3), is("122"));
		assertThat(list.get(4), is("431"));
		assertThat(list.get(5), is("565"));
		assertThat(list.get(6), is("787"));
	}

	@Test
	public void radixSortWords() {
		var list = new ArrayList<>(Arrays.asList("bee", "age", "can", "add", "bad", "cab", "ace", "a"));

		sorter.radixSort(list);

		assertThat(list.size(), is(8));
		assertThat(list.get(0), is("a"));
		assertThat(list.get(1), is("ace"));
		assertThat(list.get(2), is("add"));
		assertThat(list.get(3), is("age"));
		assertThat(list.get(4), is("bad"));
		assertThat(list.get(5), is("bee"));
		assertThat(list.get(6), is("cab"));
		assertThat(list.get(7), is("can"));
	}
}
