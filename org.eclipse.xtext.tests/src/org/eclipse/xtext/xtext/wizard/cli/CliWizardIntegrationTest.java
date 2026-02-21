/**
 * Copyright (c) 2015, 2024 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.xtext.wizard.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.xtext.tests.LineDelimiters;
import org.eclipse.xtext.util.Files;
import org.eclipse.xtext.util.JUnitVersion;
import org.eclipse.xtext.util.JavaVersion;
import org.eclipse.xtext.util.XtextVersion;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;
import org.eclipse.xtext.xtext.wizard.BuildSystem;
import org.eclipse.xtext.xtext.wizard.LanguageDescriptor;
import org.eclipse.xtext.xtext.wizard.LanguageServer;
import org.eclipse.xtext.xtext.wizard.LineDelimiter;
import org.eclipse.xtext.xtext.wizard.ProjectLayout;
import org.eclipse.xtext.xtext.wizard.SourceLayout;
import org.eclipse.xtext.xtext.wizard.WizardConfiguration;
import org.junit.Assert;
import org.junit.ComparisonFailure;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@RunWith(Parameterized.class)
public class CliWizardIntegrationTest {
	private static class GeneratedFile {
		private final String relativePath;

		private final String content;

		public GeneratedFile(String relativePath, String content) {
			this.relativePath = relativePath;
			this.content = content;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((content == null) ? 0 : content.hashCode());
			result = prime * result + ((relativePath == null) ? 0 : relativePath.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			GeneratedFile other = (GeneratedFile) obj;
			if (content == null) {
				if (other.content != null)
					return false;
			} else if (!content.equals(other.content))
				return false;
			if (relativePath == null) {
				if (other.relativePath != null)
					return false;
			} else if (!relativePath.equals(other.relativePath))
				return false;
			return true;
		}

		@Override
		public String toString() {
			ToStringBuilder b = new ToStringBuilder(this);
			b.add("relativePath", relativePath);
			b.add("content", content);
			return b.toString();
		}
	}

	/**
	 * Use this main method to update the expectations to whatever the wizard
	 * currently generates
	 */
	public static void main(String[] args) {
		try {
			CliProjectsCreator creator = CliWizardIntegrationTest.newProjectCreator();
			for (WizardConfiguration config : CliWizardIntegrationTest.projectConfigs) {
				File targetLocation = new File("testdata/wizard-expectations", config.getBaseName());
				targetLocation.mkdirs();
				Files.sweepFolder(targetLocation);
				config.setRootLocation(targetLocation.getPath());
				creator.createProjects(config);
				InputOutput.println("Updating expectations for " + config.getBaseName());
			}
		} catch (FileNotFoundException e) {
			throw Exceptions.sneakyThrow(e);
		}
	}

	private static ArrayList<WizardConfiguration> projectConfigs = Lists
			.newArrayList(CliWizardIntegrationTest.newProjectConfig(project -> {
				project.setBaseName("plainMaven");
				project.setPreferredBuildSystem(BuildSystem.MAVEN);
				project.setSourceLayout(SourceLayout.MAVEN);
				project.setProjectLayout(ProjectLayout.HIERARCHICAL);
				project.getRuntimeProject().getTestProject().setEnabled(true);
				project.getIdeProject().setEnabled(true);
				project.getWebProject().setEnabled(true);
			}), CliWizardIntegrationTest.newProjectConfig(project -> {
				project.setBaseName("mavenTycho");
				project.setPreferredBuildSystem(BuildSystem.MAVEN);
				project.setSourceLayout(SourceLayout.PLAIN);
				project.setProjectLayout(ProjectLayout.HIERARCHICAL);
				project.getRuntimeProject().getTestProject().setEnabled(true);
				project.getUiProject().setEnabled(true);
				project.getUiProject().getTestProject().setEnabled(true);
				project.getIdeProject().setEnabled(true);
				project.getWebProject().setEnabled(true);
			}), CliWizardIntegrationTest.newProjectConfig(project -> {
				project.setBaseName("mavenTychoJUnit5");
				project.setPreferredBuildSystem(BuildSystem.MAVEN);
				project.setSourceLayout(SourceLayout.PLAIN);
				project.setProjectLayout(ProjectLayout.HIERARCHICAL);
				project.getRuntimeProject().getTestProject().setEnabled(true);
				project.getUiProject().setEnabled(true);
				project.getUiProject().getTestProject().setEnabled(true);
				project.getIdeProject().setEnabled(true);
				project.getWebProject().setEnabled(true);
				project.setJunitVersion(JUnitVersion.JUNIT_5);
			}), CliWizardIntegrationTest.newProjectConfig(project -> {
				project.setBaseName("gradle");
				project.setPreferredBuildSystem(BuildSystem.GRADLE);
				project.setSourceLayout(SourceLayout.MAVEN);
				project.setProjectLayout(ProjectLayout.HIERARCHICAL);
				project.getRuntimeProject().getTestProject().setEnabled(true);
				project.getIdeProject().setEnabled(true);
				project.getWebProject().setEnabled(true);
				project.setJunitVersion(JUnitVersion.JUNIT_5);
			}), CliWizardIntegrationTest.newProjectConfig(project -> {
				project.setBaseName("eclipsePlugin");
				project.setPreferredBuildSystem(BuildSystem.NONE);
				project.setSourceLayout(SourceLayout.PLAIN);
				project.setProjectLayout(ProjectLayout.FLAT);
				project.getRuntimeProject().getTestProject().setEnabled(true);
				project.getIdeProject().setEnabled(true);
				project.getUiProject().setEnabled(true);
				project.getUiProject().getTestProject().setEnabled(true);
			}), CliWizardIntegrationTest.newProjectConfig(project -> {
				project.setBaseName("eclipsePluginP2");
				project.setPreferredBuildSystem(BuildSystem.NONE);
				project.setSourceLayout(SourceLayout.PLAIN);
				project.setProjectLayout(ProjectLayout.FLAT);
				project.getRuntimeProject().getTestProject().setEnabled(true);
				project.getIdeProject().setEnabled(true);
				project.getUiProject().setEnabled(true);
				project.getUiProject().getTestProject().setEnabled(true);
				project.getP2Project().setEnabled(true);
			}), CliWizardIntegrationTest.newProjectConfig(project -> {
				project.setBaseName("mavenTychoP2");
				project.setPreferredBuildSystem(BuildSystem.MAVEN);
				project.setSourceLayout(SourceLayout.PLAIN);
				project.setProjectLayout(ProjectLayout.HIERARCHICAL);
				project.getRuntimeProject().getTestProject().setEnabled(true);
				project.getUiProject().setEnabled(true);
				project.getUiProject().getTestProject().setEnabled(true);
				project.getIdeProject().setEnabled(true);
				project.getWebProject().setEnabled(true);
				project.getP2Project().setEnabled(true);
			}), CliWizardIntegrationTest.newProjectConfig(project -> {
				project.setBaseName("mavenTychoP2J21");
				project.setJavaVersion(JavaVersion.JAVA21);
				project.setPreferredBuildSystem(BuildSystem.MAVEN);
				project.setSourceLayout(SourceLayout.PLAIN);
				project.setProjectLayout(ProjectLayout.HIERARCHICAL);
				project.getRuntimeProject().getTestProject().setEnabled(true);
				project.getUiProject().setEnabled(true);
				project.getUiProject().getTestProject().setEnabled(true);
				project.getIdeProject().setEnabled(true);
				project.getWebProject().setEnabled(true);
				project.getP2Project().setEnabled(true);
			}), CliWizardIntegrationTest.newProjectConfig(project -> {
				project.setBaseName("mavenTychoP2J25");
				project.setJavaVersion(JavaVersion.JAVA25);
				project.setPreferredBuildSystem(BuildSystem.MAVEN);
				project.setSourceLayout(SourceLayout.PLAIN);
				project.setProjectLayout(ProjectLayout.HIERARCHICAL);
				project.getRuntimeProject().getTestProject().setEnabled(true);
				project.getUiProject().setEnabled(true);
				project.getUiProject().getTestProject().setEnabled(true);
				project.getIdeProject().setEnabled(true);
				project.getWebProject().setEnabled(true);
				project.getP2Project().setEnabled(true);
			}), CliWizardIntegrationTest.newProjectConfig(project -> {
				project.setBaseName("full");
				project.setPreferredBuildSystem(BuildSystem.GRADLE);
				project.setSourceLayout(SourceLayout.PLAIN);
				project.setProjectLayout(ProjectLayout.HIERARCHICAL);
				project.getRuntimeProject().getTestProject().setEnabled(true);
				project.getUiProject().setEnabled(true);
				project.getUiProject().getTestProject().setEnabled(true);
				project.getIdeProject().setEnabled(true);
				project.getWebProject().setEnabled(true);
				project.getP2Project().setEnabled(true);
			}), CliWizardIntegrationTest.newProjectConfig(project -> {
				project.setBaseName("lsMavenFatjar");
				project.setPreferredBuildSystem(BuildSystem.MAVEN);
				project.setSourceLayout(SourceLayout.PLAIN);
				project.setProjectLayout(ProjectLayout.HIERARCHICAL);
				project.getRuntimeProject().getTestProject().setEnabled(false);
				project.getUiProject().setEnabled(false);
				project.getUiProject().getTestProject().setEnabled(false);
				project.getIdeProject().setEnabled(true);
				project.getWebProject().setEnabled(false);
				project.setLanguageServer(LanguageServer.FATJAR);
			}), CliWizardIntegrationTest.newProjectConfig(project -> {
				project.setBaseName("lsMavenTychoFatjar");
				project.setPreferredBuildSystem(BuildSystem.MAVEN);
				project.setSourceLayout(SourceLayout.PLAIN);
				project.setProjectLayout(ProjectLayout.HIERARCHICAL);
				project.getRuntimeProject().getTestProject().setEnabled(false);
				project.getUiProject().setEnabled(true);
				project.getUiProject().getTestProject().setEnabled(false);
				project.getIdeProject().setEnabled(true);
				project.getWebProject().setEnabled(false);
				project.setLanguageServer(LanguageServer.FATJAR);
			}), CliWizardIntegrationTest.newProjectConfig(project -> {
				project.setBaseName("lsGradleFatjar");
				project.setPreferredBuildSystem(BuildSystem.GRADLE);
				project.setSourceLayout(SourceLayout.PLAIN);
				project.setProjectLayout(ProjectLayout.HIERARCHICAL);
				project.getRuntimeProject().getTestProject().setEnabled(false);
				project.getUiProject().setEnabled(false);
				project.getUiProject().getTestProject().setEnabled(false);
				project.getIdeProject().setEnabled(true);
				project.getWebProject().setEnabled(false);
				project.setLanguageServer(LanguageServer.FATJAR);
			}), CliWizardIntegrationTest.newProjectConfig(project -> {
				project.setBaseName("lsMavenApp");
				project.setPreferredBuildSystem(BuildSystem.MAVEN);
				project.setSourceLayout(SourceLayout.PLAIN);
				project.setProjectLayout(ProjectLayout.HIERARCHICAL);
				project.getRuntimeProject().getTestProject().setEnabled(false);
				project.getUiProject().setEnabled(false);
				project.getUiProject().getTestProject().setEnabled(false);
				project.getIdeProject().setEnabled(true);
				project.getWebProject().setEnabled(false);
				project.setLanguageServer(LanguageServer.APP);
			}), CliWizardIntegrationTest.newProjectConfig(project -> {
				project.setBaseName("lsMavenTychoApp");
				project.setPreferredBuildSystem(BuildSystem.MAVEN);
				project.setSourceLayout(SourceLayout.PLAIN);
				project.setProjectLayout(ProjectLayout.HIERARCHICAL);
				project.getRuntimeProject().getTestProject().setEnabled(false);
				project.getUiProject().setEnabled(true);
				project.getUiProject().getTestProject().setEnabled(false);
				project.getIdeProject().setEnabled(true);
				project.getWebProject().setEnabled(false);
				project.setLanguageServer(LanguageServer.APP);
			}), CliWizardIntegrationTest.newProjectConfig(project -> {
				project.setBaseName("lsGradleApp");
				project.setPreferredBuildSystem(BuildSystem.GRADLE);
				project.setSourceLayout(SourceLayout.PLAIN);
				project.setProjectLayout(ProjectLayout.HIERARCHICAL);
				project.getRuntimeProject().getTestProject().setEnabled(false);
				project.getUiProject().setEnabled(false);
				project.getUiProject().getTestProject().setEnabled(false);
				project.getIdeProject().setEnabled(true);
				project.getWebProject().setEnabled(false);
				project.setLanguageServer(LanguageServer.APP);
			}));

	private static WizardConfiguration newProjectConfig(Procedure1<? super WizardConfiguration> block) {
		WizardConfiguration result = new WizardConfiguration() {
			@Override
			public String toString() {
				return getPreferredBuildSystem() + "|" + getSourceLayout() + "|" + getProjectLayout() + "|"
						+ getLanguageServer();
			}
		};
		result.setXtextVersion(new XtextVersion("unspecified"));
		result.setEncoding(StandardCharsets.UTF_8);
		result.setLineDelimiter(LineDelimiter.UNIX.getValue());
		result.getLanguage().setName("mydsl.MyDsl");
		result.getLanguage().setFileExtensions(LanguageDescriptor.FileExtensions.fromString("mydsl"));
		block.apply(result);
		return result;
	}

	private static CliProjectsCreator newProjectCreator() {
		CliProjectsCreator result = new CliProjectsCreator();
		result.setLineDelimiter("\n");
		return result;
	}

	@Parameterized.Parameters(name = "{index}: {0}")
	public static ArrayList<WizardConfiguration> data() {
		return CliWizardIntegrationTest.projectConfigs;
	}

	@Rule
	public TemporaryFolder temp = new TemporaryFolder();

	private WizardConfiguration config;

	private CliProjectsCreator creator;

	public CliWizardIntegrationTest(WizardConfiguration config) {
		this.config = config;
		this.creator = CliWizardIntegrationTest.newProjectCreator();
	}

	@Test
	public void testProjectCreation() {
		validateCreatedProjects();
	}

	private void validateCreatedProjects() {
		File targetLocation = new File(temp.getRoot(), config.getBaseName());
		config.setRootLocation(targetLocation.getPath());
		creator.createProjects(config);
		File expectationLocation = new File("testdata/wizard-expectations", config.getBaseName());
		TreeSet<GeneratedFile> expectedFiles = toFileSet(expectationLocation);
		TreeSet<GeneratedFile> actualFiles = toFileSet(targetLocation);
		compareFileTrees(expectedFiles, actualFiles);
	}

	private TreeSet<GeneratedFile> toFileSet(File root) {
		List<File> allFiles = new ArrayList<>();
		collectAllFiles(root, allFiles);
		List<GeneratedFile> generatedFiles = Lists.transform(allFiles, it -> {
			String relativePath = it.getPath().replace(root.getPath(), "");
			return toGeneratedFile(it, relativePath);
		});
		Comparator<GeneratedFile> sortByPath = (GeneratedFile $0, GeneratedFile $1) -> {
			return $0.relativePath.compareTo($1.relativePath);
		};
		TreeSet<GeneratedFile> result = new TreeSet<>(sortByPath);
		result.addAll(generatedFiles);
		return result;
	}

	private void collectAllFiles(File root, List<File> children) {
		if (root.isDirectory()) {
			for (File it : root.listFiles()) {
				collectAllFiles(it, children);
			}
		} else {
			children.add(root);
		}
	}

	private GeneratedFile toGeneratedFile(File file, String relativePath) {
		try {
			String content;
			if (file.isDirectory()) {
				content = "";
			} else {
				content = com.google.common.io.Files.asCharSource(file, config.getEncoding()).read();
			}
			return new GeneratedFile(relativePath, content);
		} catch (IOException e) {
			throw Exceptions.sneakyThrow(e);
		}
	}

	private void compareFileTrees(Set<GeneratedFile> expectedFiles, Set<GeneratedFile> actualFiles) {
		Map<String, GeneratedFile> expectedFilesByPath = IterableExtensions.toMap(expectedFiles,
				(GeneratedFile it) -> it.relativePath);
		Map<String, GeneratedFile> actualFilesByPath = IterableExtensions.toMap(actualFiles,
				(GeneratedFile it) -> it.relativePath);
		Sets.SetView<GeneratedFile> missingFiles = Sets.difference(expectedFiles, actualFiles);
		Sets.SetView<GeneratedFile> unexpectedFiles = Sets.difference(actualFiles, expectedFiles);
		Sets.SetView<GeneratedFile> comparableFiles = Sets.intersection(expectedFiles, actualFiles);
		missingFiles.forEach((GeneratedFile file) -> {
			throw new ComparisonFailure("Missing file: " + file.relativePath, file.content, "");
		});
		unexpectedFiles.forEach((GeneratedFile file) -> {
			throw new ComparisonFailure("Unexpected file: " + file.relativePath, "", file.content);
		});
		comparableFiles.forEach((GeneratedFile file) -> {
			String expectedContent = LineDelimiters.toUnix(expectedFilesByPath.get(file.relativePath).content);
			String actualContent = LineDelimiters.toUnix(actualFilesByPath.get(file.relativePath).content);
			Assert.assertEquals(file.relativePath, expectedContent, actualContent);
		});
	}
}
