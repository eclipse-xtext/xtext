/**
 * Copyright (c) 2014 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtend.core.tests.compiler;

import com.google.common.base.Objects;
import com.google.inject.Inject;
import java.lang.reflect.Method;
import org.eclipse.xtend.core.tests.compiler.AbstractXtendCompilerTest;
import org.eclipse.xtend.core.xtend.XtendClass;
import org.eclipse.xtend.core.xtend.XtendFile;
import org.eclipse.xtend.core.xtend.XtendPackage;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.junit4.validation.ValidationTestHelper;
import org.eclipse.xtext.util.IAcceptor;
import org.eclipse.xtext.xbase.annotations.xAnnotations.XAnnotationsPackage;
import org.eclipse.xtext.xbase.compiler.CompilationTestHelper;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.junit.Assert;
import org.junit.Test;

@SuppressWarnings("all")
public class GetterCompilerTest extends AbstractXtendCompilerTest {
  @Inject
  @Extension
  private ValidationTestHelper _validationTestHelper;
  
  @Test
  public void testCreateSingleGetter() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("class Foo {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("@Getter int foo = 1");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      final IAcceptor<CompilationTestHelper.Result> _function = new IAcceptor<CompilationTestHelper.Result>() {
        public void accept(final CompilationTestHelper.Result it) {
          try {
            Class<?> _compiledClass = it.getCompiledClass();
            final Object instance = _compiledClass.newInstance();
            Class<?> _compiledClass_1 = it.getCompiledClass();
            final Method getFoo = _compiledClass_1.getDeclaredMethod("getFoo");
            Object _invoke = getFoo.invoke(instance);
            Assert.assertEquals(Integer.valueOf(1), _invoke);
          } catch (Throwable _e) {
            throw Exceptions.sneakyThrow(_e);
          }
        }
      };
      this.compilationTestHelper.compile(_builder, _function);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testCreateGenericGetter() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("class Foo<T> {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("@Getter T foo = null");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      final IAcceptor<CompilationTestHelper.Result> _function = new IAcceptor<CompilationTestHelper.Result>() {
        public void accept(final CompilationTestHelper.Result it) {
          try {
            String _singleGeneratedCode = it.getSingleGeneratedCode();
            boolean _contains = _singleGeneratedCode.contains("T getFoo");
            Assert.assertTrue(_contains);
            Class<?> _compiledClass = it.getCompiledClass();
            final Object instance = _compiledClass.newInstance();
            Class<?> _compiledClass_1 = it.getCompiledClass();
            final Method getFoo = _compiledClass_1.getDeclaredMethod("getFoo");
            Object _invoke = getFoo.invoke(instance);
            Assert.assertEquals(null, _invoke);
          } catch (Throwable _e) {
            throw Exceptions.sneakyThrow(_e);
          }
        }
      };
      this.compilationTestHelper.compile(_builder, _function);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testCreateStaticGetter() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("class Foo {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("@Getter static int foo = 1");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      final IAcceptor<CompilationTestHelper.Result> _function = new IAcceptor<CompilationTestHelper.Result>() {
        public void accept(final CompilationTestHelper.Result it) {
          try {
            Class<?> _compiledClass = it.getCompiledClass();
            final Method getFoo = _compiledClass.getDeclaredMethod("getFoo");
            Object _invoke = getFoo.invoke(null);
            Assert.assertEquals(Integer.valueOf(1), _invoke);
          } catch (Throwable _e) {
            throw Exceptions.sneakyThrow(_e);
          }
        }
      };
      this.compilationTestHelper.compile(_builder, _function);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testCreateAllGetters() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("@Getter class Foo {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("static String ignoreMe");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def create {} ignoreMe2() {}");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("int foo = 1");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("String bar = \"bar\"");
      _builder.newLine();
      _builder.append("\t");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def getFoo() {");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("2");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("}");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      final IAcceptor<CompilationTestHelper.Result> _function = new IAcceptor<CompilationTestHelper.Result>() {
        public void accept(final CompilationTestHelper.Result it) {
          try {
            Class<?> _compiledClass = it.getCompiledClass();
            Method[] _declaredMethods = _compiledClass.getDeclaredMethods();
            final Function1<Method, Boolean> _function = new Function1<Method, Boolean>() {
              public Boolean apply(final Method it) {
                String _name = it.getName();
                return Boolean.valueOf(Objects.equal(_name, "getIgnoreMe"));
              }
            };
            boolean _exists = IterableExtensions.<Method>exists(((Iterable<Method>)Conversions.doWrapArray(_declaredMethods)), _function);
            Assert.assertFalse(_exists);
            Class<?> _compiledClass_1 = it.getCompiledClass();
            final Object instance = _compiledClass_1.newInstance();
            Class<?> _compiledClass_2 = it.getCompiledClass();
            final Method getFoo = _compiledClass_2.getDeclaredMethod("getFoo");
            Class<?> _compiledClass_3 = it.getCompiledClass();
            final Method getBar = _compiledClass_3.getDeclaredMethod("getBar");
            Object _invoke = getFoo.invoke(instance);
            Assert.assertEquals(Integer.valueOf(2), _invoke);
            Object _invoke_1 = getBar.invoke(instance);
            Assert.assertEquals("bar", _invoke_1);
          } catch (Throwable _e) {
            throw Exceptions.sneakyThrow(_e);
          }
        }
      };
      this.compilationTestHelper.compile(_builder, _function);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testExistingGetter() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("class Foo {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("@Getter int foo = 1");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def getFoo() {");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("2");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("}");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      final String text = _builder.toString();
      XtendClass _clazz = this.clazz(text);
      this._validationTestHelper.assertWarning(_clazz, XAnnotationsPackage.Literals.XANNOTATION, "user.issue", "no effect");
      final IAcceptor<CompilationTestHelper.Result> _function = new IAcceptor<CompilationTestHelper.Result>() {
        public void accept(final CompilationTestHelper.Result it) {
          try {
            Class<?> _compiledClass = it.getCompiledClass();
            final Object instance = _compiledClass.newInstance();
            Class<?> _compiledClass_1 = it.getCompiledClass();
            final Method getFoo = _compiledClass_1.getDeclaredMethod("getFoo");
            Object _invoke = getFoo.invoke(instance);
            Assert.assertEquals(Integer.valueOf(2), _invoke);
          } catch (Throwable _e) {
            throw Exceptions.sneakyThrow(_e);
          }
        }
      };
      this.compilationTestHelper.compile(text, _function);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testCannotOverrideFinalGetter() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("class Fizz {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def final String getFoo() {\"foo\"}");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.newLine();
      _builder.append("class Buzz extends Fizz {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("@Getter String foo");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      XtendFile _file = this.file(_builder.toString());
      this._validationTestHelper.assertError(_file, XtendPackage.Literals.XTEND_FIELD, "user.issue", "final", "Fizz", "getFoo");
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testCannotOverrideWithConflictingReturnType() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("class Foo {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def String getFoo() {\"foo\"}");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.newLine();
      _builder.append("class Bar extends Foo {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("@Getter int foo");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      XtendFile _file = this.file(_builder.toString());
      this._validationTestHelper.assertError(_file, XtendPackage.Literals.XTEND_FIELD, "user.issue", "incompatible", "Foo", "getFoo");
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testCanSpecializeReturnType() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("class Foo {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def CharSequence getFoo() {\"foo\"}");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.newLine();
      _builder.append("class Bar extends Foo {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("@Getter String foo");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      XtendFile _file = this.file(_builder.toString());
      this._validationTestHelper.assertNoErrors(_file);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
