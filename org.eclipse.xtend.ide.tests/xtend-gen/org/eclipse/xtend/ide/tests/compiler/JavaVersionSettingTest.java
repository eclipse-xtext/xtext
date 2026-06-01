/**
 * Copyright (c) 2015, 2026 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtend.ide.tests.compiler;

import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.eclipse.xtend.ide.internal.XtendActivator;
import org.eclipse.xtend.ide.tests.WorkbenchTestHelper;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.ui.testing.util.IResourcesSetupUtil;
import org.eclipse.xtext.util.JavaVersion;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Miro Spoenemann - Initial contribution and API
 */
@SuppressWarnings("all")
public class JavaVersionSettingTest extends Assert {
  @Inject
  private WorkbenchTestHelper workbenchTestHelper;

  @Before
  public void setUp() throws Exception {
    XtendActivator.getInstance().getInjector("org.eclipse.xtend.core.Xtend").injectMembers(this);
  }

  @BeforeClass
  public static void setUpProject() throws Exception {
    IResourcesSetupUtil.cleanWorkspace();
  }

  @AfterClass
  public static void tearDownProject() throws Exception {
    IResourcesSetupUtil.cleanWorkspace();
  }

  @After
  public void tearDown() {
    try {
      this.workbenchTestHelper.tearDown();
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  @Test
  public void testCompileWithJava8() {
    try {
      this.workbenchTestHelper.tearDown();
      WorkbenchTestHelper.createPluginProject(WorkbenchTestHelper.TESTPROJECT_NAME, JavaVersion.JAVA8);
      IResourcesSetupUtil.waitForJdtIndex();
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("package mypackage");
      _builder.newLine();
      _builder.append("class B implements A {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("override a() {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("}");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("interface A {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void a()");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      final IFile xtendFile = this.workbenchTestHelper.createFile("mypackage/OverrideTest.xtend", _builder.toString());
      IResourcesSetupUtil.waitForBuild();
      this.assertNoErrors(xtendFile);
      final String content = this.getJavaFileContent("xtend-gen/mypackage/B.java", xtendFile.getProject());
      Assert.assertTrue("@Override annotation not was generated.", content.contains("@Override"));
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  private String getJavaFileContent(final String fileName, final IProject project) {
    try {
      final IFile javaFile = project.getFile(fileName);
      Assert.assertTrue("Generated Java file does not exist.", javaFile.exists());
      this.assertNoErrors(javaFile);
      final InputStream javaFileStream = javaFile.getContents();
      InputStreamReader _inputStreamReader = new InputStreamReader(javaFileStream, StandardCharsets.UTF_8);
      final String content = CharStreams.toString(_inputStreamReader);
      javaFileStream.close();
      return content;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  private void assertNoErrors(final IFile file) {
    try {
      final IMarker[] findMarkers = file.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
      for (final IMarker iMarker : findMarkers) {
        String _message = MarkerUtilities.getMessage(iMarker);
        int _severity = MarkerUtilities.getSeverity(iMarker);
        boolean _equals = (_severity == IMarker.SEVERITY_ERROR);
        Assert.assertFalse(_message, _equals);
      }
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
