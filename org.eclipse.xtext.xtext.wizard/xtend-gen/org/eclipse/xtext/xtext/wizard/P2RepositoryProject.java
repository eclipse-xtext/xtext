/**
 * Copyright (c) 2016 itemis AG (http://www.itemis.eu) and others.
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
import java.util.Set;
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

/**
 * @author Lorenzo Bettini - Initial contribution and API
 */
@FinalFieldsConstructor
@SuppressWarnings("all")
public class P2RepositoryProject extends ProjectDescriptor {
  @Override
  public String getNameQualifier() {
    return ".repository";
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
    return (super.isEnabled() && this.getConfig().getRuntimeProject().isEclipsePluginProject());
  }

  @Override
  public Iterable<? extends AbstractFile> getFiles() {
    ArrayList<AbstractFile> _xblockexpression = null;
    {
      final ArrayList<AbstractFile> files = CollectionLiterals.<AbstractFile>newArrayList();
      Iterable<? extends AbstractFile> _files = super.getFiles();
      Iterables.<AbstractFile>addAll(files, _files);
      PlainTextFile _file = this.file(Outlet.ROOT, "category.xml", this.categoryXml());
      files.add(_file);
      _xblockexpression = files;
    }
    return _xblockexpression;
  }

  @Override
  public Set<SourceFolderDescriptor> getSourceFolders() {
    return Collections.<SourceFolderDescriptor>unmodifiableSet(CollectionLiterals.<SourceFolderDescriptor>newHashSet());
  }

  public CharSequence categoryXml() {
    StringConcatenation _builder = new StringConcatenation();
    _builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    _builder.newLine();
    _builder.append("<site>");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("<feature id=\"");
    String _name = this.getConfig().getSdkProject().getName();
    _builder.append(_name, "\t");
    _builder.append("\" version=\"0.0.0\">");
    _builder.newLineIfNotEmpty();
    _builder.append("\t\t");
    _builder.append("<category name=\"main\"/>");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("</feature>");
    _builder.newLine();
    _builder.append("\t");
    _builder.append("<category-def name=\"main\" label=\"");
    String _simpleName = this.getConfig().getLanguage().getSimpleName();
    _builder.append(_simpleName, "\t");
    _builder.append("\"/>");
    _builder.newLineIfNotEmpty();
    _builder.append("</site>");
    _builder.newLine();
    return _builder;
  }

  @Override
  public GradleBuildFile buildGradle() {
    throw new UnsupportedOperationException("Eclipse repositories are not yet supported in Gradle");
  }

  @Override
  public PomFile pom() {
    PomFile _pom = super.pom();
    final Procedure1<PomFile> _function = (PomFile it) -> {
      it.setPackaging("eclipse-repository");
    };
    return ObjectExtensions.<PomFile>operator_doubleArrow(_pom, _function);
  }

  public P2RepositoryProject(final WizardConfiguration config) {
    super(config);
  }
}
