/*******************************************************************************
 * Copyright (c) 2026 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.maven.plugin;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.maven.executor.ExecutorRequest;
import org.apache.maven.executor.ExecutorResult;
import org.apache.maven.executor.forked.ForkedMavenExecutor;
import org.junit.Assert;

/**
 * Runs the Maven IT projects with {@code maven-executor} (forked only) and
 * provides the file/log assertions the retired {@code maven-verifier} used to
 * offer.
 */
public final class MavenExecutorUtil {

	static final boolean debug = Boolean.getBoolean("xtext.it.tests.debug");

	static final String ROOT = "/it/generate";

	private static File localRepoDir;

	private MavenExecutorUtil() {
	}

	/** Result of one IT project directory plus the last Maven invocation on it. */
	public static final class MavenExecution {
		private final File basedir;

		private final boolean updateSnapshots;

		private String stdOut = "";

		private String stdErr = "";

		private boolean executed;

		private boolean succeeded;

		private MavenExecution(File basedir, boolean updateSnapshots) {
			this.basedir = basedir;
			this.updateSnapshots = updateSnapshots;
		}

		/** Absolute path of the IT project directory (replaces {@code Verifier.getBasedir()}). */
		public String getBasedir() {
			return basedir.getAbsolutePath();
		}

		public boolean success() {
			return succeeded;
		}

		/** Combined stdout+stderr of the last invocation, for failure diagnostics. */
		public String getLog() {
			return stdOut + stdErr;
		}

		/**
		 * Runs Maven once with the shared IT options plus the given goals, in the
		 * project directory. A failed build does NOT throw; inspect {@link #success()}
		 * or call {@link #verifyErrorFreeLog()}.
		 */
		public void executeGoal(String... goals) throws IOException {
			List<String> args = baseArguments(updateSnapshots);
			for (String goal : goals) {
				args.add(goal);
			}
			run(args);
		}

		private void run(List<String> args) throws IOException {
			Path mavenHome = resolveMavenHome();
			ExecutorRequest.Builder builder = ExecutorRequest.mavenBuilder()
					.cwd(basedir.toPath())
					.arguments(args)
					.grabOutputAsString(true);
			String mavenOpts = mavenOpts();
			if (!mavenOpts.isEmpty()) {
				builder.environmentVariable("MAVEN_OPTS", mavenOpts);
			}
			try (ForkedMavenExecutor executor = new ForkedMavenExecutor(mavenHome)) {
				ExecutorResult result = executor.execute(builder.build());
				stdOut = result.stdOutString().orElse("");
				stdErr = result.stdErrString().orElse("");
				succeeded = result.success();
				executed = true;
			} catch (Exception e) {
				throw new IOException("Maven execution failed in " + basedir + " with arguments " + args, e);
			}
			if (debug) {
				System.out.println("Maven output for " + basedir + ":\n" + getLog());
			}
		}

		/** Asserts the last invocation succeeded and neither stream contains {@code [ERROR]}. */
		public void verifyErrorFreeLog() {
			Assert.assertTrue("Maven invocation did not run in " + basedir, executed);
			Assert.assertTrue("Maven build failed in " + basedir + ":\n" + getLog(), succeeded);
			Assert.assertFalse("Found [ERROR] in Maven output for " + basedir + ":\n" + getLog(),
					stdOut.contains("[ERROR]") || stdErr.contains("[ERROR]"));
		}

		/** Asserts the last invocation's output contains the given text (stdout or stderr). */
		public void verifyTextInLog(String text) {
			Assert.assertTrue("Maven invocation did not run in " + basedir, executed);
			Assert.assertTrue(
					"Expected text '" + text + "' not found in Maven output for " + basedir + ":\n" + getLog(),
					stdOut.contains(text) || stdErr.contains(text));
		}

		public void verifyFilePresent(String path) {
			Assert.assertTrue("Expected file is missing: " + path, new File(path).exists());
		}

		public void verifyFileNotPresent(String path) {
			Assert.assertFalse("Unexpected file is present: " + path, new File(path).exists());
		}

		public void verifyFileContentMatches(String path, String regex) throws IOException {
			verifyFilePresent(path);
			String content = Files.readString(Path.of(path), StandardCharsets.UTF_8);
			Assert.assertTrue("Content of " + path + " does not match regex '" + regex + "' but was:\n" + content,
					Pattern.compile(regex, Pattern.DOTALL).matcher(content).find());
		}
	}

	static MavenExecution newVerifier(File testDir, String pathToTestProject, boolean updateSnapshots)
			throws IOException {
		File projectDir = pathToTestProject.isEmpty() ? testDir : new File(testDir, pathToTestProject);
		String testMavenRepo = System.getProperty("testMavenRepo");
		Assert.assertNotNull("Required property 'testMavenRepo' is null!\n"
				+ "If you run these tests from Eclipse, first run the Maven launch\n"
				+ "'pre-integration-test.launch'\n"
				+ "then use the JUnit launch\n"
				+ "'Run ITs from Eclipse.launch'.",
				testMavenRepo);
		if (localRepoDir == null) {
			String localCentralRepository = System.getProperty("maven.repo.local",
					System.getProperty("user.home") + "/.m2/repository");
			localRepoDir = new File(localCentralRepository);
		}
		return new MavenExecution(projectDir, updateSnapshots);
	}

	/** Shared CLI options for every IT invocation (mirrors the old verifier setup). */
	private static List<String> baseArguments(boolean updateSnapshots) {
		List<String> args = new ArrayList<>();
		args.add("-B");
		if (debug) {
			args.add("-X");
			args.add("-e");
		}
		if (updateSnapshots) {
			args.add("-U");
		} else {
			args.add("--no-snapshot-updates");
		}
		String testMavenRepo = System.getProperty("testMavenRepo");
		Assert.assertNotNull("Required property 'testMavenRepo' is null!", testMavenRepo);
		args.add("-Dmaven.repo.local=" + testMavenRepo);
		if (localRepoDir == null) {
			String localCentralRepository = System.getProperty("maven.repo.local",
					System.getProperty("user.home") + "/.m2/repository");
			localRepoDir = new File(localCentralRepository);
		}
		args.add("-Dlocal-central=" + localRepoDir);
		String jenkinsURL = System.getProperty("JENKINS_URL");
		if (jenkinsURL != null) {
			args.add("-DJENKINS_URL=" + jenkinsURL);
		}
		String testSettingsXML = System.getProperty("testSettingsXML");
		if (testSettingsXML != null) {
			args.add("-s");
			args.add(testSettingsXML);
		}
		return args;
	}

	private static String mavenOpts() {
		String mvnOpts = System.getenv("MAVEN_OPTS");
		return (mvnOpts != null ? mvnOpts + " " : "") + "-Xmx1g";
	}

	static File extractTestRoot() throws IOException {
		// it's best to pass a subdirectory of the target folder for "maven.test.tmpdir":
		// that will be the directory where our IT projects will be copied and where
		// the Maven build will be executed.
		// This will make their inspection afterward easier.
		// For this reason, we delete that directory here, but NOT after the tests.
		String tempDirPath = System.getProperty("maven.test.tmpdir", System.getProperty("java.io.tmpdir"));
		File tempDir = new File(tempDirPath);
		File testDir = new File(tempDir, ROOT);
		deleteDirectory(testDir);
		String localCentralRepository = System.getProperty("maven.repo.local",
				System.getProperty("user.home") + "/.m2/repository");
		localRepoDir = new File(localCentralRepository);
		System.out.println("IT projects will be executed from " + testDir);
		System.out.println("Local Maven Central Repository " + localRepoDir);
		extractResource(MavenExecutorUtil.class, ROOT, tempDir);
		return testDir;
	}

	static Path resolveMavenHome() {
		String mavenHome = System.getProperty("maven.home");
		if (mavenHome != null && isMavenHome(Path.of(mavenHome))) {
			return Path.of(mavenHome);
		}
		String m2Home = System.getenv("M2_HOME");
		if (m2Home != null && isMavenHome(Path.of(m2Home))) {
			return Path.of(m2Home);
		}
		String found = findMaven();
		if (found != null) {
			return Path.of(found);
		}
		// Let the executor fail with a meaningful message; keep the raw property if set.
		return mavenHome != null ? Path.of(mavenHome) : Path.of("maven-home-not-found");
	}

	static boolean isMavenHome(Path home) {
		return Files.isRegularFile(home.resolve("bin/mvn"))
				|| Files.isRegularFile(home.resolve("bin/mvn.cmd"));
	}

	static String findMaven() {
		// TODO add more mavens here
		String[] mavens = new String[] { System.getProperty("maven.home"),
				"/opt/local/share/java/maven3/",
				"/usr/local/", "/usr/",
				"/opt/homebrew/"};
		for (String maven : mavens) {
			if (maven != null && new File(maven + "/bin/mvn").exists()) {
				return maven;
			}
		}
		return null;
	}

	/**
	 * Copies a test resource from the classpath (e.g. {@code /it/generate})
	 * into {@code destParent}, preserving the resource path below it, and returns
	 * the copied file or directory. Replaces {@code ResourceExtractor.extractResourcePath}.
	 */
	static File extractResource(Class<?> anchor, String resourcePath, File destParent) throws IOException {
		URL url = anchor.getResource(resourcePath);
		Assert.assertNotNull("Test resource not found on classpath: " + resourcePath, url);
		final Path source;
		try {
			source = Path.of(url.toURI());
		} catch (URISyntaxException e) {
			throw new IOException("Cannot resolve test resource " + resourcePath, e);
		}
		String relative = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
		Path target = destParent.toPath().resolve(relative);
		if (Files.isDirectory(source)) {
			deleteDirectory(target.toFile());
			Files.createDirectories(target);
			Files.walkFileTree(source, new SimpleFileVisitor<>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					Files.createDirectories(target.resolve(source.relativize(dir).toString()));
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.copy(file, target.resolve(source.relativize(file).toString()),
							StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
					return FileVisitResult.CONTINUE;
				}
			});
		} else {
			Files.createDirectories(target.getParent());
			Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
		}
		return target.toFile();
	}

	private static void deleteDirectory(File dir) throws IOException {
		Path path = dir.toPath();
		if (!Files.exists(path)) {
			return;
		}
		Files.walkFileTree(path, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}
}
