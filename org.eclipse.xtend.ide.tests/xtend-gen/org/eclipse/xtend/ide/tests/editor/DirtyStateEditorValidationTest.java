/**
 * Copyright (c) 2013, 2026 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtend.ide.tests.editor;

import com.google.common.collect.Iterators;
import com.google.inject.Inject;
import java.util.List;
import java.util.Objects;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.xtend.ide.tests.AbstractXtendUITestCase;
import org.eclipse.xtend.ide.tests.WorkbenchTestHelper;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.ui.refactoring.ui.SyncUtil;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function0;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.IteratorExtensions;
import org.eclipse.xtext.xbase.lib.ListExtensions;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.Version;

/**
 * @author Sven Efftinge - Initial contribution and API
 */
@SuppressWarnings("all")
public class DirtyStateEditorValidationTest extends AbstractXtendUITestCase {
  private static final long VALIDATION_TIMEOUT = 2000L;

  @Inject
  @Extension
  private WorkbenchTestHelper helper;

  @Inject
  @Extension
  private SyncUtil _syncUtil;

  @Inject
  private IResourceValidator validator;

  @After
  @Override
  public void tearDown() {
    try {
      this.helper.tearDown();
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  /**
   * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=414947
   */
  @Test
  public void testLinkingOfEnum() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("import static Foo.*");
      _builder.newLine();
      _builder.append("\t");
      _builder.newLine();
      _builder.append("@SuppressWarnings(\"all\")");
      _builder.newLine();
      _builder.append("class SomeClass {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def foo() {");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("val Foo x = A");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("return x");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("}");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.newLine();
      _builder.append("enum Foo {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("A, B, C");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      final String content = _builder.toString();
      final IFile file = this.helper.createFile("SomeClass.xtend", content);
      this._syncUtil.waitForBuild(null);
      Assert.assertEquals(0, file.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE).length);
      final XtextEditor editor = this.helper.openEditor(file);
      editor.getDocument().set("");
      this._syncUtil.waitForReconciler(editor);
      editor.getDocument().set(content);
      this._syncUtil.waitForReconciler(editor);
      this.assertNumberOfErrorAnnotations(editor, 0);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  @Test
  public void testBug410575() {
    try {
      final IJavaProject projectA = JavaCore.create(WorkbenchTestHelper.createPluginProject("projectA", "org.eclipse.xtext.xbase.lib", "org.eclipse.xtend.lib"));
      WorkbenchTestHelper.addExportedPackages(projectA.getProject(), "mypack");
      JavaCore.create(WorkbenchTestHelper.createPluginProject("projectB", "projectA", "org.eclipse.xtext.xbase.lib"));
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("package mypack");
      _builder.newLine();
      _builder.newLine();
      _builder.append("import org.eclipse.xtend.lib.annotations.Data");
      _builder.newLine();
      _builder.newLine();
      _builder.append("@Data class MyClass {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("String myProperty");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      final IFile declaration = this.helper.createFileImpl("projectA/src/mypack/MyClass.xtend", _builder.toString());
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("package otherpack");
      _builder_1.newLine();
      _builder_1.newLine();
      _builder_1.append("class OtherClass {");
      _builder_1.newLine();
      _builder_1.append("\t");
      _builder_1.append("def void myMethod() {");
      _builder_1.newLine();
      _builder_1.append("\t\t");
      _builder_1.append("val my = new mypack.MyClass(\"hello\")");
      _builder_1.newLine();
      _builder_1.append("\t\t");
      _builder_1.append("println(my.getMyProperty())");
      _builder_1.newLine();
      _builder_1.append("\t");
      _builder_1.append("}");
      _builder_1.newLine();
      _builder_1.append("}");
      _builder_1.newLine();
      final IFile file = this.helper.createFileImpl("projectB/src/otherpack/OtherClass.xtend", _builder_1.toString());
      this._syncUtil.waitForBuild(null);
      final IMarker[] markers = file.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
      int _length = markers.length;
      boolean _greaterThan = (_length > 0);
      if (_greaterThan) {
        final Function1<IMarker, Object> _function = (IMarker it) -> {
          try {
            return it.getAttribute(IMarker.MESSAGE);
          } catch (Throwable _e) {
            throw Exceptions.sneakyThrow(_e);
          }
        };
        Assert.fail(IterableExtensions.join(ListExtensions.<IMarker, Object>map(((List<IMarker>)Conversions.doWrapArray(markers)), _function), "\n"));
      }
      final XtextEditor editor = this.helper.openEditor(declaration);
      IXtextDocument _document = editor.getDocument();
      StringConcatenation _builder_2 = new StringConcatenation();
      _builder_2.append("package mypack");
      _builder_2.newLine();
      _builder_2.newLine();
      _builder_2.append("import org.eclipse.xtend.lib.annotations.Data");
      _builder_2.newLine();
      _builder_2.newLine();
      _builder_2.append("@Data class MyClass {");
      _builder_2.newLine();
      _builder_2.append("\t");
      _builder_2.append("boolean foo");
      _builder_2.newLine();
      _builder_2.append("}");
      _builder_2.newLine();
      _document.set(_builder_2.toString());
      this._syncUtil.waitForReconciler(editor);
      this.assertNumberOfErrorAnnotations(editor, 0);
      final XtextEditor editor2 = this.helper.openEditor(file);
      this._syncUtil.waitForReconciler(editor2);
      this.assertNumberOfErrorAnnotations(editor2, 2);
      IXtextDocument _document_1 = editor2.getDocument();
      StringConcatenation _builder_3 = new StringConcatenation();
      _builder_3.append("package otherpack");
      _builder_3.newLine();
      _builder_3.newLine();
      _builder_3.append("class OtherClass {");
      _builder_3.newLine();
      _builder_3.append("\t");
      _builder_3.append("def void myMethod() {");
      _builder_3.newLine();
      _builder_3.append("\t\t");
      _builder_3.append("val my = new mypack.MyClass(true)");
      _builder_3.newLine();
      _builder_3.append("\t\t");
      _builder_3.append("println(my.isFoo())");
      _builder_3.newLine();
      _builder_3.append("\t");
      _builder_3.append("}");
      _builder_3.newLine();
      _builder_3.append("}");
      _builder_3.newLine();
      _document_1.set(_builder_3.toString());
      this._syncUtil.waitForReconciler(editor2);
      this.assertNumberOfErrorAnnotations(editor2, 0);
      IXtextDocument _document_2 = editor2.getDocument();
      StringConcatenation _builder_4 = new StringConcatenation();
      _builder_4.append("package otherpack");
      _builder_4.newLine();
      _builder_4.newLine();
      _builder_4.append("@org.eclipse.xtend.lib.annotations.Data class OtherClass {");
      _builder_4.newLine();
      _builder_4.append("}");
      _builder_4.newLine();
      _document_2.set(_builder_4.toString());
      this._syncUtil.waitForReconciler(editor2);
      Version _version = Platform.getBundle("org.eclipse.pde.core").getVersion();
      Version _version_1 = new Version(3, 21, 300);
      int _compareTo = _version.compareTo(_version_1);
      final boolean beforePdeFix = (_compareTo < 0);
      int _xifexpression = (int) 0;
      if (beforePdeFix) {
        _xifexpression = 1;
      } else {
        _xifexpression = 0;
      }
      this.assertNumberOfErrorAnnotations(editor2, _xifexpression);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  /**
   * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=415052
   */
  @Test
  public void testLinkingOfEnum2() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("import static Foo.*");
      _builder.newLine();
      _builder.append("\t");
      _builder.newLine();
      _builder.append("@SuppressWarnings(\"all\")");
      _builder.newLine();
      _builder.append("class SomeClass {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def foo() {");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("val Foo x = A");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("return x");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("}");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      final String contentWithoutBar = _builder.toString();
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("enum Foo {");
      _builder_1.newLine();
      _builder_1.append("\t");
      _builder_1.append("A, B, C");
      _builder_1.newLine();
      _builder_1.append("}");
      _builder_1.newLine();
      final String bar = _builder_1.toString();
      final IFile file = this.helper.createFile("SomeClass.xtend", (contentWithoutBar + bar));
      this._syncUtil.waitForBuild(null);
      Assert.assertEquals(0, file.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE).length);
      final XtextEditor editor = this.helper.openEditor(file);
      editor.getDocument().set(contentWithoutBar);
      this._syncUtil.waitForReconciler(editor);
      this.assertNumberOfErrorAnnotations(editor, 3);
      editor.getDocument().set((contentWithoutBar + bar));
      this._syncUtil.waitForReconciler(editor);
      this.assertNumberOfErrorAnnotations(editor, 0);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  @Test
  public void testAddedInterfaceMethod() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("interface Foo {");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      final String interface_ = _builder.toString();
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("interface Foo {");
      _builder_1.newLine();
      _builder_1.append("\t");
      _builder_1.append("def void bar(String b)");
      _builder_1.newLine();
      _builder_1.append("}");
      _builder_1.newLine();
      final String interfaceChanged = _builder_1.toString();
      StringConcatenation _builder_2 = new StringConcatenation();
      _builder_2.append("class Bar implements Foo {");
      _builder_2.newLine();
      _builder_2.append("}");
      _builder_2.newLine();
      final String class_ = _builder_2.toString();
      final IFile interfaceFile = this.helper.createFile("Foo.xtend", interface_);
      final IFile classFile = this.helper.createFile("Bar.xtend", class_);
      this._syncUtil.waitForBuild(null);
      Assert.assertEquals(0, classFile.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE).length);
      final XtextEditor interfaceEditor = this.helper.openEditor(interfaceFile);
      final XtextEditor classEditor = this.helper.openEditor(classFile);
      this.assertNumberOfErrorAnnotations(classEditor, 0);
      interfaceEditor.getDocument().set(interfaceChanged);
      this._syncUtil.waitForReconciler(interfaceEditor);
      this._syncUtil.waitForReconciler(classEditor);
      final IUnitOfWork<Object, XtextResource> _function = (XtextResource it) -> {
        final CancelIndicator _function_1 = () -> {
          return false;
        };
        final List<Issue> issues = this.validator.validate(it, CheckMode.NORMAL_AND_FAST, _function_1);
        Assert.assertEquals(issues.toString(), 1, issues.size());
        return null;
      };
      classEditor.getDocument().<Object>readOnly(_function);
      interfaceEditor.getDocument().set(interface_);
      this._syncUtil.waitForReconciler(interfaceEditor);
      this._syncUtil.waitForReconciler(classEditor);
      final IUnitOfWork<Object, XtextResource> _function_1 = (XtextResource it) -> {
        final CancelIndicator _function_2 = () -> {
          return false;
        };
        final List<Issue> issues = this.validator.validate(it, CheckMode.NORMAL_AND_FAST, _function_2);
        Assert.assertTrue(issues.toString(), issues.isEmpty());
        return null;
      };
      classEditor.getDocument().<Object>readOnly(_function_1);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  @Test
  public void testChangedOverriddenSignature() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("interface Foo {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void bar()");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      final String interface_ = _builder.toString();
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("interface Foo {");
      _builder_1.newLine();
      _builder_1.append("\t");
      _builder_1.append("def void bar(String b)");
      _builder_1.newLine();
      _builder_1.append("}");
      _builder_1.newLine();
      final String interfaceChanged = _builder_1.toString();
      StringConcatenation _builder_2 = new StringConcatenation();
      _builder_2.append("class Bar implements Foo {");
      _builder_2.newLine();
      _builder_2.append("\t");
      _builder_2.append("override bar() {}");
      _builder_2.newLine();
      _builder_2.append("}");
      _builder_2.newLine();
      final String class_ = _builder_2.toString();
      final IFile interfaceFile = this.helper.createFile("Foo.xtend", interface_);
      final IFile classFile = this.helper.createFile("Bar.xtend", class_);
      this._syncUtil.waitForBuild(null);
      Assert.assertEquals(0, classFile.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE).length);
      final XtextEditor interfaceEditor = this.helper.openEditor(interfaceFile);
      final XtextEditor classEditor = this.helper.openEditor(classFile);
      this.assertNumberOfErrorAnnotations(classEditor, 0);
      interfaceEditor.getDocument().set(interfaceChanged);
      this._syncUtil.waitForReconciler(interfaceEditor);
      this._syncUtil.waitForReconciler(classEditor);
      final IUnitOfWork<Object, XtextResource> _function = (XtextResource it) -> {
        final CancelIndicator _function_1 = () -> {
          return false;
        };
        final List<Issue> issues = this.validator.validate(it, CheckMode.NORMAL_AND_FAST, _function_1);
        Assert.assertEquals(issues.toString(), 2, issues.size());
        return null;
      };
      classEditor.getDocument().<Object>readOnly(_function);
      interfaceEditor.getDocument().set(interface_);
      this._syncUtil.waitForReconciler(interfaceEditor);
      this._syncUtil.waitForReconciler(classEditor);
      final IUnitOfWork<Object, XtextResource> _function_1 = (XtextResource it) -> {
        final CancelIndicator _function_2 = () -> {
          return false;
        };
        final List<Issue> issues = this.validator.validate(it, CheckMode.NORMAL_AND_FAST, _function_2);
        Assert.assertTrue(issues.toString(), issues.isEmpty());
        return null;
      };
      classEditor.getDocument().<Object>readOnly(_function_1);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  @Test
  public void testValidateOnOpen() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("interface Foo {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void bar()");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      final String interface_ = _builder.toString();
      StringConcatenation _builder_1 = new StringConcatenation();
      _builder_1.append("interface Foo {");
      _builder_1.newLine();
      _builder_1.append("\t");
      _builder_1.append("def void bar(String b)");
      _builder_1.newLine();
      _builder_1.append("}");
      _builder_1.newLine();
      final String interfaceChanged = _builder_1.toString();
      StringConcatenation _builder_2 = new StringConcatenation();
      _builder_2.append("class Bar implements Foo {");
      _builder_2.newLine();
      _builder_2.append("\t");
      _builder_2.append("override bar() {}");
      _builder_2.newLine();
      _builder_2.append("}");
      _builder_2.newLine();
      final String class_ = _builder_2.toString();
      final IFile interfaceFile = this.helper.createFile("Foo.xtend", interface_);
      final IFile classFile = this.helper.createFile("Bar.xtend", class_);
      this._syncUtil.waitForBuild(null);
      Assert.assertEquals(0, classFile.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE).length);
      final XtextEditor interfaceEditor = this.helper.openEditor(interfaceFile);
      final XtextEditor classEditor = this.helper.openEditor(classFile);
      this.assertNumberOfErrorAnnotations(classEditor, 0);
      this.helper.closeEditor(classEditor, false);
      interfaceEditor.getDocument().set(interfaceChanged);
      this._syncUtil.waitForReconciler(interfaceEditor);
      final XtextEditor classEditorWithError = this.helper.openEditor(classFile);
      this.assertNumberOfErrorAnnotations(classEditorWithError, 2);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }

  private void assertNumberOfErrorAnnotations(final XtextEditor editor, final int expectedNumber) {
    final Function0<Boolean> _function = () -> {
      int _size = this.getErrorAnnotations(editor).size();
      return Boolean.valueOf((_size == expectedNumber));
    };
    this.helper.awaitUIUpdate(_function, DirtyStateEditorValidationTest.VALIDATION_TIMEOUT);
    final List<Annotation> errors = this.getErrorAnnotations(editor);
    final Function1<Annotation, String> _function_1 = (Annotation it) -> {
      String _text = it.getText();
      String _plus = (_text + "(");
      boolean _isPersistent = it.isPersistent();
      String _plus_1 = (_plus + Boolean.valueOf(_isPersistent));
      return (_plus_1 + ")");
    };
    Assert.assertEquals(IterableExtensions.join(ListExtensions.<Annotation, String>map(errors, _function_1), ", "), expectedNumber, errors.size());
  }

  private List<Annotation> getErrorAnnotations(final XtextEditor editor) {
    final Function1<Annotation, Boolean> _function = (Annotation it) -> {
      String _type = it.getType();
      return Boolean.valueOf(Objects.equals(_type, "org.eclipse.xtext.ui.editor.error"));
    };
    return IteratorExtensions.<Annotation>toList(IteratorExtensions.<Annotation>filter(Iterators.<Annotation>filter(editor.getDocumentProvider().getAnnotationModel(editor.getEditorInput()).getAnnotationIterator(), Annotation.class), _function));
  }
}
