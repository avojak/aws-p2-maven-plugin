package com.avojak.mojo.aws.p2.maven.plugin.s3.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Test class for {@link BucketPath}.
 */
public class BucketPathTest {

	/**
	 * Tests {@link BucketPath#BucketPath()}.
	 */
	@Test
	public void testNoArgsConstructor() {
		assertEquals("", new BucketPath().asString());
	}

	/**
	 * Tests that {@link BucketPath#BucketPath(BucketPath)} throws an exception when the given {@link BucketPath} is
	 * {@code null}.
	 */
	@Test(expected = NullPointerException.class)
	public void testDeepCopyConstructor_NullPath() {
		new BucketPath(null);
	}

	/**
	 * Tests {@link BucketPath#BucketPath(BucketPath)}.
	 */
	@Test
	public void testDeepCopyConstructor() {
		final BucketPath path = new BucketPath().append("mock");
		assertEquals(path, new BucketPath(path));
	}

	/**
	 * Tests that {@link BucketPath#append(String)} throws an exception when the given path is {@code null}.
	 */
	@Test(expected = NullPointerException.class)
	public void testAppendNullPath() {
		new BucketPath().append(null);
	}

	/**
	 * Tests that {@link BucketPath#append(String)} throws an exception when the given path is empty.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testAppendEmptyPath() {
		new BucketPath().append(" ");
	}

	/**
	 * Tests that {@link BucketPath#append(String)} replaces '\' with '/'.
	 */
	@Test
	public void testAppendReplaceDeliminators() {
		final String path = "directory\\file";
		final String expectedPath = "directory/file";
		assertEquals(expectedPath, new BucketPath().append(path).asString());
	}

	/**
	 * Tests that {@link BucketPath#append(String)} trims deliminators at the start and end of the path.
	 */
	@Test
	public void testAppendTrimPrefixAndSuffix() {
		final String path = "\\directory\\file\\";
		final String expectedPath = "directory/file";
		assertEquals(expectedPath, new BucketPath().append(path).asString());
	}

	/**
	 * Tests that {@link BucketPath#append(String)} adds a deliminator when one doesn't already exist.
	 */
	@Test
	public void testAddDeliminator() {
		final String directory = "directory";
		final String file = "file";
		final String expectedPath = "directory/file";
		assertEquals(expectedPath, new BucketPath().append(directory).append(file).asString());
	}

	/**
	 * Tests {@link BucketPath#asString()}.
	 */
	@Test
	public void testAsString() {
		assertEquals("mock", new BucketPath().append("mock").asString());
	}

	/**
	 * Tests {@link BucketPath#equals(Object)}.
	 */
	@Test
	public void testEquals() {
		final BucketPath bucketPath1 = new BucketPath();
		final BucketPath bucketPath2 = new BucketPath();
		final BucketPath bucketPath3 = new BucketPath().append("path");

		assertEquals(bucketPath1, bucketPath1);
		assertEquals(bucketPath1, bucketPath2);
		assertNotEquals(bucketPath1, null);
		assertNotEquals(bucketPath1, "String");
		assertNotEquals(bucketPath1, bucketPath3);
	}

	/**
	 * Tests {@link BucketPath#hashCode()}.
	 */
	@Test
	public void testHashcode() {
		final BucketPath bucketPath1 = new BucketPath();
		final BucketPath bucketPath2 = new BucketPath().append("path");

		assertEquals(bucketPath1.hashCode(), bucketPath1.hashCode());
		assertNotEquals(bucketPath1.hashCode(), bucketPath2.hashCode());
	}

	/**
	 * Tests {@link BucketPath#toString()}.
	 */
	@Test
	public void testToString() {
		final BucketPath bucketPath = new BucketPath().append("path");
		final String expected = "BucketPath{path}";
		assertEquals(expected, bucketPath.toString());
	}

}
