/**
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtext.ui.generator;

import com.google.common.base.Objects;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.generator.IShouldGenerate;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.workspace.IProjectConfig;
import org.eclipse.xtext.workspace.ProjectConfigAdapter;
import org.eclipse.xtext.xbase.lib.Exceptions;

/**
 * @author Sven Efftinge - Initial contribution and API
 */
@SuppressWarnings("all")
public class EclipseBasedShouldGenerate implements IShouldGenerate {
  @Override
  public boolean shouldGenerate(final Resource resource, final CancelIndicator cancelIndicator) {
    try {
      final URI uri = resource.getURI();
      if (((uri == null) || (!uri.isPlatformResource()))) {
        return false;
      }
      IWorkspace _workspace = ResourcesPlugin.getWorkspace();
      IWorkspaceRoot _root = _workspace.getRoot();
      String _platformString = uri.toPlatformString(true);
      Path _path = new Path(_platformString);
      final IResource member = _root.findMember(_path);
      if (((member != null) && (member.getType() == IResource.FILE))) {
        ResourceSet _resourceSet = resource.getResourceSet();
        ProjectConfigAdapter _findInEmfObject = ProjectConfigAdapter.findInEmfObject(_resourceSet);
        IProjectConfig _projectConfig = null;
        if (_findInEmfObject!=null) {
          _projectConfig=_findInEmfObject.getProjectConfig();
        }
        final IProjectConfig projectConfig = _projectConfig;
        IProject _project = member.getProject();
        String _name = _project.getName();
        String _name_1 = null;
        if (projectConfig!=null) {
          _name_1=projectConfig.getName();
        }
        boolean _equals = Objects.equal(_name, _name_1);
        if (_equals) {
          int _findMaxProblemSeverity = member.findMaxProblemSeverity(null, true, IResource.DEPTH_INFINITE);
          return (_findMaxProblemSeverity != IMarker.SEVERITY_ERROR);
        }
      }
      return false;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
