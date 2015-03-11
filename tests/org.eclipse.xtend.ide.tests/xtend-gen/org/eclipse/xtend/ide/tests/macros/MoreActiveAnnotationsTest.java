/**
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtend.ide.tests.macros;

import com.google.common.io.CharStreams;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.xtend.ide.tests.WorkbenchTestHelper;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.junit4.internal.StopwatchRule;
import org.eclipse.xtext.junit4.ui.util.IResourcesSetupUtil;
import org.eclipse.xtext.util.StringInputStream;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Sven Efftinge - Initial contribution and API
 */
@SuppressWarnings("all")
public class MoreActiveAnnotationsTest {
  @Rule
  public StopwatchRule stopwatch = new StopwatchRule(true);
  
  @After
  public void tearDown() throws Exception {
    IResourcesSetupUtil.cleanWorkspace();
  }
  
  @Test
  public void testStaticInitializers() {
    try {
      IProject _createPluginProject = WorkbenchTestHelper.createPluginProject("macroProject");
      final IJavaProject macroProject = JavaCore.create(_createPluginProject);
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("package annotation");
      _builder.newLine();
      _builder.newLine();
      _builder.append("import org.eclipse.xtend.lib.macro.AbstractClassProcessor");
      _builder.newLine();
      _builder.append("import org.eclipse.xtend.lib.macro.Active");
      _builder.newLine();
      _builder.append("import org.eclipse.xtend.lib.macro.TransformationContext");
      _builder.newLine();
      _builder.append("import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration");
      _builder.newLine();
      _builder.append("import org.eclipse.xtend.lib.macro.declaration.Visibility");
      _builder.newLine();
      _builder.newLine();
      _builder.append("@Active(MyAAProcessor)");
      _builder.newLine();
      _builder.append("annotation MyAA {");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.newLine();
      _builder.append("class MyAAProcessor extends AbstractClassProcessor {");
      _builder.newLine();
      _builder.append("\t");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("override doTransform(MutableClassDeclaration annotatedClass, extension TransformationContext context) {");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("annotatedClass.addField(\'CONSTANT_INT\') [");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("type = primitiveInt");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("visibility = Visibility.PUBLIC");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("constantValueAsInt = 42");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("]");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("annotatedClass.addField(\'CONSTANT_LONG\') [");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("type = primitiveLong");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("visibility = Visibility.PUBLIC");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("constantValueAsLong = 42");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("]");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("annotatedClass.addField(\'CONSTANT_FLOAT\') [");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("type = primitiveFloat");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("visibility = Visibility.PUBLIC");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("constantValueAsFloat = 42.34f");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("]");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("annotatedClass.addField(\'CONSTANT_DOUBLE\') [");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("type = primitiveDouble");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("visibility = Visibility.PUBLIC");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("constantValueAsDouble = 42.11d");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("]");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("annotatedClass.addField(\'CONSTANT_CHAR\') [");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("type = primitiveChar");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("visibility = Visibility.PUBLIC");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("constantValueAsChar = \'\\n\'");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("]");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("annotatedClass.addField(\'CONSTANT_STRING\') [");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("type = string");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("visibility = Visibility.PUBLIC");
      _builder.newLine();
      _builder.append("\t\t\t");
      _builder.append("constantValueAsString = \"\\n\\t\\\\all\"");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("]");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("}");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      this.newSource(macroProject, "annotation/MyAA.xtend", _builder.toString());
      this.addExportedPackage(macroProject, "annotation");
      IResourcesSetupUtil.waitForAutoBuild();
      IProject _createPluginProject_1 = WorkbenchTestHelper.createPluginProject("userProject", "com.google.inject", "org.eclipse.xtend.lib", 
        "org.eclipse.xtend.core.tests", "org.eclipse.xtext.xbase.lib", "org.eclipse.xtend.ide.tests.data", "org.junit", "macroProject");
      final IJavaProject userProject = JavaCore.create(_createPluginProject_1);
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("package client");
      _builder_1.newLine();
      _builder_1.newLine();
      _builder_1.append("@SuppressWarnings(client.sub.B.CONSTANT_STRING)");
      _builder_1.newLine();
      _builder_1.append("class A {");
      _builder_1.newLine();
      _builder_1.append("}");
      _builder_1.newLine();
      this.newSource(userProject, "client/A.xtend", _builder_1.toString());
      StringConcatenation _builder_2 = new StringConcatenation();
      _builder_2.append("package client.sub");
      _builder_2.newLine();
      _builder_2.newLine();
      _builder_2.append("import annotation.MyAA");
      _builder_2.newLine();
      _builder_2.newLine();
      _builder_2.append("@MyAA");
      _builder_2.newLine();
      _builder_2.append("class B {");
      _builder_2.newLine();
      _builder_2.append("}");
      _builder_2.newLine();
      this.newSource(userProject, "client/sub/B.xtend", _builder_2.toString());
      IResourcesSetupUtil.cleanBuild();
      NullProgressMonitor _nullProgressMonitor = new NullProgressMonitor();
      IResourcesSetupUtil.waitForBuild(_nullProgressMonitor);
      IProject _project = userProject.getProject();
      IResource _findMember = _project.findMember("xtend-gen/client/sub/B.java");
      final IFile file = ((IFile) _findMember);
      InputStream _contents = file.getContents();
      InputStreamReader _inputStreamReader = new InputStreamReader(_contents);
      final String contents = CharStreams.toString(_inputStreamReader);
      StringConcatenation _builder_3 = new StringConcatenation();
      _builder_3.append("package client.sub;");
      _builder_3.newLine();
      _builder_3.newLine();
      _builder_3.append("import annotation.MyAA;");
      _builder_3.newLine();
      _builder_3.newLine();
      _builder_3.append("@MyAA");
      _builder_3.newLine();
      _builder_3.append("@SuppressWarnings(\"all\")");
      _builder_3.newLine();
      _builder_3.append("public class B {");
      _builder_3.newLine();
      _builder_3.append("  ");
      _builder_3.append("public final static int CONSTANT_INT = 42;");
      _builder_3.newLine();
      _builder_3.append("  ");
      _builder_3.newLine();
      _builder_3.append("  ");
      _builder_3.append("public final static long CONSTANT_LONG = 42L;");
      _builder_3.newLine();
      _builder_3.append("  ");
      _builder_3.newLine();
      _builder_3.append("  ");
      _builder_3.append("public final static float CONSTANT_FLOAT = 42.34f;");
      _builder_3.newLine();
      _builder_3.append("  ");
      _builder_3.newLine();
      _builder_3.append("  ");
      _builder_3.append("public final static double CONSTANT_DOUBLE = 42.11d;");
      _builder_3.newLine();
      _builder_3.append("  ");
      _builder_3.newLine();
      _builder_3.append("  ");
      _builder_3.append("public final static char CONSTANT_CHAR = \'\\n\';");
      _builder_3.newLine();
      _builder_3.append("  ");
      _builder_3.newLine();
      _builder_3.append("  ");
      _builder_3.append("public final static String CONSTANT_STRING = \"\\n\\t\\\\all\";");
      _builder_3.newLine();
      _builder_3.append("}");
      _builder_3.newLine();
      String _string = _builder_3.toString();
      Assert.assertEquals(_string, contents);
      IResourcesSetupUtil.assertNoErrorsInWorkspace();
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  private IFile newSource(final IJavaProject it, final String fileName, final String contents) {
    try {
      IProject _project = it.getProject();
      final IFile result = _project.getFile(("src/" + fileName));
      IContainer parent = result.getParent();
      while ((!parent.exists())) {
        ((IFolder) parent).create(true, false, null);
      }
      StringInputStream _stringInputStream = new StringInputStream(contents);
      result.create(_stringInputStream, true, null);
      return result;
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  private void addExportedPackage(final IJavaProject pluginProject, final String... exportedPackages) {
    try {
      IProject _project = pluginProject.getProject();
      final IFile manifestFile = _project.getFile("META-INF/MANIFEST.MF");
      final InputStream manifestContent = manifestFile.getContents();
      Manifest _xtrycatchfinallyexpression = null;
      try {
        _xtrycatchfinallyexpression = new Manifest(manifestContent);
      } finally {
        manifestContent.close();
      }
      final Manifest manifest = _xtrycatchfinallyexpression;
      final Attributes attrs = manifest.getMainAttributes();
      boolean _containsKey = attrs.containsKey("Export-Package");
      if (_containsKey) {
        Object _get = attrs.get("Export-Package");
        String _plus = (_get + ",");
        String _join = IterableExtensions.join(((Iterable<?>)Conversions.doWrapArray(exportedPackages)), ",");
        String _plus_1 = (_plus + _join);
        attrs.putValue("Export-Package", _plus_1);
      } else {
        String _join_1 = IterableExtensions.join(((Iterable<?>)Conversions.doWrapArray(exportedPackages)), ",");
        attrs.putValue("Export-Package", _join_1);
      }
      final ByteArrayOutputStream out = new ByteArrayOutputStream();
      manifest.write(out);
      byte[] _byteArray = out.toByteArray();
      final ByteArrayInputStream in = new ByteArrayInputStream(_byteArray);
      BufferedInputStream _bufferedInputStream = new BufferedInputStream(in);
      manifestFile.setContents(_bufferedInputStream, true, true, null);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
