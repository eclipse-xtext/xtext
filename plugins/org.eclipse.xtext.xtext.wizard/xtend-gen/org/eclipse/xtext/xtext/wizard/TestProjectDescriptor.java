/**
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtext.xtext.wizard;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xtext.wizard.ExternalDependency;
import org.eclipse.xtext.xtext.wizard.Outlet;
import org.eclipse.xtext.xtext.wizard.PomFile;
import org.eclipse.xtext.xtext.wizard.ProjectDescriptor;
import org.eclipse.xtext.xtext.wizard.SourceLayout;
import org.eclipse.xtext.xtext.wizard.TestedProjectDescriptor;
import org.eclipse.xtext.xtext.wizard.WizardConfiguration;

@SuppressWarnings("all")
public abstract class TestProjectDescriptor extends ProjectDescriptor {
  @Accessors
  private final ProjectDescriptor testedProject;
  
  public TestProjectDescriptor(final TestedProjectDescriptor testedProject) {
    super(testedProject.getConfig());
    this.testedProject = testedProject;
  }
  
  @Override
  public Set<String> getSourceFolders() {
    final Function1<Outlet, String> _function = new Function1<Outlet, String>() {
      @Override
      public String apply(final Outlet it) {
        return TestProjectDescriptor.this.sourceFolder(it);
      }
    };
    List<String> _map = ListExtensions.<Outlet, String>map(Collections.<Outlet>unmodifiableList(CollectionLiterals.<Outlet>newArrayList(Outlet.TEST_JAVA, Outlet.TEST_RESOURCES, Outlet.TEST_SRC_GEN, Outlet.TEST_XTEND_GEN)), _function);
    return IterableExtensions.<String>toSet(_map);
  }
  
  public boolean isInlined() {
    boolean _and = false;
    boolean _isEnabled = this.isEnabled();
    if (!_isEnabled) {
      _and = false;
    } else {
      WizardConfiguration _config = this.getConfig();
      SourceLayout _sourceLayout = _config.getSourceLayout();
      boolean _notEquals = (!Objects.equal(_sourceLayout, SourceLayout.PLAIN));
      _and = _notEquals;
    }
    return _and;
  }
  
  public boolean isSeparate() {
    boolean _and = false;
    boolean _isEnabled = this.isEnabled();
    if (!_isEnabled) {
      _and = false;
    } else {
      WizardConfiguration _config = this.getConfig();
      SourceLayout _sourceLayout = _config.getSourceLayout();
      boolean _equals = Objects.equal(_sourceLayout, SourceLayout.PLAIN);
      _and = _equals;
    }
    return _and;
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
    ExternalDependency _externalDependency = new ExternalDependency();
    final Procedure1<ExternalDependency> _function = new Procedure1<ExternalDependency>() {
      @Override
      public void apply(final ExternalDependency it) {
        ExternalDependency.P2Coordinates _p2 = it.getP2();
        _p2.setBundleId("org.junit");
        ExternalDependency.P2Coordinates _p2_1 = it.getP2();
        _p2_1.setVersion("4.7.0");
      }
    };
    ExternalDependency _doubleArrow = ObjectExtensions.<ExternalDependency>operator_doubleArrow(_externalDependency, _function);
    deps.add(_doubleArrow);
    return deps;
  }
  
  @Override
  public PomFile pom() {
    PomFile _pom = super.pom();
    final Procedure1<PomFile> _function = new Procedure1<PomFile>() {
      @Override
      public void apply(final PomFile it) {
        String _xifexpression = null;
        boolean _isEclipsePluginProject = TestProjectDescriptor.this.isEclipsePluginProject();
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
          boolean _and = false;
          boolean _isEclipsePluginProject_1 = TestProjectDescriptor.this.isEclipsePluginProject();
          boolean _not = (!_isEclipsePluginProject_1);
          if (!_not) {
            _and = false;
          } else {
            WizardConfiguration _config = TestProjectDescriptor.this.getConfig();
            SourceLayout _sourceLayout = _config.getSourceLayout();
            boolean _equals = Objects.equal(_sourceLayout, SourceLayout.PLAIN);
            _and = _equals;
          }
          if (_and) {
            _builder.append("\t");
            _builder.append("<testSourceDirectory>");
            String _sourceFolder = TestProjectDescriptor.this.sourceFolder(Outlet.TEST_JAVA);
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
            String _sourceFolder_1 = TestProjectDescriptor.this.sourceFolder(Outlet.TEST_RESOURCES);
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
        _builder.append("<groupId>org.eclipse.xtend</groupId>");
        _builder.newLine();
        _builder.append("\t\t\t");
        _builder.append("<artifactId>xtend-maven-plugin</artifactId>");
        _builder.newLine();
        _builder.append("\t\t");
        _builder.append("</plugin>");
        _builder.newLine();
        {
          boolean _isEclipsePluginProject_2 = TestProjectDescriptor.this.isEclipsePluginProject();
          if (_isEclipsePluginProject_2) {
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
            _builder.append("<version>${tycho-version}</version>");
            _builder.newLine();
            _builder.append("\t\t");
            _builder.append("\t");
            _builder.append("<configuration>");
            _builder.newLine();
            _builder.append("\t\t");
            _builder.append("\t\t");
            _builder.append("<failIfNoTests>false</failIfNoTests>");
            _builder.newLine();
            {
              boolean _needsUiHarness = TestProjectDescriptor.this.needsUiHarness();
              if (_needsUiHarness) {
                _builder.append("\t\t");
                _builder.append("\t\t");
                _builder.append("<useUIHarness>true</useUIHarness>");
                _builder.newLine();
              }
            }
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
          boolean _isEclipsePluginProject_3 = TestProjectDescriptor.this.isEclipsePluginProject();
          boolean _not_1 = (!_isEclipsePluginProject_3);
          if (_not_1) {
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
            _builder.append("<version>1.9.1</version>");
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
            String _sourceFolder_2 = TestProjectDescriptor.this.sourceFolder(Outlet.TEST_SRC_GEN);
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
            String _sourceFolder_3 = TestProjectDescriptor.this.sourceFolder(Outlet.TEST_SRC_GEN);
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
        boolean _and_1 = false;
        boolean _isEclipsePluginProject_4 = TestProjectDescriptor.this.isEclipsePluginProject();
        if (!_isEclipsePluginProject_4) {
          _and_1 = false;
        } else {
          boolean _needsUiHarness_1 = TestProjectDescriptor.this.needsUiHarness();
          _and_1 = _needsUiHarness_1;
        }
        if (_and_1) {
          StringConcatenation _builder_1 = new StringConcatenation();
          _builder_1.append("<profiles>");
          _builder_1.newLine();
          _builder_1.append("\t");
          _builder_1.append("<profile>");
          _builder_1.newLine();
          _builder_1.append("\t\t");
          _builder_1.append("<id>testing-on-mac</id>");
          _builder_1.newLine();
          _builder_1.append("\t\t");
          _builder_1.append("<activation>");
          _builder_1.newLine();
          _builder_1.append("\t\t\t");
          _builder_1.append("<os>");
          _builder_1.newLine();
          _builder_1.append("\t\t\t\t");
          _builder_1.append("<family>mac</family>");
          _builder_1.newLine();
          _builder_1.append("\t\t\t");
          _builder_1.append("</os>");
          _builder_1.newLine();
          _builder_1.append("\t\t");
          _builder_1.append("</activation>");
          _builder_1.newLine();
          _builder_1.append("\t\t");
          _builder_1.append("<properties>");
          _builder_1.newLine();
          _builder_1.append("\t\t\t");
          _builder_1.append("<tycho.testArgLine>-XstartOnFirstThread</tycho.testArgLine>");
          _builder_1.newLine();
          _builder_1.append("\t\t");
          _builder_1.append("</properties>");
          _builder_1.newLine();
          _builder_1.append("\t");
          _builder_1.append("</profile>");
          _builder_1.newLine();
          _builder_1.append("</profiles>");
          _builder_1.newLine();
          it.setProfileSection(_builder_1.toString());
        }
      }
    };
    return ObjectExtensions.<PomFile>operator_doubleArrow(_pom, _function);
  }
  
  public boolean needsUiHarness() {
    return false;
  }
  
  @Pure
  public ProjectDescriptor getTestedProject() {
    return this.testedProject;
  }
}
