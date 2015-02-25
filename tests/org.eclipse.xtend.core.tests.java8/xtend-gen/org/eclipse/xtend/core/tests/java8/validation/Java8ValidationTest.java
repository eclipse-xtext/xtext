/**
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.xtend.core.tests.java8.validation;

import com.google.inject.Inject;
import org.eclipse.xtend.core.tests.AbstractXtendTestCase;
import org.eclipse.xtend.core.tests.java8.Java8RuntimeInjectorProvider;
import org.eclipse.xtend.core.validation.IssueCodes;
import org.eclipse.xtend.core.xtend.XtendFile;
import org.eclipse.xtend.core.xtend.XtendPackage;
import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.junit4.InjectWith;
import org.eclipse.xtext.junit4.validation.ValidationTestHelper;
import org.eclipse.xtext.xbase.XbasePackage;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.eclipse.xtext.xbase.lib.Extension;
import org.junit.Test;

/**
 * @author Miro Spoenemann - Initial contribution and API
 */
@InjectWith(Java8RuntimeInjectorProvider.class)
@SuppressWarnings("all")
public class Java8ValidationTest extends AbstractXtendTestCase {
  @Inject
  @Extension
  private ValidationTestHelper _validationTestHelper;
  
  @Test
  public void testInheritedDefaultMethod() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("interface A {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void foo() { }");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("class C implements A {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def bar() { foo }");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      XtendFile _file = this.file(_builder.toString());
      this._validationTestHelper.assertNoErrors(_file);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testRedeclaredMethodFromObject() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("interface A {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("override String toString()");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("class C implements A {");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      XtendFile _file = this.file(_builder.toString());
      this._validationTestHelper.assertNoErrors(_file);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testRedeclaredMethodFromCustomClass() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("interface A {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void m()");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("class B {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void m() {}");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("class C extends B {}");
      _builder.newLine();
      _builder.append("class D extends C implements A {");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      XtendFile _file = this.file(_builder.toString());
      this._validationTestHelper.assertNoErrors(_file);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testConflictingDefaultMethods01() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("interface A {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void foo() { }");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("interface B {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void foo() { }");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("class C implements A, B { }");
      _builder.newLine();
      XtendFile _file = this.file(_builder.toString());
      this._validationTestHelper.assertError(_file, XtendPackage.Literals.XTEND_CLASS, IssueCodes.CONFLICTING_DEFAULT_METHODS, 
        "The type C inherits multiple implementations of the method foo() from A and B.");
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testConflictingDefaultMethods02() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("interface A {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void foo() { }");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("interface B {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void foo() { }");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("interface C extends A, B { }");
      _builder.newLine();
      XtendFile _file = this.file(_builder.toString());
      this._validationTestHelper.assertError(_file, XtendPackage.Literals.XTEND_INTERFACE, IssueCodes.CONFLICTING_DEFAULT_METHODS, 
        "The type C inherits multiple implementations of the method foo() from A and B.");
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testConflictingDefaultMethods03() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("interface A {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void foo() { }");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("interface B {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void foo()");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("class C implements A, B { }");
      _builder.newLine();
      XtendFile _file = this.file(_builder.toString());
      this._validationTestHelper.assertError(_file, XtendPackage.Literals.XTEND_CLASS, IssueCodes.CONFLICTING_DEFAULT_METHODS, 
        "The non-abstract method foo() inherited from A conflicts with the method foo() inherited from B.");
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testConflictingDefaultMethods04() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("interface A {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void foo() { }");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("interface B {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void foo()");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("interface C extends A, B { }");
      _builder.newLine();
      XtendFile _file = this.file(_builder.toString());
      this._validationTestHelper.assertError(_file, XtendPackage.Literals.XTEND_INTERFACE, IssueCodes.CONFLICTING_DEFAULT_METHODS, 
        "The non-abstract method foo() inherited from A conflicts with the method foo() inherited from B.");
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testConflictingDefaultMethods05() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("interface A {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def int foo(java.util.List<String> list) { 0 }");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("interface B {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def double foo(java.util.List<Class<?>> list) { 0 }");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("class C implements A, B { }");
      _builder.newLine();
      XtendFile _file = this.file(_builder.toString());
      this._validationTestHelper.assertError(_file, XtendPackage.Literals.XTEND_CLASS, IssueCodes.CONFLICTING_DEFAULT_METHODS, 
        "The type C inherits multiple implementations of the method foo(List<String>) from A and B.");
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testConflictingDefaultMethods06() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("interface A {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def int foo(java.util.List<String> list) { 0 }");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("interface B {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def double foo(java.util.List<Class<?>> list) { 0 }");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("interface C extends A, B { }");
      _builder.newLine();
      XtendFile _file = this.file(_builder.toString());
      this._validationTestHelper.assertError(_file, XtendPackage.Literals.XTEND_INTERFACE, IssueCodes.CONFLICTING_DEFAULT_METHODS, 
        "The type C inherits multiple implementations of the method foo(List<String>) from A and B.");
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testConflictingDefaultMethods07() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("interface A {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void foo() { }");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("interface B {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void foo() { }");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("interface C {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void foo() { }");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("interface D extends A, B {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("override foo() { }");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("class E implements A, B, C, D { }");
      _builder.newLine();
      XtendFile _file = this.file(_builder.toString());
      this._validationTestHelper.assertError(_file, XtendPackage.Literals.XTEND_CLASS, IssueCodes.CONFLICTING_DEFAULT_METHODS, 
        "The type E inherits multiple implementations of the method foo() from C and D.");
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testConflictingDefaultMethods08() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("interface A {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void foo() { }");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("interface B {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void foo() { }");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("interface C {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void foo() { }");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("interface D extends A, B {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("override foo() { }");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("class E implements A, B, D, C { }");
      _builder.newLine();
      XtendFile _file = this.file(_builder.toString());
      this._validationTestHelper.assertError(_file, XtendPackage.Literals.XTEND_CLASS, IssueCodes.CONFLICTING_DEFAULT_METHODS, 
        "The type E inherits multiple implementations of the method foo() from C and D.");
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testConflictingDefaultMethods09() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("interface A {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void foo() { }");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("interface B {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void foo()");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("interface C {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void foo()");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("interface D extends A, B {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("override foo() { }");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("class E implements A, B, D, C { }");
      _builder.newLine();
      XtendFile _file = this.file(_builder.toString());
      this._validationTestHelper.assertError(_file, XtendPackage.Literals.XTEND_CLASS, IssueCodes.CONFLICTING_DEFAULT_METHODS, 
        "The non-abstract method foo() inherited from D conflicts with the method foo() inherited from C.");
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testConflictingDefaultMethods10() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("interface A {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void foo() { }");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("interface B {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void foo()");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("interface C {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void foo()");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("interface D extends A, B {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("override foo() { }");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("class E implements B, C, A, D { }");
      _builder.newLine();
      XtendFile _file = this.file(_builder.toString());
      this._validationTestHelper.assertError(_file, XtendPackage.Literals.XTEND_CLASS, IssueCodes.CONFLICTING_DEFAULT_METHODS, 
        "The non-abstract method foo() inherited from D conflicts with the method foo() inherited from C.");
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testConflictingDefaultMethods11() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("interface I {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void m() {}");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("interface J {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void m();");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("interface J1 extends J {}");
      _builder.newLine();
      _builder.append("class E implements I, J1 {}");
      _builder.newLine();
      XtendFile _file = this.file(_builder.toString());
      this._validationTestHelper.assertError(_file, XtendPackage.Literals.XTEND_CLASS, IssueCodes.CONFLICTING_DEFAULT_METHODS, 
        "The non-abstract method m() inherited from I conflicts with the method m() inherited from J.");
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testMissingImplementation() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("abstract class A {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void m();");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("abstract class B extends A {} ");
      _builder.newLine();
      _builder.append("interface I {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void m() {}");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("class C extends B implements I {}");
      _builder.newLine();
      XtendFile _file = this.file(_builder.toString());
      this._validationTestHelper.assertError(_file, XtendPackage.Literals.XTEND_CLASS, IssueCodes.CLASS_MUST_BE_ABSTRACT, 
        "The class C must be defined abstract because it does not implement m()");
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testResolvedConflict01() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("interface A {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void foo() { }");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("interface B {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void foo() { }");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("class C implements A, B {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("override foo() { }");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      XtendFile _file = this.file(_builder.toString());
      this._validationTestHelper.assertNoErrors(_file);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testResolvedConflict02() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("interface A {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void foo() { }");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("interface B {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void foo() { }");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("interface C extends A, B {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("override foo() { }");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("class D implements A, B, C { }");
      _builder.newLine();
      XtendFile _file = this.file(_builder.toString());
      this._validationTestHelper.assertNoErrors(_file);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testResolvedConflict03() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("interface A {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void foo() { }");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("interface B {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void foo() { }");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("interface C extends A, B {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("override foo() { }");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("class D implements A, C, B { }");
      _builder.newLine();
      XtendFile _file = this.file(_builder.toString());
      this._validationTestHelper.assertNoErrors(_file);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testResolvedConflict04() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("interface A {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void foo() { }");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("interface B {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void foo() { }");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("interface C extends A, B {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("override foo() { }");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("class D implements C, A, B { }");
      _builder.newLine();
      XtendFile _file = this.file(_builder.toString());
      this._validationTestHelper.assertNoErrors(_file);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testInheritedConflict01() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("interface A {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void foo() { }");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("interface B {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void foo() { }");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("class C implements A, B { }");
      _builder.newLine();
      _builder.append("class D extends C { }");
      _builder.newLine();
      XtendFile _file = this.file(_builder.toString());
      this._validationTestHelper.assertNoIssues(_file, XtendPackage.Literals.XTEND_CLASS, IssueCodes.CONFLICTING_DEFAULT_METHODS, 106, (-1), Severity.ERROR);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testInheritedConflict02() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("interface A {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void foo() { }");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("interface B {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void foo() { }");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("interface C extends A, B { }");
      _builder.newLine();
      _builder.append("interface D extends C { }");
      _builder.newLine();
      XtendFile _file = this.file(_builder.toString());
      this._validationTestHelper.assertNoIssues(_file, XtendPackage.Literals.XTEND_INTERFACE, IssueCodes.CONFLICTING_DEFAULT_METHODS, 111, (-1), Severity.ERROR);
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testAbstractMethodCall() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("interface A {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void foo()");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      _builder.append("class E implements A {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("override void foo() {");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("A.super.foo");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("}");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      XtendFile _file = this.file(_builder.toString());
      this._validationTestHelper.assertError(_file, XbasePackage.Literals.XMEMBER_FEATURE_CALL, org.eclipse.xtext.xbase.validation.IssueCodes.ABSTRACT_METHOD_INVOCATION, 
        "Cannot directly invoke the abstract method foo() of the type A");
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
  
  @Test
  public void testInterfaceSuperCall() {
    try {
      StringConcatenation _builder = new StringConcatenation();
      _builder.append("class Foo {");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("def void foo() {");
      _builder.newLine();
      _builder.append("\t\t");
      _builder.append("java.util.List.super.clear");
      _builder.newLine();
      _builder.append("\t");
      _builder.append("}");
      _builder.newLine();
      _builder.append("}");
      _builder.newLine();
      XtendFile _file = this.file(_builder.toString());
      this._validationTestHelper.assertError(_file, XbasePackage.Literals.XMEMBER_FEATURE_CALL, org.eclipse.xtext.xbase.validation.IssueCodes.NO_ENCLOSING_INSTANCE_AVAILABLE, 
        "The enclosing type does not extend or implement the interface List");
    } catch (Throwable _e) {
      throw Exceptions.sneakyThrow(_e);
    }
  }
}
