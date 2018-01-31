package com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.impl;

import com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.TrieNode;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test class for {@link FileTrieNode}.
 */
public class FileTrieNodeTest {

	/**
	 * Tests that the constructor throws an exception when the given value is {@code null}.
	 */
	@Test(expected = NullPointerException.class)
	public void testConstructorNullValue() {
		new FileTrieNode(null);
	}

	/**
	 * Tests that the constructor throws an exception when the given value is empty.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testConstructorEmptyValue() {
		new FileTrieNode(" ");
	}

	/**
	 * Tests {@link FileTrieNode#getValue()}.
	 */
	@Test
	public void testGetValue() {
		final String value = "value";
		final TrieNode<String> node = new FileTrieNode(value);
		assertTrue(node.getValue().isPresent());
		assertEquals(value, node.getValue().get());
	}

	/**
	 * Tests {@link FileTrieNode#getChildren()}.
	 */
	@Test
	public void testGetChildren() {
		assertEquals(Collections.emptyMap(), new FileTrieNode("value").getChildren());
	}

	/**
	 * Tests {@link FileTrieNode#equals(Object)}.
	 */
	@Test
	public void testEquals() {
		final TrieNode<String> node1 = new FileTrieNode("value");
		final TrieNode<String> node2 = new FileTrieNode("value");
		final TrieNode<String> node3 = new FileTrieNode("other");

		assertEquals(node1, node1);
		assertEquals(node1, node2);
		assertNotEquals(node1, null);
		assertNotEquals(node1, "String");
		assertNotEquals(node1, node3);
	}

	/**
	 * Tests {@link FileTrieNode#hashCode()}.
	 */
	@Test
	public void testHashcode() {
		final TrieNode<String> node1 = new FileTrieNode("value");
		final TrieNode<String> node2 = new FileTrieNode("other");

		assertEquals(node1.hashCode(), node1.hashCode());
		assertNotEquals(node1.hashCode(), node2.hashCode());
	}

	/**
	 * Tests {@link FileTrieNode#toString()}.
	 */
	@Test
	public void testToString() {
		final TrieNode<String> node = new FileTrieNode("value");
		final String expected = "FileTrieNode{value='value'}";
		assertEquals(expected, node.toString());
	}

}
