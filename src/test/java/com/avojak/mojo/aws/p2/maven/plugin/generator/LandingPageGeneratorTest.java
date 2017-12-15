package com.avojak.mojo.aws.p2.maven.plugin.generator;

import com.google.common.escape.Escaper;
import com.google.common.io.Resources;
import org.apache.commons.codec.Charsets;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Test class for {@link LandingPageGenerator}.
 */
@RunWith(MockitoJUnitRunner.class)
public class LandingPageGeneratorTest {

	@Mock
	private Escaper escaper;

	private final String bucketName = "mockBucket";
	private final String projectName = "mockProject";
	private final Date date = new Date();

	private LandingPageGenerator landingPageGenerator;

	/**
	 * Setup mocks.
	 */
	@Before
	public void setup() {
		landingPageGenerator = new LandingPageGenerator(escaper);
		when(escaper.escape(bucketName)).thenReturn(bucketName);
		when(escaper.escape(projectName)).thenReturn(projectName);
	}

	/**
	 * Tests that the constructor throws an exception when the given {@link Escaper} is {@code null}.
	 */
	@Test(expected = NullPointerException.class)
	public void testConstructor_NullEscaper() {
		new LandingPageGenerator(null);
	}

	/**
	 * Tests that {@link LandingPageGenerator#generate(String, String, Date)} throws an exception when the given bucket
	 * name is {@code null}.
	 *
	 * @throws IOException unexpected.
	 */
	@Test(expected = NullPointerException.class)
	public void testGenerate_NullBucketName() throws IOException {
		landingPageGenerator.generate(null, projectName, date);
	}

	/**
	 * Tests that {@link LandingPageGenerator#generate(String, String, Date)} throws an exception when the given bucket
	 * name is empty.
	 *
	 * @throws IOException unexpected.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testGenerate_EmptyBucketName() throws IOException {
		landingPageGenerator.generate(" ", projectName, date);
	}

	/**
	 * Tests that {@link LandingPageGenerator#generate(String, String, Date)} throws an exception when the given project
	 * name is {@code null}.
	 *
	 * @throws IOException unexpected.
	 */
	@Test(expected = NullPointerException.class)
	public void testGenerate_NullProjectName() throws IOException {
		landingPageGenerator.generate(bucketName, null, date);
	}

	/**
	 * Tests that {@link LandingPageGenerator#generate(String, String, Date)} throws an exception when the given project
	 * name is empty.
	 *
	 * @throws IOException unexpected.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testGenerate_EmptyProjectName() throws IOException {
		landingPageGenerator.generate(bucketName, " ", date);
	}

	/**
	 * Tests that {@link LandingPageGenerator#generate(String, String, Date)} throws an exception when the given date is
	 * {@code null}.
	 *
	 * @throws IOException unexpected.
	 */
	@Test(expected = NullPointerException.class)
	public void testGenerate_NullDate() throws IOException {
		landingPageGenerator.generate(bucketName, projectName, null);
	}

	/**
	 * Tests {@link LandingPageGenerator#generate(String, String, Date)}
	 *
	 * @throws IOException unexpected.
	 */
	@Test
	public void testGenerate() throws IOException {
		final String lineSeparator = System.lineSeparator();
		final String expectedContent = new StringBuilder("<!DOCTYPE html>").append(lineSeparator)
				.append("<html>").append(lineSeparator)
				.append("    <head>").append(lineSeparator)
				.append("        <meta charset=\"utf-8\">").append(lineSeparator)
				.append("        <title>mockBucket</title>").append(lineSeparator)
				.append("        <style>").append(lineSeparator)
				.append("            footer {").append(lineSeparator)
				.append("                font-size: small;").append(lineSeparator)
				.append("            }").append(lineSeparator)
				.append("            header {").append(lineSeparator)
				.append("                background-color: #27AE60;").append(lineSeparator)
				.append("            }").append(lineSeparator)
				.append("            main {").append(lineSeparator)
				.append("                margin-top: 50px;").append(lineSeparator)
				.append("                margin-bottom: 50px;").append(lineSeparator)
				.append("            }").append(lineSeparator)
				.append("            #banner {").append(lineSeparator)
				.append("                height: 90px;").append(lineSeparator)
				.append("                line-height: 90px;").append(lineSeparator)
				.append("                margin-left: 10px;").append(lineSeparator)
				.append("                color: #FFF;").append(lineSeparator)
				.append("            }").append(lineSeparator)
				.append("        </style>").append(lineSeparator)
				.append("    </head>").append(lineSeparator)
				.append("    <body>").append(lineSeparator)
				.append("        <header>").append(lineSeparator)
				.append("            <div id=\"banner\">").append(lineSeparator)
				.append("                <h1>mockBucket Eclipse software repository</h1>").append(lineSeparator)
				.append("            </div>").append(lineSeparator)
				.append("        </header>").append(lineSeparator)
				.append("        <main>").append(lineSeparator)
				.append("            <p>This URL is an Eclipse software repository for mockProject, and must be used in ")
				.append("Eclipse (<a href=http://help.eclipse.org/topic/org.eclipse.platform.doc.user/tasks/tasks-127.htm>")
				.append("See how</a>)</p>").append(lineSeparator)
				.append("        </main>").append(lineSeparator)
				.append("        <hr>").append(lineSeparator)
				.append("        <footer>").append(lineSeparator)
				.append("            <p><i>Generated with <a href=\"https://github.com/avojak/aws-p2-maven-plugin\">")
				.append("AWS p2 Maven Plugin</a> by <a href=\"https://avojak.com\">avojak</a>. ")
				.append(DateFormat.getDateTimeInstance().format(date))
				.append("</i></p>").append(lineSeparator)
				.append("        </footer>").append(lineSeparator)
				.append("    </body>").append(lineSeparator)
				.append("</html>")
				.toString();
		File file = null;
		try {
			file = landingPageGenerator.generate(bucketName, projectName, date);
			final String content = readFileAsString(file);
			assertEquals(expectedContent, content);
		} finally {
			if (file != null) {
				file.delete();
			}
		}
	}

	private String readFileAsString(final File file) throws IOException {
		return Resources.toString(file.toURI().toURL(), Charsets.UTF_8);
	}

}