/**
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtext.xtext.wizard;

import java.util.Set;
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.util.XtextVersion;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xtext.wizard.GradleBuildFile;
import org.eclipse.xtext.xtext.wizard.IdeProjectDescriptor;
import org.eclipse.xtext.xtext.wizard.ProjectDescriptor;
import org.eclipse.xtext.xtext.wizard.RuntimeProjectDescriptor;
import org.eclipse.xtext.xtext.wizard.WizardConfiguration;

@FinalFieldsConstructor
@SuppressWarnings("all")
public class IntellijProjectDescriptor extends ProjectDescriptor {
  @Override
  public String getNameQualifier() {
    return ".idea";
  }
  
  @Override
  public boolean isEclipsePluginProject() {
    return false;
  }
  
  @Override
  public boolean isPartOfGradleBuild() {
    return true;
  }
  
  @Override
  public boolean isPartOfMavenBuild() {
    return false;
  }
  
  @Override
  public Set<? extends ProjectDescriptor> getUpstreamProjects() {
    WizardConfiguration _config = this.getConfig();
    RuntimeProjectDescriptor _runtimeProject = _config.getRuntimeProject();
    WizardConfiguration _config_1 = this.getConfig();
    IdeProjectDescriptor _ideProject = _config_1.getIdeProject();
    return CollectionLiterals.<ProjectDescriptor>newLinkedHashSet(_runtimeProject, _ideProject);
  }
  
  @Override
  public GradleBuildFile buildGradle() {
    GradleBuildFile _buildGradle = super.buildGradle();
    final Procedure1<GradleBuildFile> _function = new Procedure1<GradleBuildFile>() {
      @Override
      public void apply(final GradleBuildFile it) {
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("//see https://github.com/xtext/xtext-gradle-plugin/tree/master/xtext-idea-gradle-plugin");
        _builder.newLine();
        _builder.append("apply plugin: \'org.xtext.idea-plugin\'");
        _builder.newLine();
        _builder.append("apply plugin: \'org.xtext.idea-repository\'");
        _builder.newLine();
        _builder.newLine();
        it.setPluginsSection(_builder.toString());
        StringConcatenation _builder_1 = new StringConcatenation();
        _builder_1.newLine();
        _builder_1.append("ideaDevelopment {");
        _builder_1.newLine();
        _builder_1.append("\t");
        _builder_1.append("ideaVersion = \'143.381.42\'");
        _builder_1.newLine();
        _builder_1.append("\t");
        _builder_1.append("pluginRepositories {");
        _builder_1.newLine();
        {
          WizardConfiguration _config = IntellijProjectDescriptor.this.getConfig();
          XtextVersion _xtextVersion = _config.getXtextVersion();
          boolean _isSnapshot = _xtextVersion.isSnapshot();
          if (_isSnapshot) {
            _builder_1.append("\t\t");
            _builder_1.append("url \"https://hudson.eclipse.org/xtext/job/xtext-intellij/lastSuccessfulBuild/artifact/git-repo/intellij/build/ideaRepository/updatePlugins.xml\"");
            _builder_1.newLine();
          } else {
            _builder_1.append("\t\t");
            _builder_1.append("url \"http://download.eclipse.org/modeling/tmf/xtext/idea/${xtextVersion}/updatePlugins.xml\"");
            _builder_1.newLine();
          }
        }
        _builder_1.append("\t");
        _builder_1.append("}");
        _builder_1.newLine();
        _builder_1.append("\t");
        _builder_1.append("pluginDependencies {");
        _builder_1.newLine();
        _builder_1.append("\t\t");
        _builder_1.append("id \'org.eclipse.xtext.idea\' version xtextVersion");
        _builder_1.newLine();
        _builder_1.append("\t");
        _builder_1.append("}");
        _builder_1.newLine();
        _builder_1.append("}");
        _builder_1.newLine();
        _builder_1.newLine();
        _builder_1.append("ideaRepository.rootUrl = \"${buildDir}/ideaRepository\"");
        _builder_1.newLine();
        it.setAdditionalContent(_builder_1.toString());
      }
    };
    return ObjectExtensions.<GradleBuildFile>operator_doubleArrow(_buildGradle, _function);
  }
  
  public IntellijProjectDescriptor(final WizardConfiguration config) {
    super(config);
  }
}
