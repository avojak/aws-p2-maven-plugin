package com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.impl;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Test class for {@link BucketTrieFactory}.
 */
public class BucketTrieFactoryTest {

	private BucketTrieFactory factory;

	/**
	 * Setup.
	 */
	@Before
	public void setup() {
		factory = new BucketTrieFactory();
	}

	/**
	 * Tests {@link BucketTrieFactory#create()}.
	 */
	@Test
	public void testCreate() {
		assertNotNull(factory.create());
	}

	/**
	 * Tests that {@link BucketTrieFactory#create(String)} throws an exception when the given prefix is {@code null}.
	 */
	@Test(expected = NullPointerException.class)
	public void testCreateWithNullPrefix() {
		factory.create(null);
	}

	/**
	 * Tests that {@link BucketTrieFactory#create(String)} throws an exception when the given prefix is empty.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testCreateWithEmptyPrefix() {
		factory.create(" ");
	}

	/**
	 * Tests {@link BucketTrieFactory#create(String)}.
	 */
	@Test
	public void testCreateWithPrefix() {
		assertNotNull(factory.create("prefix"));
	}

}
