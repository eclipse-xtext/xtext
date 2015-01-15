/**
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtext.xbase.ui.file;

import com.google.common.base.Objects;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.xtend.lib.macro.file.Path;
import org.eclipse.xtext.builder.EclipseOutputConfigurationProvider;
import org.eclipse.xtext.generator.OutputConfiguration;
import org.eclipse.xtext.xbase.file.ProjectConfig;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.IterableExtensions;

@SuppressWarnings("all")
public class EclipseProjectConfig extends ProjectConfig {
  private IProject project;
  
  private EclipseOutputConfigurationProvider configurationProvider;
  
  public EclipseProjectConfig(final IProject project, final EclipseOutputConfigurationProvider configurationProvider) {
    super(project.getName());
    this.project = project;
    this.configurationProvider = configurationProvider;
  }
  
  @Override
  public Map<Path, Path> getSourceFolderMappings() {
    try {
      final Map<Path, Path> map = super.getSourceFolderMappings();
      boolean _isEmpty = map.isEmpty();
      if (_isEmpty) {
        Set<OutputConfiguration> _outputConfigurations = this.configurationProvider.getOutputConfigurations(this.project);
        final OutputConfiguration config = IterableExtensions.<OutputConfiguration>head(_outputConfigurations);
        final IJavaProject jp = JavaCore.create(this.project);
        IPackageFragmentRoot[] _packageFragmentRoots = jp.getPackageFragmentRoots();
        for (final IPackageFragmentRoot root : _packageFragmentRoots) {
          boolean _and = false;
          int _kind = root.getKind();
          boolean _equals = (_kind == IPackageFragmentRoot.K_SOURCE);
          if (!_equals) {
            _and = false;
          } else {
            IResource _underlyingResource = root.getUnderlyingResource();
            boolean _notEquals = (!Objects.equal(_underlyingResource, null));
            _and = _notEquals;
          }
          if (_and) {
            IResource _underlyingResource_1 = root.getUnderlyingResource();
            final IContainer container = ((IContainer) _underlyingResource_1);
            IFolder target = null;
            boolean _isUseOutputPerSourceFolder = config.isUseOutputPerSourceFolder();
            if (_isUseOutputPerSourceFolder) {
              IPath _fullPath = container.getFullPath();
              IPath _fullPath_1 = this.project.getFullPath();
              IPath _makeRelativeTo = _fullPath.makeRelativeTo(_fullPath_1);
              final String projectRelativeSource = _makeRelativeTo.toString();
              String _outputDirectory = config.getOutputDirectory(projectRelativeSource);
              org.eclipse.core.runtime.Path _path = new org.eclipse.core.runtime.Path(_outputDirectory);
              IFolder _folder = this.project.getFolder(_path);
              target = _folder;
            } else {
              IContainer _parent = container.getParent();
              String _outputDirectory_1 = config.getOutputDirectory();
              org.eclipse.core.runtime.Path _path_1 = new org.eclipse.core.runtime.Path(_outputDirectory_1);
              IFolder _folder_1 = _parent.getFolder(_path_1);
              target = _folder_1;
            }
            boolean _notEquals_1 = (!Objects.equal(container, target));
            if (_notEquals_1) {
              IPath _fullPath_2 = container.getFullPath();
              String _string = _fullPath_2.toString();
              Path _path_2 = new Path(_string);
              IPath _fullPath_3 = target.getFullPath();
              String _string_1 = _fullPath_3.toString();
              Path _path_3 = new Path(_string_1);
              map.put(_path_2, _path_3);
            }
          }
        }
      }
      return map;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
