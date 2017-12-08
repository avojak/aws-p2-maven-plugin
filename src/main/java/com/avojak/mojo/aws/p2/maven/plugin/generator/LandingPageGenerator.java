package com.avojak.mojo.aws.p2.maven.plugin.s3.generator;

import com.avojak.mojo.aws.p2.maven.plugin.resource.ResourceUtil;
import com.google.common.collect.Sets;
import com.google.common.escape.Escaper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.text.MessageFormat;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class LandingPageGenerator {

	private final Escaper escaper;

	public LandingPageGenerator(final Escaper escaper) {
		this.escaper = checkNotNull(escaper, "escaper cannot be null");
	}

	public File generate(final String projectName) throws IOException {
		checkNotNull(projectName, "projectName cannot be null");
		checkArgument(!projectName.trim().isEmpty(), "projectName cannot be empty");
		final String escapedProjectName = escaper.escape(projectName);
		writeContentsToFile(createFile(), createFileContents(escapedProjectName));

		return null;
	}

	private File createFile() throws IOException {
		final Set<PosixFilePermission> permissions = Sets.newHashSet(PosixFilePermission.OWNER_READ);
		final FileAttribute attribute = PosixFilePermissions.asFileAttribute(permissions);
		return Files.createTempFile("index", "html", attribute).toFile();
	}

	private String createFileContents(final String projectName) {
		final String howToURL = ResourceUtil.getString(getClass(), "howToURL");
		final String seeHow = ResourceUtil.getString(getClass(), "seeHow");
		final String seeHowHTML = "<a href=" + howToURL + ">" + seeHow + "</a>";
		final String landingPageFormat = ResourceUtil.getString(getClass(), "landingPageMessage");
		final String message = MessageFormat.format(landingPageFormat, projectName, seeHowHTML);
		return new StringBuilder("<!DOCTYPE html>\n")
				.append("<html>\n")
				.append("\t<head>\n")
				.append("\t\t<meta charset=\"utf-8\">\n")
				.append("\t\t<title>")
				.append(projectName)
				.append("</title>\n")
				.append("\t</head>\n")
				.append("\t<body>\n")
				.append("\t\t<p>")
				.append(message)
				.append("</p>\n")
				.append("\t</body>\n")
				.append("</html>\n").toString();
	}

	private void writeContentsToFile(final File file, final String contents) throws IOException {
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(file);
			fileWriter.write(contents);
		} finally {
			if (fileWriter != null) {
				fileWriter.close();
			}
		}
	}

}
