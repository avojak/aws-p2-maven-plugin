package com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.printer.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import static org.mockito.Mockito.verify;

/**
 * Test class for {@link DebugLogTriePrinter}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DebugLogTriePrinterTest {

	@Mock
	private Logger logger;

	/**
	 * Tests that the constructor throws an exception when the given {@link Logger} is {@code null}.
	 */
	@Test(expected = NullPointerException.class)
	public void testConstructor_NullLogger() {
		new DebugLogTriePrinter(null);
	}

	/**
	 * Tests {@link DebugLogTriePrinter#print(String)}.
	 */
	@Test
	public void testPrint() {
		final String line = "Hello, world!";
		new DebugLogTriePrinter(logger).print(line);
		verify(logger).debug(line);
	}

}
