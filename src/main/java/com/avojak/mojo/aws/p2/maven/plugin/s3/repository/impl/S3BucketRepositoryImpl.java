package com.avojak.mojo.aws.p2.maven.plugin.s3.repository.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.HeadBucketRequest;
import com.amazonaws.services.s3.model.HeadBucketResult;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.avojak.mojo.aws.p2.maven.plugin.resource.ResourceUtil;
import com.avojak.mojo.aws.p2.maven.plugin.s3.exception.BucketDoesNotExistException;
import com.avojak.mojo.aws.p2.maven.plugin.s3.exception.ObjectRequestCreationException;
import com.avojak.mojo.aws.p2.maven.plugin.s3.model.BucketPath;
import com.avojak.mojo.aws.p2.maven.plugin.s3.repository.S3BucketRepository;
import com.avojak.mojo.aws.p2.maven.plugin.s3.repository.S3BucketRepositoryFactory;
import com.avojak.mojo.aws.p2.maven.plugin.s3.request.factory.delete.DeleteObjectRequestFactory;
import com.avojak.mojo.aws.p2.maven.plugin.s3.request.factory.head.HeadBucketRequestFactory;
import com.avojak.mojo.aws.p2.maven.plugin.s3.request.factory.list.ListObjectsRequestFactory;
import com.avojak.mojo.aws.p2.maven.plugin.s3.request.factory.put.PutObjectRequestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Implementation of {@link S3BucketRepository} to wrap an {@link AmazonS3} bucket. Instances should be created with
 * {@link S3BucketRepositoryFactory}.
 */
public class S3BucketRepositoryImpl implements S3BucketRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(S3BucketRepositoryImpl.class);

	private final AmazonS3 client;
	private final String bucketName;
	private final PutObjectRequestFactory putObjectRequestFactory;
	private final DeleteObjectRequestFactory deleteObjectRequestFactory;
	private final ListObjectsRequestFactory listObjectsRequestFactory;
	private final HeadBucketRequestFactory headBucketRequestFactory;

	/**
	 * Constructor.
	 *
	 * @param client                     The {@link AmazonS3} client. Cannot be {@code null}.
	 * @param bucketName                 The name of the bucket that this repository represents. Cannot be {@code null}
	 *                                   or empty.
	 * @param putObjectRequestFactory    The {@link PutObjectRequestFactory} for {@link File files}. Cannot be {@code
	 *                                   null}.
	 * @param deleteObjectRequestFactory The {@link DeleteObjectRequestFactory}. Cannot be {@code null}.
	 * @param listObjectsRequestFactory  The {@link ListObjectsRequestFactory}. Cannot be {@code null}.
	 * @param headBucketRequestFactory   The {@link HeadBucketRequestFactory}. Cannot be {@code null}.
	 *
	 * @throws BucketDoesNotExistException if the specified bucketName does not refer to an existing bucket.
	 */
	public S3BucketRepositoryImpl(final AmazonS3 client, final String bucketName,
	                              final PutObjectRequestFactory putObjectRequestFactory,
	                              final DeleteObjectRequestFactory deleteObjectRequestFactory,
	                              final ListObjectsRequestFactory listObjectsRequestFactory,
	                              final HeadBucketRequestFactory headBucketRequestFactory) throws BucketDoesNotExistException {
		this.client = checkNotNull(client, "client cannot be null");
		this.bucketName = checkNotNull(bucketName, "bucketName cannot be null");
		checkArgument(!bucketName.trim().isEmpty(), "bucketName cannot be empty");
		this.putObjectRequestFactory = checkNotNull(putObjectRequestFactory, "putObjectRequestFactory cannot be null");
		this.deleteObjectRequestFactory =
				checkNotNull(deleteObjectRequestFactory, "deleteObjectRequestFactory cannot be null");
		this.listObjectsRequestFactory =
				checkNotNull(listObjectsRequestFactory, "listObjectsRequestFactory cannot be null");
		this.headBucketRequestFactory =
				checkNotNull(headBucketRequestFactory, "headBucketRequestFactory cannot be null");
		if (!client.doesBucketExist(bucketName)) {
			throw new BucketDoesNotExistException(bucketName);
		}
	}

	@Override
	public URL uploadFile(final File src, final BucketPath dest) {
		checkNotNull(src, "src cannot be null");
		checkNotNull(dest, "dest cannot be null");
		if (!src.exists() || !src.isFile()) {
			LOGGER.warn(ResourceUtil.getString(getClass(), "warn.fileNotAccessible"), src.getName());
			return null;
		}
		final String key = dest.asString();
		try {
			LOGGER.debug(ResourceUtil.getString(getClass(), "debug.uploadingFile"), key);
			client.putObject(putObjectRequestFactory.create(src, key));
		} catch (final ObjectRequestCreationException e) {
			LOGGER.error(ResourceUtil.getString(getClass(), "error.failedUploadRequestCreation"), e);
			return null;
		}
		return client.getUrl(bucketName, key);
	}

	@Override
	public URL uploadDirectory(final File srcDir, final BucketPath dest) {
		checkNotNull(srcDir, "srcDir cannot be null");
		checkNotNull(dest, "dest cannot be null");
		if (!srcDir.exists() || !srcDir.isDirectory()) {
			LOGGER.warn(ResourceUtil.getString(getClass(), "warn.directoryNotAccessible"), srcDir.getName());
			return null;
		}
		final String key = dest.asString();
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

	@Override
	public void deleteDirectory(final String prefix) {
		checkNotNull(prefix, "prefix cannot be null");
		checkArgument(!prefix.trim().isEmpty(), "prefix cannot be empty");
		final ListObjectsRequest listObjectsRequest = listObjectsRequestFactory.create(prefix);
		ObjectListing objectListing = client.listObjects(listObjectsRequest);
		List<S3ObjectSummary> objectSummaries = objectListing.getObjectSummaries();
		while (true) {
			for (final S3ObjectSummary summary : objectSummaries) {
				final String key = summary.getKey();
				final DeleteObjectRequest deleteObjectRequest = deleteObjectRequestFactory.create(key);
				LOGGER.debug(ResourceUtil.getString(getClass(), "debug.deleteExistingObject"), key);
				client.deleteObject(deleteObjectRequest);
			}
			// Ensure that we get all objects. Not all may be returned by the first call to listObjects()
			if (objectListing.isTruncated()) {
				objectListing = client.listNextBatchOfObjects(objectListing);
				objectSummaries = objectListing.getObjectSummaries();
			} else {
				break;
			}
		}
	}

	@Override
	public String getHostingUrl(final String key) {
		final String hostingUrlFormat = ResourceUtil.getString(getClass(), "hostingUrlFormat");
		final HeadBucketRequest headBucketRequest = headBucketRequestFactory.create();
		final HeadBucketResult headBucketResult = client.headBucket(headBucketRequest);
		final String bucketRegion = headBucketResult.getBucketRegion();
		return MessageFormat.format(hostingUrlFormat, bucketName, bucketRegion, key == null ? "" : key);
	}

}
