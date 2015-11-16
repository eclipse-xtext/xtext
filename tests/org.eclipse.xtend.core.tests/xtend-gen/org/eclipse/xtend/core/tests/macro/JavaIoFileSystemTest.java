/**
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtend.core.tests.macro;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.util.List;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtend.core.macro.JavaIOFileSystemSupport;
import org.eclipse.xtend.lib.macro.file.MutableFileSystemSupport;
import org.eclipse.xtend.lib.macro.file.Path;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.junit4.TemporaryFolder;
import org.eclipse.xtext.parser.IEncodingProvider;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.workspace.FileProjectConfig;
import org.eclipse.xtext.workspace.IProjectConfig;
import org.eclipse.xtext.workspace.IProjectConfigProvider;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Sven Efftinge - Initial contribution and API
 */
@SuppressWarnings("all")
public class JavaIoFileSystemTest {
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  
  @Extension
  protected MutableFileSystemSupport fs;
  
  @Before
  public void setUp() {
    try {
      final File tempDir = this.temporaryFolder.newFolder();
      JavaIOFileSystemSupport _javaIOFileSystemSupport = new JavaIOFileSystemSupport();
      final Procedure1<JavaIOFileSystemSupport> _function = new Procedure1<JavaIOFileSystemSupport>() {
        @Override
        public void apply(final JavaIOFileSystemSupport it) {
          final IProjectConfigProvider _function = new IProjectConfigProvider() {
            @Override
            public IProjectConfig getProjectConfig(final ResourceSet it) {
              File _file = new File(tempDir, "foo");
              FileProjectConfig _fileProjectConfig = new FileProjectConfig(_file);
              final Procedure1<FileProjectConfig> _function = new Procedure1<FileProjectConfig>() {
                @Override
                public void apply(final FileProjectConfig it) {
                  it.addSourceFolder("src");
                }
              };
              return ObjectExtensions.<FileProjectConfig>operator_doubleArrow(_fileProjectConfig, _function);
            }
          };
          it.setProjectConfigProvider(_function);
          IEncodingProvider.Runtime _runtime = new IEncodingProvider.Runtime();
          it.setEncodingProvider(_runtime);
          XtextResourceSet _xtextResourceSet = new XtextResourceSet();
          it.setContext(_xtextResourceSet);
        }
      };
      JavaIOFileSystemSupport _doubleArrow = ObjectExtensions.<JavaIOFileSystemSupport>operator_doubleArrow(_javaIOFileSystemSupport, _function);
      this.fs = _doubleArrow;
      this.createProject("foo");
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  protected Object createProject(final String name) {
    Path _path = new Path(name);
    URI _uRI = this.fs.toURI(_path);
    final File file = new File(_uRI);
    file.mkdirs();
    return file;
  }
  
  @Test
  public void testMakeAndDeleteFolder() {
    final Path someFolder = new Path("/foo/bar");
    final Path someFile = someFolder.append("Foo.txt");
    boolean _exists = this.fs.exists(someFolder);
    Assert.assertFalse(_exists);
    boolean _isFile = this.fs.isFile(someFolder);
    Assert.assertFalse(_isFile);
    boolean _isFolder = this.fs.isFolder(someFolder);
    Assert.assertFalse(_isFolder);
    this.fs.setContents(someFile, "Hello Foo");
    boolean _isFile_1 = this.fs.isFile(someFolder);
    Assert.assertFalse(_isFile_1);
    boolean _isFolder_1 = this.fs.isFolder(someFolder);
    Assert.assertTrue(_isFolder_1);
    boolean _exists_1 = this.fs.exists(someFolder);
    Assert.assertTrue(_exists_1);
    this.fs.delete(someFolder);
    boolean _exists_2 = this.fs.exists(someFolder);
    Assert.assertTrue(_exists_2);
    this.fs.delete(someFile);
    this.fs.delete(someFolder);
    boolean _exists_3 = this.fs.exists(someFolder);
    Assert.assertFalse(_exists_3);
  }
  
  @Test
  public void testMakeAndDeleteFile() {
    final Path path = new Path("/foo/src/my/pack/Foo.txt");
    boolean _exists = this.fs.exists(path);
    Assert.assertFalse(_exists);
    this.fs.setContents(path, "Hello Foo");
    boolean _exists_1 = this.fs.exists(path);
    Assert.assertTrue(_exists_1);
    CharSequence _contents = this.fs.getContents(path);
    Assert.assertEquals("Hello Foo", _contents);
    boolean _isFile = this.fs.isFile(path);
    Assert.assertTrue(_isFile);
    boolean _isFolder = this.fs.isFolder(path);
    Assert.assertFalse(_isFolder);
    this.fs.delete(path);
    boolean _exists_2 = this.fs.exists(path);
    Assert.assertFalse(_exists_2);
    boolean _isFile_1 = this.fs.isFile(path);
    Assert.assertFalse(_isFile_1);
    boolean _isFolder_1 = this.fs.isFolder(path);
    Assert.assertFalse(_isFolder_1);
  }
  
  @Test
  public void testModificationStamp_01() {
    try {
      final Path path = new Path("/foo/src/my/pack/Foo.txt");
      long _lastModification = this.fs.getLastModification(path);
      Assert.assertEquals(0L, _lastModification);
      this.fs.setContents(path, "Hello Foo");
      final long mod = this.fs.getLastModification(path);
      CharSequence _contents = this.fs.getContents(path);
      Assert.assertEquals("Hello Foo", _contents);
      long _lastModification_1 = this.fs.getLastModification(path);
      Assert.assertEquals(mod, _lastModification_1);
      Thread.sleep(1000);
      this.fs.setContents(path, "Hello Foo");
      long _lastModification_2 = this.fs.getLastModification(path);
      Assert.assertEquals(mod, _lastModification_2);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testModificationStamp_02() {
    try {
      final Path path = new Path("/foo/src/my/pack/Foo.txt");
      long _lastModification = this.fs.getLastModification(path);
      Assert.assertEquals(0L, _lastModification);
      this.fs.setContents(path, "Hello Foo");
      final long mod = this.fs.getLastModification(path);
      CharSequence _contents = this.fs.getContents(path);
      Assert.assertEquals("Hello Foo", _contents);
      long _lastModification_1 = this.fs.getLastModification(path);
      Assert.assertEquals(mod, _lastModification_1);
      Thread.sleep(1000);
      this.fs.setContents(path, "Hello Bar");
      long _lastModification_2 = this.fs.getLastModification(path);
      boolean _lessThan = (mod < _lastModification_2);
      Assert.assertTrue(_lessThan);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testGetWorkspaceChildren() {
    Iterable<? extends Path> _children = this.fs.getChildren(Path.ROOT);
    final Function1<Path, CharSequence> _function = new Function1<Path, CharSequence>() {
      @Override
      public CharSequence apply(final Path it) {
        List<String> _segments = it.getSegments();
        return IterableExtensions.join(_segments, ".");
      }
    };
    String _join = IterableExtensions.join(_children, "[", ", ", "]", _function);
    Iterable<? extends Path> _children_1 = this.fs.getChildren(Path.ROOT);
    int _size = IterableExtensions.size(_children_1);
    Assert.assertEquals(_join, 1, _size);
  }
  
  @Test
  public void testGetProjectChildren() {
    final Path projectFolder = new Path("/foo");
    boolean _exists = this.fs.exists(projectFolder);
    Assert.assertTrue(_exists);
    Iterable<? extends Path> _children = this.fs.getChildren(projectFolder);
    int _size = IterableExtensions.size(_children);
    final int expectedChildrenSize = (_size + 1);
    Path _path = new Path("/foo/Foo.text");
    this.fs.setContents(_path, "Hello Foo");
    Iterable<? extends Path> _children_1 = this.fs.getChildren(projectFolder);
    String _plus = ("" + _children_1);
    Iterable<? extends Path> _children_2 = this.fs.getChildren(projectFolder);
    int _size_1 = IterableExtensions.size(_children_2);
    Assert.assertEquals(_plus, expectedChildrenSize, _size_1);
  }
  
  @Test
  public void testGetFolderChildren() {
    final Path folder = new Path("/foo/bar");
    boolean _exists = this.fs.exists(folder);
    Assert.assertFalse(_exists);
    Path _append = folder.append("Bar.txt");
    this.fs.setContents(_append, "Hello Bar");
    boolean _exists_1 = this.fs.exists(folder);
    Assert.assertTrue(_exists_1);
    Iterable<? extends Path> _children = this.fs.getChildren(folder);
    int _size = IterableExtensions.size(_children);
    Assert.assertEquals(1, _size);
    Path _path = new Path("/foo/bar/Foo.text");
    this.fs.setContents(_path, "Hello Foo");
    Iterable<? extends Path> _children_1 = this.fs.getChildren(folder);
    int _size_1 = IterableExtensions.size(_children_1);
    Assert.assertEquals(2, _size_1);
  }
  
  @Test
  public void testGetFileChildren() {
    final Path file = new Path("/foo/bar/Foo.text");
    boolean _exists = this.fs.exists(file);
    Assert.assertFalse(_exists);
    Iterable<? extends Path> _children = this.fs.getChildren(file);
    int _size = IterableExtensions.size(_children);
    Assert.assertEquals(0, _size);
    this.fs.setContents(file, "Hello Foo");
    boolean _exists_1 = this.fs.exists(file);
    Assert.assertTrue(_exists_1);
    Iterable<? extends Path> _children_1 = this.fs.getChildren(file);
    int _size_1 = IterableExtensions.size(_children_1);
    Assert.assertEquals(0, _size_1);
  }
  
  @Test
  public void testGetFileURI() {
    final Path file = new Path("/foo/bar/Foo.text");
    boolean _exists = this.fs.exists(file);
    Assert.assertFalse(_exists);
    URI _uRI = this.fs.toURI(file);
    Assert.assertNotNull(_uRI);
    this.fs.setContents(file, "Hello Foo");
    boolean _exists_1 = this.fs.exists(file);
    Assert.assertTrue(_exists_1);
    this.assertToURI(file, "Hello Foo");
  }
  
  @Test
  public void testGetFolderURI() {
    final Path path = new Path("/foo/bar");
    boolean _exists = this.fs.exists(path);
    Assert.assertFalse(_exists);
    URI _uRI = this.fs.toURI(path);
    Assert.assertNotNull(_uRI);
    Path _append = path.append("Foo.txt");
    this.fs.setContents(_append, "Hello Foo");
    boolean _exists_1 = this.fs.exists(path);
    Assert.assertTrue(_exists_1);
    URI _uRI_1 = this.fs.toURI(path);
    Assert.assertNotNull(_uRI_1);
  }
  
  @Test
  public void testGetProjectURI() {
    final Path path = new Path("/foo");
    boolean _exists = this.fs.exists(path);
    Assert.assertTrue(_exists);
    URI _uRI = this.fs.toURI(path);
    Assert.assertNotNull(_uRI);
  }
  
  @Test
  public void testGetWorkspaceURI() {
    final Path path = Path.ROOT;
    boolean _exists = this.fs.exists(path);
    Assert.assertTrue(_exists);
    URI _uRI = this.fs.toURI(path);
    Assert.assertNull(_uRI);
  }
  
  @Test
  public void testWorkspaceIsFolder() {
    boolean _isFolder = this.fs.isFolder(Path.ROOT);
    Assert.assertTrue(_isFolder);
  }
  
  @Test
  public void testWorkspaceIsFile() {
    boolean _isFile = this.fs.isFile(Path.ROOT);
    Assert.assertFalse(_isFile);
  }
  
  @Test
  public void testGetWorkspaceLastModification() {
    long _lastModification = this.fs.getLastModification(Path.ROOT);
    Assert.assertEquals(0L, _lastModification);
  }
  
  @Test
  public void testGetWorkspaceCharset() {
    String _charset = this.fs.getCharset(Path.ROOT);
    Assert.assertNotNull(_charset);
  }
  
  @Test
  public void testGetWorkspaceContent() {
    try {
      this.fs.getContents(Path.ROOT);
      Assert.fail();
    } catch (final Throwable _t) {
      if (_t instanceof IllegalArgumentException) {
        final IllegalArgumentException e = (IllegalArgumentException)_t;
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
  }
  
  @Test
  public void testGetWorkspaceContentAsSteam() {
    try {
      this.fs.getContentsAsStream(Path.ROOT);
      Assert.fail();
    } catch (final Throwable _t) {
      if (_t instanceof IllegalArgumentException) {
        final IllegalArgumentException e = (IllegalArgumentException)_t;
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
  }
  
  @Test
  public void testSetWorkspaceContents() {
    try {
      this.fs.setContents(Path.ROOT, "Hello World!");
      Assert.fail();
    } catch (final Throwable _t) {
      if (_t instanceof IllegalArgumentException) {
        final IllegalArgumentException e = (IllegalArgumentException)_t;
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
  }
  
  @Test
  public void testSetWorkspaceContentsAsStream() {
    try {
      byte[] _bytes = "Hello World!".getBytes();
      ByteArrayInputStream _byteArrayInputStream = new ByteArrayInputStream(_bytes);
      this.fs.setContentsAsStream(Path.ROOT, _byteArrayInputStream);
      Assert.fail();
    } catch (final Throwable _t) {
      if (_t instanceof IllegalArgumentException) {
        final IllegalArgumentException e = (IllegalArgumentException)_t;
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
  }
  
  @Test
  public void testDeleteWorkspace() {
    boolean _exists = this.fs.exists(Path.ROOT);
    Assert.assertTrue(_exists);
    this.fs.delete(Path.ROOT);
    boolean _exists_1 = this.fs.exists(Path.ROOT);
    Assert.assertTrue(_exists_1);
  }
  
  protected void assertToURI(final Path file, final String expectedContent) {
    final URI uri = this.fs.toURI(file);
    Assert.assertNotNull(uri);
    try {
      final File javaIoFile = new File(uri);
      boolean _exists = javaIoFile.exists();
      Assert.assertTrue(_exists);
      long _length = javaIoFile.length();
      final byte[] data = new byte[((int) _length)];
      final FileInputStream fis = new FileInputStream(javaIoFile);
      fis.read(data);
      fis.close();
      String _string = new String(data);
      Assert.assertEquals(expectedContent, _string);
    } catch (final Throwable _t) {
      if (_t instanceof Exception) {
        final Exception e = (Exception)_t;
        StringConcatenation _builder = new StringConcatenation();
        _builder.append("URI: ");
        _builder.append(uri, "");
        _builder.append("; ");
        String _message = e.getMessage();
        _builder.append(_message, "");
        Assert.fail(_builder.toString());
      } else {
        throw Exceptions.sneakyThrow(_t);
      }
    }
  }
}
