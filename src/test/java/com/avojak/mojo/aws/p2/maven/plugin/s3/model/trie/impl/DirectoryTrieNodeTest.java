package com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.impl;

import com.google.common.base.Optional;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Test class for {@link DirectoryTrieNode}.
 */
public class DirectoryTrieNodeTest {

	/**
	 * Tests {@link DirectoryTrieNode#getValue()}.
	 */
	@Test
	public void testGetValue() {
		assertEquals(Optional.absent(), new DirectoryTrieNode().getValue());
	}

	/**
	 * Tests {@link DirectoryTrieNode#getChildren()}.
	 */
	@Test
	public void testGetChildren() {
		final DirectoryTrieNode node = new DirectoryTrieNode();
		assertEquals(Collections.emptyMap(), node.getChildren());
	}

	/**
	 * Tests {@link DirectoryTrieNode#equals(Object)}.
	 */
	@Test
	public void testEquals() {
		final DirectoryTrieNode node1 = new DirectoryTrieNode();
		final DirectoryTrieNode node2 = new DirectoryTrieNode();
		final DirectoryTrieNode node3 = new DirectoryTrieNode();
		node3.getChildren().put("key", new DirectoryTrieNode());

		assertEquals(node1, node1);
		assertEquals(node1, node2);
		assertNotEquals(node1, "String");
		assertNotEquals(node1, null);
		assertNotEquals(node1, node3);
	}

	/**
	 * Tests {@link DirectoryTrieNode#hashCode()}.
	 */
	@Test
	public void testHashcode() {
		final DirectoryTrieNode node1 = new DirectoryTrieNode();
		final DirectoryTrieNode node2 = new DirectoryTrieNode();
		node2.getChildren().put("key", new DirectoryTrieNode());

		assertEquals(node1.hashCode(), node1.hashCode());
		assertNotEquals(node1.hashCode(), node2.hashCode());
	}

	/**
	 * Tests {@link DirectoryTrieNode#toString()}.
	 */
	@Test
	public void testToString() {
		final DirectoryTrieNode node = new DirectoryTrieNode();
		final String expected = "DirectoryTrieNode{children=" + node.getChildren().toString() + '}';
		assertEquals(expected, node.toString());
	}

}
