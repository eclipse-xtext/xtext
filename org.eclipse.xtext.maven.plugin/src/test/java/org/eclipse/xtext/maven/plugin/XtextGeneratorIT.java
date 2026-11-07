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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import org.eclipse.xtext.generator.trace.AbstractTraceRegion;
import org.eclipse.xtext.generator.trace.TraceRegionSerializer;
import org.eclipse.xtext.maven.trace.ClassFileDebugSourceExtractor;
import org.eclipse.xtext.util.JavaRuntimeVersion;
import org.eclipse.xtext.maven.plugin.MavenExecutorUtil.MavenExecution;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

public class XtextGeneratorIT {

	private static File testDir;

	@BeforeClass
	static public void setUpOnce() throws IOException {
		testDir = MavenExecutorUtil.extractTestRoot();
		Path mavenHome = MavenExecutorUtil.resolveMavenHome();
		if (!MavenExecutorUtil.isMavenHome(mavenHome)) {
			Assert.fail("Maven home not found: '" + mavenHome + "'.\n"
					+ "Consider to set the envVar 'M2_HOME' or System property 'maven.home'.\n"
					+ "Current settings are: maven.home=" + System.getProperty("maven.home") + " M2_HOME="
					+ System.getenv("M2_HOME"));
		}
		System.out.println("Current maven.home=" + System.getProperty("maven.home"));
		System.out.println("Maven installation: " + mavenHome);
		verifyErrorFreeLog("" /*root*/, false, "clean", "install");
	}

	@Test
	public void simpleLang() throws Exception {
		MavenExecution verifier = verifyErrorFreeLog("simple-lang");
		verifier.verifyFileContentMatches(verifier.getBasedir() + "/src-gen/RefModel.nojdt.txt", "People to greet\\: Test");
	}

	@Test
	public void simpleLangWithPluginDependencies() throws Exception {
		MavenExecution verifier = verifyErrorFreeLog("simple-lang-plugin-dependencies");
		verifier.verifyFileContentMatches(verifier.getBasedir() +
			"/simple-lang-model/src-gen/Model.nojdt.txt", "People to greet\\: maven");
		verifier.verifyFileContentMatches(verifier.getBasedir() +
			"/simple-lang-ref-model/src-gen/RefModel.nojdt.txt", "People to greet\\: Test");
	}

	@Test
	public void simpleLangIncremental() throws Exception {
		String project = "simple-lang-incremental";
		MavenExecution verifier = verifyErrorFreeLog(project);
		String modelOutput = verifier.getBasedir() + "/src-gen/Model.nojdt.txt";
		String refModelOutput = verifier.getBasedir() + "/src-gen/RefModel.nojdt.txt";

		verifier.verifyFileContentMatches(modelOutput, "@\\d+\\RPeople to greet\\: maven");
		verifier.verifyFileContentMatches(refModelOutput, "@\\d+\\RPeople to greet\\: Test");

		String modelOutputContent = Files.readString(Path.of(modelOutput), StandardCharsets.UTF_8);
		String refModelOutputContent = Files.readString(Path.of(refModelOutput), StandardCharsets.UTF_8);

		verifier = verifyErrorFreeLog(project, false, "verify");
		verifier.verifyFileContentMatches(modelOutput, Pattern.quote(modelOutputContent));
		verifier.verifyFileContentMatches(refModelOutput, Pattern.quote(refModelOutputContent));
		// Modify output file
		Files.writeString(Path.of(refModelOutput), refModelOutputContent + " // changed", StandardCharsets.UTF_8);

		verifier = verifyErrorFreeLog(project, false, "verify");
		verifier.verifyFileContentMatches(modelOutput, Pattern.quote(modelOutputContent));
		String updatedRefModelOutputContent = Files.readString(Path.of(refModelOutput), StandardCharsets.UTF_8);
		assertFalse(refModelOutputContent.equals(updatedRefModelOutputContent));
		assertFalse(updatedRefModelOutputContent.endsWith("// changed"));

		String modelFile = verifier.getBasedir() + "/src/main/java/Model.nojdt";
		String model = Files.readString(Path.of(modelFile), StandardCharsets.UTF_8);
		Files.writeString(Path.of(modelFile), model + " // changed", StandardCharsets.UTF_8);
		verifier = verifyErrorFreeLog(project, false, "verify");
		modelOutputContent = Files.readString(Path.of(modelOutput), StandardCharsets.UTF_8);
		verifier.verifyFileContentMatches(refModelOutput, Pattern.quote(updatedRefModelOutputContent));

		String refModelFile = verifier.getBasedir() + "/src/main/java/RefModel.nojdt";
		Files.delete(Path.of(refModelFile));
		verifier = verifyErrorFreeLog(project, false, "verify");
		verifier.verifyFileNotPresent(refModelOutput);

		Files.writeString(Path.of(refModelFile), "Hello Test (from maven)!", StandardCharsets.UTF_8);
		verifier = verifyErrorFreeLog(project, false, "verify");
		verifier.verifyFileContentMatches(refModelOutput, "People to greet\\: Test");
		verifier.verifyFileContentMatches(modelOutput, Pattern.quote(modelOutputContent));
	}
	
	@Test
	public void simpleLangWriteStorageResources() throws Exception {
		String project = "simple-lang-write-storage-resources";
		MavenExecution verifier = verifyErrorFreeLog(project);
		verifier.verifyFileContentMatches(verifier.getBasedir() + "/src-gen/Model.nojdt.txt",
				"People to greet\\: maven");
		verifier.verifyFileContentMatches(verifier.getBasedir() + "/src-gen/RefModel.nojdt.txt",
				"People to greet\\: Test");
		
		verifier.verifyFilePresent(verifier.getBasedir() + "/src-gen/.Model.nojdtbin");
		verifier.verifyFilePresent(verifier.getBasedir() + "/src-gen/.RefModel.nojdtbin");
	}


	@Test
	public void mavenConfiguration() throws Exception {
		MavenExecution verifier = verifyErrorFreeLog("maven-config");
		verifier.verifyFileContentMatches(verifier.getBasedir() + "/model2-output/Model.nojdt.txt",
				"People to greet\\: maven2");
		verifier.verifyFilePresent(verifier.getBasedir() + "/model-output/IntegrationTestXbase.java");
	}

	@Test
	public void purexbase() throws Exception {
		MavenExecution verifier = verifyErrorFreeLog("purexbase");
		verifier.verifyFilePresent(verifier.getBasedir() + "/src-gen/IntegrationTestXbase.java");
		verifier.verifyFilePresent(verifier.getBasedir() + "/target/xtext-temp/stub-classes/IntegrationTestXbase.class");
	}

	@Test
	public void clustering() throws Exception {
		MavenExecution verifier = verifyErrorFreeLog("clustering");
		verifier.verifyFilePresent(verifier.getBasedir() + "/src-gen/IntegrationTestXbase.java");
		verifier.verifyFilePresent(verifier.getBasedir() + "/src-gen/IntegrationTestXbase2.java");
		verifier.verifyFilePresent(verifier.getBasedir() + "/target/xtext-temp/stub-classes/IntegrationTestXbase.class");
		verifier.verifyFilePresent(verifier.getBasedir() + "/target/xtext-temp/stub-classes/IntegrationTestXbase2.class");
	}

	@Test
	public void outputPerGoal() throws Exception {
		MavenExecution verifier = verifyErrorFreeLog("output-per-goal");
		verifier.verifyFilePresent(verifier.getBasedir() + "/src-gen/SimpleClassXbase.java");
		verifier.verifyFilePresent(verifier.getBasedir() + "/target/xtext-temp/stub-classes/SimpleClassXbase.class");
		verifier.verifyFilePresent(verifier.getBasedir() + "/src-test-gen/SimpleTestClassXbase.java");
		verifier.verifyFilePresent(verifier.getBasedir() + "/target/xtext-temp-test/stub-classes/SimpleTestClassXbase.class");
	}

	@Test
	public void outputPerSource() throws Exception {
		MavenExecution verifier = verifyErrorFreeLog("output-per-source");
		verifier.verifyFilePresent(verifier.getBasedir() + "/src-gen/IntegrationTestXbase.java");
		verifier.verifyFilePresent(verifier.getBasedir() + "/target/xtext-temp/stub-classes/IntegrationTestXbase.class");
		verifier.verifyFilePresent(verifier.getBasedir() + "/other-gen/OtherIntegrationTestXbase.java");
		verifier.verifyFilePresent(
				verifier.getBasedir() + "/target/xtext-temp/stub-classes/OtherIntegrationTestXbase.class");
	}

	@Test
	public void javaLangBiRef() throws Exception {
		MavenExecution verifier = verifyErrorFreeLog("java-lang-bi-ref");
		verifier.verifyFilePresent(verifier.getBasedir() + "/src-gen/XbaseReferToJava.java");
		verifier.verifyFilePresent(verifier.getBasedir() + "/target/xtext-temp/stub-classes/XbaseReferToJava.class");
		verifier.verifyFilePresent(verifier.getBasedir() + "/target/xtext-temp/stub-classes/JavaClazz.class");
		verifier.verifyFilePresent(verifier.getBasedir() + "/target/xtext-temp/stub-classes/JavaRecord.class");
	}

	@Test
	public void javaLang21BiRef() throws Exception {
		Assume.assumeTrue("Active only on Java 21 and later", JavaRuntimeVersion.isJava21OrLater());
		MavenExecution verifier = verifyErrorFreeLog("java-lang-21-bi-ref");
		verifier.verifyFilePresent(verifier.getBasedir() + "/src-gen/XbaseReferToJava.java");
		verifier.verifyFilePresent(verifier.getBasedir() + "/target/xtext-temp/stub-classes/XbaseReferToJava.class");
		verifier.verifyFilePresent(verifier.getBasedir() + "/target/xtext-temp/stub-classes/JavaRecord.class");
	}

	@Test
	public void aggregation() throws Exception {
		MavenExecution verifier = verifyErrorFreeLog("aggregate");
		verifier.verifyFilePresent(verifier.getBasedir() + "/purexbase/src-gen/IntegrationTestXbase.java");
		verifier.verifyFilePresent(
				verifier.getBasedir() + "/purexbase/target/xtext-temp/stub-classes/IntegrationTestXbase.class");
	}

	@Test
	public void xcore() throws Exception {
		MavenExecution verifier = verifyErrorFreeLog("xcore-lang", false, "clean", "verify");
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
		MavenExecution verifier = verifyErrorFreeLog("bug463946");
		verifier.verifyFilePresent(verifier.getBasedir() + "/src-gen/xcore/bug463946/pack/MyModel.java");
	}

	@Test
	public void traceFileIsGenerated() throws Exception {
		MavenExecution verifier = verifyErrorFreeLog("trace");
		verifier.verifyFilePresent(verifier.getBasedir() + "/src-gen/IntegrationTestXbase.java");
		verifier.verifyFilePresent(verifier.getBasedir() + "/src-gen/.IntegrationTestXbase.java._trace");

		String expectedSourceName = "IntegrationTestXbase.xbase";
		File classFile = new File(verifier.getBasedir() + "/target/classes/IntegrationTestXbase.class");

		assertTraceSourceFileName(expectedSourceName, classFile);
	}

	@Test
	public void locationInTraceFileIsRelative_issue_2957() throws Exception {
		MavenExecution verifier = verifyErrorFreeLog("trace-issue-2957");
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

	private static MavenExecution verifyErrorFreeLog(String pathToTestProject) throws IOException {
		return verifyErrorFreeLog(pathToTestProject, false, "clean", "verify");
	}

	private static MavenExecution verifyErrorFreeLog(String pathToTestProject, boolean updateSnapshots, String... goals)
			throws IOException {
		if (goals == null || goals.length < 1) {
			throw new IllegalArgumentException("You need to pass at least one goal to verify log");
		}
		MavenExecution verifier = MavenExecutorUtil.newVerifier(testDir, pathToTestProject, updateSnapshots);
		for (String goal : goals) {
			verifier.executeGoal(goal);
		}
		verifier.verifyErrorFreeLog();
		return verifier;
	}

}
