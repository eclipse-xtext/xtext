/**
 * Copyright (c) 2015, 2025 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.xtext.wizard;

import com.google.common.collect.Iterables;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

@FinalFieldsConstructor
@SuppressWarnings("all")
public class WebProjectDescriptor extends ProjectDescriptor {
  private static final String REQUIREJS_VERSION = "2.3.7";

  private static final String JQUERY_VERSION = "3.6.0";

  private static final String JETTY_VERSION = "11.0.25";

  private static final String SLF4J_VERSION = "2.0.5";

  private static final String ACE_VERSION = "1.3.3";

  @Override
  public Set<? extends ProjectDescriptor> getUpstreamProjects() {
    return CollectionLiterals.<ProjectDescriptor>newLinkedHashSet(this.getConfig().getRuntimeProject(), this.getConfig().getIdeProject());
  }

  @Override
  public String getNameQualifier() {
    return ".web";
  }

  @Override
  public boolean isEclipsePluginProject() {
    return false;
  }

  @Override
  public boolean isPartOfGradleBuild() {
    BuildSystem _preferredBuildSystem = this.getConfig().getPreferredBuildSystem();
    return Objects.equals(_preferredBuildSystem, BuildSystem.GRADLE);
  }

  @Override
  public boolean isPartOfMavenBuild() {
    BuildSystem _preferredBuildSystem = this.getConfig().getPreferredBuildSystem();
    return Objects.equals(_preferredBuildSystem, BuildSystem.MAVEN);
  }

  @Override
  public Set<ExternalDependency> getExternalDependencies() {
    final LinkedHashSet<ExternalDependency> deps = CollectionLiterals.<ExternalDependency>newLinkedHashSet();
    Set<ExternalDependency> _externalDependencies = super.getExternalDependencies();
    Iterables.<ExternalDependency>addAll(deps, _externalDependencies);
    ExternalDependency _createXtextDependency = ExternalDependency.createXtextDependency("org.eclipse.xtext.xbase.web");
    deps.add(_createXtextDependency);
    ExternalDependency _createXtextDependency_1 = ExternalDependency.createXtextDependency("org.eclipse.xtext.web.servlet");
    deps.add(_createXtextDependency_1);
    ExternalDependency _createMavenDependency = ExternalDependency.createMavenDependency("org.eclipse.xtext:org.eclipse.xtend.lib:${xtextVersion}");
    deps.add(_createMavenDependency);
    ExternalDependency _createMavenDependency_1 = ExternalDependency.createMavenDependency(("org.webjars:requirejs:" + WebProjectDescriptor.REQUIREJS_VERSION));
    deps.add(_createMavenDependency_1);
    ExternalDependency _createMavenDependency_2 = ExternalDependency.createMavenDependency(("org.webjars:jquery:" + WebProjectDescriptor.JQUERY_VERSION));
    deps.add(_createMavenDependency_2);
    ExternalDependency _createMavenDependency_3 = ExternalDependency.createMavenDependency(("org.webjars:ace:" + WebProjectDescriptor.ACE_VERSION));
    deps.add(_createMavenDependency_3);
    ExternalDependency _createMavenDependency_4 = ExternalDependency.createMavenDependency(("org.eclipse.jetty:jetty-annotations:" + WebProjectDescriptor.JETTY_VERSION));
    final Procedure1<ExternalDependency> _function = (ExternalDependency it) -> {
      ExternalDependency.MavenCoordinates _maven = it.getMaven();
      _maven.setScope(Scope.PROVIDED);
    };
    ExternalDependency _doubleArrow = ObjectExtensions.<ExternalDependency>operator_doubleArrow(_createMavenDependency_4, _function);
    deps.add(_doubleArrow);
    ExternalDependency _createMavenDependency_5 = ExternalDependency.createMavenDependency(("org.slf4j:slf4j-simple:" + WebProjectDescriptor.SLF4J_VERSION));
    final Procedure1<ExternalDependency> _function_1 = (ExternalDependency it) -> {
      ExternalDependency.MavenCoordinates _maven = it.getMaven();
      _maven.setScope(Scope.PROVIDED);
    };
    ExternalDependency _doubleArrow_1 = ObjectExtensions.<ExternalDependency>operator_doubleArrow(_createMavenDependency_5, _function_1);
    deps.add(_doubleArrow_1);
    return deps;
  }

  @Override
  public GradleBuildFile buildGradle() {
    GradleBuildFile _buildGradle = super.buildGradle();
    final Procedure1<GradleBuildFile> _function = (GradleBuildFile it) -> {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("plugins {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("id \'war\'");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      it.setPluginsSection(_builder.toString());
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("task jettyRun(type:JavaExec) {");
      _builder_1.newLine();
      _builder_1.append("\t");
      _builder_1.append("dependsOn(sourceSets.main.runtimeClasspath)");
      _builder_1.newLine();
      _builder_1.append("\t");
      _builder_1.append("classpath = sourceSets.main.runtimeClasspath.filter{it.exists()}");
      _builder_1.newLine();
      _builder_1.append("\t");
      _builder_1.append("mainClass = \'");
      String _basePackage = this.getConfig().getLanguage().getBasePackage();
      _builder_1.append(_basePackage, "\t");
      _builder_1.append(".web.ServerLauncher\'");
      _builder_1.newLineIfNotEmpty();
      _builder_1.append("\t");
      _builder_1.append("standardInput = System.in");
      _builder_1.newLine();
      _builder_1.append("\t");
      _builder_1.append("group = \'run\'");
      _builder_1.newLine();
      _builder_1.append("\t");
      _builder_1.append("description = \'Starts an example Jetty server with your language\'");
      _builder_1.newLine();
      _builder_1.append("}");
      _builder_1.newLine();
      it.setAdditionalContent(_builder_1.toString());
    };
    return ObjectExtensions.<GradleBuildFile>operator_doubleArrow(_buildGradle, _function);
  }

  @Override
  public PomFile pom() {
    PomFile _pom = super.pom();
    final Procedure1<PomFile> _function = (PomFile it) -> {
      it.setPackaging("war");
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("<build>");
      _builder.newLine();
      {
        SourceLayout _sourceLayout = this.getConfig().getSourceLayout();
        boolean _equals = Objects.equals(_sourceLayout, SourceLayout.PLAIN);
        if (_equals) {
          _builder.append("\t");
          _builder.append("<sourceDirectory>");
          String _sourceFolder = this.sourceFolder(Outlet.MAIN_JAVA);
          _builder.append(_sourceFolder, "\t");
          _builder.append("</sourceDirectory>");
          _builder.newLineIfNotEmpty();
          _builder.append("\t");
          _builder.append("<resources>");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("\t");
          _builder.append("<resource>");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("\t\t");
          _builder.append("<directory>");
          String _sourceFolder_1 = this.sourceFolder(Outlet.MAIN_RESOURCES);
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
          _builder.append("</resource>");
          _builder.newLine();
          _builder.append("\t");
          _builder.append("</resources>");
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
      _builder.append("\t\t");
      _builder.append("<plugin>");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("<artifactId>maven-war-plugin</artifactId>");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("<version>3.4.0</version>");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("<configuration>");
      _builder.newLine();
      _builder.append("\t\t\t\t");
      _builder.append("<warSourceDirectory>");
      String _sourceFolder_2 = this.sourceFolder(Outlet.WEBAPP);
      _builder.append(_sourceFolder_2, "\t\t\t\t");
      _builder.append("</warSourceDirectory>");
      _builder.newLineIfNotEmpty();
      _builder.append("\t\t\t\t");
      _builder.append("<failOnMissingWebXml>false</failOnMissingWebXml>");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("</configuration>");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("</plugin>");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("<plugin>");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("<groupId>org.eclipse.jetty</groupId>");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("<artifactId>jetty-maven-plugin</artifactId>");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("<version>");
      _builder.append(WebProjectDescriptor.JETTY_VERSION, "\t\t\t");
      _builder.append("</version>");
      _builder.newLineIfNotEmpty();
      _builder.append("\t\t\t");
      _builder.append("<configuration>");
      _builder.newLine();
      _builder.append("\t\t\t\t");
      _builder.append("<webAppSourceDirectory>");
      String _sourceFolder_3 = this.sourceFolder(Outlet.WEBAPP);
      _builder.append(_sourceFolder_3, "\t\t\t\t");
      _builder.append("</webAppSourceDirectory>");
      _builder.newLineIfNotEmpty();
      _builder.append("\t\t\t");
      _builder.append("</configuration>");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("</plugin>");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("<plugin>");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("<groupId>org.codehaus.mojo</groupId>");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("<artifactId>build-helper-maven-plugin</artifactId>");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("<version>3.3.0</version>");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("<executions>");
      _builder.newLine();
      _builder.append("\t\t\t\t");
      _builder.append("<execution>");
      _builder.newLine();
      _builder.append("\t\t\t\t\t");
      _builder.append("<?m2e ignore?>");
      _builder.newLine();
      _builder.append("\t\t\t\t\t");
      _builder.append("<id>add-source</id>");
      _builder.newLine();
      _builder.append("\t\t\t\t\t");
      _builder.append("<phase>initialize</phase>");
      _builder.newLine();
      _builder.append("\t\t\t\t\t");
      _builder.append("<goals>");
      _builder.newLine();
      _builder.append("\t\t\t\t\t\t");
      _builder.append("<goal>add-source</goal>");
      _builder.newLine();
      _builder.append("\t\t\t\t\t\t");
      _builder.append("<goal>add-resource</goal>");
      _builder.newLine();
      _builder.append("\t\t\t\t\t");
      _builder.append("</goals>");
      _builder.newLine();
      _builder.append("\t\t\t\t\t");
      _builder.append("<configuration>");
      _builder.newLine();
      _builder.append("\t\t\t\t\t\t");
      _builder.append("<sources>");
      _builder.newLine();
      _builder.append("\t\t\t\t\t\t\t");
      _builder.append("<source>");
      String _sourceFolder_4 = this.sourceFolder(Outlet.MAIN_SRC_GEN);
      _builder.append(_sourceFolder_4, "\t\t\t\t\t\t\t");
      _builder.append("</source>");
      _builder.newLineIfNotEmpty();
      _builder.append("\t\t\t\t\t\t");
      _builder.append("</sources>");
      _builder.newLine();
      _builder.append("\t\t\t\t\t\t");
      _builder.append("<resources>");
      _builder.newLine();
      _builder.append("\t\t\t\t\t\t\t");
      _builder.append("<resource>");
      _builder.newLine();
      _builder.append("\t\t\t\t\t\t\t\t");
      _builder.append("<directory>");
      String _sourceFolder_5 = this.sourceFolder(Outlet.MAIN_SRC_GEN);
      _builder.append(_sourceFolder_5, "\t\t\t\t\t\t\t\t");
      _builder.append("</directory>");
      _builder.newLineIfNotEmpty();
      _builder.append("\t\t\t\t\t\t\t\t");
      _builder.append("<excludes>");
      _builder.newLine();
      _builder.append("\t\t\t\t\t\t\t\t\t");
      _builder.append("<exclude>**/*.java</exclude>");
      _builder.newLine();
      _builder.append("\t\t\t\t\t\t\t\t");
      _builder.append("</excludes>");
      _builder.newLine();
      _builder.append("\t\t\t\t\t\t\t");
      _builder.append("</resource>");
      _builder.newLine();
      _builder.append("\t\t\t\t\t\t");
      _builder.append("</resources>");
      _builder.newLine();
      _builder.append("\t\t\t\t\t");
      _builder.append("</configuration>");
      _builder.newLine();
      _builder.append("\t\t\t\t");
      _builder.append("</execution>");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("</executions>");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("</plugin>");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("</plugins>");
      _builder.newLine();
      _builder.append("</build>");
      _builder.newLine();
      it.setBuildSection(_builder.toString());
    };
    return ObjectExtensions.<PomFile>operator_doubleArrow(_pom, _function);
  }

  public WebProjectDescriptor(final WizardConfiguration config) {
    super(config);
  }
}
