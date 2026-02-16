/**
 * Copyright (c) 2015, 2023 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.xtext.wizard;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Objects;

import org.eclipse.xtext.util.XtextVersion;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class WizardConfigurationTest {
	private WizardConfiguration config;

	@Before
	public void setupConfig() {
		WizardConfiguration wizardConfiguration = new WizardConfiguration();
		wizardConfiguration.setBaseName("org.example.mydsl");
		wizardConfiguration.setRootLocation("");
		wizardConfiguration.getLanguage().setName("org.example.mydsl.MyDsl");
		wizardConfiguration.getLanguage().setFileExtensions(LanguageDescriptor.FileExtensions.fromString("mydsl"));
		config = wizardConfiguration;
	}

	@Test
	public void runtimeProjectCanBeBuiltAsPlainMavenProject() {
		config.setPreferredBuildSystem(BuildSystem.MAVEN);
		assertFalse(config.getRuntimeProject().isEclipsePluginProject());
		assertFalse(config.getRuntimeProject().pom().getContent().contains("eclipse-plugin"));
		assertFalse(config.getParentProject().pom().getContent().contains("tycho"));
		assertFalse(IterableExtensions.exists(
				Iterables.filter(allJavaProjects(), (ProjectDescriptor it) -> !it.isEclipsePluginProject()),
				(ProjectDescriptor it) -> (it.pom().getContent().contains("tycho"))));
	}

	@Test
	public void runtimeProjectIsPluginProjectWhenUiProjectIsEnabled() {
		config.getUiProject().setEnabled(true);
		assertTrue(config.getRuntimeProject().isEclipsePluginProject());
	}

	@Test
	public void eclipsePluginsAreBuiltWithTychoWhenMavenBuiltIsEnabled() {
		config.getUiProject().setEnabled(true);
		config.getIdeProject().setEnabled(true);
		config.setPreferredBuildSystem(BuildSystem.MAVEN);
		assertTrue(config.needsTychoBuild());
		for (ProjectDescriptor it : Lists.newArrayList(config.getRuntimeProject(), config.getIdeProject(),
				config.getUiProject())) {
			assertTrue(it.pom().getContent().contains("eclipse-plugin"));
		}
		assertTrue(config.getParentProject().pom().getContent().contains("tycho"));
	}

	@Test
	public void p2AndSdkProjectsAreBuiltWithTychoWhenMavenBuiltIsEnabled() {
		config.getUiProject().setEnabled(true);
		config.getP2Project().setEnabled(true);
		config.setPreferredBuildSystem(BuildSystem.MAVEN);
		assertTrue(config.needsTychoBuild());
		assertTrue(config.getSdkProject().pom().getContent().contains("eclipse-feature"));
		assertTrue(config.getP2Project().pom().getContent().contains("eclipse-repository"));
		assertTrue(config.getParentProject().pom().getContent().contains("tycho"));
	}
	
	@Test
	public void p2ProjectsEnablesSourceGenerationWithTychoWhenMavenBuiltIsEnabled() {
		config.getUiProject().setEnabled(true);
		config.getP2Project().setEnabled(true);
		config.setPreferredBuildSystem(BuildSystem.MAVEN);
		assertTrue(config.needsTychoBuild());
		assertTrue(config.getParentProject().pom().getContent().contains("tycho-source-plugin"));
		assertFalse(config.getParentProject().pom().getContent().contains("tycho-source-feature-plugin"));
		assertTrue(config.getParentProject().pom().getContent().contains("<goal>feature-source</goal>"));
	}

	@Test
	public void aTychoBuildIncludesATargetPlatform() {
		config.getUiProject().setEnabled(true);
		config.setPreferredBuildSystem(BuildSystem.MAVEN);
		assertTrue(config.getTargetPlatformProject().isEnabled());
	}

	@Test
	public void testProjectIsPluginProjectWhenRuntimeProjectIsPluginProject() {
		config.getRuntimeProject().getTestProject().setEnabled(true);
		assertTrue(config.getRuntimeProject().getTestProject().isEclipsePluginProject());
	}

	@Test
	public void p2ProjectEnablesSdkProject() {
		config.getP2Project().setEnabled(true);
		config.getSdkProject().setEnabled(false);
		assertTrue(config.getSdkProject().isEnabled());
	}

	@Test
	public void eclipseUiCanBeBuiltWithPDE() {
		config.getUiProject().setEnabled(true);
		assertFalse(config.needsGradleBuild());
		assertFalse(config.needsMavenBuild());
	}

	@Test
	public void eclipseUiIsBuiltWithTychoWhenNotUsingPDE() {
		config.getUiProject().setEnabled(true);
		config.setPreferredBuildSystem(BuildSystem.GRADLE);
		assertTrue(config.needsTychoBuild());
	}

	@Test
	public void mavenNeedsAParentProject() {
		config.setPreferredBuildSystem(BuildSystem.MAVEN);
		assertTrue(config.getParentProject().isEnabled());
	}

	@Test
	public void gradleNeedsAParentProject() {
		config.setPreferredBuildSystem(BuildSystem.GRADLE);
		assertTrue(config.getParentProject().isEnabled());
	}

	@Test
	public void inlinedTestProjectsDontOverrideMainSources() {
		config.getRuntimeProject().getTestProject().setEnabled(true);
		config.setPreferredBuildSystem(BuildSystem.MAVEN);
		config.setSourceLayout(SourceLayout.MAVEN);
		AbstractFile pom = IterableExtensions.findFirst(config.getRuntimeProject().getFiles(),
				(AbstractFile it) -> "pom.xml".equals(it.getRelativePath()));
		assertTrue(pom instanceof PomFile);
		assertTrue(((PomFile) pom).getContent().toString().contains("<artifactId>org.example.mydsl</artifactId>"));
	}

	@Test
	public void inlinedTestProjectsAddTheirDependenciesToTheMainProject() {
		config.getRuntimeProject().getTestProject().setEnabled(true);
		config.setSourceLayout(SourceLayout.MAVEN);
		for (ExternalDependency testDependency : config.getRuntimeProject().getTestProject()
				.getExternalDependencies()) {
			assertTrue(IterableExtensions.exists(config.getRuntimeProject().getExternalDependencies(),
					(ExternalDependency it) -> {
						return Objects.equals(it.getMaven().getArtifactId(), testDependency.getMaven().getArtifactId())
								&& Objects.equals(it.getP2().getBundleId(), testDependency.getP2().getBundleId())
								&& Objects.equals(it.getP2().getPackages(), testDependency.getP2().getPackages());
					}));
		}
	}

	@Test
	public void inlinedTestProjectsAddTheirSourceFoldersToTheMainProject() {
		config.getRuntimeProject().getTestProject().setEnabled(true);
		config.setSourceLayout(SourceLayout.MAVEN);
		for (SourceFolderDescriptor folder : config.getRuntimeProject().getTestProject().getSourceFolders()) {
			assertTrue(config.getRuntimeProject().getSourceFolders().contains(folder));
		}
	}

	@Test
	public void mavenProjectsHaveAPom() {
		config.setPreferredBuildSystem(BuildSystem.MAVEN);
		for (ProjectDescriptor it : Iterables.filter(allJavaProjects(),
				(ProjectDescriptor it) -> (it.isPartOfMavenBuild()))) {
			it.setEnabled(true);
			assertTrue(IterableExtensions.exists(it.getFiles(),
					(AbstractFile file) -> "pom.xml".equals(file.getRelativePath())));
		}
	}

	@Test
	public void gradleProjectsHaveABuildFile() {
		config.setPreferredBuildSystem(BuildSystem.GRADLE);
		for (ProjectDescriptor it : Iterables.filter(allJavaProjects(),
				(ProjectDescriptor it) -> (it.isPartOfGradleBuild()))) {
			it.setEnabled(true);
			assertTrue(IterableExtensions.exists(it.getFiles(),
					(AbstractFile f) -> "build.gradle".equals(f.getRelativePath())));
		}
	}

	@Test
	public void pluginProjectsHaveEclipseMetaData() {
		for (ProjectDescriptor it : Iterables.filter(allJavaProjects(),
				(ProjectDescriptor it) -> it.isEclipsePluginProject())) {
			it.setEnabled(true);
			assertTrue(IterableExtensions.exists(it.getFiles(),
					(AbstractFile file) -> "MANIFEST.MF".equals(file.getRelativePath())));
			assertTrue(IterableExtensions.exists(it.getFiles(),
					(AbstractFile file) -> "build.properties".equals(file.getRelativePath())));
		}
	}

	@Test
	public void featureProjectsHaveEclipseBuildProperties() {
		assertTrue(IterableExtensions.exists(config.getSdkProject().getFiles(),
				(AbstractFile it) -> "build.properties".equals(it.getRelativePath())));
	}

	@Test
	public void projectsCanBeBuiltAgainstXtextNightlies() {
		config.setPreferredBuildSystem(BuildSystem.GRADLE);
		config.getUiProject().setEnabled(true);
		config.setXtextVersion(new XtextVersion("2.9.0-SNAPSHOT"));
		String snapshotsRepo = "repositories/snapshots";
		assertTrue(config.getParentProject().pom().getContent().contains(snapshotsRepo));
		assertTrue(config.getParentProject().buildGradle().getContent().contains(snapshotsRepo));
		String nightlyUpdateSite = "xtext/updates/nightly";
		assertTrue(config.getTargetPlatformProject().target().toString().contains(nightlyUpdateSite));
	}

	@Test
	public void projectsCanBeBuiltAgainstXtextReleases() {
		config.setPreferredBuildSystem(BuildSystem.GRADLE);
		config.getUiProject().setEnabled(true);
		config.setXtextVersion(new XtextVersion("2.9.0"));
		String snapshotsRepo = "<id>sonatype-snapshots</id>";
		assertFalse(config.getParentProject().pom().getContent().contains(snapshotsRepo));
		assertFalse(config.getParentProject().buildGradle().getContent().contains(snapshotsRepo));
		String releaseUpdateSite = "xtext/updates/releases/2.9.0";
		assertTrue(config.getTargetPlatformProject().target().toString().contains(releaseUpdateSite));
	}

	@Test
	public void gradleEclipseClasspathIsIgnoredForPluginProjects() {
		config.setPreferredBuildSystem(BuildSystem.GRADLE);
		config.getUiProject().setEnabled(true);
		config.getIdeProject().setEnabled(true);
		for (ProjectDescriptor it : Iterables.filter(config.getEnabledProjects(),
				(ProjectDescriptor it) -> it.isEclipsePluginProject())) {
			assertTrue(it.buildGradle().getContent().contains("eclipseClasspath.enabled=false"));
		}
	}

	@Test
	public void parentContainsOtherProjectsInHierarchicallayout() {
		config.setProjectLayout(ProjectLayout.HIERARCHICAL);
		for (ProjectDescriptor it : allJavaProjects()) {
			assertTrue(it.getLocation().startsWith(config.getParentProject().getLocation() + "/"));
		}
	}

	@Test
	public void plainMavenProjectsCanBeBuiltWithEclipseLayout() {
		config.setPreferredBuildSystem(BuildSystem.MAVEN);
		config.setSourceLayout(SourceLayout.PLAIN);
		Iterable<? extends ProjectDescriptor> plainMavenProjects = Iterables.filter(allJavaProjects(),
				(ProjectDescriptor it) -> !it.isEclipsePluginProject() && it.isPartOfMavenBuild());
		for (ProjectDescriptor it : plainMavenProjects) {
			assertTrue(it.pom().getContent().contains("<directory>src</directory>"));
			assertTrue(it.pom().getContent().contains("<source>src-gen</source>"));
			assertTrue(it.pom().getContent().contains("<directory>src-gen</directory>"));
		}
		for (ProjectDescriptor it : Iterables.filter(plainMavenProjects,
				(ProjectDescriptor it) -> !(it instanceof TestProjectDescriptor))) {
			assertTrue(it.pom().getContent().contains("<sourceDirectory>src</sourceDirectory>"));
			assertTrue(it.pom().getContent().contains("add-source"));
			assertTrue(it.pom().getContent().contains("add-resource"));
			assertFalse(it.pom().getContent().contains("add-test-source"));
			assertFalse(it.pom().getContent().contains("add-test-resource"));
		}
		for (TestProjectDescriptor it : Iterables.filter(plainMavenProjects, TestProjectDescriptor.class)) {
			assertTrue(it.pom().getContent().contains("<testSourceDirectory>src</testSourceDirectory>"));
			assertTrue(it.pom().getContent().contains("add-test-source"));
			assertTrue(it.pom().getContent().contains("add-test-resource"));
			assertFalse(it.pom().getContent().contains("add-source"));
			assertFalse(it.pom().getContent().contains("add-resource"));
		}
	}

	@Test
	public void plainMavenProjectsCanBeBuiltWithMavenLayout() {
		config.setPreferredBuildSystem(BuildSystem.MAVEN);
		config.setSourceLayout(SourceLayout.MAVEN);
		Iterable<? extends ProjectDescriptor> plainMavenProjects = Iterables.filter(allJavaProjects(),
				(ProjectDescriptor it) -> !it.isEclipsePluginProject() && it.isPartOfMavenBuild());
		Iterable<? extends ProjectDescriptor> mainProjects = Iterables.filter(plainMavenProjects,
				(ProjectDescriptor it) -> !(it instanceof TestProjectDescriptor));
		for (ProjectDescriptor it : plainMavenProjects) {
			it.setEnabled(true);
		}
		assertTrue(IterableExtensions.forall(Iterables.filter(plainMavenProjects, TestProjectDescriptor.class),
				(TestProjectDescriptor it) -> (it.isInlined())));
		for (ProjectDescriptor it : mainProjects) {
			assertTrue(it.pom().getContent().contains("add-source"));
			assertTrue(it.pom().getContent().contains("add-resource"));
			assertTrue(it.pom().getContent().contains("<source>src/main/xtext-gen</source>"));
			assertTrue(it.pom().getContent().contains("<directory>src/main/xtext-gen</directory>"));
		}
		for (TestedProjectDescriptor it : Iterables.filter(mainProjects, TestedProjectDescriptor.class)) {
			assertTrue(it.pom().getContent().contains("add-test-source"));
			assertTrue(it.pom().getContent().contains("add-test-resource"));
			assertTrue(it.pom().getContent().contains("<source>src/test/xtext-gen</source>"));
			assertTrue(it.pom().getContent().contains("<directory>src/test/xtext-gen</directory>"));
		}
	}

	@Test
	public void pomFileDontDuplicateMavenDefaults() {
		config.setPreferredBuildSystem(BuildSystem.MAVEN);
		config.setSourceLayout(SourceLayout.MAVEN);
		Iterable<? extends ProjectDescriptor> plainMavenProjects = Iterables.filter(allJavaProjects(),
				(ProjectDescriptor it) -> !it.isEclipsePluginProject() && it.isPartOfMavenBuild());
		Iterable<? extends ProjectDescriptor> mainProjects = Iterables.filter(plainMavenProjects,
				(ProjectDescriptor it) -> !(it instanceof TestProjectDescriptor));
		for (ProjectDescriptor it : plainMavenProjects) {
			it.setEnabled(true);
		}
		for (ProjectDescriptor it : mainProjects) {
			assertFalse(it.pom().getContent().contains("<sourceDirectory>"));
			assertFalse(it.pom().getContent().contains("<directory>src/main/resources</directory>"));
			assertFalse(it.pom().getContent().contains("<testSourceDirectory>"));
			assertFalse(it.pom().getContent().contains("<directory>src/test/resources</directory>"));
		}
	}

	@Test
	public void uiTestsNeedTychoUiHarness() {
		config.setPreferredBuildSystem(BuildSystem.MAVEN);
		config.getUiProject().setEnabled(true);
		String pom = config.getUiProject().getTestProject().pom().getContent();
		assertTrue(pom.contains("useUIHarness"));
	}

	@Test
	public void runtimeTestsDontNeedTychoUiHarness() {
		config.setPreferredBuildSystem(BuildSystem.MAVEN);
		config.getUiProject().setEnabled(true);
		String pom = config.getRuntimeProject().getTestProject().pom().getContent();
		assertFalse(pom.contains("useUIHarness"));
	}

	@Test
	public void tychoDoesNotFailOnMissingTests() {
		config.setPreferredBuildSystem(BuildSystem.MAVEN);
		config.getUiProject().setEnabled(true);
		assertTrue(config.getParentProject().pom().getContent().contains("failIfNoTests"));
	}

	@Test
	public void allBuildSystemsUseJava17() {
		String parentPom = config.getParentProject().pom().getContent();
		assertTrue(parentPom.contains("<maven.compiler.release>17</maven.compiler.release>"));
		String parentGradle = config.getParentProject().buildGradle().getContent();
		assertTrue(parentGradle.contains("sourceCompatibility = JavaVersion.VERSION_17"));
		assertTrue(parentGradle.contains("targetCompatibility = JavaVersion.VERSION_17"));
		for (String it : Lists.transform(allJavaProjects(), (ProjectDescriptor it) -> it.manifest())) {
			assertTrue(it.contains("Bundle-RequiredExecutionEnvironment: JavaSE-17"));
		}
	}

	public List<? extends ProjectDescriptor> allJavaProjects() {
		return Lists.newArrayList(config.getRuntimeProject(), config.getRuntimeProject().getTestProject(),
				config.getUiProject(), config.getUiProject().getTestProject(), config.getIdeProject(),
				config.getWebProject());
	}

	@Test
	public void binExcludes() {
		assertEquals(Sets.newHashSet("**/*.mwe2", "**/*.xtend"), config.getRuntimeProject().getBinExcludes());
		assertEquals(Sets.newHashSet("**/*.xtend"), config.getRuntimeProject().getTestProject().getBinExcludes());
		assertEquals(Sets.newHashSet("**/*.xtend"), config.getUiProject().getBinExcludes());
		assertEquals(Sets.newHashSet("**/*.xtend"), config.getUiProject().getTestProject().getBinExcludes());
		assertEquals(Sets.newHashSet(), config.getSdkProject().getBinExcludes());
	}
}
