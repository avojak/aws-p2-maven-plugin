package com.thedesertmonk.mojo.aws.p2.maven.plugin.s3.request.factory.put;

import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.thedesertmonk.mojo.aws.p2.maven.plugin.s3.exception.ObjectRequestCreationException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Factory class to create instances of {@link PutObjectRequest}.
 */
public class PutObjectRequestFactory {

	private final String bucketName;

	/**
	 * Constructor.
	 *
	 * @param bucketName The name of the bucket for which requests are created. Cannot be {@code null} or empty.
	 */
	public PutObjectRequestFactory(final String bucketName) {
		this.bucketName = checkNotNull(bucketName, "bucketName cannot be null");
		checkArgument(!bucketName.trim().isEmpty(), "bucketName cannot be empty");
	}

	/**
	 * Creates a new instance of {@link PutObjectRequest}.
	 *
	 * @param file The {@link File} to be uploaded in the request. Cannot be {@code null}.
	 * @param dest The destination path in the bucket for the file. Cannot be {@code null} or empty.
	 *
	 * @return A new, non-{@code null} instance of {@link PutObjectRequest}.
	 *
	 * @throws ObjectRequestCreationException if there is an error while creating the request.
	 */
	public PutObjectRequest create(final File file, final String dest) throws ObjectRequestCreationException {
		checkNotNull(file, "file cannot be null");
		checkNotNull(dest, "dest cannot be null");
		checkArgument(!dest.trim().isEmpty(), "dest cannot be empty");

		final InputStream inputStream;
		try {
			inputStream = new FileInputStream(file);
		} catch (final FileNotFoundException e) {
			throw new ObjectRequestCreationException(e);
		}
		final ObjectMetadata metadata = new ObjectMetadata();
		metadata.setContentLength(file.length());
		return new PutObjectRequest(bucketName, dest, inputStream, metadata)
				.withCannedAcl(CannedAccessControlList.PublicRead);
	}

}
