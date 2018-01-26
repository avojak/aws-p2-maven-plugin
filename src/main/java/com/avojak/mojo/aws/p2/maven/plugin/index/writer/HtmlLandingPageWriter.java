package com.avojak.mojo.aws.p2.maven.plugin.index.writer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Implementation of {@link LandingPageWriter} to write HTML files.
 */
public class HtmlLandingPageWriter implements LandingPageWriter {

	private static final String FILE_EXTENSION = ".html";

	@Override
	public File write(final String content, final String filename) throws IOException {
		checkNotNull(content, "content cannot be null");
		checkArgument(!content.trim().isEmpty(), "content cannot be empty");
		checkNotNull(filename, "filename cannot be null");
		checkArgument(!filename.trim().isEmpty(), "filename cannot be empty");

		return writeContentsToFile(createEmptyFile(filename), content);
	}

	/**
	 * Creates the empty landing page HTML file.
	 *
	 * @return The landing page {@link File}.
	 *
	 * @throws IOException If an {@link IOException} occurs.
	 */
	private File createEmptyFile(final String filename) throws IOException {
		return Files.createTempFile(filename, FILE_EXTENSION).toFile();
	}

	/**
	 * Writes the String content to the {@link File}.
	 *
	 * @param file     The file to write content to.
	 * @param contents The String contents.
	 *
	 * @return The non-{@code null} {@link File}.
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