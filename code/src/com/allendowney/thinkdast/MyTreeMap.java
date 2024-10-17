/**
 *
 */
package com.allendowney.thinkdast;

import java.util.*;

/**
 * Implementation of a Map using a binary search tree.
 *
 * @param <K>
 * @param <V>
 *
 */
public class MyTreeMap<K, V> implements Map<K, V> {

	private int size = 0;
	private Node root = null;

	/**
	 * Represents a node in the tree.
	 *
	 */
	protected class Node {
		public K key;
		public V value;
		public Node left = null;
		public Node right = null;

		/**
		 * @param key
		 * @param value
		 * @param left
		 * @param right
		 */
		public Node(K key, V value) {
			this.key = key;
			this.value = value;
		}
	}

	@Override
	public void clear() {
		size = 0;
		root = null;
	}

	@Override
	public boolean containsKey(Object target) {
		return findNode(target) != null;
	}

	/**
	 * Returns the entry that contains the target key, or null if there is none.
	 *
	 * @param target
	 */
	private Node findNode(Object target) {
		// some implementations can handle null as a key, but not this one
		if (target == null) {
			throw new IllegalArgumentException();
		}

		// something to make the compiler happy
		@SuppressWarnings("unchecked")
		Comparable<? super K> k = (Comparable<? super K>) target;

		// BST rules
		// 1. if current == null       ====> RETURN NULL;
		// 2. if target == current.key ===> RETURN current;
		// 3. if current.key > target  ====> CHECK LEFT
		//	1. if current.left == null ====> RETURN NULL;
		//  2. if current = current.left
		// 4. else 					   ====> CHECK RIGHT
		// 	1. current.right == null =====> RETURN NULL;
		//  2. current = current.right;
		Node current = this.root;
		int compare;
		while (current != null) {
			compare = k.compareTo(current.key);
			if (compare == 0) return current;

			current = (compare > 0) ? current.right : current.left;

		}

		return null;
	}

	/**
	 * Compares two keys or two values, handling null correctly.
	 *
	 * @param target
	 * @param obj
	 * @return
	 */
	private boolean equals(Object target, Object obj) {
		if (target == null) {
			return obj == null;
		}
		return target.equals(obj);
	}

	@Override
	public boolean containsValue(Object target) {
		return containsValueHelper(root, target);
	}

	private boolean containsValueHelper(Node node, Object target) {
		if (node == null) return false;

		if (containsValueHelper(node.left, target)) return true;
		if (equals(target, node.value)) return true;

		return containsValueHelper(node.right, target);
	}

	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		throw new UnsupportedOperationException();
	}

	@Override
	public V get(Object key) {
		Node node = findNode(key);
		if (node == null) {
			return null;
		}
		return node.value;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public Set<K> keySet() {
		Set<K> set = new LinkedHashSet<K>();
		keySetHelper(set, root);
//		keySetHelperIterative(set);
		return set;
	}

	// Do a DFS over BST. Elements should come in order if no duplicates exist.
	// Make sure it's in-order so that the left (smallest) element comes first, then the root, then the right (greatest)
	private void keySetHelper(Set<K> set, Node current) {
		if (current == null) return;

		if (current.left != null) keySetHelper(set, current.left);

		set.add(current.key);

		if (current.right != null) keySetHelper(set, current.right);
	}

	private void keySetHelperIterative(Set<K> set) {
		if (root == null) return;

		Deque<Node> st = new ArrayDeque<>();
		st.push(root);

		Node curr = st.peek();
		while (!st.isEmpty()) {
			while (curr != null) {
				st.push(curr);
				curr = curr.left;
			}

			curr = st.pop();
			set.add(curr.key);

			curr = curr.right;
		}
	}

	@Override
	public V put(K key, V value) {
		if (key == null) {
			throw new NullPointerException();
		}
		if (root == null) {
			root = new Node(key, value);
			size++;
			return null;
		}

		return putHelper(root, key, value);
	}

	private V putHelper(Node node, K key, V value) {
		// Traverse until expected pos is found.
		// 1. If NULL => parent.(left | right) = new Node(key, value);
		// 2. ELSE node.val = value;
		@SuppressWarnings("unchecked")
		Comparable<? super K> k = (Comparable<? super K>) node.key;
		final int comparison = k.compareTo(key);

		// Check left (lesser)
		if (comparison > 0) {
			if (node.left != null) return putHelper(node.left, key, value);

			node.left = new Node(key, value);
			++size;
			return null;
		}

		// Check right (greater)
		if (comparison < 0) {
			if (node.right != null) return putHelper(node.right, key, value);

			node.right = new Node(key, value);
			++size;
			return null;
		}


		final V old = node.value;
		node.value = value;
		return old;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> map) {
		for (Map.Entry<? extends K, ? extends V> entry: map.entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public V remove(Object key) {
		if (key == null) throw new IllegalArgumentException();
		if (root == null) return null;

		Node forRemoval;
		if (equals(root.key, key)) {
			forRemoval = root;
			root = null;
		} else {
			Node parent = findParentOf(key, root);
			if (parent == null) return null; // Key not found

			@SuppressWarnings("unchecked")
			Comparable<? super K> k = (Comparable<? super K>) key;
			int comp = k.compareTo(parent.key);

			if (comp < 0) {
				forRemoval = parent.left;
				parent.left = null;
			} else {
				forRemoval = parent.right;
				parent.right = null;
			}
		}
		--size; // Removed node

		reBalanceFrom(forRemoval);

		return forRemoval.value;
	}

	/**
	 * A heuristic re-balancing where the children of {@code from} are re-inserted into the tree.
	 *
	 * @param from
	 */
	private void reBalanceFrom(Node from) {
		// Persist collected children.
		List<Node> children = collectChildren(from);
		size -= children.size();
		// When cleared, call add on the tree.
		children.forEach((x) -> this.put(x.key, x.value));
	}

	/**
	 * Collects children of parent {@code node} by traversing them using a post-order BFS, to keep the natural order.
	 * @param parent The starting point (inclusive).
	 * @return List of nodes that are removed.
	 */
	private List<Node> collectChildren(Node parent) {
		List<Node> removed = new LinkedList<>();
		if (parent == null) return removed;

		Queue<Node> queue = new LinkedList<>();
		if (parent.right != null)  queue.offer(parent.right);
		if (parent.left != null) queue.offer(parent.left);

		Node current;
		while (!queue.isEmpty()) {
			current = queue.poll();
			removed.add(current);
			// Makes sense to place the greater (right) element before the left (smaller).
			if (current.right != null)  queue.offer(current.right);
			if (current.left != null) queue.offer(current.left);
		}

		return removed;
	}

	private Node findParentOf(Object target, Node node) {
		if (target == null) throw new IllegalArgumentException("Aborted search because Map doesn't support null values");
		if (node == null) return null;

		@SuppressWarnings("unchecked")
		Comparable<? super K> k = (Comparable<? super K>) target;
		int comp = k.compareTo(node.key);
		if (comp < 0) {
			if (node.left != null && equals(node.left.key, target)) return node;

			return findParentOf(target, node.left);
		}
		if (comp > 0) {
			if (node.right != null && equals(node.right.key, target)) return node;

			return findParentOf(target, node.right);
		}

		// in case it's root.
		return null;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public Collection<V> values() {
		Set<V> set = new HashSet<V>();
		Deque<Node> stack = new LinkedList<Node>();
		stack.push(root);
		while (!stack.isEmpty()) {
			Node node = stack.pop();
			if (node == null) continue;
			set.add(node.value);
			stack.push(node.left);
			stack.push(node.right);
		}
		return set;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Map<String, Integer> map = new MyTreeMap<String, Integer>();
		map.put("Word1", 1);
		map.put("Word2", 2);
		Integer value = map.get("Word1");
		System.out.println(value);

		for (String key: map.keySet()) {
			System.out.println(key + ", " + map.get(key));
		}
	}

	/**
	 * Makes a node.
	 *
	 * This is only here for testing purposes.  Should not be used otherwise.
	 *
	 * @param key
	 * @param value
	 * @return
	 */
	public MyTreeMap<K, V>.Node makeNode(K key, V value) {
		return new Node(key, value);
	}

	/**
	 * Sets the instance variables.
	 *
	 * This is only here for testing purposes.  Should not be used otherwise.
	 *
	 * @param node
	 * @param size
	 */
	public void setTree(Node node, int size ) {
		this.root = node;
		this.size = size;
	}

	/**
	 * Returns the height of the tree.
	 *
	 * This is only here for testing purposes.  Should not be used otherwise.
	 *
	 * @return
	 */
	public int height() {
		return heightHelper(root);
	}

	private int heightHelper(Node node) {
		if (node == null) {
			return 0;
		}
		int left = heightHelper(node.left);
		int right = heightHelper(node.right);
		return Math.max(left, right) + 1;
	}
}
