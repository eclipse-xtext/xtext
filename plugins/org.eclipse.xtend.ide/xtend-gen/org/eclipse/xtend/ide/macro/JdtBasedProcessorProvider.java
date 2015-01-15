/**
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtend.ide.macro;

import com.google.common.base.Objects;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.xtend.core.macro.ProcessorInstanceForJvmTypeProvider;
import org.eclipse.xtend.lib.macro.TransformationContext;
import org.eclipse.xtext.common.types.JvmType;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;

@SuppressWarnings("all")
public class JdtBasedProcessorProvider extends ProcessorInstanceForJvmTypeProvider {
  @Override
  public Object getProcessorInstance(final JvmType type) {
    try {
      Resource _eResource = type.eResource();
      ResourceSet _resourceSet = _eResource.getResourceSet();
      Object _classpathURIContext = ((XtextResourceSet) _resourceSet).getClasspathURIContext();
      final IJavaProject project = ((IJavaProject) _classpathURIContext);
      String _identifier = type.getIdentifier();
      final ClassLoader classLoader = this.createClassLoader(_identifier, project);
      String _identifier_1 = type.getIdentifier();
      final Class<?> result = classLoader.loadClass(_identifier_1);
      return result.newInstance();
    } catch (final Throwable _t) {
      if (_t instanceof Exception) {
        final Exception e = (Exception)_t;
        String _identifier_2 = type.getIdentifier();
        String _plus = ("Problem during instantiation of " + _identifier_2);
        String _plus_1 = (_plus + " : ");
        String _message = e.getMessage();
        String _plus_2 = (_plus_1 + _message);
        throw new IllegalStateException(_plus_2, e);
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
  }
  
  @Override
  public ClassLoader getClassLoader(final EObject ctx) {
    Resource _eResource = ctx.eResource();
    ResourceSet _resourceSet = _eResource.getResourceSet();
    Object _classpathURIContext = ((XtextResourceSet) _resourceSet).getClasspathURIContext();
    final IJavaProject project = ((IJavaProject) _classpathURIContext);
    return this.createClassLoaderForJavaProject(project);
  }
  
  protected ClassLoader createClassLoader(final String typeName, final IJavaProject javaProject) {
    try {
      URLClassLoader _xblockexpression = null;
      {
        final IType type = javaProject.findType(typeName);
        boolean _equals = Objects.equal(type, null);
        if (_equals) {
          Class<? extends JdtBasedProcessorProvider> _class = this.getClass();
          return _class.getClassLoader();
        }
        final IJavaProject projectToUse = type.getJavaProject();
        _xblockexpression = this.createClassLoaderForJavaProject(projectToUse);
      }
      return _xblockexpression;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  protected URLClassLoader createClassLoaderForJavaProject(final IJavaProject projectToUse) {
    try {
      final IClasspathEntry[] resolvedClasspath = projectToUse.getResolvedClasspath(true);
      final List<URL> urls = CollectionLiterals.<URL>newArrayList();
      List<URL> _outputFolders = this.getOutputFolders(projectToUse);
      urls.addAll(_outputFolders);
      for (final IClasspathEntry entry : resolvedClasspath) {
        {
          URL url = null;
          int _entryKind = entry.getEntryKind();
          switch (_entryKind) {
            case IClasspathEntry.CPE_SOURCE:
              break;
            case IClasspathEntry.CPE_PROJECT:
              IPath path = entry.getPath();
              IWorkspaceRoot _workspaceRoot = this.getWorkspaceRoot(projectToUse);
              final IResource project = _workspaceRoot.findMember(path);
              IProject _project = project.getProject();
              IJavaProject _create = JavaCore.create(_project);
              List<URL> _outputFolders_1 = this.getOutputFolders(_create);
              urls.addAll(_outputFolders_1);
              break;
            case IClasspathEntry.CPE_LIBRARY:
              IPath path_1 = entry.getPath();
              IWorkspaceRoot _workspaceRoot_1 = this.getWorkspaceRoot(projectToUse);
              final IResource library = _workspaceRoot_1.findMember(path_1);
              URL _xifexpression = null;
              boolean _notEquals = (!Objects.equal(library, null));
              if (_notEquals) {
                URI _rawLocationURI = library.getRawLocationURI();
                _xifexpression = _rawLocationURI.toURL();
              } else {
                File _file = path_1.toFile();
                URI _uRI = _file.toURI();
                _xifexpression = _uRI.toURL();
              }
              url = _xifexpression;
              break;
            default:
              {
                IPath path_2 = entry.getPath();
                File _file_1 = path_2.toFile();
                URI _uRI_1 = _file_1.toURI();
                URL _uRL = _uRI_1.toURL();
                url = _uRL;
              }
              break;
          }
          boolean _notEquals_1 = (!Objects.equal(url, null));
          if (_notEquals_1) {
            urls.add(url);
          }
        }
      }
      ClassLoader _parentClassLoader = this.getParentClassLoader();
      return new URLClassLoader(((URL[])Conversions.unwrapArray(urls, URL.class)), _parentClassLoader);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  protected ClassLoader getParentClassLoader() {
    final ClassLoader bundleClassLoader = TransformationContext.class.getClassLoader();
    return bundleClassLoader;
  }
  
  private IWorkspaceRoot getWorkspaceRoot(final IJavaProject javaProject) {
    IProject _project = javaProject.getProject();
    IWorkspace _workspace = _project.getWorkspace();
    return _workspace.getRoot();
  }
  
  private List<URL> getOutputFolders(final IJavaProject javaProject) {
    try {
      final List<URL> result = CollectionLiterals.<URL>newArrayList();
      IPath _outputLocation = javaProject.getOutputLocation();
      IPath path = _outputLocation.addTrailingSeparator();
      String _string = path.toString();
      org.eclipse.emf.common.util.URI _createPlatformResourceURI = org.eclipse.emf.common.util.URI.createPlatformResourceURI(_string, true);
      String _string_1 = _createPlatformResourceURI.toString();
      URL url = new URL(_string_1);
      result.add(url);
      IClasspathEntry[] _rawClasspath = javaProject.getRawClasspath();
      for (final IClasspathEntry entry : _rawClasspath) {
        int _entryKind = entry.getEntryKind();
        switch (_entryKind) {
          case IClasspathEntry.CPE_SOURCE:
            IPath _outputLocation_1 = entry.getOutputLocation();
            path = _outputLocation_1;
            boolean _notEquals = (!Objects.equal(path, null));
            if (_notEquals) {
              IPath _addTrailingSeparator = path.addTrailingSeparator();
              String _string_2 = _addTrailingSeparator.toString();
              org.eclipse.emf.common.util.URI _createPlatformResourceURI_1 = org.eclipse.emf.common.util.URI.createPlatformResourceURI(_string_2, true);
              String _string_3 = _createPlatformResourceURI_1.toString();
              URL _uRL = new URL(_string_3);
              url = _uRL;
              result.add(url);
            }
            break;
        }
      }
      return result;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
