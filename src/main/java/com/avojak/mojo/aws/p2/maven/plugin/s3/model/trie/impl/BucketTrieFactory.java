package com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.impl;

import com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.printer.TriePrinter;
import com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.printer.impl.DebugLogTriePrinter;
import com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.printer.impl.SystemOutTriePrinter;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Factory class to create instances of {@link BucketTrie}.
 */
public class BucketTrieFactory {

	private final TriePrinter systemPrinter;
	private final TriePrinter loggerPrinter;

	/**
	 * Constructor.
	 */
	public BucketTrieFactory() {
		systemPrinter = new SystemOutTriePrinter();
		loggerPrinter = new DebugLogTriePrinter(LoggerFactory.getLogger(BucketTrie.class));
	}

	/**
	 * Creates and returns a new instance of {@link BucketTrie}.
	 *
	 * @return A new, non-{@code null} instance of {@link BucketTrie}.
	 */
	public BucketTrie create() {
		return new BucketTrie(null, systemPrinter, loggerPrinter);
	}

	/**
	 * Creates and returns a new instance of {@link BucketTrie}.
	 *
	 * @param prefix The prefix for the trie content. Cannot be {@code null} or empty.
	 *
	 * @return A new, non-{@code null} instance of {@link BucketTrie}.
	 */
	public BucketTrie create(final String prefix) {
		checkNotNull(prefix, "prefix cannot be null");
		checkArgument(!prefix.trim().isEmpty(), "prefix cannot be empty");
		return new BucketTrie(prefix, systemPrinter, loggerPrinter);
	}

}
