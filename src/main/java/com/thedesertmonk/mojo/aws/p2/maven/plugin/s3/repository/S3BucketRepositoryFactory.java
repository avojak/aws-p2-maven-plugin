package com.thedesertmonk.mojo.aws.p2.maven.plugin.s3.repository;

import com.amazonaws.services.s3.AmazonS3;
import com.thedesertmonk.mojo.aws.p2.maven.plugin.s3.exception.BucketDoesNotExistException;
import com.thedesertmonk.mojo.aws.p2.maven.plugin.s3.request.factory.delete.DeleteObjectRequestFactory;
import com.thedesertmonk.mojo.aws.p2.maven.plugin.s3.request.factory.put.PutObjectRequestFactory;

/**
 * Factory class to create instances of {@link S3BucketRepository}.
 */
public class S3BucketRepositoryFactory {

	private final AmazonS3 client;

	/**
	 * Constructor.
	 *
	 * @param client The instance of {@link AmazonS3}.
	 */
	public S3BucketRepositoryFactory(final AmazonS3 client) {
		this.client = client;
	}

	/**
	 * Creates and returns a new instance of {@link S3BucketRepository}.
	 *
	 * @param bucketName The name of the S3 bucket.
	 *
	 * @return A new, non-{@code null} instance of {@link S3BucketRepository}.
	 *
	 * @throws BucketDoesNotExistException if the specified bucket does not exist.
	 */
	public S3BucketRepository create(final String bucketName) throws BucketDoesNotExistException {
		final PutObjectRequestFactory filePutObjectRequestFactory = new PutObjectRequestFactory(bucketName);
		final DeleteObjectRequestFactory deleteObjectRequestFactory = new DeleteObjectRequestFactory(bucketName);
		return new S3BucketRepository(client, bucketName, filePutObjectRequestFactory, deleteObjectRequestFactory);
	}

}
