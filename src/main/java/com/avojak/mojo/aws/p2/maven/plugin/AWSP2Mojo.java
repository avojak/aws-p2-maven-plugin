package com.avojak.mojo.aws.p2.maven.plugin;

import com.avojak.mojo.aws.p2.maven.plugin.index.generator.LandingPageGenerator;
import com.avojak.mojo.aws.p2.maven.plugin.index.generator.LandingPageGeneratorFactory;
import com.avojak.mojo.aws.p2.maven.plugin.s3.AmazonS3ClientFactory;
import com.avojak.mojo.aws.p2.maven.plugin.s3.exception.BucketDoesNotExistException;
import com.avojak.mojo.aws.p2.maven.plugin.s3.model.BucketPath;
import com.avojak.mojo.aws.p2.maven.plugin.s3.model.trie.Trie;
import com.avojak.mojo.aws.p2.maven.plugin.s3.repository.S3BucketRepository;
import com.avojak.mojo.aws.p2.maven.plugin.s3.repository.S3BucketRepositoryFactory;
import com.avojak.mojo.aws.p2.maven.plugin.util.resource.ResourceUtil;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * Deploys a p2 update site to an AWS S3 bucket.
 *
 * @author Andrew Vojak
 */
@Mojo(name = "deploy", defaultPhase = LifecyclePhase.DEPLOY, requiresOnline = true)
public class AWSP2Mojo extends AbstractMojo {

	private static final String REPOSITORY_DIR = "repository";
	private static final String SNAPSHOT_QUALIFIER = "-SNAPSHOT";
	private static final String SNAPSHOT_DIR = "snapshots";
	private static final String RELEASE_DIR = "releases";

	private static final Logger LOGGER = LoggerFactory.getLogger(AWSP2Mojo.class);

	private final S3BucketRepositoryFactory repositoryFactory;
	private final LandingPageGeneratorFactory landingPageGeneratorFactory;

	/**
	 * The name of the S3 bucket to host the p2 site.
	 * <p>
	 * <em>This value is required.</em>
	 */
	@Parameter(name = "bucket", property = "aws-p2.bucket", required = true)
	private String bucket;

	/**
	 * Whether or not to deploy snapshot sites. The default value is {@code true}.
	 */
	@Parameter(name = "deploySnapshots", property = "aws-p2.deploySnapshots", defaultValue = "true")
	private boolean deploySnapshots;

	/**
	 * Whether or not to skip execution. The default value is {@code false}.
	 */
	@Parameter(name = "skip", property = "aws-p2.skip", defaultValue = "false")
	private boolean skip;

	/**
	 * Whether or not to write a web-accessible landing page for the update site. If {@code true}, the HTML landing page
	 * will be created and uploaded into the root of the update site.
	 */
	@Parameter(name = "generateLandingPage", property = "aws-p2.generateLandingPage", defaultValue = "false")
	private boolean generateLandingPage;

	/**
	 * The project name, which will be the top level directory where the repository will be placed. The default location
	 * is:
	 * <pre>
	 *     ${project.name}
	 * </pre>
	 */
	@Parameter(name = "projectName", property = "aws-p2.projectName", defaultValue = "${project.name}")
	private String projectName;

	/**
	 * The top level output directory of the build. The default value is:
	 * <pre>
	 *     ${project.build.directory}
	 * </pre>
	 * <em>This value is not configurable by consumers.</em>
	 */
	@Parameter(defaultValue = "${project.build.directory}", readonly = true)
	private File outputDirectory;

	/**
	 * The Maven project. The default value is:
	 * <pre>
	 *     ${project}
	 * </pre>
	 * <em>This value is not configurable by consumers.</em>
	 */
	@Parameter(defaultValue = "${project}", readonly = true)
	private MavenProject project;

	// TODO: Allow additional metadata for putObject call?

	/**
	 * Default constructor invoked at runtime.
	 */
	public AWSP2Mojo() {
		this(new S3BucketRepositoryFactory(new AmazonS3ClientFactory().create()), new LandingPageGeneratorFactory());
	}

	/**
	 * Constructor.
	 * <p>
	 * <em>Package-private scoped for testing purposes.</em>
	 *
	 * @param repositoryFactory
	 * 		The {@link S3BucketRepositoryFactory}.
	 * @param landingPageGeneratorFactory
	 * 		The {@link LandingPageGeneratorFactory}.
	 */
	AWSP2Mojo(final S3BucketRepositoryFactory repositoryFactory,
			  final LandingPageGeneratorFactory landingPageGeneratorFactory) {
		this.repositoryFactory = repositoryFactory;
		this.landingPageGeneratorFactory = landingPageGeneratorFactory;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void execute() throws MojoFailureException {
		if (skip) {
			LOGGER.info(ResourceUtil.getString(getClass(), "info.skippingExecution"));
			return;
		}
		final boolean isSnapshotVersion = isSnapshotVersion(project.getVersion());
		if (isSnapshotVersion && !deploySnapshots) {
			LOGGER.info(ResourceUtil.getString(getClass(), "info.skippingSnapshot"));
			return;
		}

		final S3BucketRepository repository;
		try {
			repository = repositoryFactory.create(bucket);
		} catch (final BucketDoesNotExistException e) {
			throw new MojoFailureException("The specified bucket does not exist", e);
		}

		final File repositoryDirectory = new File(outputDirectory, REPOSITORY_DIR);
		final BucketPath destination = new BucketPath();

		if (projectName == null || projectName.trim().isEmpty()) {
			throw new MojoFailureException("Project name has not been specified");
		}
		destination.append(projectName)
				.append(isSnapshotVersion ? SNAPSHOT_DIR : RELEASE_DIR)
				.append(project.getVersion());

		repository.deleteDirectory(destination.asString());
		final Trie<String, String> content = repository.uploadDirectory(repositoryDirectory, destination);
		// TODO: Log a message before this
		content.log();

		// Generate an HTML landing page if specified
		if (generateLandingPage) {
			try {
				final BucketPath landingPageDestination = new BucketPath(destination).append("index.html");
				final LandingPageGenerator landingPageGenerator = landingPageGeneratorFactory.create();
				final File index = landingPageGenerator.generate(bucket, project.getArtifactId(), content, new Date());
				repository.uploadFile(index, landingPageDestination);
			} catch (IOException e) {
				throw new MojoFailureException("Unable to generate landing page", e);
			}
		}

		final String url = repository.getHostingUrl(destination.asString());
		LOGGER.info(ResourceUtil.getString(getClass(), "info.uploadComplete"), url);
	}

	/**
	 * Checks whether or not a version is a snapshot version. From Maven documentation, a version is a snapshot version
	 * if it contains the qualifier "-SNAPSHOT".
	 */
	private boolean isSnapshotVersion(final String version) {
		return version != null && version.trim().endsWith(SNAPSHOT_QUALIFIER);
	}

	/**
	 * Sets the Maven project.
	 * <p>
	 * <em>Package-private scoped for testing purposes.</em>
	 * </p>
	 *
	 * @param project
	 * 		The {@link MavenProject}.
	 */
	protected void setProject(final MavenProject project) {
		this.project = project;
	}

	/**
	 * Sets the bucket name.
	 * <p>
	 * <em>Package-private scoped for testing purposes.</em>
	 * </p>
	 *
	 * @param bucket
	 * 		The bucket name.
	 */
	protected void setBucket(final String bucket) {
		this.bucket = bucket;
	}

	/**
	 * Sets the deploy snapshots flag.
	 * <p>
	 * <em>Package-private scoped for testing purposes.</em>
	 * </p>
	 *
	 * @param deploySnapshots
	 * 		The deploy snapshots flag.
	 */
	protected void setDeploySnapshots(final boolean deploySnapshots) {
		this.deploySnapshots = deploySnapshots;
	}

	/**
	 * Sets the skip execution flag.
	 * <p>
	 * <em>Package-private scoped for testing purposes.</em>
	 * </p>
	 *
	 * @param skip
	 * 		The skip execution flag.
	 */
	protected void setSkip(final boolean skip) {
		this.skip = skip;
	}

	/**
	 * Sets the project name.
	 * <p>
	 * <em>Package-private scoped for testing purposes.</em>
	 * </p>
	 *
	 * @param projectName
	 * 		The project name.
	 */
	protected void setProjectName(final String projectName) {
		this.projectName = projectName;
	}

	/**
	 * Sets the write landing page flag.
	 * <p>
	 * <em>Package-private scoped for testing purposes.</em>
	 * </p>
	 *
	 * @param generateLandingPage
	 * 		The write landing page flag.
	 */
	protected void setGenerateLandingPage(final boolean generateLandingPage) {
		this.generateLandingPage = generateLandingPage;
	}

	/**
	 * Sets the output directory.
	 * <p>
	 * <em>Package-private scoped for testing purposes.</em>
	 * </p>
	 *
	 * @param outputDirectory
	 * 		The output directory {@link File}.
	 */
	protected void setOutputDirectory(final File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

}
