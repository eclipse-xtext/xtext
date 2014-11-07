/**
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtext.xbase.ui.tests.file;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.util.List;
import java.util.Set;
import org.eclipse.core.internal.resources.ProjectDescription;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.xtend.lib.macro.file.Path;
import org.eclipse.xtext.parser.IEncodingProvider;
import org.eclipse.xtext.xbase.lib.CollectionLiterals;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.eclipse.xtext.xbase.tests.file.JavaIoFileSystemTest;
import org.eclipse.xtext.xbase.ui.file.EclipseFileSystemSupportImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Sven Efftinge - Initial contribution and API
 */
@SuppressWarnings("all")
public class EclipseFileSystemTest extends JavaIoFileSystemTest {
  private Set<String> knownProjects = CollectionLiterals.<String>newHashSet();
  
  @Before
  public void setUp() {
    try {
      IWorkspace _workspace = ResourcesPlugin.getWorkspace();
      IWorkspaceRoot _root = _workspace.getRoot();
      IProject[] _projects = _root.getProjects();
      for (final IProject p : _projects) {
        String _name = p.getName();
        this.knownProjects.add(_name);
      }
      IWorkspace _workspace_1 = ResourcesPlugin.getWorkspace();
      final IWorkspaceRoot root = _workspace_1.getRoot();
      final IProject project = root.getProject("foo");
      boolean _exists = project.exists();
      if (_exists) {
        Assert.fail("Project \'foo\' should not exist yet");
      }
      project.create(null);
      project.open(null);
      EclipseFileSystemSupportImpl _eclipseFileSystemSupportImpl = new EclipseFileSystemSupportImpl();
      final Procedure1<EclipseFileSystemSupportImpl> _function = new Procedure1<EclipseFileSystemSupportImpl>() {
        public void apply(final EclipseFileSystemSupportImpl it) {
          it.setWorkspaceRoot(root);
          IEncodingProvider.Runtime _runtime = new IEncodingProvider.Runtime();
          it.setEncodingProvider(_runtime);
        }
      };
      EclipseFileSystemSupportImpl _doubleArrow = ObjectExtensions.<EclipseFileSystemSupportImpl>operator_doubleArrow(_eclipseFileSystemSupportImpl, _function);
      this.fs = _doubleArrow;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @After
  public void tearDown() {
    try {
      IWorkspace _workspace = ResourcesPlugin.getWorkspace();
      final IWorkspaceRoot root = _workspace.getRoot();
      IProject[] _projects = root.getProjects();
      for (final IProject p : _projects) {
        String _name = p.getName();
        boolean _remove = this.knownProjects.remove(_name);
        boolean _not = (!_remove);
        if (_not) {
          p.delete(true, null);
        }
      }
      boolean _isEmpty = this.knownProjects.isEmpty();
      Assert.assertTrue(_isEmpty);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testMakeandDeleteFile() {
    super.testMakeandDeleteFile();
  }
  
  @Test
  public void testGetWorkspaceChildren() {
    Iterable<? extends Path> _children = this.fs.getChildren(Path.ROOT);
    final Function1<Path, CharSequence> _function = new Function1<Path, CharSequence>() {
      public CharSequence apply(final Path it) {
        List<String> _segments = it.getSegments();
        return IterableExtensions.join(_segments, ".");
      }
    };
    String _join = IterableExtensions.join(_children, "[", ", ", "]", _function);
    Iterable<? extends Path> _children_1 = this.fs.getChildren(Path.ROOT);
    int _size = IterableExtensions.size(_children_1);
    Assert.assertEquals(_join, 1, _size);
    final Path path = new Path("/bar");
    this.fs.mkdir(path);
    boolean _exists = this.fs.exists(path);
    Assert.assertTrue(_exists);
    Iterable<? extends Path> _children_2 = this.fs.getChildren(Path.ROOT);
    final Function1<Path, CharSequence> _function_1 = new Function1<Path, CharSequence>() {
      public CharSequence apply(final Path it) {
        List<String> _segments = it.getSegments();
        return IterableExtensions.join(_segments, ".");
      }
    };
    String _join_1 = IterableExtensions.join(_children_2, "[", ", ", "]", _function_1);
    Iterable<? extends Path> _children_3 = this.fs.getChildren(Path.ROOT);
    int _size_1 = IterableExtensions.size(_children_3);
    Assert.assertEquals(_join_1, 2, _size_1);
  }
  
  @Test
  public void testGetURIForImportedProject() {
    try {
      IWorkspace _workspace = ResourcesPlugin.getWorkspace();
      final IWorkspaceRoot root = _workspace.getRoot();
      final ProjectDescription description = new ProjectDescription();
      description.setName("bar");
      IPath _location = root.getLocation();
      IPath _append = _location.append("foo/bar");
      description.setLocation(_append);
      final IProject project = root.getProject("bar");
      project.create(description, null);
      project.open(null);
      final Path file = new Path("/bar/Foo.text");
      boolean _exists = this.fs.exists(file);
      Assert.assertFalse(_exists);
      URI _uRI = this.fs.toURI(file);
      Assert.assertNotNull(_uRI);
      this.fs.setContents(file, "Hello Foo");
      boolean _exists_1 = this.fs.exists(file);
      Assert.assertTrue(_exists_1);
      final URI uri = this.fs.toURI(file);
      Assert.assertNotNull(uri);
      String _string = uri.toString();
      boolean _endsWith = _string.endsWith("/foo/bar/Foo.text");
      Assert.assertTrue(("" + uri), _endsWith);
      final File javaIoFile = new File(uri);
      boolean _exists_2 = javaIoFile.exists();
      Assert.assertTrue(_exists_2);
      long _length = javaIoFile.length();
      final byte[] data = new byte[((int) _length)];
      final FileInputStream fis = new FileInputStream(javaIoFile);
      fis.read(data);
      fis.close();
      String _string_1 = new String(data);
      Assert.assertEquals("Hello Foo", _string_1);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
