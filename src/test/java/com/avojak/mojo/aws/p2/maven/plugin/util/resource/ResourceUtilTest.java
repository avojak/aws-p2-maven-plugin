package com.avojak.mojo.aws.p2.maven.plugin.util.resource;

import com.avojak.mojo.aws.p2.maven.plugin.AWSP2Mojo;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test class for {@link ResourceUtil}.
 */
public class ResourceUtilTest {

	/**
	 * Tests that {@link ResourceUtil#getString(Class, String)} throws an exception when the given class is
	 * {@code null}.
	 */
	@Test(expected = NullPointerException.class)
	public void testGetStringNullClass() {
		ResourceUtil.getString(null, "key");
	}

	/**
	 * Tests that {@link ResourceUtil#getString(Class, String)} throws an exception when the given key is {@code null}.
	 */
	@Test(expected = NullPointerException.class)
	public void testGetStringNullKey() {
		ResourceUtil.getString(getClass(), null);
	}

	/**
	 * Tests that {@link ResourceUtil#getString(Class, String)} throws an exception when the given key is empty.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testGetStringEmptyKey() {
		ResourceUtil.getString(getClass(), " ");
	}

	/**
	 * Tests that {@link ResourceUtil#getString(Class, String)} can locate the resource bundle by checking for a single
	 * entry.
	 */
	@Test
	public void testGetString() {
		final String expected = "Skipping execution";
		final String actual = ResourceUtil.getString(AWSP2Mojo.class, "info.skippingExecution");
		assertEquals(actual, expected);
	}

}
