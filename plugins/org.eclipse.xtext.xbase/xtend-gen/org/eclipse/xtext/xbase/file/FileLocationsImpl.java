/**
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtext.xbase.file;

import com.google.common.base.Objects;
import com.google.inject.Inject;
import java.util.List;
import java.util.Set;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtend.lib.macro.file.FileLocations;
import org.eclipse.xtend.lib.macro.file.Path;
import org.eclipse.xtext.generator.IContextualOutputConfigurationProvider;
import org.eclipse.xtext.generator.OutputConfiguration;
import org.eclipse.xtext.workspace.IProjectConfig;
import org.eclipse.xtext.workspace.ISourceFolder;
import org.eclipse.xtext.workspace.IWorkspaceConfig;
import org.eclipse.xtext.workspace.IWorkspaceConfigProvider;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Pure;

/**
 * @author Sven Efftinge - Initial contribution and API
 */
@SuppressWarnings("all")
public class FileLocationsImpl implements FileLocations {
  @Inject
  @Accessors
  private IWorkspaceConfigProvider projectInformationProvider;
  
  @Inject
  @Accessors
  private IContextualOutputConfigurationProvider outputConfigurationProvider;
  
  @Accessors
  private Resource context;
  
  protected IProjectConfig getProjectConfig(final Path path) {
    List<String> _segments = path.getSegments();
    final String string = _segments.get(0);
    IWorkspaceConfig _workspaceConfig = this.projectInformationProvider.getWorkspaceConfig(this.context);
    final IProjectConfig projectConfig = _workspaceConfig.findProjectByName(string);
    boolean _equals = Objects.equal(projectConfig, null);
    if (_equals) {
      throw new IllegalArgumentException((("The project \'" + string) + "\' has not been configured."));
    }
    return projectConfig;
  }
  
  @Override
  public Path getSourceFolder(final Path path) {
    Set<Path> _projectSourceFolders = this.getProjectSourceFolders(path);
    final Function1<Path, Boolean> _function = new Function1<Path, Boolean>() {
      @Override
      public Boolean apply(final Path sourceFolder) {
        return Boolean.valueOf(path.startsWith(sourceFolder));
      }
    };
    return IterableExtensions.<Path>findFirst(_projectSourceFolders, _function);
  }
  
  @Override
  public Path getTargetFolder(final Path path) {
    Path _xblockexpression = null;
    {
      final Path projectFolder = this.getProjectFolder(path);
      boolean _equals = Objects.equal(projectFolder, null);
      if (_equals) {
        return null;
      }
      Set<OutputConfiguration> _outputConfigurations = this.outputConfigurationProvider.getOutputConfigurations(this.context);
      final OutputConfiguration outputConfiguration = IterableExtensions.<OutputConfiguration>head(_outputConfigurations);
      final Path sourceFolder = this.getSourceFolder(path);
      String _xifexpression = null;
      boolean _equals_1 = Objects.equal(sourceFolder, null);
      if (_equals_1) {
        _xifexpression = outputConfiguration.getOutputDirectory();
      } else {
        List<String> _segments = sourceFolder.getSegments();
        Iterable<String> _tail = IterableExtensions.<String>tail(_segments);
        String _join = IterableExtensions.join(_tail, "/");
        _xifexpression = outputConfiguration.getOutputDirectory(_join);
      }
      final String outputFolder = _xifexpression;
      _xblockexpression = projectFolder.append(outputFolder);
    }
    return _xblockexpression;
  }
  
  @Override
  public Path getProjectFolder(final Path path) {
    final IProjectConfig config = this.getProjectConfig(path);
    String _name = config.getName();
    return Path.ROOT.append(_name);
  }
  
  @Override
  public Set<Path> getProjectSourceFolders(final Path path) {
    IProjectConfig _projectConfig = this.getProjectConfig(path);
    Set<? extends ISourceFolder> _sourceFolders = _projectConfig.getSourceFolders();
    final Function1<ISourceFolder, Path> _function = new Function1<ISourceFolder, Path>() {
      @Override
      public Path apply(final ISourceFolder it) {
        Path _projectFolder = FileLocationsImpl.this.getProjectFolder(path);
        String _name = it.getName();
        return _projectFolder.append(_name);
      }
    };
    Iterable<Path> _map = IterableExtensions.map(_sourceFolders, _function);
    return IterableExtensions.<Path>toSet(_map);
  }
  
  @Pure
  public IWorkspaceConfigProvider getProjectInformationProvider() {
    return this.projectInformationProvider;
  }
  
  public void setProjectInformationProvider(final IWorkspaceConfigProvider projectInformationProvider) {
    this.projectInformationProvider = projectInformationProvider;
  }
  
  @Pure
  public IContextualOutputConfigurationProvider getOutputConfigurationProvider() {
    return this.outputConfigurationProvider;
  }
  
  public void setOutputConfigurationProvider(final IContextualOutputConfigurationProvider outputConfigurationProvider) {
    this.outputConfigurationProvider = outputConfigurationProvider;
  }
  
  @Pure
  public Resource getContext() {
    return this.context;
  }
  
  public void setContext(final Resource context) {
    this.context = context;
  }
}
