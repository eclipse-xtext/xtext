/**
 * Copyright (c) 2012 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtend.core.tests.compiler.batch;

import com.google.inject.Inject;
import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import org.eclipse.xtend.core.compiler.batch.XtendBatchCompiler;
import org.eclipse.xtend.core.tests.RuntimeInjectorProvider;
import org.eclipse.xtext.junit4.InjectWith;
import org.eclipse.xtext.junit4.XtextRunner;
import org.eclipse.xtext.util.Files;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Functions.Function2;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Batch compiler tests.
 * @see org.eclipse.xtend.core.compiler.batch.XtendBatchCompiler
 */
@RunWith(value = XtextRunner.class)
@InjectWith(value = RuntimeInjectorProvider.class)
@SuppressWarnings("all")
public class TestBatchCompiler {
  @Inject
  private XtendBatchCompiler batchCompiler;
  
  private static String OUTPUT_DIRECTORY_WITH_SPACES = "./test result";
  
  private static String OUTPUT_DIRECTORY = "./test-result";
  
  private static String XTEND_SRC_DIRECTORY = "./test data";
  
  private static String TEMP_DIRECTORY = "./test-temp-dir";
  
  private static String TEMP_DIRECTORY_WITH_SPACES = "./test temp dir";
  
  @Before
  public void onSetup() {
    try {
      this.batchCompiler.setSourcePath(TestBatchCompiler.XTEND_SRC_DIRECTORY);
      this.batchCompiler.setOutputPath(TestBatchCompiler.OUTPUT_DIRECTORY);
      this.batchCompiler.setDeleteTempDirectory(true);
      this.batchCompiler.setUseCurrentClassLoaderAsParent(true);
      File _file = new File(TestBatchCompiler.OUTPUT_DIRECTORY);
      _file.mkdir();
      File _file_1 = new File(TestBatchCompiler.OUTPUT_DIRECTORY);
      Files.cleanFolder(_file_1, null, true, false);
      File _file_2 = new File(TestBatchCompiler.OUTPUT_DIRECTORY_WITH_SPACES);
      _file_2.mkdir();
      File _file_3 = new File(TestBatchCompiler.OUTPUT_DIRECTORY_WITH_SPACES);
      Files.cleanFolder(_file_3, null, true, false);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @After
  public void onTearDown() {
    try {
      File _file = new File(TestBatchCompiler.OUTPUT_DIRECTORY);
      Files.cleanFolder(_file, null, true, true);
      File _file_1 = new File(TestBatchCompiler.OUTPUT_DIRECTORY_WITH_SPACES);
      Files.cleanFolder(_file_1, null, true, true);
      File _file_2 = new File(TestBatchCompiler.TEMP_DIRECTORY);
      boolean _exists = _file_2.exists();
      if (_exists) {
        File _file_3 = new File(TestBatchCompiler.TEMP_DIRECTORY);
        Files.cleanFolder(_file_3, null, true, true);
      }
      File _file_4 = new File(TestBatchCompiler.TEMP_DIRECTORY_WITH_SPACES);
      boolean _exists_1 = _file_4.exists();
      if (_exists_1) {
        File _file_5 = new File(TestBatchCompiler.TEMP_DIRECTORY_WITH_SPACES);
        Files.cleanFolder(_file_5, null, true, true);
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void bug368551() {
    this.batchCompiler.setTempDirectory(TestBatchCompiler.TEMP_DIRECTORY_WITH_SPACES);
    this.batchCompiler.setSourcePath(TestBatchCompiler.XTEND_SRC_DIRECTORY);
    this.batchCompiler.setOutputPath(TestBatchCompiler.OUTPUT_DIRECTORY_WITH_SPACES);
    this.batchCompiler.compile();
    String _plus = (TestBatchCompiler.OUTPUT_DIRECTORY_WITH_SPACES + "/test");
    File _file = new File(_plus);
    String[] _list = _file.list();
    int _size = ((List<String>)Conversions.doWrapArray(_list)).size();
    Assert.assertEquals(14, _size);
  }
  
  @Test
  public void bug387829() {
    this.batchCompiler.setTempDirectory(TestBatchCompiler.TEMP_DIRECTORY_WITH_SPACES);
    this.batchCompiler.setSourcePath(TestBatchCompiler.XTEND_SRC_DIRECTORY);
    this.batchCompiler.setOutputPath(TestBatchCompiler.OUTPUT_DIRECTORY_WITH_SPACES);
    this.batchCompiler.setClassPath(TestBatchCompiler.XTEND_SRC_DIRECTORY);
    this.batchCompiler.compile();
    String _plus = (TestBatchCompiler.OUTPUT_DIRECTORY_WITH_SPACES + "/test");
    File _file = new File(_plus);
    final File compilerOutputDir = _file;
    boolean _exists = compilerOutputDir.exists();
    Assert.assertTrue("Compiler output exists", _exists);
    String[] _list = compilerOutputDir.list();
    int _size = ((List<String>)Conversions.doWrapArray(_list)).size();
    Assert.assertEquals(14, _size);
  }
  
  @Test
  public void testCompileTestDataWithTrace() {
    this.batchCompiler.setWriteTraceFiles(true);
    this.batchCompiler.compile();
    String _plus = (TestBatchCompiler.OUTPUT_DIRECTORY + "/test");
    File _file = new File(_plus);
    final Function2<File,String,Boolean> _function = new Function2<File,String,Boolean>() {
        public Boolean apply(final File dir, final String name) {
          boolean _endsWith = name.endsWith(".java");
          return _endsWith;
        }
      };
    String[] _list = _file.list(new FilenameFilter() {
        public boolean accept(File dir,String name) {
          return _function.apply(dir,name);
        }
    });
    int _size = ((List<String>)Conversions.doWrapArray(_list)).size();
    Assert.assertEquals(7, _size);
    String _plus_1 = (TestBatchCompiler.OUTPUT_DIRECTORY + "/test");
    File _file_1 = new File(_plus_1);
    final Function2<File,String,Boolean> _function_1 = new Function2<File,String,Boolean>() {
        public Boolean apply(final File dir, final String name) {
          boolean _endsWith = name.endsWith("._trace");
          return _endsWith;
        }
      };
    String[] _list_1 = _file_1.list(new FilenameFilter() {
        public boolean accept(File dir,String name) {
          return _function_1.apply(dir,name);
        }
    });
    int _size_1 = ((List<String>)Conversions.doWrapArray(_list_1)).size();
    Assert.assertEquals(7, _size_1);
  }
  
  @Test
  public void testCompileTestDataWithoutTrace() {
    this.batchCompiler.setWriteTraceFiles(false);
    this.batchCompiler.compile();
    String _plus = (TestBatchCompiler.OUTPUT_DIRECTORY + "/test");
    File _file = new File(_plus);
    final Function2<File,String,Boolean> _function = new Function2<File,String,Boolean>() {
        public Boolean apply(final File dir, final String name) {
          boolean _endsWith = name.endsWith(".java");
          return _endsWith;
        }
      };
    String[] _list = _file.list(new FilenameFilter() {
        public boolean accept(File dir,String name) {
          return _function.apply(dir,name);
        }
    });
    int _size = ((List<String>)Conversions.doWrapArray(_list)).size();
    Assert.assertEquals(7, _size);
    String _plus_1 = (TestBatchCompiler.OUTPUT_DIRECTORY + "/test");
    File _file_1 = new File(_plus_1);
    final Function2<File,String,Boolean> _function_1 = new Function2<File,String,Boolean>() {
        public Boolean apply(final File dir, final String name) {
          boolean _endsWith = name.endsWith("._trace");
          return _endsWith;
        }
      };
    String[] _list_1 = _file_1.list(new FilenameFilter() {
        public boolean accept(File dir,String name) {
          return _function_1.apply(dir,name);
        }
    });
    int _size_1 = ((List<String>)Conversions.doWrapArray(_list_1)).size();
    Assert.assertEquals(0, _size_1);
  }
  
  @Test
  public void tempDirectory() {
    this.batchCompiler.setDeleteTempDirectory(false);
    this.batchCompiler.setTempDirectory(TestBatchCompiler.TEMP_DIRECTORY);
    boolean _compile = this.batchCompiler.compile();
    Assert.assertTrue(_compile);
    String _tempDirectory = this.batchCompiler.getTempDirectory();
    File _file = new File(_tempDirectory);
    String[] _list = _file.list();
    int _size = ((List<String>)Conversions.doWrapArray(_list)).size();
    Assert.assertEquals(2, _size);
    boolean _compile_1 = this.batchCompiler.compile();
    Assert.assertTrue(_compile_1);
    String _tempDirectory_1 = this.batchCompiler.getTempDirectory();
    File _file_1 = new File(_tempDirectory_1);
    String[] _list_1 = _file_1.list();
    int _size_1 = ((List<String>)Conversions.doWrapArray(_list_1)).size();
    Assert.assertEquals(4, _size_1);
  }
  
  @Test
  public void deleteTempDirectory() {
    this.batchCompiler.setDeleteTempDirectory(true);
    this.batchCompiler.setTempDirectory(TestBatchCompiler.TEMP_DIRECTORY);
    boolean _compile = this.batchCompiler.compile();
    Assert.assertTrue(_compile);
    File _file = new File(TestBatchCompiler.TEMP_DIRECTORY);
    String[] _list = _file.list();
    int _size = ((List<String>)Conversions.doWrapArray(_list)).size();
    Assert.assertEquals(0, _size);
  }
}
