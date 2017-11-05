package com.thedesertmonk.mojo.aws.p2.maven.plugin.s3.request.factory.put;

import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.google.common.collect.Sets;
import com.thedesertmonk.mojo.aws.p2.maven.plugin.s3.exception.ObjectRequestCreationException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Test class for {@link PutObjectRequestFactory}.
 */
public class PutObjectRequestFactoryTest {

	private final String bucketName = "mock";
	private final String destination = "repository";

	private PutObjectRequestFactory factory;

	/**
	 * Setup.
	 */
	@Before
	public void setup() {
		factory = new PutObjectRequestFactory(bucketName);
	}

	/**
	 * Tests that the constructor throws an exception when the given bucket name is {@code null}.
	 */
	@Test(expected = NullPointerException.class)
	public void testConstructor_NullBucketName() {
		new PutObjectRequestFactory(null);
	}

	/**
	 * Tests that the constructor throws an exception when the given bucket name is empty.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testConstructor_EmptyBucketName() {
		new PutObjectRequestFactory(" ");
	}

	/**
	 * Tests that {@link PutObjectRequestFactory#create(File, String)} throws an exception when the given file is {@code
	 * null}.
	 *
	 * @throws ObjectRequestCreationException Unexpected.
	 */
	@Test(expected = NullPointerException.class)
	public void testCreate_NullFile() throws ObjectRequestCreationException {
		factory.create(null, destination);
	}

	/**
	 * Tests that {@link PutObjectRequestFactory#create(File, String)} throws an exception when the given destination is
	 * {@code null}.
	 *
	 * @throws ObjectRequestCreationException Unexpected.
	 * @throws IOException                    Unexpected.
	 */
	@Test(expected = NullPointerException.class)
	public void testCreate_NullDestination() throws ObjectRequestCreationException, IOException {
		factory.create(createTemporaryFile(), null);
	}

	/**
	 * Tests that {@link PutObjectRequestFactory#create(File, String)} throws an exception when the given destination is
	 * empty.
	 *
	 * @throws ObjectRequestCreationException Unexpected.
	 * @throws IOException                    Unexpected.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testCreate_EmptyDestination() throws ObjectRequestCreationException, IOException {
		factory.create(createTemporaryFile(), " ");
	}

	/**
	 * Tests that {@link PutObjectRequestFactory#create(File, String)} throws an exception when the given file cannot be
	 * found.
	 *
	 * @throws ObjectRequestCreationException Expected.
	 */
	@Test(expected = ObjectRequestCreationException.class)
	public void testCreate_FileNotFound() throws ObjectRequestCreationException {
		factory.create(new File(""), destination);
	}

	/**
	 * Tests {@link PutObjectRequestFactory#create(File, String)}.
	 *
	 * @throws ObjectRequestCreationException Unexpected.
	 * @throws IOException                    Unexpected.
	 */
	@Test
	public void testCreate() throws ObjectRequestCreationException, IOException {
		final File file = createTemporaryFile();
		final PutObjectRequest request = factory.create(file, destination);

		assertEquals(bucketName, request.getBucketName());
		assertEquals(destination, request.getKey());
		assertEquals(file.length(), request.getMetadata().getContentLength());
		assertEquals(CannedAccessControlList.PublicRead, request.getCannedAcl());
	}

	/**
	 * Creates a temporary file.
	 */
	private File createTemporaryFile() throws IOException {
		final Set<PosixFilePermission> permissions = Sets.newHashSet(PosixFilePermission.OWNER_READ);
		final FileAttribute attribute = PosixFilePermissions.asFileAttribute(permissions);
		return Files.createTempFile("mock", null, attribute).toFile();
	}

}
