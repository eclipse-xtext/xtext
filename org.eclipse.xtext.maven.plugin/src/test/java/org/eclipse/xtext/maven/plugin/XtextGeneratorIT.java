/*******************************************************************************
 * Copyright (c) 2018 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.maven.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.apache.maven.shared.utils.cli.CommandLineUtils;
import org.apache.maven.shared.utils.io.FileUtils;
import org.eclipse.xtext.generator.trace.AbstractTraceRegion;
import org.eclipse.xtext.generator.trace.TraceRegionSerializer;
import org.eclipse.xtext.maven.trace.ClassFileDebugSourceExtractor;
import org.eclipse.xtext.util.JavaRuntimeVersion;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.base.Joiner;

public class XtextGeneratorIT {

	private final static boolean debug = Boolean.getBoolean("xtext.it.tests.debug");
	private final static boolean fork = !debug;

	private static String ROOT = "/it/generate";
	private static File testDir;

	/**
	 * As soon as a {@link Verifier} object is created System.out and System.err are
	 * captured by the verifier; we save the original System.out here so that we can
	 * print information to the console later.
	 */
	private static PrintStream systemOut;

	/**
	 * This is the local Maven repository (typically ~/.m2/repository) that we
	 * will pass to the parent POM of IT projects through the property "local-central".
	 * The idea is to use our local Maven repository cache that we filled up to now as a
	 * "remote" Maven repository when building our IT projects, to speed-up the tests.
	 * Inspired by https://maven.apache.org/plugins/maven-invoker-plugin/examples/fast-use.html
	 */
	private static File localRepoDir;

	@BeforeClass
	static public void setUpOnce() throws IOException, VerificationException {
		systemOut = System.out;
		testDir = extractTestRoot();
		if (fork) {
			systemOut.println("Current maven.home=" + System.getProperty("maven.home"));
			File mvnExecutable = new File(new Verifier(testDir.getAbsolutePath()).getExecutable());
			if (!mvnExecutable.exists()) {
				String mavenHome = findMaven();
				if (mavenHome != null) {
					System.setProperty("maven.home", mavenHome);
					systemOut.println("Detected maven.home=" + System.getProperty("maven.home"));
				} else {
					Assert.fail("Maven home not found. Tried to call '" + mvnExecutable
							+ "'.\nConsider to set the envVar 'M2_HOME' or System property 'maven.home'.\n"
							+ "Current settings are: maven.home=" + System.getProperty("maven.home") + " M2_HOME="
							+ CommandLineUtils.getSystemEnvVars().getProperty("M2_HOME"));
				}
			} else {
				systemOut.println("Maven executable: " + mvnExecutable);
			}
		}
		verifyErrorFreeLog("" /*root*/, false, "clean", "install");
	}

	private static File extractTestRoot() throws IOException {
		// it's best to pass a subdirectory of the target folder for "maven.test.tmpdir":
		// that will be the directory where our IT projects will be copied and where
		// the Maven build will be executed.
		// This will make their inspection afterward (the log.txt file) easier.
		// For this reason, we delete that directory here, but NOT after the tests.
		String tempDirPath = System.getProperty("maven.test.tmpdir", System.getProperty("java.io.tmpdir"));
		File tempDir = new File(tempDirPath);
		File testDir = new File(tempDir, ROOT);
		FileUtils.deleteDirectory(testDir);
		String localCentralRepository = System.getProperty("maven.repo.local",
				System.getProperty("user.home") + "/.m2/repository");
		localRepoDir = new File( localCentralRepository );
		systemOut.println("IT projects will be executed from " + testDir);
		systemOut.println("Local Maven Central Repository " + localRepoDir);
		testDir = ResourceExtractor.extractResourcePath(XtextGeneratorIT.class, ROOT, tempDir, true);
		return testDir;
	}

	private static String findMaven() {
		// TODO add more mavens here
		String[] mavens = new String[] { System.getProperty("maven.home"),
				"/opt/local/share/java/maven3/",
				"/usr/local/", "/usr/",
				"/opt/homebrew/"};
		for (String maven : mavens) {
			if (new File(maven + "/bin/mvn").exists()) {
				return maven;
			}
		}
		return null;
	}

	@Test
	public void simpleLang() throws Exception {
		Verifier verifier = verifyErrorFreeLog("simple-lang");
		verifier.verifyFileContentMatches(verifier.getBasedir() + "/src-gen/RefModel.nojdt.txt", "People to greet\\: Test");
	}

	@Test
	public void simpleLangWithPluginDependencies() throws Exception {
		Verifier verifier = verifyErrorFreeLog("simple-lang-plugin-dependencies");
		verifier.verifyFileContentMatches(verifier.getBasedir() +
			"/simple-lang-model/src-gen/Model.nojdt.txt", "People to greet\\: maven");
		verifier.verifyFileContentMatches(verifier.getBasedir() +
			"/simple-lang-ref-model/src-gen/RefModel.nojdt.txt", "People to greet\\: Test");
	}

	@Test
	public void simpleLangIncremental() throws Exception {
		String project = "simple-lang-incremental";
		Verifier verifier = verifyErrorFreeLog(project);
		String modelOutput = verifier.getBasedir() + "/src-gen/Model.nojdt.txt";
		String refModelOutput = verifier.getBasedir() + "/src-gen/RefModel.nojdt.txt";

		verifier.verifyFileContentMatches(modelOutput, "@\\d+\\RPeople to greet\\: maven");
		verifier.verifyFileContentMatches(refModelOutput, "@\\d+\\RPeople to greet\\: Test");

		String modelOutputContent = FileUtils.fileRead(modelOutput);
		String refModelOutputContent = FileUtils.fileRead(refModelOutput);

		verifier = verifyErrorFreeLog(project, false, "verify");
		verifier.verifyFileContentMatches(modelOutput, Pattern.quote(modelOutputContent));
		verifier.verifyFileContentMatches(refModelOutput, Pattern.quote(refModelOutputContent));
		// Modify output file
		FileUtils.fileWrite(refModelOutput, refModelOutputContent + " // changed");

		verifier = verifyErrorFreeLog(project, false, "verify");
		verifier.verifyFileContentMatches(modelOutput, Pattern.quote(modelOutputContent));
		String updatedRefModelOutputContent = FileUtils.fileRead(refModelOutput);
		assertFalse(refModelOutputContent.equals(updatedRefModelOutputContent));
		assertFalse(updatedRefModelOutputContent.endsWith("// changed"));

		String modelFile = verifier.getBasedir() + "/src/main/java/Model.nojdt";
		String model = FileUtils.fileRead(modelFile);
		FileUtils.fileWrite(modelFile, model + " // changed");
		verifier = verifyErrorFreeLog(project, false, "verify");
		modelOutputContent = FileUtils.fileRead(modelOutput);
		verifier.verifyFileContentMatches(refModelOutput, Pattern.quote(updatedRefModelOutputContent));

		String refModelFile = verifier.getBasedir() + "/src/main/java/RefModel.nojdt";
		FileUtils.fileDelete(refModelFile);
		verifier = verifyErrorFreeLog(project, false, "verify");
		verifier.verifyFileNotPresent(refModelOutput);

		FileUtils.fileWrite(refModelFile, "Hello Test (from maven)!");
		verifier = verifyErrorFreeLog(project, false, "verify");
		verifier.verifyFileContentMatches(refModelOutput, "People to greet\\: Test");
		verifier.verifyFileContentMatches(modelOutput, Pattern.quote(modelOutputContent));
	}
	
	@Test
	public void simpleLangWriteStorageResources() throws Exception {
		String project = "simple-lang-write-storage-resources";
		Verifier verifier = verifyErrorFreeLog(project);
		verifier.verifyFileContentMatches(verifier.getBasedir() + "/src-gen/Model.nojdt.txt",
				"People to greet\\: maven");
		verifier.verifyFileContentMatches(verifier.getBasedir() + "/src-gen/RefModel.nojdt.txt",
				"People to greet\\: Test");
		
		verifier.verifyFilePresent(verifier.getBasedir() + "/src-gen/.Model.nojdtbin");
		verifier.verifyFilePresent(verifier.getBasedir() + "/src-gen/.RefModel.nojdtbin");
	}


	@Test
	public void mavenConfiguration() throws Exception {
		Verifier verifier = verifyErrorFreeLog("maven-config");
		verifier.verifyFileContentMatches(verifier.getBasedir() + "/model2-output/Model.nojdt.txt",
				"People to greet\\: maven2");
		verifier.verifyFilePresent(verifier.getBasedir() + "/model-output/IntegrationTestXbase.java");
	}

	@Test
	public void purexbase() throws Exception {
		Verifier verifier = verifyErrorFreeLog("purexbase");
		verifier.verifyFilePresent(verifier.getBasedir() + "/src-gen/IntegrationTestXbase.java");
		verifier.verifyFilePresent(verifier.getBasedir() + "/target/xtext-temp/stub-classes/IntegrationTestXbase.class");
	}

	@Test
	public void clustering() throws Exception {
		Verifier verifier = verifyErrorFreeLog("clustering");
		verifier.verifyFilePresent(verifier.getBasedir() + "/src-gen/IntegrationTestXbase.java");
		verifier.verifyFilePresent(verifier.getBasedir() + "/src-gen/IntegrationTestXbase2.java");
		verifier.verifyFilePresent(verifier.getBasedir() + "/target/xtext-temp/stub-classes/IntegrationTestXbase.class");
		verifier.verifyFilePresent(verifier.getBasedir() + "/target/xtext-temp/stub-classes/IntegrationTestXbase2.class");
	}

	@Test
	public void outputPerGoal() throws Exception {
		Verifier verifier = verifyErrorFreeLog("output-per-goal");
		verifier.verifyFilePresent(verifier.getBasedir() + "/src-gen/SimpleClassXbase.java");
		verifier.verifyFilePresent(verifier.getBasedir() + "/target/xtext-temp/stub-classes/SimpleClassXbase.class");
		verifier.verifyFilePresent(verifier.getBasedir() + "/src-test-gen/SimpleTestClassXbase.java");
		verifier.verifyFilePresent(verifier.getBasedir() + "/target/xtext-temp-test/stub-classes/SimpleTestClassXbase.class");
	}

	@Test
	public void outputPerSource() throws Exception {
		Verifier verifier = verifyErrorFreeLog("output-per-source");
		verifier.verifyFilePresent(verifier.getBasedir() + "/src-gen/IntegrationTestXbase.java");
		verifier.verifyFilePresent(verifier.getBasedir() + "/target/xtext-temp/stub-classes/IntegrationTestXbase.class");
		verifier.verifyFilePresent(verifier.getBasedir() + "/other-gen/OtherIntegrationTestXbase.java");
		verifier.verifyFilePresent(
				verifier.getBasedir() + "/target/xtext-temp/stub-classes/OtherIntegrationTestXbase.class");
	}

	@Test
	public void javaLangBiRef() throws Exception {
		Verifier verifier = verifyErrorFreeLog("java-lang-bi-ref");
		verifier.verifyFilePresent(verifier.getBasedir() + "/src-gen/XbaseReferToJava.java");
		verifier.verifyFilePresent(verifier.getBasedir() + "/target/xtext-temp/stub-classes/XbaseReferToJava.class");
		verifier.verifyFilePresent(verifier.getBasedir() + "/target/xtext-temp/stub-classes/JavaClazz.class");
		verifier.verifyFilePresent(verifier.getBasedir() + "/target/xtext-temp/stub-classes/JavaRecord.class");
	}

	@Test
	public void javaLang21BiRef() throws Exception {
		Assume.assumeTrue("Active only on Java 21 and later", JavaRuntimeVersion.isJava21OrLater());
		Verifier verifier = verifyErrorFreeLog("java-lang-21-bi-ref");
		verifier.verifyFilePresent(verifier.getBasedir() + "/src-gen/XbaseReferToJava.java");
		verifier.verifyFilePresent(verifier.getBasedir() + "/target/xtext-temp/stub-classes/XbaseReferToJava.class");
		verifier.verifyFilePresent(verifier.getBasedir() + "/target/xtext-temp/stub-classes/JavaRecord.class");
	}

	@Test
	public void aggregation() throws Exception {
		Verifier verifier = verifyErrorFreeLog("aggregate");
		verifier.verifyFilePresent(verifier.getBasedir() + "/purexbase/src-gen/IntegrationTestXbase.java");
		verifier.verifyFilePresent(
				verifier.getBasedir() + "/purexbase/target/xtext-temp/stub-classes/IntegrationTestXbase.class");
	}

	@Test
	public void xcore() throws Exception {
		Verifier verifier = verifyErrorFreeLog("xcore-lang", false, "clean", "verify");
		verifier.verifyFilePresent(verifier.getBasedir() + "/src-gen/org/eclipse/xcoretest/MyClass2.java");
		verifier.verifyFilePresent(
				verifier.getBasedir() + "/target/xtext-temp/stub-classes/org/eclipse/xcoretest/MyClass2.class");
		verifier.verifyFileContentMatches(verifier.getBasedir() + "/src-gen/org/eclipse/xcoretest/MyEnum.java",
				"(?s).*MY_FIRST_LITERAL\\(-7.*MY_SECOND_LITERAL\\(137.*");
	}

	@Test
	public void xcoreMapping() throws Exception {
		verifyErrorFreeLog("xcore-mapping", false, "clean", "verify");
	}

	@Test
	public void xcoreAutoMapping() throws Exception {
		verifyErrorFreeLog("xcore-auto-mapping", false, "clean", "verify");
	}

	@Test
	public void bug463946() throws Exception {
		Verifier verifier = verifyErrorFreeLog("bug463946");
		verifier.verifyFilePresent(verifier.getBasedir() + "/src-gen/xcore/bug463946/pack/MyModel.java");
	}

	@Test
	public void traceFileIsGenerated() throws Exception {
		Verifier verifier = verifyErrorFreeLog("trace");
		verifier.verifyFilePresent(verifier.getBasedir() + "/src-gen/IntegrationTestXbase.java");
		verifier.verifyFilePresent(verifier.getBasedir() + "/src-gen/.IntegrationTestXbase.java._trace");

		String expectedSourceName = "IntegrationTestXbase.xbase";
		File classFile = new File(verifier.getBasedir() + "/target/classes/IntegrationTestXbase.class");

		assertTraceSourceFileName(expectedSourceName, classFile);
	}

	@Test
	public void locationInTraceFileIsRelative_issue_2957() throws Exception {
		Verifier verifier = verifyErrorFreeLog("trace-issue-2957");
		assertRelativeLocationInTraceFile("foo/bar/IntegrationTestXbase.xbase",
				verifier.getBasedir() + "/src-gen/.IntegrationTestXbase.java._trace");
		assertRelativeLocationInTraceFile("com/acme/OtherIntegrationTestXbase.xbase",
				verifier.getBasedir() + "/other-gen/.OtherIntegrationTestXbase.java._trace");
	}

	private void assertTraceSourceFileName(String expectedSourceName, File file) throws IOException {
		String sourceName = new ClassFileDebugSourceExtractor().getDebugSourceFileName(file);
		assertEquals("Source file name doesn't match", expectedSourceName, sourceName);
	}

	private void assertRelativeLocationInTraceFile(String expectedLocation, String traceFile1Path) throws IOException {
		String source = loadTraceSourcePath(traceFile1Path);
		assertEquals(expectedLocation, source);
	}

	private String loadTraceSourcePath(String traceFile1Path) throws IOException {
		try (FileInputStream in = new FileInputStream(traceFile1Path)) {
			AbstractTraceRegion region = new TraceRegionSerializer().readTraceRegionFrom(in);
			return region.getAssociatedSrcRelativePath().toString();
		}
	}

	private Verifier verifyErrorFreeLog(String pathToTestProject) throws IOException, VerificationException {
		return verifyErrorFreeLog(pathToTestProject, false, "clean", "verify");
	}

	private static Verifier verifyErrorFreeLog(String pathToTestProject, boolean updateSnapshots, String... goals)
			throws IOException, VerificationException {
		if (goals == null || goals.length < 1) {
			throw new IllegalArgumentException("You need to pass at least one goal to verify log");
		}
		Verifier verifier = newVerifier(pathToTestProject);
		if (updateSnapshots) {
			verifier.addCliOption("-U");
		} else {
			verifier.addCliOption("--no-snapshot-updates");
		}
		for (String goal : goals) {
			verifier.executeGoal(goal);
		}
		verifier.verifyErrorFreeLog();
		if (debug) {
			List<String> lines = verifier.loadFile(verifier.getBasedir(), verifier.getLogFileName(), false);
			systemOut.println(Joiner.on('\n').join(lines));
		}
		verifier.resetStreams();
		return verifier;
	}

	private static Verifier newVerifier(String pathToTestProject) throws IOException, VerificationException {
		File projectDir = pathToTestProject.isEmpty() ? testDir : new File(testDir, pathToTestProject);
		Verifier verifier = new Verifier(projectDir.getAbsolutePath(), true);
		verifier.setAutoclean(false);
		if (fork) {
			String mvnOpts = CommandLineUtils.getSystemEnvVars().getProperty("MAVEN_OPTS");
			String modMvnOpts = (mvnOpts != null ? mvnOpts + " " : "") + "-Xmx1g";
			verifier.setEnvironmentVariable("MAVEN_OPTS", modMvnOpts);
			if (debug) {
				systemOut.println("Modified Maven Opts: " + modMvnOpts);
			}
		} else {
			verifier.setForkJvm(false);
		}
		String jenkinsURL = System.getProperty("JENKINS_URL");
		if (jenkinsURL != null) {
			verifier.setSystemProperty("JENKINS_URL", jenkinsURL);
		}
		if (debug) {
			verifier.setMavenDebug(debug);
		}
		String testMavenRepo = System.getProperty("testMavenRepo");
		Assert.assertNotNull(
			"Required property 'testMavenRepo' is null!\n"
			+ "If you run these tests from Eclipse, first run the Maven launch\n"
			+ "'pre-integration-test.launch'\n"
			+ "then use the JUnit launch\n"
			+ "'Run ITs from Eclipse.launch'.",
			testMavenRepo);
		verifier.setLocalRepo(testMavenRepo);
		verifier.setSystemProperty("local-central", localRepoDir.toString());
		return verifier;
	}

}
