package com.thedesertmonk.mojo.aws.p2.maven.plugin.s3.repository;

import com.amazonaws.services.s3.AmazonS3;
import com.thedesertmonk.mojo.aws.p2.maven.plugin.resource.ResourceUtil;
import com.thedesertmonk.mojo.aws.p2.maven.plugin.s3.exception.BucketDoesNotExistException;
import com.thedesertmonk.mojo.aws.p2.maven.plugin.s3.exception.ObjectRequestCreationException;
import com.thedesertmonk.mojo.aws.p2.maven.plugin.s3.model.BucketPath;
import com.thedesertmonk.mojo.aws.p2.maven.plugin.s3.request.factory.delete.DeleteObjectRequestFactory;
import com.thedesertmonk.mojo.aws.p2.maven.plugin.s3.request.factory.put.PutObjectRequestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Repository class to wrap an {@link AmazonS3} bucket. Instances should be created with {@link
 * S3BucketRepositoryFactory}.
 */
public class S3BucketRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(S3BucketRepository.class);

	private final AmazonS3 client;
	private final String bucketName;
	private final PutObjectRequestFactory putObjectRequestFactory;
	private final DeleteObjectRequestFactory deleteObjectRequestFactory;

	/**
	 * Constructor.
	 *
	 * @param client                     The {@link AmazonS3} client. Cannot be {@code null}.
	 * @param bucketName                 The name of the bucket that this repository represents. Cannot be {@code null}
	 *                                   or empty.
	 * @param putObjectRequestFactory    The {@link PutObjectRequestFactory} for {@link File files}. Cannot be {@code
	 *                                   null}.
	 * @param deleteObjectRequestFactory The {@link DeleteObjectRequestFactory}. Cannot be {@code null}.
	 *
	 * @throws BucketDoesNotExistException if the specified bucketName does not refer to an existing bucket.
	 */
	S3BucketRepository(final AmazonS3 client, final String bucketName,
			final PutObjectRequestFactory putObjectRequestFactory,
			final DeleteObjectRequestFactory deleteObjectRequestFactory) throws BucketDoesNotExistException {
		this.client = checkNotNull(client, "client cannot be null");
		this.bucketName = checkNotNull(bucketName, "bucketName cannot be null");
		checkArgument(!bucketName.trim().isEmpty(), "bucketName cannot be empty");
		this.putObjectRequestFactory = checkNotNull(putObjectRequestFactory, "putObjectRequestFactory cannot be null");
		this.deleteObjectRequestFactory = checkNotNull(deleteObjectRequestFactory,
				"deleteObjectRequestFactory cannot be null");
		if (!client.doesBucketExist(bucketName)) {
			throw new BucketDoesNotExistException(bucketName);
		}
	}

	/**
	 * Uploads a file into the given location in the bucket. The destination path should refer to the desired name of
	 * the file in the bucket.
	 * <p>
	 * For example, uploading the file: <pre>target/dir/example.xml</pre>
	 * <p>
	 * The expected method call would be: <pre>uploadFile(new File("target/dir/example.xml"), new
	 * BucketPath("some/directory/example.xml"))</pre>
	 * <p>
	 * The destination path need not exist in the bucket prior to calling this method. Any non-existent folders will be
	 * created as needed.
	 * <p>
	 * In the event that a file at the same destination path already exists, that file will be deleted and replaced with
	 * the given file.
	 *
	 * @param src  The source {@link File} to upload. Cannot be {@code null}.
	 * @param dest The destination {@link BucketPath} location within the bucket. Cannot be {@code null}.
	 *
	 * @return The {@link URL} of the file which was uploaded, or {@code null} if no file was uploaded.
	 */
	public URL uploadFile(final File src, final BucketPath dest) {
		checkNotNull(src, "src cannot be null");
		checkNotNull(dest, "dest cannot be null");
		if (!isAccessible(src) || !src.isFile()) {
			LOGGER.warn(ResourceUtil.getString(getClass(), "warn.fileNotAccessible"), src.getName());
			return null;
		}
		final String key = dest.asString();
		deleteExistingObjectIfExists(key);
		try {
			LOGGER.debug(ResourceUtil.getString(getClass(), "debug.uploadingFile"), key);
			client.putObject(putObjectRequestFactory.create(src, key));
		} catch (final ObjectRequestCreationException e) {
			LOGGER.error(ResourceUtil.getString(getClass(), "error.failedUploadRequestCreation"), e);
			return null;
		}
		return client.getUrl(bucketName, key);
	}

	/**
	 * Uploads a directory and its contents into the given location in the bucket. The destination path should refer to
	 * the desired name of the folder in the bucket.
	 * <p>
	 * For example, uploading the directory: <pre>target/dir/</pre>
	 * <p>
	 * The expected method call would be: <pre>uploadFile(new File("target/dir/"), new
	 * BucketPath("some/directory/"))</pre>
	 * <p>
	 * The destination path need not exist in the bucket prior to calling this method. Any non-existent folders will be
	 * created as needed.
	 * <p>
	 * In the event that a folder at the same destination path already exists, that folder will be deleted and replaced
	 * with the given directory.
	 * <p>
	 * Empty directories will be ignored.
	 *
	 * @param srcDir The source directory {@link File} to upload. Cannot be {@code null}.
	 * @param dest   The destination {@link BucketPath} location within the bucket. Cannot be {@code null}.
	 *
	 * @return The {@link URL} of the directory which was uploaded, or {@code null} if no directory was uploaded.
	 */
	public URL uploadDirectory(final File srcDir, final BucketPath dest) {
		checkNotNull(srcDir, "srcDir cannot be null");
		checkNotNull(dest, "dest cannot be null");
		if (!isAccessible(srcDir) || !srcDir.isDirectory()) {
			LOGGER.warn(ResourceUtil.getString(getClass(), "warn.directoryNotAccessible"), srcDir.getName());
			return null;
		}
		final String key = dest.asString();
		deleteExistingObjectIfExists(key);
		final File[] directoryContents = srcDir.listFiles();
		if (directoryContents == null) {
			// Should never happen, since we already verify that srcDir is a directory
			LOGGER.warn(ResourceUtil.getString(getClass(), "warn.directoryContentsNull"), srcDir.getName());
			return null;
		}
		if (directoryContents.length == 0) {
			LOGGER.debug(ResourceUtil.getString(getClass(), "debug.skippingEmptyDirectory"), srcDir.getName());
			return null;
		}
		for (final File file : directoryContents) {
			if (file.isFile()) {
				uploadFile(file, new BucketPath(dest).append(file.getName()));
			} else if (file.isDirectory()) {
				uploadDirectory(file, new BucketPath(dest).append(file.getName()));
			}
		}
		return client.getUrl(bucketName, key);
	}

	private boolean isAccessible(final File file) {
		return file.exists() && file.canRead();
	}

	// TODO: Explicitly declare the exceptions thrown by the AmazonS3 client?
	private void deleteExistingObjectIfExists(final String key) {
		if (client.doesObjectExist(bucketName, key)) {
			LOGGER.debug(ResourceUtil.getString(getClass(), "debug.deleteExistingObject"), key);
			client.deleteObject(deleteObjectRequestFactory.create(key));
		}
	}

}
