/**
 * Copyright (c) 2015, 2026 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.xtext.wizard;

import java.nio.charset.Charset;
import java.util.Set;

import org.eclipse.xtext.util.JUnitVersion;
import org.eclipse.xtext.util.JavaVersion;
import org.eclipse.xtext.util.Strings;
import org.eclipse.xtext.util.XtextVersion;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class WizardConfiguration {
	private String rootLocation;

	private String baseName;

	private XtextVersion xtextVersion = XtextVersion.getCurrent();

	private final Ecore2XtextConfiguration ecore2Xtext = new Ecore2XtextConfiguration();

	private Charset encoding = Charset.defaultCharset();

	private String lineDelimiter = Strings.newLine();

	private BuildSystem preferredBuildSystem = BuildSystem.NONE;

	private SourceLayout sourceLayout = SourceLayout.PLAIN;

	private ProjectLayout projectLayout = ProjectLayout.FLAT;

	private boolean needsGradleWrapper = true;

	private JavaVersion javaVersion = JavaVersion.JAVA21;

	private LanguageServer languageServer = LanguageServer.NONE;

	private JUnitVersion junitVersion = JUnitVersion.DEFAULT;

	private final LanguageDescriptor language = new LanguageDescriptor();

	private final RuntimeProjectDescriptor runtimeProject = new RuntimeProjectDescriptor(this);

	private final IdeProjectDescriptor ideProject = new IdeProjectDescriptor(this);

	private final UiProjectDescriptor uiProject = new UiProjectDescriptor(this);

	private final WebProjectDescriptor webProject = new WebProjectDescriptor(this);

	private final ParentProjectDescriptor parentProject = new ParentProjectDescriptor(this);

	private final TargetPlatformProject targetPlatformProject = new TargetPlatformProject(this);

	private final SdkFeatureProject sdkProject = new SdkFeatureProject(this);

	private final P2RepositoryProject p2Project = new P2RepositoryProject(this);

	public Set<ProjectDescriptor> getEnabledProjects() {
		Iterable<ProjectDescriptor> productionProjects = Iterables.filter(
				Lists.newArrayList(parentProject, runtimeProject, ideProject,
						uiProject, webProject, targetPlatformProject, sdkProject, p2Project),
				ProjectDescriptor::isEnabled);
		Iterable<TestProjectDescriptor> testProjects = Iterables
				.filter(Iterables.transform(
						Iterables.filter(productionProjects, TestedProjectDescriptor.class),
						TestedProjectDescriptor::getTestProject), it -> it.isEnabled() && it.isSeparate());
		return ImmutableSet.copyOf(Iterables.concat(productionProjects, testProjects));
	}

	public boolean needsMavenBuild() {
		return BuildSystem.MAVEN.equals(preferredBuildSystem)
				|| BuildSystem.GRADLE.equals(preferredBuildSystem) && uiProject.isEnabled();
	}

	public boolean needsTychoBuild() {
		return needsMavenBuild() && runtimeProject.isEclipsePluginProject();
	}

	public boolean needsGradleBuild() {
		return BuildSystem.GRADLE.equals(preferredBuildSystem);
	}

	public boolean isNeedsGradleWrapper() {
		return needsGradleWrapper && needsGradleBuild();
	}

	public String getRootLocation() {
		return rootLocation;
	}

	public void setRootLocation(String rootLocation) {
		this.rootLocation = rootLocation;
	}

	public String getBaseName() {
		return baseName;
	}

	public void setBaseName(String baseName) {
		this.baseName = baseName;
	}

	public XtextVersion getXtextVersion() {
		return xtextVersion;
	}

	public void setXtextVersion(XtextVersion xtextVersion) {
		this.xtextVersion = xtextVersion;
	}

	public Ecore2XtextConfiguration getEcore2Xtext() {
		return ecore2Xtext;
	}

	public Charset getEncoding() {
		return encoding;
	}

	public void setEncoding(Charset encoding) {
		this.encoding = encoding;
	}

	public String getLineDelimiter() {
		return lineDelimiter;
	}

	public void setLineDelimiter(String lineDelimiter) {
		this.lineDelimiter = lineDelimiter;
	}

	public BuildSystem getPreferredBuildSystem() {
		return preferredBuildSystem;
	}

	public void setPreferredBuildSystem(BuildSystem preferredBuildSystem) {
		this.preferredBuildSystem = preferredBuildSystem;
	}

	public SourceLayout getSourceLayout() {
		return sourceLayout;
	}

	public void setSourceLayout(SourceLayout sourceLayout) {
		this.sourceLayout = sourceLayout;
	}

	public ProjectLayout getProjectLayout() {
		return projectLayout;
	}

	public void setProjectLayout(ProjectLayout projectLayout) {
		this.projectLayout = projectLayout;
	}

	public void setNeedsGradleWrapper(boolean needsGradleWrapper) {
		this.needsGradleWrapper = needsGradleWrapper;
	}

	public JavaVersion getJavaVersion() {
		return javaVersion;
	}

	public void setJavaVersion(JavaVersion javaVersion) {
		this.javaVersion = javaVersion;
	}

	public LanguageServer getLanguageServer() {
		return languageServer;
	}

	public void setLanguageServer(LanguageServer languageServer) {
		this.languageServer = languageServer;
	}

	public JUnitVersion getJunitVersion() {
		return junitVersion;
	}

	public void setJunitVersion(JUnitVersion junitVersion) {
		this.junitVersion = junitVersion;
	}

	public LanguageDescriptor getLanguage() {
		return language;
	}

	public RuntimeProjectDescriptor getRuntimeProject() {
		return runtimeProject;
	}

	public IdeProjectDescriptor getIdeProject() {
		return ideProject;
	}

	public UiProjectDescriptor getUiProject() {
		return uiProject;
	}

	public WebProjectDescriptor getWebProject() {
		return webProject;
	}

	public ParentProjectDescriptor getParentProject() {
		return parentProject;
	}

	public TargetPlatformProject getTargetPlatformProject() {
		return targetPlatformProject;
	}

	public SdkFeatureProject getSdkProject() {
		return sdkProject;
	}

	public P2RepositoryProject getP2Project() {
		return p2Project;
	}
}
