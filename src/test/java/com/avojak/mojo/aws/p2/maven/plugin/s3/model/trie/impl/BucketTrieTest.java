package com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.impl;

import com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.Trie;
import com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.TrieNode;
import com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.printer.TriePrinter;
import com.google.common.base.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static uk.org.lidalia.slf4jtest.LoggingEvent.debug;

/**
 * Test class for {@link BucketTrie}.
 */
@RunWith(MockitoJUnitRunner.class)
public class BucketTrieTest {

	@Mock
	private TriePrinter systemPrinter;

	@Mock
	private TriePrinter loggerPrinter;

	private final TestLogger logger = TestLoggerFactory.getTestLogger(BucketTrie.class);

	/**
	 * Tests that the constructor throws an exception when the given system {@link TriePrinter} is {@code null}.
	 */
	@Test(expected = NullPointerException.class)
	public void testConstructor_NullSystemPrinter() {
		new BucketTrie(null, null, loggerPrinter);
	}

	/**
	 * Tests that the constructor throws an exception when the given logger {@link TriePrinter} is {@code null}.
	 */
	@Test(expected = NullPointerException.class)
	public void testConstructor_NullLoggerPrinter() {
		new BucketTrie(null, systemPrinter, null);
	}

	/**
	 * Tests that {@link BucketTrie#insert(String, String)} throws an exception when the given key is {@code null}.
	 */
	@Test(expected = NullPointerException.class)
	public void testInsert_NullKey() {
		new BucketTrie(null, systemPrinter, loggerPrinter).insert(null, "");
	}

	/**
	 * Tests that {@link BucketTrie#insert(String, String)} throws an exception when the given key is empty.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testInsert_EmptyKey() {
		new BucketTrie(null, systemPrinter, loggerPrinter).insert(" ", "");
	}

	/**
	 * Tests {@link BucketTrie#insert(String, String)} when the given key does not match the prefix.
	 */
	@Test
	public void testInsert_KeyDoesNotMatchPrefix() {
		final String prefix = "prefix";
		final String key = "key";
		final Trie<String, String> trie = new BucketTrie(prefix, systemPrinter, loggerPrinter);
		trie.insert(key, "value");

		assertTrue(trie.isEmpty());
		assertThat(logger.getLoggingEvents(), is(singletonList(debug("Given key [{}] does not begin with prefix [{}]", key, prefix))));
	}

	/**
	 * Tests {@link BucketTrie#insert(String, String)} when the given key matches the prefix.
	 */
	@Test
	public void testInsert_KeyMatchesPrefix() {
		final String prefix = "prefix";
		final String shortKey = "key";
		final String longKey = prefix + "/" + shortKey;
		final String value = "value";
		final TrieNode<String> expected = new FileTrieNode(value);

		final Trie<String, String> trie = new BucketTrie(prefix, systemPrinter, loggerPrinter);
		trie.insert(longKey, value);

		assertFalse(trie.isEmpty());
		assertEquals(1, trie.getRoot().getChildren().values().size());
		assertEquals(expected, trie.getRoot().getChildren().get(shortKey));
	}

	/**
	 * Tests {@link BucketTrie#insert(String, String)} when there is no prefix.
	 */
	@Test
	public void testInsert_NoPrefix() {
		final String key = "key";
		final String value = "value";
		final TrieNode<String> expected = new FileTrieNode(value);

		final Trie<String, String> trie = new BucketTrie(null, systemPrinter, loggerPrinter);
		trie.insert(key, value);

		assertFalse(trie.isEmpty());
		assertEquals(1, trie.getRoot().getChildren().values().size());
		assertEquals(expected, trie.getRoot().getChildren().get(key));
	}

	/**
	 * Tests {@link BucketTrie#insert(String, String)} with a folder and a file.
	 */
	@Test
	public void testInsert_FolderAndFile() {
		final String filename = "file.tmp";
		final String key = "folder/" + filename;
		final String value = "http://www.example.com/file.tmp";
		final TrieNode<String> expectedFileNode = new FileTrieNode(value);
		final TrieNode<String> expectedDirectoryNode = new DirectoryTrieNode();
		expectedDirectoryNode.getChildren().put(filename, expectedFileNode);

		final Trie<String, String> trie = new BucketTrie(null, systemPrinter, loggerPrinter);
		trie.insert(key, value);

		assertFalse(trie.isEmpty());
		assertEquals(1, trie.getRoot().getChildren().values().size());
		final TrieNode<String> actualDirectoryNode = trie.getRoot().getChildren().values().iterator().next();
		assertEquals(expectedDirectoryNode, actualDirectoryNode);
		assertEquals(1, actualDirectoryNode.getChildren().values().size());
		final TrieNode<String> actualFileNode = actualDirectoryNode.getChildren().values().iterator().next();
		assertEquals(expectedFileNode, actualFileNode);
		assertTrue(actualFileNode.getChildren().isEmpty());

	}

	/**
	 * Tests {@link BucketTrie#getRoot()}.
	 */
	@Test
	public void testGetRoot() {
		final TrieNode<String> expectedRoot = new DirectoryTrieNode();
		assertEquals(expectedRoot, new BucketTrie(null, systemPrinter, loggerPrinter).getRoot());
	}

	/**
	 * Tests {@link BucketTrie#getPrefix()} when there is no prefix.
	 */
	@Test
	public void testGetPrefix_NullPrefix() {
		assertEquals(Optional.absent(), new BucketTrie(null, systemPrinter, loggerPrinter).getPrefix());
	}

	/**
	 * Tests {@link BucketTrie#getPrefix()}.
	 */
	@Test
	public void testGetPrefix() {
		final String prefix = "prefix";
		assertEquals(Optional.of(prefix), new BucketTrie(prefix, systemPrinter, loggerPrinter).getPrefix());
	}

	/**
	 * Tests {@link BucketTrie#isEmpty()}.
	 */
	@Test
	public void testIsEmpty() {
		final Trie<String, String> trie = new BucketTrie(null, systemPrinter, loggerPrinter);
		assertTrue(trie.isEmpty());
		trie.insert("key", "value");
		assertFalse(trie.isEmpty());
	}

	/**
	 * Tests {@link BucketTrie#print()} when there is not content in the trie.
	 */
	@Test
	public void testPrint_NoContent() {
		new BucketTrie(null, systemPrinter, loggerPrinter).print();
		verify(systemPrinter, never()).print(anyString());
	}

	/**
	 * Tests {@link BucketTrie#print()}.
	 */
	@Test
	public void testPrint() {
		final Trie<String, String> trie = new BucketTrie(null, systemPrinter, loggerPrinter);
		trie.insert("fileA.tmp", "A");
		trie.insert("folderA/fileB.tmp", "B");
		trie.insert("folderA/fileC.tmp", "C");
		trie.insert("folderB/fileD.tmp", "D");
		trie.print();

		final InOrder inOrder = inOrder(systemPrinter);
		inOrder.verify(systemPrinter).print("+--fileA.tmp");
		inOrder.verify(systemPrinter).print("+--folderA/");
		inOrder.verify(systemPrinter).print("|  +--fileB.tmp");
		inOrder.verify(systemPrinter).print("|  +--fileC.tmp");
		inOrder.verify(systemPrinter).print("+--folderB/");
		inOrder.verify(systemPrinter).print("   +--fileD.tmp");

		verifyZeroInteractions(loggerPrinter);
	}

	/**
	 * Tests {@link BucketTrie#log()} when there is not content in the trie.
	 */
	@Test
	public void testLog_NoContent() {
		new BucketTrie(null, systemPrinter, loggerPrinter).log();
		verify(loggerPrinter, never()).print(anyString());
	}

	/**
	 * Tests {@link BucketTrie#log()}.
	 */
	@Test
	public void testLog() {
		final Trie<String, String> trie = new BucketTrie(null, systemPrinter, loggerPrinter);
		trie.insert("fileA.tmp", "A");
		trie.insert("folderA/fileB.tmp", "B");
		trie.insert("folderA/fileC.tmp", "C");
		trie.insert("folderB/fileD.tmp", "D");
		trie.log();

		final InOrder inOrder = inOrder(loggerPrinter);
		inOrder.verify(loggerPrinter).print("+--fileA.tmp");
		inOrder.verify(loggerPrinter).print("+--folderA/");
		inOrder.verify(loggerPrinter).print("|  +--fileB.tmp");
		inOrder.verify(loggerPrinter).print("|  +--fileC.tmp");
		inOrder.verify(loggerPrinter).print("+--folderB/");
		inOrder.verify(loggerPrinter).print("   +--fileD.tmp");

		verifyZeroInteractions(systemPrinter);
	}

	/**
	 * Tests {@link BucketTrie#equals(Object)}.
	 */
	@Test
	public void testEquals() {
		final Trie<String, String> trie1 = new BucketTrie(null, systemPrinter, loggerPrinter);
		final Trie<String, String> trie2 = new BucketTrie(null, systemPrinter, loggerPrinter);
		final Trie<String, String> trie3 = new BucketTrie("prefix", systemPrinter, loggerPrinter);
		assertEquals(trie1, trie1);
		assertEquals(trie1, trie2);
		assertNotEquals(trie1, "String");
		assertNotEquals(trie1, null);
		assertNotEquals(trie1, trie3);
	}

	/**
	 * Tests {@link BucketTrie#hashCode()}.
	 */
	@Test
	public void testHashcode() {
		final Trie<String, String> trie1 = new BucketTrie(null, systemPrinter, loggerPrinter);
		final Trie<String, String> trie2 = new BucketTrie("prefix", systemPrinter, loggerPrinter);
		assertEquals(trie1.hashCode(), trie1.hashCode());
		assertNotEquals(trie1.hashCode(), trie2.hashCode());
	}

	/**
	 * Tests {@link BucketTrie#toString()}.
	 */
	@Test
	public void testToString() {
		final Trie<String, String> trie = new BucketTrie(null, systemPrinter, loggerPrinter);
		final String expected = "BucketTrie{root=" + trie.getRoot().toString() + ", prefix='" + null + "'}";
		assertEquals(expected, trie.toString());
	}

}
