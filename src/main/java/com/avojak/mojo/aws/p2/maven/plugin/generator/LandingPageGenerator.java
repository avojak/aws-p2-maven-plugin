package com.avojak.mojo.aws.p2.maven.plugin.generator;

import com.avojak.mojo.aws.p2.maven.plugin.resource.ResourceUtil;
import com.google.common.escape.Escaper;
import com.google.common.io.Resources;
import org.apache.commons.codec.Charsets;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Class to generate HTML landing pages for a repository.
 */
public class LandingPageGenerator {

	private static final String TITLE_PLACEHOLDER = "{{title}}";
	private static final String BANNER_PLACEHOLDER = "{{banner}}";
	private static final String MESSAGE_PLACEHOLDER = "{{message}}";
	private static final String TIMESTAMP_PLACEHOLDER = "{{timestamp}}";

	private final Escaper escaper;

	/**
	 * Constructor.
	 *
	 * @param escaper The {@link Escaper}. Cannot be {@code null}.
	 */
	public LandingPageGenerator(final Escaper escaper) {
		this.escaper = checkNotNull(escaper, "escaper cannot be null");
	}

	/**
	 * Generates the landing page HTML file.
	 *
	 * @param bucket      The bucket name. Cannot be null or empty.
	 * @param projectName The project name. Cannot be null or empty.
	 *
	 * @return The non-null landing page {@link File}.
	 *
	 * @throws IOException if an {@link IOException} occurs.
	 */
	public File generate(final String bucket, final String projectName, final Date date) throws IOException {
		checkNotNull(bucket, "bucket cannot be null");
		checkArgument(!bucket.trim().isEmpty(), "bucket cannot be empty");
		checkNotNull(projectName, "projectName cannot be null");
		checkArgument(!projectName.trim().isEmpty(), "projectName cannot be empty");
		checkNotNull(date, "date cannot be null");

		final String escapedBucketName = escaper.escape(bucket);
		final String escapedProjectName = escaper.escape(projectName);
		final String template = readTemplateFileAsString();
		final String fileContents = createFileContents(template, escapedBucketName, escapedProjectName, date);

		return writeContentsToFile(createEmptyFile(), fileContents);
	}

	/**
	 * Creates the empty landing page HTML file.
	 *
	 * @return The landing page {@link File}.
	 *
	 * @throws IOException if an {@link IOException} occurs.
	 */
	private File createEmptyFile() throws IOException {
		return Files.createTempFile("index", ".html").toFile();
	}

	/**
	 * Reads the HTML template file as a String.
	 *
	 * @return The HTML template file as a String.
	 *
	 * @throws IOException if the template file cannot be found, or if an {@link IOException} occurs.
	 */
	private String readTemplateFileAsString() throws IOException {
		final String templateFilename = ResourceUtil.getString(getClass(), "templateFile");
		final String template = "html/" + templateFilename;
		return Resources.toString(Resources.getResource(template), Charsets.UTF_8);
	}

	/**
	 * Creates the String content to be written to the file.
	 *
	 * @param template    The HTML template as a String.
	 * @param bucketName  The bucket name.
	 * @param projectName The project name.
	 * @param date        The date of the build.
	 *
	 * @return The HTML content as a String.
	 */
	private String createFileContents(final String template, final String bucketName, final String projectName, final Date date) {
		final String howToURL = ResourceUtil.getString(getClass(), "howToURL");
		final String seeHow = ResourceUtil.getString(getClass(), "seeHow");
		final String seeHowHTML = "<a href=" + howToURL + ">" + seeHow + "</a>";
		final String landingPageFormat = ResourceUtil.getString(getClass(), "landingPageMessage");
		final String message = MessageFormat.format(landingPageFormat, projectName, seeHowHTML);
		final String bannerFormat = ResourceUtil.getString(getClass(), "bannerFormat");
		final String banner = MessageFormat.format(bannerFormat, bucketName);
		final String timestamp = DateFormat.getDateTimeInstance().format(date);

		return template.replace(TITLE_PLACEHOLDER, bucketName)
				.replace(BANNER_PLACEHOLDER, banner)
				.replace(MESSAGE_PLACEHOLDER, message)
				.replace(TIMESTAMP_PLACEHOLDER, timestamp);
	}

	/**
	 * Writes the String content to the {@link File}.
	 *
	 * @param file     The file to write content to.
	 * @param contents The String contents.
	 *
	 * @return The {@link File}.
	 *
	 * @throws IOException if an {@link IOException} occurs.
	 */
	private File writeContentsToFile(final File file, final String contents) throws IOException {
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(file);
			fileWriter.write(contents);
		} finally {
			if (fileWriter != null) {
				fileWriter.close();
			}
		}
		return file;
	}

}