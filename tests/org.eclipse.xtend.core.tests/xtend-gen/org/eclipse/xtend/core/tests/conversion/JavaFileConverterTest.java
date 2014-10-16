/**
 * Copyright (c) 2014 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtend.core.tests.conversion;

import com.google.common.base.Predicate;
import com.google.common.io.Files;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.io.File;
import java.nio.charset.Charset;
import java.util.Set;
import org.eclipse.emf.common.util.URI;
import org.eclipse.xtend.core.conversion.JavaConverter;
import org.eclipse.xtend.core.tests.AbstractXtendTestCase;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.mwe.PathTraverser;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.InputOutput;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author dhuebner - Initial contribution and API
 */
@SuppressWarnings("all")
public class JavaFileConverterTest extends AbstractXtendTestCase {
  @Inject
  private Provider<JavaConverter> javaConverter;
  
  private String sourceProject;
  
  private String targetProject;
  
  private int errorsExpected = 0;
  
  private int problemsExpected = 0;
  
  @Before
  public void setUp() {
    this.sourceProject = null;
    this.targetProject = null;
    this.errorsExpected = 0;
    this.problemsExpected = 0;
  }
  
  @Test
  @Ignore
  public void testConvertFilesInThisProject() throws Exception {
    this.sourceProject = "org.eclipse.xtend.core.tests";
    this.targetProject = "test-converter";
    this.errorsExpected = 0;
    this.problemsExpected = 33;
    this.runConverter();
  }
  
  @Test
  @Ignore
  public void testConvertFilesInXtextTestsProject() throws Exception {
    this.sourceProject = "org.eclipse.xtext.tests";
    this.targetProject = "org.eclipse.xtext.tests.converted";
    this.errorsExpected = 198;
    this.problemsExpected = 12173;
    this.runConverter();
  }
  
  public void runConverter() {
    try {
      File _file = new File("");
      File _absoluteFile = _file.getAbsoluteFile();
      File _parentFile = _absoluteFile.getParentFile();
      final File srcProjectRoot = new File(_parentFile, this.sourceProject);
      File _parentFile_1 = srcProjectRoot.getParentFile();
      final File testProject = new File(_parentFile_1, this.targetProject);
      String _path = srcProjectRoot.getPath();
      String _plus = ("Working in " + _path);
      InputOutput.<String>println(_plus);
      final PathTraverser pathTraverser = new PathTraverser();
      String _absolutePath = srcProjectRoot.getAbsolutePath();
      final Set<URI> allResourceUris = pathTraverser.findAllResourceUris(_absolutePath, 
        new Predicate<URI>() {
          public boolean apply(final URI input) {
            final String fileName = input.toFileString();
            boolean _and = false;
            boolean _and_1 = false;
            String _fileExtension = input.fileExtension();
            boolean _equals = "java".equals(_fileExtension);
            if (!_equals) {
              _and_1 = false;
            } else {
              boolean _contains = fileName.contains("xtend-gen");
              boolean _not = (!_contains);
              _and_1 = _not;
            }
            if (!_and_1) {
              _and = false;
            } else {
              boolean _contains_1 = fileName.contains("batch-compiler-data");
              boolean _not_1 = (!_contains_1);
              _and = _not_1;
            }
            return _and;
          }
        });
      int errors = 0;
      int problems = 0;
      int files = 0;
      int filesWithErrorsOrProblems = 0;
      for (final URI uri : allResourceUris) {
        {
          String _fileString = uri.toFileString();
          final File file = new File(_fileString);
          String _absolutePath_1 = file.getAbsolutePath();
          String _plus_1 = ("Converting: " + _absolutePath_1);
          InputOutput.<String>println(_plus_1);
          Charset _defaultCharset = Charset.defaultCharset();
          final String javaCode = Files.toString(file, _defaultCharset);
          String _name = file.getName();
          final JavaConverter.ConversionResult xtendResult = this.converToXtend(_name, javaCode);
          Iterable<String> _problems = xtendResult.getProblems();
          final int problemsFound = IterableExtensions.size(_problems);
          String xtendCode = xtendResult.getXtendCode();
          String _fileString_1 = uri.toFileString();
          String _absolutePath_2 = srcProjectRoot.getAbsolutePath();
          final String javaFileProjRelPath = _fileString_1.replace(_absolutePath_2, "");
          String fileName = (javaFileProjRelPath + ".xtend");
          try {
            this.file(xtendCode, true);
          } catch (final Throwable _t) {
            if (_t instanceof AssertionError) {
              final AssertionError error = (AssertionError)_t;
              if ((problemsFound != 0)) {
                this.writeToFile(testProject, javaFileProjRelPath, javaCode);
                String _fileName = fileName;
                fileName = (_fileName + ".error");
              } else {
                StringConcatenation _builder = new StringConcatenation();
                _builder.append(uri, "");
                _builder.append(" - ");
                String _message = error.getMessage();
                _builder.append(_message, "");
                System.err.println(_builder);
                errors++;
              }
              filesWithErrorsOrProblems++;
            } else {
              throw Exceptions.sneakyThrow(_t);
            }
          }
          int _problems_1 = problems;
          problems = (_problems_1 + problemsFound);
          files++;
          this.writeToFile(testProject, fileName, xtendCode);
        }
      }
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("Files read (");
      _builder.append(files, "");
      _builder.append(")");
      InputOutput.<String>println(_builder.toString());
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("Files with errors/problems (");
      _builder_1.append(filesWithErrorsOrProblems, "");
      _builder_1.append(")");
      InputOutput.<String>println(_builder_1.toString());
      StringConcatenation _builder_2 = new StringConcatenation();
      _builder_2.append("Errors (");
      _builder_2.append(errors, "");
      _builder_2.append(")");
      InputOutput.<String>println(_builder_2.toString());
      StringConcatenation _builder_3 = new StringConcatenation();
      _builder_3.append("Problems (");
      _builder_3.append(problems, "");
      _builder_3.append(")");
      InputOutput.<String>println(_builder_3.toString());
      InputOutput.<String>println("Done...");
      Assert.assertEquals(this.problemsExpected, problems);
      Assert.assertEquals(this.errorsExpected, errors);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public JavaConverter.ConversionResult converToXtend(final String unitName, final String javaCode) {
    try {
      final JavaConverter j2x = this.javaConverter.get();
      JavaConverter.ConversionResult result = j2x.toXtend(unitName, javaCode);
      Iterable<String> _problems = result.getProblems();
      int _size = IterableExtensions.size(_problems);
      boolean _equals = (_size == 0);
      if (_equals) {
        try {
          String _xtendCode = result.getXtendCode();
          this.file(_xtendCode, true);
        } catch (final Throwable _t) {
          if (_t instanceof AssertionError) {
            final AssertionError error = (AssertionError)_t;
            JavaConverter _useRobustSyntax = j2x.useRobustSyntax();
            JavaConverter.ConversionResult _xtend = _useRobustSyntax.toXtend(unitName, javaCode);
            result = _xtend;
          } else {
            throw Exceptions.sneakyThrow(_t);
          }
        }
      }
      return result;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  public void writeToFile(final File parent, final String fileName, final String content) {
    try {
      final File targetFile = new File(parent, fileName);
      String _absolutePath = targetFile.getAbsolutePath();
      String _plus = ("Writing to: " + _absolutePath);
      InputOutput.<String>println(_plus);
      boolean _exists = targetFile.exists();
      boolean _not = (!_exists);
      if (_not) {
        Files.createParentDirs(targetFile);
        targetFile.createNewFile();
      }
      Charset _defaultCharset = Charset.defaultCharset();
      Files.write(content, targetFile, _defaultCharset);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
