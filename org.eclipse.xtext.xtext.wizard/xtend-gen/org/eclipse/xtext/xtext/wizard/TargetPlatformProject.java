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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.util.JUnitVersion;
import org.eclipse.xtext.util.JavaVersion;
import org.eclipse.xtext.util.XtextVersion;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

@FinalFieldsConstructor
@SuppressWarnings("all")
public class TargetPlatformProject extends ProjectDescriptor {
  @Override
  public String getNameQualifier() {
    return ".target";
  }

  @Override
  public boolean isEclipsePluginProject() {
    return false;
  }

  @Override
  public boolean isPartOfGradleBuild() {
    return false;
  }

  @Override
  public boolean isPartOfMavenBuild() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return this.getConfig().needsTychoBuild();
  }

  @Override
  public void setEnabled(final boolean enabled) {
    throw new UnsupportedOperationException("The target platform project is activated automatically for Tycho builds");
  }

  @Override
  public Iterable<? extends AbstractFile> getFiles() {
    ArrayList<AbstractFile> _xblockexpression = null;
    {
      final ArrayList<AbstractFile> files = CollectionLiterals.<AbstractFile>newArrayList();
      Iterable<? extends AbstractFile> _files = super.getFiles();
      Iterables.<AbstractFile>addAll(files, _files);
      String _name = this.getName();
      String _plus = (_name + ".target");
      PlainTextFile _file = this.file(Outlet.ROOT, _plus, this.target());
      files.add(_file);
      _xblockexpression = files;
    }
    return _xblockexpression;
  }

  @Override
  public Set<SourceFolderDescriptor> getSourceFolders() {
    return Collections.<SourceFolderDescriptor>unmodifiableSet(CollectionLiterals.<SourceFolderDescriptor>newHashSet());
  }

  public CharSequence target() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>");
    _builder.newLine();
    _builder.append("<?pde version=\"3.8\"?>");
    _builder.newLine();
    _builder.append("<target name=\"");
    String _name = this.getName();
    _builder.append(_name);
    _builder.append("\" sequenceNumber=\"1\">");
    _builder.newLineIfNotEmpty();
    {
      boolean _isAtLeast = this.getConfig().getJavaVersion().isAtLeast(JavaVersion.JAVA21);
      if (_isAtLeast) {
        _builder.append("\t");
        _builder.append("<targetJRE path=\"org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-21\"/>");
        _builder.newLine();
      } else {
        _builder.append("\t");
        _builder.append("<targetJRE path=\"org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-17\"/>");
        _builder.newLine();
      }
    }
    _builder.append("\t");
    _builder.append("<locations>");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("<location includeAllPlatforms=\"false\" includeConfigurePhase=\"false\" includeMode=\"planner\" includeSource=\"true\" type=\"InstallableUnit\">");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<unit id=\"org.eclipse.jdt.feature.group\" version=\"0.0.0\"/>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<unit id=\"org.eclipse.platform.feature.group\" version=\"0.0.0\"/>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<unit id=\"org.eclipse.pde.feature.group\" version=\"0.0.0\"/>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<unit id=\"org.eclipse.draw2d.feature.group\" version=\"0.0.0\"/>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<unit id=\"org.eclipse.emf.sdk.feature.group\" version=\"0.0.0\"/>");
    _builder.newLine();
    {
      boolean _isAtLeast_1 = this.getConfig().getJavaVersion().isAtLeast(JavaVersion.JAVA21);
      if (_isAtLeast_1) {
        _builder.append("\t\t\t");
        _builder.append("<repository location=\"https://download.eclipse.org/releases/2025-06\"/>");
        _builder.newLine();
      } else {
        _builder.append("\t\t\t");
        _builder.append("<repository location=\"https://download.eclipse.org/releases/2024-12\"/>");
        _builder.newLine();
      }
    }
    _builder.append("\t\t");
    _builder.append("</location>");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("<location includeAllPlatforms=\"false\" includeConfigurePhase=\"false\" includeMode=\"planner\" includeSource=\"true\" type=\"InstallableUnit\">");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<unit id=\"org.eclipse.emf.mwe2.launcher.feature.group\" version=\"0.0.0\"/>");
    _builder.newLine();
    {
      boolean _matches = this.getConfig().getXtextVersion().getMweVersion().matches("\\d+\\.\\d+(\\.\\d+)+");
      if (_matches) {
        _builder.append("\t\t\t");
        _builder.append("<repository location=\"https://download.eclipse.org/modeling/emft/mwe/updates/releases/");
        String _mweVersion = this.getConfig().getXtextVersion().getMweVersion();
        _builder.append(_mweVersion, "\t\t\t");
        _builder.append("/\"/>");
        _builder.newLineIfNotEmpty();
      } else {
        _builder.append("\t\t\t");
        _builder.append("<repository location=\"https://download.eclipse.org/modeling/emft/mwe/updates/milestones/");
        String _mweBuildNumber = this.getConfig().getXtextVersion().getMweBuildNumber();
        _builder.append(_mweBuildNumber, "\t\t\t");
        _builder.append("/\"/>");
        _builder.newLineIfNotEmpty();
      }
    }
    _builder.append("\t\t");
    _builder.append("</location>");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("<location includeAllPlatforms=\"false\" includeConfigurePhase=\"false\" includeMode=\"planner\" includeSource=\"true\" type=\"InstallableUnit\">");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<unit id=\"org.eclipse.xtext.sdk.feature.group\" version=\"0.0.0\"/>");
    _builder.newLine();
    {
      boolean _isSnapshot = this.getConfig().getXtextVersion().isSnapshot();
      if (_isSnapshot) {
        _builder.append("\t\t\t");
        _builder.append("<repository location=\"https://download.eclipse.org/modeling/tmf/xtext/updates/nightly/\"/>");
        _builder.newLine();
      } else {
        boolean _isStable = this.getConfig().getXtextVersion().isStable();
        if (_isStable) {
          _builder.append("\t\t\t");
          _builder.append("<repository location=\"https://download.eclipse.org/modeling/tmf/xtext/updates/milestones/\"/>");
          _builder.newLine();
        } else {
          _builder.append("\t\t\t");
          _builder.append("<repository location=\"https://download.eclipse.org/modeling/tmf/xtext/updates/releases/");
          XtextVersion _xtextVersion = this.getConfig().getXtextVersion();
          _builder.append(_xtextVersion, "\t\t\t");
          _builder.append("/\"/>");
          _builder.newLineIfNotEmpty();
        }
      }
    }
    _builder.append("\t\t");
    _builder.append("</location>");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("<location includeAllPlatforms=\"false\" includeConfigurePhase=\"false\" includeMode=\"planner\" includeSource=\"true\" type=\"InstallableUnit\">");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<unit id=\"com.google.gson\" version=\"2.12.1\"/>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<unit id=\"com.google.inject\" version=\"7.0.0\"/>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<unit id=\"jakarta.inject.jakarta.inject-api\" version=\"2.0.1\"/>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<unit id=\"org.antlr.runtime\" version=\"3.2.0.v20230929-1400\"/>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<unit id=\"org.junit\" version=\"0.0.0\"/>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<unit id=\"org.hamcrest\" version=\"2.2.0\"/>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<unit id=\"org.hamcrest.core\" version=\"2.2.0.v20230809-1000\"/>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<unit id=\"org.apache.commons.commons-logging\" version=\"0.0.0\"/>");
    _builder.newLine();
    {
      JUnitVersion _junitVersion = this.getConfig().getJunitVersion();
      boolean _equals = Objects.equals(_junitVersion, JUnitVersion.JUNIT_5);
      if (_equals) {
        _builder.append("\t\t\t");
        _builder.append("<unit id=\"org.apiguardian.api\" version=\"0.0.0\"/>");
        _builder.newLine();
        _builder.append("\t\t\t");
        _builder.append("<unit id=\"junit-jupiter-api\" version=\"0.0.0\"/>");
        _builder.newLine();
        _builder.append("\t\t\t");
        _builder.append("<unit id=\"junit-jupiter-engine\" version=\"0.0.0\"/>");
        _builder.newLine();
        _builder.append("\t\t\t");
        _builder.append("<unit id=\"junit-jupiter-migrationsupport\" version=\"0.0.0\"/>");
        _builder.newLine();
        _builder.append("\t\t\t");
        _builder.append("<unit id=\"junit-jupiter-params\" version=\"0.0.0\"/>");
        _builder.newLine();
        _builder.append("\t\t\t");
        _builder.append("<unit id=\"junit-platform-commons\" version=\"0.0.0\"/>");
        _builder.newLine();
        _builder.append("\t\t\t");
        _builder.append("<unit id=\"junit-platform-engine\" version=\"0.0.0\"/>");
        _builder.newLine();
        _builder.append("\t\t\t");
        _builder.append("<unit id=\"junit-platform-launcher\" version=\"0.0.0\"/>");
        _builder.newLine();
        _builder.append("\t\t\t");
        _builder.append("<unit id=\"junit-platform-runner\" version=\"0.0.0\"/>");
        _builder.newLine();
        _builder.append("\t\t\t");
        _builder.append("<unit id=\"junit-platform-suite-commons\" version=\"0.0.0\"/>");
        _builder.newLine();
        _builder.append("\t\t\t");
        _builder.append("<unit id=\"junit-vintage-engine\" version=\"0.0.0\"/>");
        _builder.newLine();
        _builder.append("\t\t\t");
        _builder.append("<unit id=\"org.opentest4j\" version=\"0.0.0\"/>");
        _builder.newLine();
      }
    }
    _builder.append("\t\t\t");
    _builder.append("<unit id=\"org.objectweb.asm\" version=\"9.7.1\"/>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<unit id=\"io.github.classgraph.classgraph\" version=\"0.0.0\"/>");
    _builder.newLine();
    _builder.append("\t\t\t");
    _builder.append("<repository location=\"https://download.eclipse.org/tools/orbit/simrel/orbit-aggregation/2025-03\"/>");
    _builder.newLine();
    _builder.append("\t\t");
    _builder.append("</location>");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("</locations>");
    _builder.newLine();
    _builder.append("</target>");
    _builder.newLine();
    return _builder;
  }

  @Override
  public GradleBuildFile buildGradle() {
    throw new UnsupportedOperationException("Eclipse target platforms are not yet supported in Gradle");
  }

  @Override
  public PomFile pom() {
    PomFile _pom = super.pom();
    final Procedure1<PomFile> _function = (PomFile it) -> {
      it.setPackaging("eclipse-target-definition");
    };
    return ObjectExtensions.<PomFile>operator_doubleArrow(_pom, _function);
  }

  public TargetPlatformProject(final WizardConfiguration config) {
    super(config);
  }
}
