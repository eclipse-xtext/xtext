/**
 * Copyright (c) 2015, 2022 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.xtext.wizard;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.util.JUnitVersion;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Pure;

@SuppressWarnings("all")
public abstract class TestProjectDescriptor extends ProjectDescriptor {
  @Accessors
  private final ProjectDescriptor testedProject;

  public TestProjectDescriptor(final TestedProjectDescriptor testedProject) {
    super(testedProject.getConfig());
    this.testedProject = testedProject;
  }

  @Override
  public Set<SourceFolderDescriptor> getSourceFolders() {
    final Function1<Outlet, SourceFolderDescriptor> _function = (Outlet it) -> {
      String _sourceFolder = this.sourceFolder(it);
      boolean _isTest = this.isTest(it);
      return new SourceFolderDescriptor(_sourceFolder, _isTest);
    };
    return IterableExtensions.<SourceFolderDescriptor>toSet(ListExtensions.<Outlet, SourceFolderDescriptor>map(Collections.<Outlet>unmodifiableList(CollectionLiterals.<Outlet>newArrayList(Outlet.TEST_JAVA, Outlet.TEST_RESOURCES, Outlet.TEST_SRC_GEN, Outlet.TEST_XTEND_GEN)), _function));
  }

  public boolean isInlined() {
    return (this.isEnabled() && (!Objects.equals(this.getConfig().getSourceLayout(), SourceLayout.PLAIN)));
  }

  public boolean isSeparate() {
    return (this.isEnabled() && Objects.equals(this.getConfig().getSourceLayout(), SourceLayout.PLAIN));
  }

  @Override
  public String getNameQualifier() {
    String _nameQualifier = this.testedProject.getNameQualifier();
    return (_nameQualifier + ".tests");
  }

  @Override
  public Set<? extends ProjectDescriptor> getUpstreamProjects() {
    return Collections.<ProjectDescriptor>unmodifiableSet(CollectionLiterals.<ProjectDescriptor>newHashSet(this.testedProject));
  }

  @Override
  public boolean isEclipsePluginProject() {
    return this.testedProject.isEclipsePluginProject();
  }

  @Override
  public Set<ExternalDependency> getExternalDependencies() {
    final LinkedHashSet<ExternalDependency> deps = CollectionLiterals.<ExternalDependency>newLinkedHashSet();
    Set<ExternalDependency> _externalDependencies = super.getExternalDependencies();
    Iterables.<ExternalDependency>addAll(deps, _externalDependencies);
    JUnitVersion _junitVersion = this.getConfig().getJunitVersion();
    boolean _equals = Objects.equals(_junitVersion, JUnitVersion.JUNIT_4);
    if (_equals) {
      ExternalDependency _externalDependency = new ExternalDependency();
      final Procedure1<ExternalDependency> _function = (ExternalDependency it) -> {
        ExternalDependency.P2Coordinates _p2 = it.getP2();
        _p2.setBundleId("org.junit");
        ExternalDependency.P2Coordinates _p2_1 = it.getP2();
        _p2_1.setVersion("4.13.2");
        ExternalDependency.MavenCoordinates _maven = it.getMaven();
        _maven.setGroupId("junit");
        ExternalDependency.MavenCoordinates _maven_1 = it.getMaven();
        _maven_1.setArtifactId("junit");
        ExternalDependency.MavenCoordinates _maven_2 = it.getMaven();
        _maven_2.setScope(Scope.TESTCOMPILE);
      };
      ExternalDependency _doubleArrow = ObjectExtensions.<ExternalDependency>operator_doubleArrow(_externalDependency, _function);
      deps.add(_doubleArrow);
    }
    JUnitVersion _junitVersion_1 = this.getConfig().getJunitVersion();
    boolean _equals_1 = Objects.equals(_junitVersion_1, JUnitVersion.JUNIT_5);
    if (_equals_1) {
      ExternalDependency _externalDependency_1 = new ExternalDependency();
      final Procedure1<ExternalDependency> _function_1 = (ExternalDependency it) -> {
        ExternalDependency.P2Coordinates _p2 = it.getP2();
        LinkedHashSet<String> _newLinkedHashSet = Sets.<String>newLinkedHashSet();
        final Procedure1<LinkedHashSet<String>> _function_2 = (LinkedHashSet<String> it_1) -> {
          it_1.add("org.junit.jupiter.api;version=\"[5.1.0,6.0.0)\"");
          it_1.add("org.junit.jupiter.api.extension;version=\"[5.1.0,6.0.0)\"");
        };
        LinkedHashSet<String> _doubleArrow_1 = ObjectExtensions.<LinkedHashSet<String>>operator_doubleArrow(_newLinkedHashSet, _function_2);
        _p2.setPackages(_doubleArrow_1);
        ExternalDependency.P2Coordinates _p2_1 = it.getP2();
        _p2_1.setVersion("[5.1.0,6.0.0)");
        ExternalDependency.MavenCoordinates _maven = it.getMaven();
        _maven.setGroupId("org.junit.jupiter");
        ExternalDependency.MavenCoordinates _maven_1 = it.getMaven();
        _maven_1.setArtifactId("junit-jupiter-api");
        ExternalDependency.MavenCoordinates _maven_2 = it.getMaven();
        _maven_2.setScope(Scope.TESTCOMPILE);
      };
      ExternalDependency _doubleArrow_1 = ObjectExtensions.<ExternalDependency>operator_doubleArrow(_externalDependency_1, _function_1);
      deps.add(_doubleArrow_1);
      ExternalDependency _externalDependency_2 = new ExternalDependency();
      final Procedure1<ExternalDependency> _function_2 = (ExternalDependency it) -> {
        ExternalDependency.MavenCoordinates _maven = it.getMaven();
        _maven.setGroupId("org.junit.jupiter");
        ExternalDependency.MavenCoordinates _maven_1 = it.getMaven();
        _maven_1.setArtifactId("junit-jupiter-engine");
        ExternalDependency.MavenCoordinates _maven_2 = it.getMaven();
        _maven_2.setScope(Scope.TESTRUNTIME);
      };
      ExternalDependency _doubleArrow_2 = ObjectExtensions.<ExternalDependency>operator_doubleArrow(_externalDependency_2, _function_2);
      deps.add(_doubleArrow_2);
      ExternalDependency _externalDependency_3 = new ExternalDependency();
      final Procedure1<ExternalDependency> _function_3 = (ExternalDependency it) -> {
        ExternalDependency.MavenCoordinates _maven = it.getMaven();
        _maven.setGroupId("org.junit.platform");
        ExternalDependency.MavenCoordinates _maven_1 = it.getMaven();
        _maven_1.setArtifactId("junit-platform-launcher");
        ExternalDependency.MavenCoordinates _maven_2 = it.getMaven();
        _maven_2.setScope(Scope.TESTRUNTIME);
        ExternalDependency.MavenCoordinates _maven_3 = it.getMaven();
        _maven_3.setGradleOnly(true);
      };
      ExternalDependency _doubleArrow_3 = ObjectExtensions.<ExternalDependency>operator_doubleArrow(_externalDependency_3, _function_3);
      deps.add(_doubleArrow_3);
    }
    return deps;
  }

  @Override
  public PomFile pom() {
    PomFile _pom = super.pom();
    final Procedure1<PomFile> _function = (PomFile it) -> {
      String _xifexpression = null;
      boolean _isEclipsePluginProject = this.isEclipsePluginProject();
      if (_isEclipsePluginProject) {
        _xifexpression = "eclipse-test-plugin";
      } else {
        _xifexpression = "jar";
      }
      it.setPackaging(_xifexpression);
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("<build>");
      _builder.newLine();
      {
        if (((!this.isEclipsePluginProject()) && Objects.equals(this.getConfig().getSourceLayout(), SourceLayout.PLAIN))) {
          _builder.append("\t");
          _builder.append("<testSourceDirectory>");
          String _sourceFolder = this.sourceFolder(Outlet.TEST_JAVA);
          _builder.append(_sourceFolder, "\t");
          _builder.append("</testSourceDirectory>");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.append("<testResources>");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("\t");
          _builder.append("<testResource>");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("\t\t");
          _builder.append("<directory>");
          String _sourceFolder_1 = this.sourceFolder(Outlet.TEST_RESOURCES);
          _builder.append(_sourceFolder_1, "\t\t\t");
          _builder.append("</directory>");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.append("\t\t");
          _builder.append("<excludes>");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("\t\t\t");
          _builder.append("<exclude>**/*.java</exclude>");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("\t\t\t");
          _builder.append("<exclude>**/*.xtend</exclude>");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("\t\t");
          _builder.append("</excludes>");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("\t");
          _builder.append("</testResource>");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("</testResources>");
          _builder.newLine();
        }
      }
      _builder.append("\t");
      _builder.append("<plugins>");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("<plugin>");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("<groupId>org.eclipse.xtext</groupId>");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("<artifactId>xtend-maven-plugin</artifactId>");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("</plugin>");
      _builder.newLine();
      {
        if ((this.isEclipsePluginProject() && this.needsUiHarness())) {
          _builder.append("\t\t");
          _builder.append("<plugin>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t");
          _builder.append("<groupId>org.eclipse.tycho</groupId>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t");
          _builder.append("<artifactId>tycho-surefire-plugin</artifactId>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t");
          _builder.append("<configuration>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t");
          _builder.append("<useUIHarness>true</useUIHarness>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t");
          _builder.append("</configuration>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("</plugin>");
          _builder.newLine();
        }
      }
      {
        boolean _isEclipsePluginProject_1 = this.isEclipsePluginProject();
        boolean _not = (!_isEclipsePluginProject_1);
        if (_not) {
          _builder.append("\t\t");
          _builder.append("<plugin>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t");
          _builder.append("<groupId>org.codehaus.mojo</groupId>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t");
          _builder.append("<artifactId>build-helper-maven-plugin</artifactId>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t");
          _builder.append("<version>3.3.0</version>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t");
          _builder.append("<executions>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t");
          _builder.append("<execution>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t\t");
          _builder.append("<?m2e ignore?>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t\t");
          _builder.append("<id>add-test-source</id>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t\t");
          _builder.append("<phase>initialize</phase>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t\t");
          _builder.append("<goals>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t\t\t");
          _builder.append("<goal>add-test-source</goal>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t\t\t");
          _builder.append("<goal>add-test-resource</goal>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t\t");
          _builder.append("</goals>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t\t");
          _builder.append("<configuration>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t\t\t");
          _builder.append("<sources>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t\t\t\t");
          _builder.append("<source>");
          String _sourceFolder_2 = this.sourceFolder(Outlet.TEST_SRC_GEN);
          _builder.append(_sourceFolder_2, "\t\t\t\t\t\t\t");
          _builder.append("</source>");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t");
          _builder.append("\t\t\t\t");
          _builder.append("</sources>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t\t\t");
          _builder.append("<resources>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t\t\t\t");
          _builder.append("<resource>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t\t\t\t\t");
          _builder.append("<directory>");
          String _sourceFolder_3 = this.sourceFolder(Outlet.TEST_SRC_GEN);
          _builder.append(_sourceFolder_3, "\t\t\t\t\t\t\t\t");
          _builder.append("</directory>");
          _builder.newLineIfNotEmpty();
          _builder.append("\t\t");
          _builder.append("\t\t\t\t\t\t");
          _builder.append("<excludes>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t\t\t\t\t\t");
          _builder.append("<exclude>**/*.java</exclude>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t\t\t\t\t");
          _builder.append("</excludes>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t\t\t\t");
          _builder.append("</resource>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t\t\t");
          _builder.append("</resources>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t\t");
          _builder.append("</configuration>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t\t");
          _builder.append("</execution>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("\t");
          _builder.append("</executions>");
          _builder.newLine();
          _builder.append("\t\t");
          _builder.append("</plugin>");
          _builder.newLine();
        }
      }
      _builder.append("\t");
      _builder.append("</plugins>");
      _builder.newLine();
      _builder.append("</build>");
      _builder.newLine();
      it.setBuildSection(_builder.toString());
    };
    return ObjectExtensions.<PomFile>operator_doubleArrow(_pom, _function);
  }

  public boolean needsUiHarness() {
    return false;
  }

  @Override
  public GradleBuildFile buildGradle() {
    GradleBuildFile _buildGradle = super.buildGradle();
    final Procedure1<GradleBuildFile> _function = (GradleBuildFile it) -> {
      JUnitVersion _junitVersion = this.getConfig().getJunitVersion();
      boolean _equals = Objects.equals(_junitVersion, JUnitVersion.JUNIT_5);
      if (_equals) {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("test {");
        _builder.newLine();
        _builder.append("\t");
        _builder.append("useJUnitPlatform()");
        _builder.newLine();
        _builder.append("}");
        _builder.newLine();
        it.setAdditionalContent(_builder.toString());
      }
    };
    return ObjectExtensions.<GradleBuildFile>operator_doubleArrow(_buildGradle, _function);
  }

  @Pure
  public ProjectDescriptor getTestedProject() {
    return this.testedProject;
  }
}
