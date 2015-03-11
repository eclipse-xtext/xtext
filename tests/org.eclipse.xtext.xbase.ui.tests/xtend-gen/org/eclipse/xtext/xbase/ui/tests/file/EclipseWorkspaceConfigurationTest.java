/**
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtext.xbase.ui.tests.file;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.xtend.lib.macro.file.Path;
import org.eclipse.xtext.generator.IFileSystemAccess;
import org.eclipse.xtext.generator.OutputConfiguration;
import org.eclipse.xtext.junit4.ui.util.IResourcesSetupUtil;
import org.eclipse.xtext.junit4.ui.util.JavaProjectSetupUtil;
import org.eclipse.xtext.xbase.file.ProjectConfig;
import org.eclipse.xtext.xbase.file.WorkspaceConfig;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.ui.file.EclipseWorkspaceConfigProvider;
import org.eclipse.xtext.xbase.ui.tests.file.MockEclipseOutputConfiguration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Sven Efftinge - Initial contribution and API
 */
@SuppressWarnings("all")
public class EclipseWorkspaceConfigurationTest {
  @After
  public void tearDown() {
    try {
      IResourcesSetupUtil.cleanWorkspace();
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testConfig() {
    try {
      IWorkspace _workspace = ResourcesPlugin.getWorkspace();
      final IWorkspaceRoot wsroot = _workspace.getRoot();
      IProject[] _projects = wsroot.getProjects();
      for (final IProject p : _projects) {
        p.delete(true, true, null);
      }
      IProject[] _projects_1 = wsroot.getProjects();
      int _length = _projects_1.length;
      Assert.assertEquals(0, _length);
      JavaProjectSetupUtil.createJavaProject("projectA");
      EclipseWorkspaceConfigProvider _eclipseWorkspaceConfigProvider = new EclipseWorkspaceConfigProvider();
      final Procedure1<EclipseWorkspaceConfigProvider> _function = new Procedure1<EclipseWorkspaceConfigProvider>() {
        @Override
        public void apply(final EclipseWorkspaceConfigProvider it) {
          it.setWorkspaceRoot(wsroot);
          final Function1<IProject, Set<OutputConfiguration>> _function = new Function1<IProject, Set<OutputConfiguration>>() {
            @Override
            public Set<OutputConfiguration> apply(final IProject it) {
              OutputConfiguration _outputConfiguration = new OutputConfiguration(IFileSystemAccess.DEFAULT_OUTPUT);
              final Procedure1<OutputConfiguration> _function = new Procedure1<OutputConfiguration>() {
                @Override
                public void apply(final OutputConfiguration it) {
                  it.setOutputDirectory("xtend-gen");
                  it.setOverrideExistingResources(true);
                  it.setCreateOutputDirectory(true);
                  it.setCanClearOutputDirectory(false);
                  it.setCleanUpDerivedResources(true);
                  it.setSetDerivedProperty(true);
                }
              };
              OutputConfiguration _doubleArrow = ObjectExtensions.<OutputConfiguration>operator_doubleArrow(_outputConfiguration, _function);
              return Collections.<OutputConfiguration>unmodifiableSet(CollectionLiterals.<OutputConfiguration>newHashSet(_doubleArrow));
            }
          };
          MockEclipseOutputConfiguration _mockEclipseOutputConfiguration = new MockEclipseOutputConfiguration(_function);
          it.setConfigurationProvider(_mockEclipseOutputConfiguration);
        }
      };
      final EclipseWorkspaceConfigProvider provider = ObjectExtensions.<EclipseWorkspaceConfigProvider>operator_doubleArrow(_eclipseWorkspaceConfigProvider, _function);
      WorkspaceConfig _get = provider.get();
      final ProjectConfig projectAConfig = _get.getProject("projectA");
      Path _path = new Path("/projectA/src/com/acme");
      final Path sourceBeforeInit = projectAConfig.getContainingSourceFolder(_path);
      String _string = sourceBeforeInit.toString();
      Assert.assertEquals("/projectA/src", _string);
      Map<Path, Path> _sourceFolderMappings = projectAConfig.getSourceFolderMappings();
      int _size = _sourceFolderMappings.size();
      Assert.assertEquals(1, _size);
      Map<Path, Path> _sourceFolderMappings_1 = projectAConfig.getSourceFolderMappings();
      Path _path_1 = new Path("/projectA/src");
      final Path target = _sourceFolderMappings_1.get(_path_1);
      String _string_1 = target.toString();
      Assert.assertEquals("/projectA/xtend-gen", _string_1);
      Path _path_2 = new Path("/projectA/src/com/acme");
      final Path sourceAfterInit = projectAConfig.getContainingSourceFolder(_path_2);
      String _string_2 = sourceAfterInit.toString();
      Assert.assertEquals("/projectA/src", _string_2);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
