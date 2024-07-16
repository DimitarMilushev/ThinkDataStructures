package com.allendowney.thinkdast;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides sorting algorithms.
 *
 */
public class ListSorter<T> {

	/**
	 * Sorts a list using a Comparator object.
	 * 
	 * @param list
	 * @param comparator
	 * @return
	 */
	public void insertionSort(List<T> list, Comparator<T> comparator) {
	
		for (int i=1; i < list.size(); i++) {
			T elt_i = list.get(i);
			int j = i;
			while (j > 0) {
				T elt_j = list.get(j-1);
				if (comparator.compare(elt_i, elt_j) >= 0) {
					break;
				}
				list.set(j, elt_j);
				j--;
			}
			list.set(j, elt_i);
		}
	}

	/**
	 * Sorts a list using a Comparator object.
	 * 
	 * @param list
	 * @param comparator
	 * @return
	 */
	public void mergeSortInPlace(List<T> list, Comparator<T> comparator) {
		List<T> sorted = mergeSort(list, comparator);
		list.clear();
		list.addAll(sorted);
	}

	/**
	 * Sorts a list using a Comparator object.
	 * 
	 * Returns a list that might be new.
	 * 
	 * @param list
	 * @param comparator
	 * @return
	 */
	public List<T> mergeSort(List<T> list, Comparator<T> comparator) {
		if(list.size() < 2) return list;
		final int mid = Math.ceilDiv (list.size(), 2);
		final List<T> left = this.mergeSort(list.subList(0, mid), comparator);
		final List<T> right = this.mergeSort(list.subList(mid, list.size()), comparator);
		return Stream.concat(left.stream(), right.stream()).sorted(comparator).toList();
	}

	/**
	 * Merges two sorted lists into a single sorted list.
	 * 
	 * @param first
	 * @param second
	 * @param comparator
	 * @return
	 */
	private List<T> merge(List<T> first, List<T> second, Comparator<T> comparator) {
		// NOTE: using LinkedList is important because we need to 
		// remove from the beginning in constant time
		List<T> result = new LinkedList<T>();
		int total = first.size() + second.size();
		for (int i=0; i<total; i++) {
			List<T> winner = pickWinner(first, second, comparator);
			result.add(winner.remove(0));
		}
		return result;
	}

	/**
	 * Returns the list with the smaller first element, according to `comparator`.
	 * 
	 * If either list is empty, `pickWinner` returns the other.
	 * 
	 * @param first
	 * @param second
	 * @param comparator
	 * @return
	 */
	private List<T> pickWinner(List<T> first, List<T> second, Comparator<T> comparator) {
		if (first.size() == 0) {
			return second;
		}
		if (second.size() == 0) {
			return first;
		}
		int res = comparator.compare(first.get(0), second.get(0));
		if (res < 0) {
			return first;
		}
		if (res > 0) {
			return second;
		}
		return first;
	}

	/**
	 * Sorts a list using a Comparator object.
	 * 
	 * @param list
	 * @param comparator
	 * @return
	 */
	public void heapSort(List<T> list, Comparator<T> comparator) {
		PriorityQueue<T> heap = new PriorityQueue<T>(list.size(), comparator);
		heap.addAll(list);
        list.replaceAll(ignored -> heap.poll());
	}

	
	/**
	 * Returns the largest `k` elements in `list` in ascending order.
	 * 
	 * @param k
	 * @param list
	 * @param comparator
	 * @return 
	 * @return
	 */
	public List<T> topK(int k, List<T> list, Comparator<T> comparator) {
		PriorityQueue<T> heap = new PriorityQueue<T>(list.size(), comparator);
		for (T element: list) {
			if (heap.size() < k) {
				heap.offer(element);
				continue;
			}
			int cmp = comparator.compare(element, heap.peek());
			if (cmp > 0) {
				heap.poll();
				heap.offer(element);
			}
		}
		List<T> res = new ArrayList<T>();
		while (!heap.isEmpty()) {
			res.add(heap.poll());
		}
		return res;
	}

	public void radixSort(List<String> list) {
		final int max = Collections.max(list.stream().map(String::length).toList());
		int sigIndex = max - 1;
		while (sigIndex >= 0) {
			countingSort(list, max, sigIndex);
			sigIndex--;
		}
	}

	public void countingSort(List<String> list, int n, int sig) {
		final int[] occurrences = this.getOccurrenceTable(
				list.stream().map(x -> mapStringToNumber(x, n, sig)).toList()
		);
		int counted = 0;
		// accumulate positions
		for (int i = 0; i < occurrences.length; i++) {
			counted += occurrences[i];
			occurrences[i] = counted;
		}

		final String[] lookup = list.toArray(String[]::new);
		for (int i = lookup.length -1; i >= 0; i--) {
			int temp = --occurrences[mapStringToNumber(lookup[i], n, sig)];
			list.set(temp, lookup[i]);
		}
	}

	/**
	 * Handles char value mapping of inconsistent length string values
	 * Ex. (1, 123) -> it treats it as (001, 123) so that at index 0 we get 1 and 3
	 * @param el string value
	 * @param size expected size of string (largest)
	 * @param index char index in string
	 */
	private int mapStringToNumber(String el, int size, int index) {
		final int sDiff = size - el.length();
		if (sDiff > 0) {
			//reverse index
			index -= sDiff;
			if (index < 0) return '0';
		}
		if (el.length() <= index) return '0';
		return el.charAt(index);
	}
	public void countingSortChar(List<Character> list) {
		List<Integer> asciiValues = list.stream().map(x -> (int) x).collect(Collectors.toList());
		this.countingSort(asciiValues);
		for (int i = 0; i < list.size(); i++) {
			list.set(i, (char) asciiValues.get(i).intValue());
		}
	}

	public void countingSort(List<Integer> list) {
		if (list.isEmpty()) return;

		final int[] occurrences = this.getOccurrenceTable(list);
		int counted = 0;
		// accumulate positions
		for (int i = 0; i < occurrences.length; i++) {
			counted += occurrences[i];
			occurrences[i] = counted;
		}

		final Integer[] lookup = list.toArray(Integer[]::new);
		for (int i = lookup.length -1; i >= 0; i--) {
			int temp = --occurrences[lookup[i]];
			list.set(temp, lookup[i]);
		}
	}

	public int[] getOccurrenceTable(List<Integer> list) {
		final int max = Collections.max(list);
		final int[] counter = new int[max + 1];
		for (var el : list) {
			counter[el]++;
		}
		return counter;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		List<Integer> list = new ArrayList<Integer>(Arrays.asList(3, 5, 1, 4, 2));
		
		Comparator<Integer> comparator = new Comparator<Integer>() {
			@Override
			public int compare(Integer elt1, Integer elt2) {
				return elt1.compareTo(elt2);
			}
		};
		
		ListSorter<Integer> sorter = new ListSorter<Integer>();
		sorter.insertionSort(list, comparator);
		System.out.println(list);

		list = new ArrayList<Integer>(Arrays.asList(3, 5, 1, 4, 2));
		sorter.mergeSortInPlace(list, comparator);
		System.out.println(list);

		list = new ArrayList<Integer>(Arrays.asList(3, 5, 1, 4, 2));
		sorter.heapSort(list, comparator);
		System.out.println(list);
	
		list = new ArrayList<Integer>(Arrays.asList(6, 3, 5, 8, 1, 4, 2, 7));
		List<Integer> queue = sorter.topK(4, list, comparator);
		System.out.println(queue);
//
		list = new ArrayList<Integer>(Arrays.asList(2, 1, 5, 2, 1, 7, 7));
		sorter.countingSort(list);
		System.out.println(list);

		var charList = new ArrayList<>(Arrays.asList('a', '3', '5', 'c', '1', '7'));
		sorter.countingSortChar(charList);
		System.out.println(charList);

		var strList = new ArrayList<>(Arrays.asList("bee", "age", "can", "add", "bad", "cab", "ace", "a"));
		sorter.radixSort(strList);
		System.out.println(strList);

		strList = new ArrayList<>(Arrays.asList("122", "431", "565", "22", "1", "47", "787"));
		sorter.radixSort(strList);
		System.out.println(strList);
	}
}
