package com.avojak.mojo.aws.p2.maven.plugin.s3;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

/**
 * Factory class to create instances of {@link AmazonS3}.
 */
public class AmazonS3ClientFactory {

	// Attempt to use an arbitrary default region for creating the client, even if it's incorrect.
	// See: https://github.com/aws/aws-sdk-java/issues/1142#issuecomment-300308009
	private static final String DEFAULT_REGION = "us-east-1";

	/**
	 * Creates and returns a new instance of {@link AmazonS3}.
	 *
	 * @return The new, non-{@code null} instance of {@link AmazonS3}.
	 */
	public AmazonS3 create() {
		return AmazonS3ClientBuilder.standard().withRegion(DEFAULT_REGION)
				.withForceGlobalBucketAccessEnabled(true)
				.withCredentials(new DefaultAWSCredentialsProviderChain())
				.build();
	}

}
