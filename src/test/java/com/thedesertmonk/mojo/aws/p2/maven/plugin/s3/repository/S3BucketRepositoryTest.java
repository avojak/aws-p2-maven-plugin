package com.thedesertmonk.mojo.aws.p2.maven.plugin.s3.repository;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.google.common.collect.Sets;
import com.thedesertmonk.mojo.aws.p2.maven.plugin.s3.exception.BucketDoesNotExistException;
import com.thedesertmonk.mojo.aws.p2.maven.plugin.s3.exception.ObjectRequestCreationException;
import com.thedesertmonk.mojo.aws.p2.maven.plugin.s3.model.BucketPath;
import com.thedesertmonk.mojo.aws.p2.maven.plugin.s3.request.factory.delete.DeleteObjectRequestFactory;
import com.thedesertmonk.mojo.aws.p2.maven.plugin.s3.request.factory.put.PutObjectRequestFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.org.lidalia.slf4jtest.LoggingEvent.debug;
import static uk.org.lidalia.slf4jtest.LoggingEvent.error;
import static uk.org.lidalia.slf4jtest.LoggingEvent.warn;

/**
 * Test class for {@link S3BucketRepository}.
 */
@RunWith(MockitoJUnitRunner.class)
public class S3BucketRepositoryTest {

	@Mock
	private AmazonS3 client;

	@Mock
	private PutObjectRequestFactory putObjectRequestFactory;

	@Mock
	private PutObjectRequest putObjectRequest;

	@Mock
	private DeleteObjectRequestFactory deleteObjectRequestFactory;

	@Mock
	private DeleteObjectRequest deleteObjectRequest;

	private final TestLogger logger = TestLoggerFactory.getTestLogger(S3BucketRepository.class);

	private final String bucketName = "mock";

	private S3BucketRepository repository;

	/**
	 * Setup mocks.
	 *
	 * @throws BucketDoesNotExistException Unexpected.
	 */
	@Before
	public void setup() throws BucketDoesNotExistException {
		when(client.doesBucketExist(bucketName)).thenReturn(true);
		repository = new S3BucketRepository(client, bucketName, putObjectRequestFactory, deleteObjectRequestFactory);
	}

	/**
	 * Clear loggers.
	 */
	@After
	public void clearLoggers() {
		TestLoggerFactory.clear();
	}

	/**
	 * Tests that the constructor throws an exception when the given {@link AmazonS3} client is {@code null}.
	 *
	 * @throws BucketDoesNotExistException Unexpected.
	 */
	@Test(expected = NullPointerException.class)
	public void testConstructor_NullClient() throws BucketDoesNotExistException {
		new S3BucketRepository(null, bucketName, putObjectRequestFactory, deleteObjectRequestFactory);
	}

	/**
	 * Tests that the constructor throws an exception when the given bucket name is {@code null}.
	 *
	 * @throws BucketDoesNotExistException Unexpected.
	 */
	@Test(expected = NullPointerException.class)
	public void testConstructor_NullBucketName() throws BucketDoesNotExistException {
		new S3BucketRepository(client, null, putObjectRequestFactory, deleteObjectRequestFactory);
	}

	/**
	 * Tests that the constructor throws an exception when the given bucket name is empty.
	 *
	 * @throws BucketDoesNotExistException Unexpected.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testConstructor_EmptyBucketName() throws BucketDoesNotExistException {
		new S3BucketRepository(client, " ", putObjectRequestFactory, deleteObjectRequestFactory);
	}

	/**
	 * Tests that the constructor throws an exception when the given {@link PutObjectRequestFactory} is {@code null}.
	 *
	 * @throws BucketDoesNotExistException Unexpected.
	 */
	@Test(expected = NullPointerException.class)
	public void testConstructor_NullPutObjectRequestFactory() throws BucketDoesNotExistException {
		new S3BucketRepository(client, bucketName, null, deleteObjectRequestFactory);
	}

	/**
	 * Tests that the constructor throws an exception when the given {@link DeleteObjectRequestFactory} is {@code
	 * null}.
	 *
	 * @throws BucketDoesNotExistException Unexpected.
	 */
	@Test(expected = NullPointerException.class)
	public void testConstructor_NullDeleteObjectRequestFactory() throws BucketDoesNotExistException {
		new S3BucketRepository(client, bucketName, putObjectRequestFactory, null);
	}

	/**
	 * Tests that the constructor throws an exception when the given bucket does not exist.
	 *
	 * @throws BucketDoesNotExistException Expected.
	 */
	@Test(expected = BucketDoesNotExistException.class)
	public void testConstructor_BucketDoesNotExist() throws BucketDoesNotExistException {
		when(client.doesBucketExist(bucketName)).thenReturn(false);
		new S3BucketRepository(client, bucketName, putObjectRequestFactory, deleteObjectRequestFactory);
	}

	/**
	 * Tests that {@link S3BucketRepository#uploadFile(File, BucketPath)} throws an exception when the given {@link
	 * File} is {@code null}.
	 */
	@Test(expected = NullPointerException.class)
	public void testUploadFile_NullFile() {
		repository.uploadFile(null, new BucketPath());
	}

	/**
	 * Tests that {@link S3BucketRepository#uploadFile(File, BucketPath)} throws an exception when the given destination
	 * {@link BucketPath} is {@code null}.
	 *
	 * @throws IOException Unexpected.
	 */
	@Test(expected = NullPointerException.class)
	public void testUploadFile_NullDestination() throws IOException {
		repository.uploadFile(createAccessibleFile(), null);
	}

	/**
	 * Tests {@link S3BucketRepository#uploadFile(File, BucketPath)} when the given {@link File} is not accessible.
	 *
	 * @throws IOException Unexpected.
	 */
	@Test
	public void testUploadFile_FileNotAccessible() throws IOException {
		final File file = createInaccessibleFile();
		final URL url = repository.uploadFile(file, new BucketPath());

		assertNull(url);
		assertThat(logger.getLoggingEvents(), is(singletonList(warn("File is not accessible: {}", file.getName()))));
		verify(client).doesBucketExist(bucketName);
		verifyNoMoreInteractions(client);
	}

	/**
	 * Tests {@link S3BucketRepository#uploadFile(File, BucketPath)} when the given {@link File} is not a file.
	 *
	 * @throws IOException Unexpected.
	 */
	@Test
	public void testUploadFile_FileNotAFile() throws IOException {
		final File file = createAccessibleDirectory();
		final URL url = repository.uploadFile(file, new BucketPath());

		assertNull(url);
		assertThat(logger.getLoggingEvents(), is(singletonList(warn("File is not accessible: {}", file.getName()))));
		verify(client).doesBucketExist(bucketName);
		verifyNoMoreInteractions(client);
	}

	/**
	 * Tests {@link S3BucketRepository#uploadFile(File, BucketPath)} when the creation of the upload request fails.
	 *
	 * @throws IOException                    Unexpected.
	 * @throws ObjectRequestCreationException Expected to be caught and handled.
	 */
	@Test
	public void testUploadFile_UploadRequestCreationFailed() throws IOException, ObjectRequestCreationException {
		final File file = createAccessibleFile();
		final BucketPath destination = new BucketPath().append("repository");
		final Throwable throwable = new ObjectRequestCreationException();
		when(client.doesObjectExist(bucketName, destination.asString())).thenReturn(false);
		when(putObjectRequestFactory.create(file, destination.asString())).thenThrow(throwable);

		final URL url = repository.uploadFile(file, destination);

		assertNull(url);
		verify(client).doesBucketExist(bucketName);
		verify(client).doesObjectExist(bucketName, destination.asString());
		verifyNoMoreInteractions(client);

		final LoggingEvent uploadLoggingEvent = debug("Uploading file: {}", destination.asString());
		final LoggingEvent requestCreationFailedLoggingEvent = error(throwable, "Failed to create upload request");
		assertThat(logger.getLoggingEvents(), is(asList(uploadLoggingEvent, requestCreationFailedLoggingEvent)));
	}

	/**
	 * Tests {@link S3BucketRepository#uploadFile(File, BucketPath)} when the specified file already exists in the
	 * bucket and is first deleted.
	 *
	 * @throws IOException                    Unexpected.
	 * @throws ObjectRequestCreationException Unexpected.
	 */
	@Test
	public void testUploadFile_DeleteExistingFile() throws IOException, ObjectRequestCreationException {
		final File file = createAccessibleFile();
		final BucketPath destination = new BucketPath().append("repository");
		when(client.doesObjectExist(bucketName, destination.asString())).thenReturn(true);
		when(deleteObjectRequestFactory.create(destination.asString())).thenReturn(deleteObjectRequest);
		when(putObjectRequestFactory.create(file, destination.asString())).thenReturn(putObjectRequest);
		final URL expectedUrl = new URL("http", "example", "mock");
		when(client.getUrl(bucketName, destination.asString())).thenReturn(expectedUrl);

		final URL url = repository.uploadFile(file, destination);

		assertEquals(expectedUrl, url);

		final InOrder inOrder = Mockito.inOrder(client);
		inOrder.verify(client).deleteObject(deleteObjectRequest);
		inOrder.verify(client).putObject(putObjectRequest);

		final LoggingEvent deleteLoggingEvent = debug("Deleting existing object: {}", destination.asString());
		final LoggingEvent uploadLoggingEvent = debug("Uploading file: {}", destination.asString());
		assertThat(logger.getLoggingEvents(), is(asList(deleteLoggingEvent, uploadLoggingEvent)));
	}

	/**
	 * Tests {@link S3BucketRepository#uploadFile(File, BucketPath)}.
	 *
	 * @throws IOException                    Unexpected.
	 * @throws ObjectRequestCreationException Unexpected.
	 */
	@Test
	public void testUploadFile() throws IOException, ObjectRequestCreationException {
		final File file = createAccessibleFile();
		final BucketPath destination = new BucketPath().append("repository");
		when(client.doesObjectExist(bucketName, destination.asString())).thenReturn(false);
		when(putObjectRequestFactory.create(file, destination.asString())).thenReturn(putObjectRequest);
		final URL expectedUrl = new URL("http", "example", "mock");
		when(client.getUrl(bucketName, destination.asString())).thenReturn(expectedUrl);

		final URL url = repository.uploadFile(file, destination);

		assertEquals(expectedUrl, url);
		verify(client).putObject(putObjectRequest);
		assertThat(logger.getLoggingEvents(), is(singletonList(debug("Uploading file: {}", destination.asString()))));
	}

	/**
	 * Tests that {@link S3BucketRepository#uploadDirectory(File, BucketPath)} throws an exception when the given
	 * directory is {@code null}.
	 */
	@Test(expected = NullPointerException.class)
	public void testUploadDirectory_NullDirectory() {
		repository.uploadDirectory(null, new BucketPath());
	}

	/**
	 * Tests that {@link S3BucketRepository#uploadDirectory(File, BucketPath)} throws an exception when the given
	 * destination is {@code null}.
	 *
	 * @throws IOException Unexpected.
	 */
	@Test(expected = NullPointerException.class)
	public void testUploadDirectory_NullDestination() throws IOException {
		repository.uploadDirectory(createAccessibleDirectory(), null);
	}

	/**
	 * Tests {@link S3BucketRepository#uploadDirectory(File, BucketPath)} when the given directory is not accessible.
	 *
	 * @throws IOException Unexpected.
	 */
	@Test
	public void testUploadDirectory_DirectoryNotAccessible() throws IOException {
		final File directory = createInaccessibleDirectory();
		final URL url = repository.uploadDirectory(directory, new BucketPath());

		assertNull(url);
		assertThat(logger.getLoggingEvents(),
				is(singletonList(warn("Directory is not accessible: {}", directory.getName()))));
		verify(client).doesBucketExist(bucketName);
		verifyNoMoreInteractions(client);
	}

	/**
	 * Tests {@link S3BucketRepository#uploadDirectory(File, BucketPath)} when the given directory is not a directory.
	 *
	 * @throws IOException Unexpected.
	 */
	@Test
	public void testUploadDirectory_DirectoryNotADirectory() throws IOException {
		final File directory = createAccessibleFile();
		final URL url = repository.uploadDirectory(directory, new BucketPath());

		assertNull(url);
		assertThat(logger.getLoggingEvents(),
				is(singletonList(warn("Directory is not accessible: {}", directory.getName()))));
		verify(client).doesBucketExist(bucketName);
		verifyNoMoreInteractions(client);
	}

	/**
	 * Tests {@link S3BucketRepository#uploadDirectory(File, BucketPath)} when the given directory already exists.
	 *
	 * @throws IOException                    Unexpected.
	 * @throws ObjectRequestCreationException Unexpected.
	 */
	@Test
	public void testUploadDirectory_DeleteExistingDirectory() throws IOException, ObjectRequestCreationException {
		final File directory = createAccessibleDirectory();

		final BucketPath destination = new BucketPath().append("repository");
		when(client.doesObjectExist(bucketName, destination.asString())).thenReturn(true);
		when(deleteObjectRequestFactory.create(destination.asString())).thenReturn(deleteObjectRequest);

		final URL url = repository.uploadDirectory(directory, destination);

		assertNull(url);
		verify(client).doesBucketExist(bucketName);
		verify(client).doesObjectExist(bucketName, destination.asString());
		verify(client).deleteObject(deleteObjectRequest);
		verifyNoMoreInteractions(client);

		final LoggingEvent deleteLoggingEvent = debug("Deleting existing object: {}", destination.asString());
		final LoggingEvent emptyDirectoryLoggingEvent = debug("Skipping upload of empty directory: {}",
				directory.getName());
		assertThat(logger.getLoggingEvents(), is(asList(deleteLoggingEvent, emptyDirectoryLoggingEvent)));
	}

	/**
	 * Tests {@link S3BucketRepository#uploadDirectory(File, BucketPath)} when the given directory is empty.
	 *
	 * @throws IOException                    Unexpected.
	 * @throws ObjectRequestCreationException Unexpected.
	 */
	@Test
	public void testUploadDirectory_EmptyDirectory() throws IOException, ObjectRequestCreationException {
		final File directory = createAccessibleDirectory();
		final BucketPath destination = new BucketPath().append("repository");
		when(client.doesObjectExist(bucketName, destination.asString())).thenReturn(false);

		final URL url = repository.uploadDirectory(directory, destination);

		assertNull(url);
		verify(client).doesBucketExist(bucketName);
		verify(client).doesObjectExist(bucketName, destination.asString());
		verifyNoMoreInteractions(client);

		assertThat(logger.getLoggingEvents(),
				is(singletonList(debug("Skipping upload of empty directory: {}", directory.getName()))));
	}

	/**
	 * Tests {@link S3BucketRepository#uploadDirectory(File, BucketPath)} when the given directory has a child
	 * directory.
	 *
	 * @throws IOException                    Unexpected.
	 * @throws ObjectRequestCreationException Unexpected.
	 */
	@Test
	public void testUploadDirectory_ChildDirectory() throws IOException, ObjectRequestCreationException {
		final File parentDirectory = createAccessibleDirectory();
		final File childDirectory = createAccessibleDirectory(parentDirectory.toPath());

		final BucketPath parentDirectoryDestination = new BucketPath().append("repository");
		final BucketPath childDirectoryDestination = new BucketPath(parentDirectoryDestination)
				.append(childDirectory.getName());
		when(client.doesObjectExist(bucketName, parentDirectoryDestination.asString())).thenReturn(false);
		when(client.doesObjectExist(bucketName, childDirectoryDestination.asString())).thenReturn(false);
		final URL expectedUrl = new URL("http", "example", "mock");
		when(client.getUrl(bucketName, parentDirectoryDestination.asString())).thenReturn(expectedUrl);

		final URL url = repository.uploadDirectory(parentDirectory, parentDirectoryDestination);

		assertEquals(expectedUrl, url);
		verify(client).doesBucketExist(bucketName);
		verify(client).doesObjectExist(bucketName, parentDirectoryDestination.asString());
		verify(client).doesObjectExist(bucketName, childDirectoryDestination.asString());
		verify(client).getUrl(bucketName, parentDirectoryDestination.asString());
		verifyNoMoreInteractions(client);
	}

	/**
	 * Tests {@link S3BucketRepository#uploadDirectory(File, BucketPath)} when the given directory has a child file.
	 *
	 * @throws IOException                    Unexpected.
	 * @throws ObjectRequestCreationException Unexpected.
	 */
	@Test
	public void testUploadDirectory_ChildFile() throws IOException, ObjectRequestCreationException {
		final File directory = createAccessibleDirectory();
		final File file = createAccessibleFile(directory.toPath());

		final BucketPath directoryDestination = new BucketPath().append("repository");
		final BucketPath fileDestination = new BucketPath(directoryDestination).append(file.getName());
		when(client.doesObjectExist(bucketName, directoryDestination.asString())).thenReturn(false);
		when(client.doesObjectExist(bucketName, fileDestination.asString())).thenReturn(false);
		when(putObjectRequestFactory.create(file, fileDestination.asString())).thenReturn(putObjectRequest);
		final URL expectedUrl = new URL("http", "example", "mock");
		when(client.getUrl(bucketName, directoryDestination.asString())).thenReturn(expectedUrl);

		final URL url = repository.uploadDirectory(directory, directoryDestination);

		assertEquals(expectedUrl, url);
		verify(client).doesBucketExist(bucketName);
		verify(client).doesObjectExist(bucketName, directoryDestination.asString());
		verify(client).doesObjectExist(bucketName, fileDestination.asString());
		verify(client).putObject(putObjectRequest);
		verify(client).getUrl(bucketName, directoryDestination.asString());
		verify(client).getUrl(bucketName, fileDestination.asString());
		verifyNoMoreInteractions(client);
	}

	/**
	 * Creates an accessible temporary file.
	 */
	private File createAccessibleFile() throws IOException {
		return createAccessibleFile(null);
	}

	/**
	 * Creates an accessible temporary file in the given directory.
	 */
	private File createAccessibleFile(final Path directory) throws IOException {
		return createTemporaryFile(Sets.newHashSet(PosixFilePermission.OWNER_READ), directory);
	}

	/**
	 * Creates an inaccessible temporary file.
	 */
	private File createInaccessibleFile() throws IOException {
		return createTemporaryFile(new HashSet<>(), null);
	}

	/**
	 * Creates a temporary file with the given permissions.
	 */
	private File createTemporaryFile(final Set<PosixFilePermission> permissions, final Path directory)
			throws IOException {
		final FileAttribute attribute = PosixFilePermissions.asFileAttribute(permissions);
		if (directory != null) {
			return Files.createTempFile(directory, "mock", null, attribute).toFile();
		}
		return Files.createTempFile("mock", null, attribute).toFile();
	}

	/**
	 * Creates an accessible temporary directory.
	 */
	private File createAccessibleDirectory() throws IOException {
		return createAccessibleDirectory(null);
	}

	/**
	 * Creates an accessible temporary directory in the given directory.
	 */
	private File createAccessibleDirectory(final Path directory) throws IOException {
		return createTemporaryDirectory(Sets.newHashSet(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE,
				PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.GROUP_READ, PosixFilePermission.GROUP_EXECUTE),
				directory);
	}

	/**
	 * Creates an inaccessible temporary directory.
	 */
	private File createInaccessibleDirectory() throws IOException {
		return createTemporaryDirectory(new HashSet<>(), null);
	}

	/**
	 * Creates a temporary directory with the given permissions.
	 */
	private File createTemporaryDirectory(final Set<PosixFilePermission> permissions, final Path directory)
			throws IOException {
		final FileAttribute attribute = PosixFilePermissions.asFileAttribute(permissions);
		if (directory != null) {
			return Files.createTempDirectory(directory, "mock", attribute).toFile();
		}
		return Files.createTempDirectory("mock", attribute).toFile();
	}

}
