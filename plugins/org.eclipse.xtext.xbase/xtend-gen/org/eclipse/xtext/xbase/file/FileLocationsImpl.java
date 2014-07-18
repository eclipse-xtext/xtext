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
import com.google.inject.Provider;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.xtend.lib.annotations.Accessors;
import org.eclipse.xtend.lib.macro.file.FileLocations;
import org.eclipse.xtend.lib.macro.file.Path;
import org.eclipse.xtext.xbase.file.ProjectConfig;
import org.eclipse.xtext.xbase.file.WorkspaceConfig;
import org.eclipse.xtext.xbase.lib.Pure;

/**
 * @author Sven Efftinge - Initial contribution and API
 */
@SuppressWarnings("all")
public class FileLocationsImpl implements FileLocations {
  @Inject
  @Accessors
  private Provider<WorkspaceConfig> projectInformationProvider;
  
  protected ProjectConfig getProjectConfig(final Path path) {
    List<String> _segments = path.getSegments();
    final String string = _segments.get(0);
    WorkspaceConfig _get = this.projectInformationProvider.get();
    Map<String, ProjectConfig> _projects = _get.getProjects();
    final ProjectConfig projectConfig = _projects.get(string);
    boolean _equals = Objects.equal(projectConfig, null);
    if (_equals) {
      throw new IllegalArgumentException((("The project \'" + string) + "\' has not been configured."));
    }
    return projectConfig;
  }
  
  public Path getSourceFolder(final Path path) {
    final ProjectConfig config = this.getProjectConfig(path);
    Map<Path, Path> _sourceFolderMappings = config.getSourceFolderMappings();
    Set<Path> _keySet = _sourceFolderMappings.keySet();
    for (final Path src : _keySet) {
      boolean _startsWith = path.startsWith(src);
      if (_startsWith) {
        return src;
      }
    }
    return null;
  }
  
  public Path getTargetFolder(final Path path) {
    final ProjectConfig config = this.getProjectConfig(path);
    Map<Path, Path> _sourceFolderMappings = config.getSourceFolderMappings();
    Set<Path> _keySet = _sourceFolderMappings.keySet();
    for (final Path src : _keySet) {
      boolean _startsWith = path.startsWith(src);
      if (_startsWith) {
        Map<Path, Path> _sourceFolderMappings_1 = config.getSourceFolderMappings();
        return _sourceFolderMappings_1.get(src);
      }
    }
    return null;
  }
  
  public Path getProjectFolder(final Path path) {
    final ProjectConfig config = this.getProjectConfig(path);
    return config.getRootPath();
  }
  
  @Pure
  public Provider<WorkspaceConfig> getProjectInformationProvider() {
    return this.projectInformationProvider;
  }
  
  public void setProjectInformationProvider(final Provider<WorkspaceConfig> projectInformationProvider) {
    this.projectInformationProvider = projectInformationProvider;
  }
}
