/**
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.xtext.wizard;

import com.google.common.collect.Iterables;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

@SuppressWarnings("all")
public class UiProjectDescriptor extends TestedProjectDescriptor {
  private UiTestProjectDescriptor testProject;

  public UiProjectDescriptor(final WizardConfiguration config) {
    super(config);
    UiTestProjectDescriptor _uiTestProjectDescriptor = new UiTestProjectDescriptor(this);
    this.testProject = _uiTestProjectDescriptor;
  }

  @Override
  public TestProjectDescriptor getTestProject() {
    return this.testProject;
  }

  @Override
  public Set<? extends ProjectDescriptor> getUpstreamProjects() {
    RuntimeProjectDescriptor _runtimeProject = this.getConfig().getRuntimeProject();
    IdeProjectDescriptor _ideProject = this.getConfig().getIdeProject();
    final Function1<ProjectDescriptor, Boolean> _function = (ProjectDescriptor it) -> {
      return Boolean.valueOf(it.isEnabled());
    };
    return IterableExtensions.toSet(IterableExtensions.filter(Collections.<ProjectDescriptor>unmodifiableList(CollectionLiterals.<ProjectDescriptor>newArrayList(_runtimeProject, _ideProject)), _function));
  }

  @Override
  public String getNameQualifier() {
    return ".ui";
  }

  @Override
  public boolean isEclipsePluginProject() {
    return true;
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
  public Set<ExternalDependency> getExternalDependencies() {
    LinkedHashSet<ExternalDependency> _xblockexpression = null;
    {
      final LinkedHashSet<ExternalDependency> deps = CollectionLiterals.<ExternalDependency>newLinkedHashSet();
      Set<ExternalDependency> _externalDependencies = super.getExternalDependencies();
      Iterables.<ExternalDependency>addAll(deps, _externalDependencies);
      ExternalDependency _createXtextDependency = ExternalDependency.createXtextDependency("org.eclipse.xtext.ui");
      deps.add(_createXtextDependency);
      ExternalDependency _createXtextDependency_1 = ExternalDependency.createXtextDependency("org.eclipse.xtext.ui.shared");
      deps.add(_createXtextDependency_1);
      ExternalDependency _createXtextDependency_2 = ExternalDependency.createXtextDependency("org.eclipse.xtext.ui.codetemplates.ui");
      deps.add(_createXtextDependency_2);
      ExternalDependency _externalDependency = new ExternalDependency();
      final Procedure1<ExternalDependency> _function = (ExternalDependency it) -> {
        final Procedure1<ExternalDependency.P2Coordinates> _function_1 = (ExternalDependency.P2Coordinates it_1) -> {
          it_1.setBundleId("org.eclipse.ui.editors");
          it_1.setVersion("3.14.300");
        };
        it.p2(_function_1);
      };
      ExternalDependency _doubleArrow = ObjectExtensions.<ExternalDependency>operator_doubleArrow(_externalDependency, _function);
      deps.add(_doubleArrow);
      ExternalDependency _externalDependency_1 = new ExternalDependency();
      final Procedure1<ExternalDependency> _function_1 = (ExternalDependency it) -> {
        final Procedure1<ExternalDependency.P2Coordinates> _function_2 = (ExternalDependency.P2Coordinates it_1) -> {
          it_1.setBundleId("org.eclipse.ui.ide");
          it_1.setVersion("3.18.500");
        };
        it.p2(_function_2);
      };
      ExternalDependency _doubleArrow_1 = ObjectExtensions.<ExternalDependency>operator_doubleArrow(_externalDependency_1, _function_1);
      deps.add(_doubleArrow_1);
      boolean _isEnabled = this.getConfig().getIdeProject().isEnabled();
      boolean _not = (!_isEnabled);
      if (_not) {
        Set<ExternalDependency> _externalDependencies_1 = this.getConfig().getIdeProject().getExternalDependencies();
        Iterables.<ExternalDependency>addAll(deps, _externalDependencies_1);
      }
      _xblockexpression = deps;
    }
    return _xblockexpression;
  }

  @Override
  public Set<String> getImportedPackages() {
    final Set<String> packs = super.getImportedPackages();
    packs.add("org.apache.log4j");
    return packs;
  }

  @Override
  public Set<String> getBinIncludes() {
    LinkedHashSet<String> _xblockexpression = null;
    {
      final LinkedHashSet<String> includes = CollectionLiterals.<String>newLinkedHashSet();
      Set<String> _binIncludes = super.getBinIncludes();
      Iterables.<String>addAll(includes, _binIncludes);
      includes.add("plugin.xml");
      _xblockexpression = includes;
    }
    return _xblockexpression;
  }

  @Override
  public PomFile pom() {
    PomFile _pom = super.pom();
    final Procedure1<PomFile> _function = (PomFile it) -> {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("<build>");
      _builder.newLine();
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
      _builder.append("\t");
      _builder.append("</plugins>");
      _builder.newLine();
      _builder.append("</build>");
      _builder.newLine();
      it.setBuildSection(_builder.toString());
      it.setPackaging("eclipse-plugin");
    };
    return ObjectExtensions.<PomFile>operator_doubleArrow(_pom, _function);
  }
}
