package com.allendowney.thinkdast;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.StringTokenizer;

/**
 * 
 */
/**
 * @author downey
 *
 */
public class WikiParser {
	
	// the list of paragraphs we should search
	private Elements paragraphs;
	
	// the stack of open delimiters
	private long openedParenthesis;
	

	/**
	 * Initializes a WikiParser with a list of Elements.
	 * 
	 * @param paragraphs
	 */
	public WikiParser(Elements paragraphs) {
		this.paragraphs = paragraphs;
		this.openedParenthesis = 0;
	}
	
	/**
	 * Searches the paragraphs for a valid link.
	 * 
	 * Warns if a paragraph ends with unbalanced parentheses.
	 * 
	 * @return
	 */
	public Element findFirstLink() {
		for (Element paragraph: paragraphs) { 
			Element firstLink = findFirstLinkPara(paragraph);
			if (firstLink != null) {
				return firstLink;
			}
			if (isInParens()) {
				System.err.println("Warning: unbalanced parentheses."); 
	   	 	}
		}
		return null;
	}

	/**
	 * Returns the first valid link in a paragraph, or null.
	 * 
	 * @param root
	 */
	private Element findFirstLinkPara(Node root) {
		// create an Iterable that traverses the tree
		Iterable<Node> nt = new WikiNodeIterable(root);

		// loop through the nodes
		for (Node node: nt) {
			// process TextNodes to get parentheses
			if (node instanceof TextNode) {
				processTextNode((TextNode) node);
			}
			// process elements to get find links
			if (node instanceof Element) {
		        Element firstLink = processElement((Element) node);
		        if (firstLink != null) {
					return firstLink;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the element if it is a valid link, null otherwise.
	 * 
	 * 
	 * 
	 * @param elt
	 */
	private Element processElement(Element elt) {
		//System.out.println(elt.tagName());
		if (validLink(elt)) {
			return elt;
		}
		return null;
	}

	/**
	 * Checks whether a link is value.
	 * 
	 * @param elt
	 * @return
	 */
	private boolean validLink(Element elt) {
		// it's no good if it's not a link
		if (!elt.tagName().equals("a")) {
			return false;
		}
		// is red link (missing page)
		if (elt.hasClass("new")) {
			return false;
		}
		// in italics
		if (isItalic(elt)) {
			return false;
		}
		// in parenthesis
		if (isInParens()) {
			return false;
		}
		// a bookmark
		if (startsWith(elt, "#")) {
			return false;
		}
		// a Wikipedia help page
		if (startsWith(elt, "/wiki/Help:")) {
			return false;
		}
		// is external origin
		if (!startsWith(elt, "/wiki/")) {
			return false;
		}
		// Skip uppercase links
		if (startsWithUppercase(elt)) {
			return false;
		}
		// Redirects back to the same page
		// I'm not sure if I can do this without updating the API as
		// my only idea is checking for anchors, but they can also be for
		// different pages.
		// The other solution is getting the Document or passing the URL to the constructor.
		return true;
	}

	private boolean startsWithUppercase(Element elt) {
		return elt.text().charAt(0) >= 'A' && elt.text().charAt(0) <= 'Z';
	}

	/**
	 * Checks whether a link starts with a given String.
	 * 
	 * @param elt
	 * @param s
	 * @return
	 */
	private boolean startsWith(Element elt, String s) {
		//System.out.println(elt.attr("href"));
		return (elt.attr("href").startsWith(s));
	}

	/**
	 * Checks whether the element is in parentheses (possibly nested).
	 */
	private boolean isInParens() {
		// check whether there are any parentheses on the stack
		return openedParenthesis > 0;
	}

	/**
	 * Checks whether the element is in italics.
	 * 
	 * (Either a "i" or "em" tag)
	 * 
	 * @param start
	 * @return
	 */
	private boolean isItalic(Element start) {
		// follow the parent chain until we get to null
		for (Element elt=start; elt != null; elt = elt.parent()) {
			if (elt.tagName().equals("i") || elt.tagName().equals("em") || elt.hasClass("hatnote")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Processes a text node, splitting it up and checking parentheses.
	 * 
	 * @param node
	 */
	private void processTextNode(TextNode node) {
		StringTokenizer st = new StringTokenizer(node.text(), " ()", true);
		while (st.hasMoreTokens()) {
		     String token = st.nextToken();
		     // System.out.print(token);
		     if (token.equals("(")) {
		    	 ++this.openedParenthesis;
		     }
		     if (token.equals(")")) {
		    	 if (!isInParens()) {
		    		 System.err.println("Warning: unbalanced parentheses."); 
		    	 }
				--this.openedParenthesis;
		     }
		}
	}
}
