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
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.eclipse.xtext.xbase.lib.IterableExtensions;

@SuppressWarnings("all")
public class EclipseProjectConfig extends ProjectConfig {
  private IProject project;
  
  private EclipseOutputConfigurationProvider configurationProvider;
  
  public EclipseProjectConfig(final IProject project, final EclipseOutputConfigurationProvider configurationProvider) {
    super(new Function0<String>() {
      public String apply() {
        String _name = project.getName();
        return _name;
      }
    }.apply());
    this.project = project;
    this.configurationProvider = configurationProvider;
  }
  
  public Map<Path,Path> getSourceFolderMappings() {
    try {
      final Map<Path,Path> map = super.getSourceFolderMappings();
      boolean _isEmpty = map.isEmpty();
      if (_isEmpty) {
        Set<OutputConfiguration> _outputConfigurations = this.configurationProvider.getOutputConfigurations(this.project);
        final OutputConfiguration config = IterableExtensions.<OutputConfiguration>head(_outputConfigurations);
        final IJavaProject jp = JavaCore.create(this.project);
        IPackageFragmentRoot[] _allPackageFragmentRoots = jp.getAllPackageFragmentRoots();
        for (final IPackageFragmentRoot root : _allPackageFragmentRoots) {
          boolean _and = false;
          int _kind = root.getKind();
          boolean _equals = (_kind == IPackageFragmentRoot.K_SOURCE);
          if (!_equals) {
            _and = false;
          } else {
            IResource _underlyingResource = root.getUnderlyingResource();
            boolean _notEquals = (!Objects.equal(_underlyingResource, null));
            _and = (_equals && _notEquals);
          }
          if (_and) {
            IResource _underlyingResource_1 = root.getUnderlyingResource();
            final IContainer container = ((IContainer) _underlyingResource_1);
            IContainer _parent = container.getParent();
            String _outputDirectory = config.getOutputDirectory();
            final IResource target = _parent.findMember(_outputDirectory);
            boolean _equals_1 = container.equals(target);
            boolean _not = (!_equals_1);
            if (_not) {
              IPath _fullPath = container.getFullPath();
              String _string = _fullPath.toString();
              Path _path = new Path(_string);
              IPath _fullPath_1 = target.getFullPath();
              String _string_1 = _fullPath_1.toString();
              Path _path_1 = new Path(_string_1);
              map.put(_path, _path_1);
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
