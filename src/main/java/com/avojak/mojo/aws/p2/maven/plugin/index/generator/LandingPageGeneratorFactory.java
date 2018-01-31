package com.avojak.mojo.aws.p2.maven.plugin.index.generator;

import com.avojak.mojo.aws.p2.maven.plugin.index.formatter.HtmlLandingPageFormatter;
import com.avojak.mojo.aws.p2.maven.plugin.index.formatter.LandingPageFormatter;
import com.avojak.mojo.aws.p2.maven.plugin.index.writer.HtmlLandingPageWriter;
import com.avojak.mojo.aws.p2.maven.plugin.index.writer.LandingPageWriter;
import com.avojak.mojo.aws.p2.maven.plugin.util.file.FileFactory;
import com.avojak.mojo.aws.p2.maven.plugin.util.file.FileWriterFactory;
import com.google.common.escape.Escaper;
import com.google.common.html.HtmlEscapers;

import java.io.IOException;

/**
 * Factory class to create instances of {@link LandingPageGenerator}.
 */
public class LandingPageGeneratorFactory {

	/**
	 * Creates and returns a new instance of {@link LandingPageGenerator}.
	 *
	 * @return The new, non-{@code null} {@link LandingPageGenerator}.
	 *
	 * @throws IOException If an IO exception occurs.
	 */
	public LandingPageGenerator create() throws IOException {
		final Escaper escaper = HtmlEscapers.htmlEscaper();
		final LandingPageFormatter formatter = new HtmlLandingPageFormatter(escaper);
		final FileFactory fileFactory = new FileFactory();
		final FileWriterFactory fileWriterFactory = new FileWriterFactory();
		final LandingPageWriter writer = new HtmlLandingPageWriter(fileFactory, fileWriterFactory);
		return new LandingPageGenerator(formatter, writer);
	}

}
