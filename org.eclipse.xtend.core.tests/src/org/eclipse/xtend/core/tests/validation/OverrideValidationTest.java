/*******************************************************************************
 * Copyright (c) 2011, 2017 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtend.core.tests.validation;

import static org.eclipse.xtend.core.validation.IssueCodes.*;
import static org.eclipse.xtend.core.xtend.XtendPackage.Literals.*;
import static org.eclipse.xtext.xbase.XbasePackage.Literals.*;
import static org.eclipse.xtext.xbase.annotations.xAnnotations.XAnnotationsPackage.Literals.*;
import static org.eclipse.xtext.xbase.validation.IssueCodes.*;

import org.eclipse.xtend.core.tests.AbstractXtendTestCase;
import org.eclipse.xtend.core.xtend.XtendClass;
import org.eclipse.xtend.core.xtend.XtendInterface;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;
import org.eclipse.xtext.xbase.XbasePackage;
import org.junit.Test;

import com.google.inject.Inject;

/**
 * @author Jan Koehnlein - Initial contribution and API
 * @author Holger Schill
 * @author Sebastian Zarnekow
 */
public class OverrideValidationTest extends AbstractXtendTestCase {

	@Inject
	private ValidationTestHelper helper;

	@Test public void testDuplicateMethod_0() throws Exception {
		var source = "class Foo { def bar(int x) {true} def bar(int x) {false} }";
		XtendClass xtendClass = clazz(source);
		helper.assertError(xtendClass.getMembers().get(0), XTEND_FUNCTION, DUPLICATE_METHOD,
				source.indexOf("bar"), "bar".length(),
				"duplicate", "method");
		helper.assertError(xtendClass.getMembers().get(0), XTEND_FUNCTION, DUPLICATE_METHOD,
				source.lastIndexOf("bar"), "bar".length(),
				"duplicate", "method");
	}

	@Test public void testDuplicateMethod_1() throws Exception {
		XtendClass xtendClass = clazz("class Foo { def bar(String x) {true} def bar(int x) {false} }");
		helper.assertNoError(xtendClass, DUPLICATE_METHOD);
	}

	@Test public void testDuplicateMethod_2() throws Exception {
		XtendClass xtendClass = clazz("class Foo { def bar(String x) {true} def bar(String x, int x) {false} }");
		helper.assertNoError(xtendClass, DUPLICATE_METHOD);
	}

	@Test public void testDuplicateMethod_3() throws Exception {
		XtendClass xtendClass = clazz("class Foo { def bar(java.util.List<String> x) {true} def bar(java.util.List<Integer> x) {false} }");
		helper.assertError(xtendClass.getMembers().get(0), XTEND_FUNCTION, DUPLICATE_METHOD,
				"erasure", "List)", "method",
				"List<String");
		helper.assertError(xtendClass.getMembers().get(1), XTEND_FUNCTION, DUPLICATE_METHOD,
				"erasure", "List)", "method",
				"List<Integer");
	}

	@Test public void testDuplicateMethod_4() throws Exception {
		XtendClass xtendClass = clazz("class Foo { def <T> bar(T t) { '' } def <T> bar(T t) { 1 } }");
		helper.assertError(xtendClass.getMembers().get(0), XTEND_FUNCTION, DUPLICATE_METHOD, "duplicate", "bar(T)");
		helper.assertError(xtendClass.getMembers().get(1), XTEND_FUNCTION, DUPLICATE_METHOD, "duplicate", "bar(T)");
	}

	@Test public void testDuplicateMethod_5() throws Exception {
		XtendClass xtendClass = clazz("class Foo { def <T> bar(T t) { '' } def <V> bar(V v) { 1 } }");
		helper.assertError(xtendClass.getMembers().get(0), XTEND_FUNCTION, DUPLICATE_METHOD, "erasure", "Object)", "T");
		helper.assertError(xtendClass.getMembers().get(1), XTEND_FUNCTION, DUPLICATE_METHOD, "erasure", "Object)", "V");
	}

	@Test public void testDuplicateMethod_6() throws Exception {
		XtendClass xtendClass = clazz("class Foo { def <T> bar(CharSequence seq) { '' } def <V extends CharSequence> bar(V v) { 1 } }");
		helper.assertError(xtendClass.getMembers().get(0), XTEND_FUNCTION, DUPLICATE_METHOD, "erasure", "CharSequence)");
		helper.assertError(xtendClass.getMembers().get(1), XTEND_FUNCTION, DUPLICATE_METHOD, "erasure",
				"CharSequence)", "bar(V)");
	}

	@Test public void testDuplicateMethod_7() throws Exception {
		XtendClass xtendClass = clazz("class Foo { def <T extends CharSequence> bar(T t) { '' } def <V extends String> bar(V v) { 1 } }");
		helper.assertNoErrors(xtendClass);
	}
	
	@Test public void testDuplicateMethod_8() throws Exception {
		XtendClass xtendClass = clazz("class Foo { def bar(Unknown x) {true} def bar(Unknown x) {true} def bar(DoesNotExist x) {false} }");
		helper.assertNoError(xtendClass, DUPLICATE_METHOD);
	}
	
	@Test public void testDuplicateMethod_9() throws Exception {
		XtendClass xtendClass = clazz("class Foo { def <T> !(CharSequence seq) { '' } def <V extends CharSequence> operator_not(V v) { 1 } }");
		helper.assertError(xtendClass.getMembers().get(0), XTEND_FUNCTION, DUPLICATE_METHOD, "erasure", "CharSequence)");
		helper.assertError(xtendClass.getMembers().get(1), XTEND_FUNCTION, DUPLICATE_METHOD, "erasure",
				"CharSequence)", "operator_not(V)");
	}

	@Test public void testOverrideGenericMethod_1() throws Exception {
		XtendClass xtendClass = clazz(" import java.util.List abstract class Foo<T> extends test.GenericSuperTypeClass<T> {  " +
									"override <RenamedT1> getValue1(List<RenamedT1> t) {}" +
									"}");
		helper.assertNoErrors(xtendClass);
	}
	@Test public void testOverrideGenericMethod_2() throws Exception {
		XtendClass xtendClass = clazz(" abstract class Foo<T> extends test.GenericSuperTypeClass<T> {  " +
								    "override <T2> getValue2(T2[] t) {} " +
									"}");
		helper.assertNoErrors(xtendClass);
	}
	@Test public void testOverrideGenericMethod_3() throws Exception {
		XtendClass xtendClass = clazz(" import java.util.List abstract class Foo<T> extends test.GenericSuperTypeClass<T> {  " +
									"override <T3> getValue3(List<T3> t) {} " +
									"}");
		helper.assertNoErrors(xtendClass);
	}
	@Test public void testOverrideGenericMethod_4() throws Exception {
		XtendClass xtendClass = clazz(" import java.util.List abstract class Foo<T> extends test.GenericSuperTypeClass<T> {  " +
								    "override <T3> getValue4(List<T3[]> t) {} " +
									"}");
		helper.assertNoErrors(xtendClass);
	}
	@Test public void testOverrideGenericMethod_5() throws Exception {
		XtendClass xtendClass = clazz(" import java.util.List abstract class Foo<T> extends test.GenericSuperTypeClass<T> {  " +
								    "override <T3> getValue5(List<List<T3>[]> t) {} " +
									"}");
		helper.assertNoErrors(xtendClass);
	}
	@Test public void testOverrideGenericMethod_6() throws Exception {
		XtendClass xtendClass = clazz(" import java.util.List abstract class Foo<T> extends test.GenericSuperTypeClass<T> {  " +
								    "override <T3> getValue6(List<? extends T3> t) {} " +
									"}");
		helper.assertNoErrors(xtendClass);
	}
	@Test public void testOverrideGenericMethod_7() throws Exception {
		XtendClass xtendClass = clazz(" import java.util.List abstract class Foo<T> extends test.GenericSuperTypeClass<T> {  " +
								    "override <T3> getValue7(List<? super T3> t) {} " +
									"}");
		helper.assertNoErrors(xtendClass);
	}
	@Test public void testOverrideGenericMethod_8() throws Exception {
		XtendClass xtendClass = clazz(" import java.util.List abstract class Foo<T> extends test.GenericSuperTypeClass<T> {  " +
									"override <T1, T2 extends T1> getValue8(List<T1> t, List<T2> t2) {} " +
									"}");
		helper.assertNoErrors(xtendClass);
	}
	@Test public void testOverrideGenericMethod_9() throws Exception {
		XtendClass xtendClass = clazz(" import java.util.List import java.io.Serializable abstract class Foo<T> extends test.GenericSuperTypeClass<T> {  " +
									"override <T2, T1 extends Serializable & CharSequence> getValue9(List<T1> t) {}"+
									"}");
		helper.assertNoErrors(xtendClass);
	}

	@Test public void testOverrideGenericMethod_10() throws Exception {
		var source = " abstract class Foo<T> extends test.GenericSuperTypeClass<T> {  " +
									"override <T extends String> foo1() {}"+
									"}";
		XtendClass xtendClass = clazz(source);
		helper.assertError(xtendClass, XTEND_FUNCTION, DUPLICATE_METHOD,
				source.indexOf("foo1"), "foo1".length(),
				"Name clash", "same erasure", "GenericSuperTypeClass",
				"does not override it");
	}

	@Test public void testOverrideGenericMethod_11() throws Exception {
		XtendClass xtendClass = clazz(" abstract class Foo<T> extends test.GenericSuperTypeClass<T> {  " +
									"override <T extends CharSequence> foo1() {}"+
									"}");
		helper.assertNoErrors(xtendClass);
	}

	@Test public void testOverrideGenericMethod_12() throws Exception {
		XtendClass xtendClass = clazz(" abstract class Foo<T> extends test.GenericSuperTypeClass<T> {  " +
									"override <Unused> void foo2(T t, (T)=>void proc){} "+
									"}");
		helper.assertNoErrors(xtendClass);
	}
	
	@Test public void testOverrideGenericMethod_13() throws Exception {
		XtendClass xtendClass = clazz("abstract class Foo extends test.GenericSuperTypeClass<String> {  " +
									"override <C extends Comparable<C>> C getComparable(){ null } "+
									"}");
		helper.assertNoErrors(xtendClass);
	}
	
	@Test public void testOverrideGenericMethod_14() throws Exception {
		XtendClass xtendClass = clazz("abstract class Foo extends test.GenericSuperTypeClass<String> {  " +
									"override <X extends Comparable<X>> void useComparable(X x){} "+
									"}");
		helper.assertNoErrors(xtendClass);
	}
	
	@Test public void testOverrideGenericMethod_15() throws Exception {
		XtendClass xtendClass = clazz(" abstract class Foo<T> extends test.GenericSuperTypeClass<T> {  " +
									"override <T> void foo3(T t, (T)=>void proc){} "+
									"}");
		helper.assertNoErrors(xtendClass);
	}

	@Test public void testStaticMethodHidesInstanceMethod() throws Exception {
		var source = " abstract class Foo<T> extends test.GenericSuperTypeClass<T> {  " +
						"static def foo1() {}"+
						"}";
		XtendClass xtendClass = clazz(source);
		helper.assertError(xtendClass, XTEND_FUNCTION, DUPLICATE_METHOD,
				source.indexOf("foo1"), "foo1".length(),
				"The static method", "GenericSuperTypeClass",
				"cannot hide the instance method");
	}

	@Test public void testOverrideStaticMethod() throws Exception {
		var source = " abstract class Foo extends testdata.Methods {  " +
						"override staticMethod() {}"+
						"}";
		XtendClass xtendClass = clazz(source);
		helper.assertError(xtendClass, XTEND_FUNCTION, DUPLICATE_METHOD,
				source.indexOf("staticMethod"), "staticMethod".length(),
				"The instance method", "Methods",
				"cannot override the static method");
	}

	@Test public void testOverrideReturnType() throws Exception {
		XtendClass xtendClass = clazz("abstract class Foo<T> extends test.GenericSuperTypeClass<T> {  " +
									"override <T> T getSomething(T t){} "+
									"}");
		helper.assertNoErrors(xtendClass);
	}

	@Test public void testOverrideReturnType_1() throws Exception {
		XtendClass xtendClass = clazz("import java.util.List import java.io.Serializable abstract class Foo<T> extends test.GenericSuperTypeClass<T> {  " +
									"override <T1> T1[] getValue1(List<T1> t) {}" +
									"}");
		helper.assertNoErrors(xtendClass);
	}

	@Test public void testOverrideReturnType_2() throws Exception {
		XtendClass xtendClass = clazz("import java.util.List import java.io.Serializable abstract class Foo<T> extends test.GenericSuperTypeClass<T> {  " +
									"override <T3> List<T3> getValue3(List<T3> t) {}" +
									"}");
		helper.assertNoErrors(xtendClass);
	}
	
	@Test public void testOverrideReturnType_3() throws Exception {
		XtendClass xtendClass = clazz("abstract class Foo extends test.GenericSuperTypeClass<Comparable<String>> {  " +
									"override String getTypeParamValue(){ null } "+
									"}");
		helper.assertNoErrors(xtendClass);
	}

	@Test public void testOverrideReturnType_4() throws Exception {
		XtendClass xtendClass = clazz("abstract class Foo extends test.GenericSuperTypeClass<Integer> {  " +
									"override String getComparable(){ null }" +
									"}");
		helper.assertNoErrors(xtendClass);
	}

	@Test public void testObsoleteOverride_0() throws Exception {
		XtendClass xtendClass = clazz("class Foo { override bar() {true} }");
		helper.assertError(xtendClass.getMembers().get(0), XTEND_FUNCTION, OBSOLETE_OVERRIDE);
	}
	
	@Test public void testObsoleteOverrideAnnotation_0() throws Exception {
		XtendClass xtendClass = clazz("class Foo { @Override def bar() {true} }");
		helper.assertError(xtendClass.getMembers().get(0).getAnnotations().get(0), XANNOTATION, OBSOLETE_ANNOTATION_OVERRIDE);
	}

	@Test public void testObsoleteOverrideAnnotation_1() throws Exception {
		XtendClass xtendClass = clazz("class Foo extends test.SuperClass { @Override override string() { null}}");
		helper.assertWarning(xtendClass.getMembers().get(0).getAnnotations().get(0), XANNOTATION, OBSOLETE_ANNOTATION_OVERRIDE);
	}
	
	@Test public void testObsoleteOverrideAnnotation_2() throws Exception {
		XtendClass xtendClass = clazz("class Foo extends test.SuperClass { @Override def string() { null}}");
		helper.assertNoErrors(xtendClass.getMembers().get(0).getAnnotations().get(0), XANNOTATION, OBSOLETE_ANNOTATION_OVERRIDE);
	}

	@Test public void testObsoleteOverride_1() throws Exception {
		XtendClass xtendClass = clazz("class Foo extends test.SuperClass { override bar() {true} }");
		helper.assertError(xtendClass.getMembers().get(0), XTEND_FUNCTION, OBSOLETE_OVERRIDE);
	}

	@Test public void testObsoleteOverride_2() throws Exception {
		XtendClass xtendClass = clazz("class Foo extends test.SuperClass { override privateMethod() {true}}");
		helper.assertError(xtendClass.getMembers().get(0), XTEND_FUNCTION, OBSOLETE_OVERRIDE);
	}
	
	@Test public void testObsoleteOverride_3() throws Exception {
		XtendClass xtendClass = clazz("class Foo { def void ok() {} override bar() {true} }");
		helper.assertError(xtendClass.getMembers().get(0), XTEND_FUNCTION, OBSOLETE_OVERRIDE);
	}
	
	@Test public void testObsoleteOverride_4() throws Exception {
		XtendClass xtendClass = clazz("class Foo extends test.SuperClass { def void ok() {} override bar() {true} }");
		helper.assertError(xtendClass.getMembers().get(0), XTEND_FUNCTION, OBSOLETE_OVERRIDE);
	}
	
	@Test public void testObsoleteOverride_5() throws Exception {
		XtendClass xtendClass = clazz("class Foo extends test.SuperClass { def void ok() {} override privateMethod() {true}}");
		helper.assertError(xtendClass.getMembers().get(0), XTEND_FUNCTION, OBSOLETE_OVERRIDE);
	}
	
	@Test public void testMissingOverride_0() throws Exception {
		XtendClass xtendClass = clazz("class Foo extends test.SuperClass { def string() {null} }");
		helper.assertError(xtendClass.getMembers().get(0), XTEND_FUNCTION, MISSING_OVERRIDE);
	}

	@Test public void testMissingOverride_1() throws Exception {
		XtendClass xtendClass = clazz("class Foo extends test.SuperClass { def string(int i) { null} }");
		helper.assertNoError(xtendClass.getMembers().get(0), MISSING_OVERRIDE);
	}

	@Test public void testMissingOverride_2() throws Exception {
		XtendClass xtendClass = clazz("class Foo extends test.SuperClass { def string(String s) {null} }");
		helper.assertError(xtendClass.getMembers().get(0), XTEND_FUNCTION, MISSING_OVERRIDE);
	}

	@Test public void testMissingOverride_3() throws Exception {
		XtendClass xtendClass = clazz("class Foo extends test.SuperClass { def object(String s) {null} }");
		helper.assertNoError(xtendClass.getMembers().get(0), MISSING_OVERRIDE);
	}

	@Test public void testMissingOverride_4() throws Exception {
		XtendClass xtendClass = clazz("class Foo implements test.SomeInterface { def foo() { true } }");
		helper.assertError(xtendClass.getMembers().get(0), XTEND_FUNCTION, MISSING_OVERRIDE);
	}

	@Test public void testMissingOverride_5() throws Exception {
		XtendClass xtendClass = clazz("class Foo { def boolean equals(Object x) { return true } }");
		helper.assertError(xtendClass.getMembers().get(0), XTEND_FUNCTION, MISSING_OVERRIDE);
	}

	@Test public void testIncompatibleReturnType_0() throws Exception {
		XtendClass xtendClass = clazz("class Foo extends test.SuperClass { override Boolean string() {true} }");
		helper.assertError(xtendClass.getMembers().get(0), XTEND_FUNCTION, INCOMPATIBLE_RETURN_TYPE);
	}

	@Test public void testIncompatibleReturnType_1() throws Exception {
		XtendClass xtendClass = clazz("class Foo extends test.SuperClass { override String object() {''} }");
		helper.assertNoError(xtendClass.getMembers().get(0), INCOMPATIBLE_RETURN_TYPE);
	}

	@Test public void testIncompatibleReturnType_2() throws Exception {
		XtendClass xtendClass = clazz("class Foo implements test.SomeInterface { override void foo() {} }");
		helper.assertError(xtendClass.getMembers().get(0), XTEND_FUNCTION, INCOMPATIBLE_RETURN_TYPE);
	}

	@Test public void testIncompatibleGenericReturnType_0() throws Exception {
		XtendClass xtendClass = clazz("class Foo extends test.SuperClass { override java.util.ArrayList<String> returnsListString() {} }");
		helper.assertNoError(xtendClass.getMembers().get(0), INCOMPATIBLE_RETURN_TYPE);
	}

	@Test public void testIncompatibleGenericReturnType_1() throws Exception {
		XtendClass xtendClass = clazz("class Foo extends test.SuperClass { override java.util.List<Object> returnsListString() {} }");
		helper.assertError(xtendClass.getMembers().get(0), XTEND_FUNCTION, INCOMPATIBLE_RETURN_TYPE);
	}

	@Test public void testIncompatibleGenericReturnType_2() throws Exception {
		XtendClass xtendClass = clazz("class Foo extends test.SuperClass { override java.util.ArrayList<String> returnsListExtendsObject() {} }");
		helper.assertNoError(xtendClass.getMembers().get(0), INCOMPATIBLE_RETURN_TYPE);
	}

	@Test public void testOverrideVoidFunction() throws Exception {
		XtendClass xtendClass = clazz("class Foo extends test.SuperClass { override voidFunction() {''} }");
		helper.assertNoError(xtendClass.getMembers().get(0), INCOMPATIBLE_RETURN_TYPE);
	}

	@Test public void testOverrideVoidFunction_1() throws Exception {
		XtendClass xtendClass = clazz("class Foo extends test.SuperClass { override String voidFunction() {''} }");
		helper.assertError(xtendClass.getMembers().get(0), XTEND_FUNCTION, INCOMPATIBLE_RETURN_TYPE);
	}

	@Test public void testOverrideWithTypeParameter() throws Exception {
		XtendClass xtendClass = clazz("class Foo extends test.GenericClass { override java.util.List<String> foo() {newArrayList} }");
		helper.assertNoError(xtendClass.getMembers().get(0), INCOMPATIBLE_RETURN_TYPE);
	}

	@Test public void testOverrideWithTypeParameter_1() throws Exception {
		XtendClass xtendClass = clazz("class Foo extends test.GenericClass<String> { override java.util.List<String> foo() {newArrayList} }");
		helper.assertNoError(xtendClass.getMembers().get(0), INCOMPATIBLE_RETURN_TYPE);
	}

	@Test public void testOverrideWithTypeParameter_2() throws Exception {
		XtendClass xtendClass = clazz("class Foo<S> extends test.GenericClass<S> { override java.util.List<S> foo() {null} }");
		helper.assertNoError(xtendClass.getMembers().get(0), INCOMPATIBLE_RETURN_TYPE);
	}

	@Test public void testClassMustBeAbstract_01() throws Exception {
		var source = "class Foo<S> implements Comparable<S> { }";
		XtendClass xtendClass = clazz(source);
		helper.assertError(xtendClass, XTEND_CLASS, CLASS_MUST_BE_ABSTRACT,
				source.indexOf("Foo"), "Foo".length(),
				"abstract", "not", "implement",
				"compareTo(S)");
	}

	@Test public void testClassMustBeAbstract_02() throws Exception {
		var source = "class Foo<S> implements Comparable { }";
		XtendClass xtendClass = clazz(source);
		helper.assertError(xtendClass, XTEND_CLASS, CLASS_MUST_BE_ABSTRACT,
				source.indexOf("Foo"), "Foo".length(),
				"abstract", "not", "implement",
				"compareTo(Object)");
	}

	@Test public void testClassMustBeAbstract_03() throws Exception {
		var source = "class Foo implements Comparable<String> { }";
		XtendClass xtendClass = clazz(source);
		helper.assertError(xtendClass, XTEND_CLASS, CLASS_MUST_BE_ABSTRACT,
				source.indexOf("Foo"), "Foo".length(),
				"abstract", "not", "implement",
				"compareTo(String)");
	}

	@Test public void testClassMustBeAbstract_04() throws Exception {
		XtendClass xtendClass = clazz("class Foo implements Comparable { }");
		helper.assertError(xtendClass, XTEND_CLASS, CLASS_MUST_BE_ABSTRACT, "abstract", "not", "implement",
				"compareTo(Object)");
	}

	@Test
	public void testClassMustBeAbstract_05() throws Exception {
		XtendClass xtendClass = clazz("class MyList<T> extends java.util.ArrayList<T> { }");
		helper.assertNoErrors(xtendClass);
	}
	
	@Test
	public void testClassMustBeAbstract_06() throws Exception {
		XtendClass xtendClass = clazz("class StringList extends java.util.ArrayList<StringList> { }");
		helper.assertNoErrors(xtendClass);
	}

	@Test public void testOverrideFinalClass() throws Exception {
		var source = "class Foo extends String { }";
		XtendClass xtendClass = clazz(source);
		helper.assertError(xtendClass, XTEND_CLASS, OVERRIDDEN_FINAL,
				source.indexOf("String"), "String".length(),
				"override", "final");
	}

	@Test public void testOverrideFinalMethod() throws Exception {
		var source = "class Foo extends test.ClassWithFinalMembers { def foo() {} }";
		XtendClass xtendClass = clazz(source);
		helper.assertError(xtendClass.getMembers().get(0), XTEND_FUNCTION, OVERRIDDEN_FINAL,
				source.indexOf("foo"), "foo".length(),
				"override", "final");
	}

	@Test public void testOverrideSameVisibility() throws Exception {
		XtendClass xtendClass = clazz("class Foo extends test.Visibilities { override publicMethod() {} }");
		helper.assertNoError(xtendClass.getMembers().get(0), OVERRIDE_REDUCES_VISIBILITY);
	}

	@Test public void testOverrideReveals() throws Exception {
		XtendClass xtendClass = clazz("class Foo extends test.Visibilities { override public protectedMethod() {} }");
		helper.assertNoError(xtendClass.getMembers().get(0), OVERRIDE_REDUCES_VISIBILITY);
	}

	@Test public void testOverrideHides() throws Exception {
		var source = "class Foo extends test.Visibilities { override protected publicMethod() {} }";
		XtendClass xtendClass = clazz(source);
		helper.assertError(xtendClass.getMembers().get(0), XTEND_FUNCTION, OVERRIDE_REDUCES_VISIBILITY,
				source.indexOf("publicMethod"), "publicMethod".length(),
				"reduce", "visibility");
	}

	@Test public void testDispatchHides() throws Exception {
		var source = "class Foo extends test.Visibilities { def protected dispatch publicMethod() {} }";
		XtendClass xtendClass = clazz(source);
		helper.assertError(xtendClass.getMembers().get(0), XTEND_FUNCTION, OVERRIDE_REDUCES_VISIBILITY,
				source.indexOf("protected"), "protected".length(),
				"reduce", "visibility");
	}

	@Test public void testIncompatibleThrowsClause() throws Exception {
		var source = "class Foo extends test.ExceptionThrowing { override ioException() throws Exception {} }";
		XtendClass xtendClass = clazz(source);
		helper.assertError(xtendClass.getMembers().get(0), XTEND_FUNCTION, INCOMPATIBLE_THROWS_CLAUSE,
				source.lastIndexOf("Exception"), "Exception".length(),
				"Exception", "is not", "compatible", "throws", "clause");
	}
	
	@Test public void testIncompatibleThrowsClause_01() throws Exception {
		XtendClass xtendClass = clazz("class Foo extends test.ExceptionThrowing { override runtimeException() throws Exception {} }");
		helper.assertError(xtendClass.getMembers().get(0), XTEND_FUNCTION, INCOMPATIBLE_THROWS_CLAUSE,
				"Exception", "not", "compatible", "throws", "clause");
	}
	
	@Test public void testIncompatibleThrowsClause_02() throws Exception {
		XtendClass xtendClass = clazz("class Foo extends test.ExceptionThrowing { override nullPointerException() throws Exception {} }");
		helper.assertError(xtendClass.getMembers().get(0), XTEND_FUNCTION, INCOMPATIBLE_THROWS_CLAUSE,
				"Exception", "not", "compatible", "throws", "clause");
	}
	
	@Test public void testIncompatibleThrowsClause_03() throws Exception {
		XtendClass xtendClass = clazz("class Foo extends test.ExceptionThrowing { override noException() throws Exception {} }");
		helper.assertError(xtendClass.getMembers().get(0), XTEND_FUNCTION, INCOMPATIBLE_THROWS_CLAUSE,
				"Exception", "not", "compatible", "throws", "clause");
	}
	
	@Test public void testIncompatibleThrowsClause_04() throws Exception {
		XtendClass xtendClass = clazz("class Foo extends test.ExceptionThrowing { override generifiedIoException() throws java.net.URISyntaxException {} }");
		helper.assertError(xtendClass.getMembers().get(0), XTEND_FUNCTION, INCOMPATIBLE_THROWS_CLAUSE,
				"Exception", "URISyntaxException", "not", "compatible", "throws", "clause");
	}
	
	@Test public void testIncompatibleThrowsClause_05() throws Exception {
		XtendClass xtendClass = clazz("class Foo extends test.ExceptionThrowing { override generifiedRuntimeException() throws java.io.FileNotFoundException {} }");
		helper.assertError(xtendClass.getMembers().get(0), XTEND_FUNCTION, INCOMPATIBLE_THROWS_CLAUSE,
				"Exception", "FileNotFoundException", "not", "compatible", "throws", "clause");
	}

	/**
	 * Two incompatible exceptions from three supertypes;
	 * the marker is set on the offending exceptions only.
	 * https://github.com/eclipse-xtext/xtext/issues/2912
	 */
	@Test public void testIncompatibleThrowsClause_06() throws Exception {
		var source = "class Foo extends test.ExceptionThrowing implements test.ExceptionThrowingInterface, test.ExceptionThrowingInterface2 {"
				+ "override nullPointerException() throws NullPointerException, java.io.IOException, java.io.FileNotFoundException {} "
				+ "}";
		XtendClass xtendClass = clazz(source);
		var expectedSuffix = " is not compatible with the throws clause in "
				+ "ExceptionThrowing.nullPointerException(), ExceptionThrowingInterface.nullPointerException() and ExceptionThrowingInterface2.nullPointerException()";
		helper.assertError(xtendClass.getMembers().get(0), XTEND_FUNCTION, INCOMPATIBLE_THROWS_CLAUSE,
				source.indexOf("java.io.IOException"), "java.io.IOException".length(),
				"declared exception IOException",
				expectedSuffix);
		helper.assertError(xtendClass.getMembers().get(0), XTEND_FUNCTION, INCOMPATIBLE_THROWS_CLAUSE,
				source.indexOf("java.io.FileNotFoundException"), "java.io.FileNotFoundException".length(),
				"declared exception FileNotFoundException",
				expectedSuffix);
	}

	@Test public void testCompatibleThrowsClause() throws Exception {
		XtendClass xtendClass = clazz("class Foo extends test.ExceptionThrowing { override ioException() throws java.io.FileNotFoundException {} }");
		helper.assertNoError(xtendClass.getMembers().get(0), INCOMPATIBLE_THROWS_CLAUSE);
	}

	@Test public void testCompatibleThrowsClause_01() throws Exception {
		XtendClass xtendClass = clazz("class Foo extends test.ExceptionThrowing { override runtimeException() throws NullPointerException {} }");
		helper.assertNoError(xtendClass.getMembers().get(0), INCOMPATIBLE_THROWS_CLAUSE);
	}

	@Test public void testCompatibleThrowsClause_02() throws Exception {
		XtendClass xtendClass = clazz("class Foo extends test.ExceptionThrowing { override nullPointerException() throws RuntimeException {} }");
		helper.assertNoError(xtendClass.getMembers().get(0), INCOMPATIBLE_THROWS_CLAUSE);
	}

	@Test public void testCompatibleThrowsClause_03() throws Exception {
		XtendClass xtendClass = clazz("class Foo extends test.ExceptionThrowing { override noException() throws RuntimeException {} }");
		helper.assertNoError(xtendClass.getMembers().get(0), INCOMPATIBLE_THROWS_CLAUSE);
	}

	@Test public void testCompatibleThrowsClause_04() throws Exception {
		XtendClass xtendClass = clazz("class Foo extends test.ExceptionThrowing { override throwable() throws RuntimeException {} }");
		helper.assertNoError(xtendClass.getMembers().get(0), INCOMPATIBLE_THROWS_CLAUSE);
	}

	@Test public void testCompatibleThrowsClause_05() throws Exception {
		XtendClass xtendClass = clazz("class Foo extends test.ExceptionThrowing { override throwable() throws Exception {} }");
		helper.assertNoError(xtendClass.getMembers().get(0), INCOMPATIBLE_THROWS_CLAUSE);
	}

	@Test public void testCompatibleThrowsClause_06() throws Exception {
		XtendClass xtendClass = clazz("class Foo extends test.ExceptionThrowing { override throwable() throws Throwable {} }");
		helper.assertNoError(xtendClass.getMembers().get(0), INCOMPATIBLE_THROWS_CLAUSE);
	}

	@Test public void testCompatibleThrowsClause_07() throws Exception {
		XtendClass xtendClass = clazz("class Foo extends test.ExceptionThrowing { override ioException() {} }");
		helper.assertNoError(xtendClass.getMembers().get(0), INCOMPATIBLE_THROWS_CLAUSE);
	}
	
	@Test public void testCompatibleThrowsClause_08() throws Exception {
		XtendClass xtendClass = clazz("class Foo extends test.ExceptionThrowing { override <E extends java.io.IOException> generifiedIoException() throws E {} }");
		helper.assertNoError(xtendClass.getMembers().get(0), INCOMPATIBLE_THROWS_CLAUSE);
	}
	
	@Test public void testCompatibleThrowsClause_09() throws Exception {
		XtendClass xtendClass = clazz("class Foo extends test.ExceptionThrowing { override <E extends java.io.IOException> generifiedIoException() throws E {} }");
		helper.assertNoError(xtendClass.getMembers().get(0), INCOMPATIBLE_THROWS_CLAUSE);
	}
	
	@Test public void testCompatibleThrowsClause_10() throws Exception {
		XtendClass xtendClass = clazz("class Foo extends test.ExceptionThrowing { override <E extends java.io.IOException> generifiedIoException() throws E, NullPointerException, OutOfMemoryError {} }");
		helper.assertNoError(xtendClass.getMembers().get(0), INCOMPATIBLE_THROWS_CLAUSE);
	}
	
	@Test public void testCompatibleThrowsClause_11() throws Exception {
		XtendClass xtendClass = clazz("class Foo extends test.ExceptionThrowing { override generifiedIoException() throws java.io.IOException {} }");
		helper.assertNoError(xtendClass.getMembers().get(0), INCOMPATIBLE_THROWS_CLAUSE);
	}
	
	@Test public void testCompatibleThrowsClause_12() throws Exception {
		XtendClass xtendClass = clazz("class Foo extends test.ExceptionThrowing { override generifiedRuntimeException() throws NullPointerException {} }");
		helper.assertNoError(xtendClass.getMembers().get(0), INCOMPATIBLE_THROWS_CLAUSE);
	}
	
	@Test public void testCompatibleThrowsClause_13() throws Exception {
		XtendClass xtendClass = clazz("class Foo extends test.ExceptionThrowing { override <E extends RuntimeException> generifiedRuntimeException() throws E, NullPointerException {} }");
		helper.assertNoError(xtendClass.getMembers().get(0), INCOMPATIBLE_THROWS_CLAUSE);
	}
	
	// ***************************************************************************************************
	// * and now the same for the interfaces 
	// ***************************************************************************************************
	
	@Test public void testDuplicateInterfaceMethod_0() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo { def void bar(int x) def void bar(int x) }");
		helper.assertError(xtendInterface.getMembers().get(0), XTEND_FUNCTION, DUPLICATE_METHOD, "duplicate");
	}

	@Test public void testDuplicateInterfaceMethod_1() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo { def void bar(String x) def void bar(int x) }");
		helper.assertNoError(xtendInterface, DUPLICATE_METHOD);
	}

	@Test public void testDuplicateInterfaceMethod_2() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo { def void bar(String x) def void bar(String x, int x) }");
		helper.assertNoError(xtendInterface, DUPLICATE_METHOD);
	}

	@Test public void testDuplicateInterfaceMethod_3() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo { def void bar(java.util.List<String> x) def void bar(java.util.List<Integer> x) }");
		helper.assertError(xtendInterface.getMembers().get(0), XTEND_FUNCTION, DUPLICATE_METHOD, "erasure", "List)",
				"List<String");
		helper.assertError(xtendInterface.getMembers().get(1), XTEND_FUNCTION, DUPLICATE_METHOD, "erasure", "List)",
				"List<Integer");
	}

	@Test public void testDuplicateInterfaceMethod_4() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo { def <T> void bar(T t) def <T> void bar(T t) }");
		helper.assertError(xtendInterface.getMembers().get(0), XTEND_FUNCTION, DUPLICATE_METHOD, "duplicate", "bar(T)");
		helper.assertError(xtendInterface.getMembers().get(1), XTEND_FUNCTION, DUPLICATE_METHOD, "duplicate", "bar(T)");
	}

	@Test public void testDuplicateInterfaceMethod_5() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo { def <T> void bar(T t) def <V> void bar(V v) }");
		helper.assertError(xtendInterface.getMembers().get(0), XTEND_FUNCTION, DUPLICATE_METHOD, "erasure", "Object)", "T");
		helper.assertError(xtendInterface.getMembers().get(1), XTEND_FUNCTION, DUPLICATE_METHOD, "erasure", "Object)", "V");
	}

	@Test public void testDuplicateInterfaceMethod_6() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo { def <T> void bar(CharSequence seq) def <V extends CharSequence> void bar(V v) }");
		helper.assertError(xtendInterface.getMembers().get(0), XTEND_FUNCTION, DUPLICATE_METHOD, "erasure", "CharSequence)");
		helper.assertError(xtendInterface.getMembers().get(1), XTEND_FUNCTION, DUPLICATE_METHOD, "erasure",
				"CharSequence)", "bar(V)");
	}

	@Test public void testDuplicateInterfaceMethod_7() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo { def <T extends CharSequence> void bar(T t) def <V extends String> void bar(V v) }");
		helper.assertNoErrors(xtendInterface);
	}

	@Test public void testOverrideGenericInterfaceMethod_1() throws Exception {
		XtendInterface xtendInterface = interfaze(" import java.util.List interface Foo<T> extends test.GenericSuperTypeInterface<T> {  " +
									"override <RenamedT1> getValue1(List<RenamedT1> t)" +
									"}");
		helper.assertNoErrors(xtendInterface);
	}
	@Test public void testOverrideGenericInterfaceMethod_2() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo<T> extends test.GenericSuperTypeInterface<T> {  " +
								    "override <T2> getValue2(T2[] t)" +
									"}");
		helper.assertNoErrors(xtendInterface);
	}
	@Test public void testOverrideGenericInterfaceMethod_3() throws Exception {
		XtendInterface xtendInterface = interfaze(" import java.util.List interface Foo<T> extends test.GenericSuperTypeInterface<T> {  " +
									"override <T3> getValue3(List<T3> t) " +
									"}");
		helper.assertNoErrors(xtendInterface);
	}
	@Test public void testOverrideGenericInterfaceMethod_4() throws Exception {
		XtendInterface xtendInterface = interfaze(" import java.util.List interface Foo<T> extends test.GenericSuperTypeInterface<T> {  " +
								    "override <T3> getValue4(List<T3[]> t) " +
									"}");
		helper.assertNoErrors(xtendInterface);
	}
	@Test public void testOverrideGenericInterfaceMethod_5() throws Exception {
		XtendInterface xtendInterface = interfaze(" import java.util.List interface Foo<T> extends test.GenericSuperTypeInterface<T> {  " +
								    "override <T3> getValue5(List<List<T3>[]> t) " +
									"}");
		helper.assertNoErrors(xtendInterface);
	}
	@Test public void testOverrideGenericInterfaceMethod_6() throws Exception {
		XtendInterface xtendInterface = interfaze(" import java.util.List interface Foo<T> extends test.GenericSuperTypeInterface<T> {  " +
								    "override <T3> getValue6(List<? extends T3> t) " +
									"}");
		helper.assertNoErrors(xtendInterface);
	}
	@Test public void testOverrideGenericInterfaceMethod_7() throws Exception {
		XtendInterface xtendInterface = interfaze(" import java.util.List interface Foo<T> extends test.GenericSuperTypeInterface<T> {  " +
								    "override <T3> getValue7(List<? super T3> t) " +
									"}");
		helper.assertNoErrors(xtendInterface);
	}
	@Test public void testOverrideGenericInterfaceMethod_8() throws Exception {
		XtendInterface xtendInterface = interfaze(" import java.util.List interface Foo<T> extends test.GenericSuperTypeInterface<T> {  " +
									"override <T1, T2 extends T1> getValue8(List<T1> t, List<T2> t2) " +
									"}");
		helper.assertNoErrors(xtendInterface);
	}
	@Test public void testOverrideGenericInterfaceMethod_9() throws Exception {
		XtendInterface xtendInterface = interfaze(" import java.util.List import java.io.Serializable interface Foo<T> extends test.GenericSuperTypeInterface<T> {  " +
									"override <T2, T1 extends Serializable & CharSequence> getValue9(List<T1> t)"+
									"}");
		helper.assertNoErrors(xtendInterface);
	}

	@Test public void testOverrideGenericInterfaceMethod_10() throws Exception {
		XtendInterface xtendInterface = interfaze(" interface Foo<T> extends test.GenericSuperTypeInterface<T> {  " +
									"override <T extends String> foo1()"+
									"}");
		helper.assertError(xtendInterface, XTEND_FUNCTION, DUPLICATE_METHOD);
	}

	@Test public void testOverrideGenericInterfaceMethod_11() throws Exception {
		XtendInterface xtendInterface = interfaze(" interface Foo<T> extends test.GenericSuperTypeInterface<T> {  " +
									"override <T extends CharSequence> foo1()"+
									"}");
		helper.assertNoErrors(xtendInterface);
	}

	@Test public void testOverrideGenericInterfaceMethod_12() throws Exception {
		XtendInterface xtendInterface = interfaze(" interface Foo<T> extends test.GenericSuperTypeInterface<T> {  " +
									"override <Unused> void foo2(T t, (T)=>void proc) "+
									"}");
		helper.assertNoErrors(xtendInterface);
	}
	
	@Test public void testOverrideGenericInterfaceMethod_13() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo extends test.GenericSuperTypeInterface<String> {  " +
									"override <C extends Comparable<C>> C getComparable()"+
									"}");
		helper.assertNoErrors(xtendInterface);
	}
	
	@Test public void testOverrideGenericInterfaceMethod_14() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo extends test.GenericSuperTypeInterface<String> {  " +
									"override <X extends Comparable<X>> void useComparable(X x) "+
									"}");
		helper.assertNoErrors(xtendInterface);
	}
	
	@Test public void testOverrideGenericInterfaceMethod_15() throws Exception {
		XtendInterface xtendInterface = interfaze(" interface Foo<T> extends test.GenericSuperTypeInterface<T> {  " +
									"override <T> void foo3(T t, (T)=>void proc) "+
									"}");
		helper.assertNoErrors(xtendInterface);
	}

	@Test public void testInterfaceOverrideReturnType() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo<T> extends test.GenericSuperTypeInterface<T> {  " +
									"override <T> T getSomething(T t) "+
									"}");
		helper.assertNoErrors(xtendInterface);
	}

	@Test public void testInterfaceOverrideReturnType_1() throws Exception {
		XtendInterface xtendInterface = interfaze("import java.util.List import java.io.Serializable interface Foo<T> extends test.GenericSuperTypeInterface<T> {  " +
									"override <T1> T1[] getValue1(List<T1> t)" +
									"}");
		helper.assertNoErrors(xtendInterface);
	}

	@Test public void testInterfaceOverrideReturnType_2() throws Exception {
		XtendInterface xtendInterface = interfaze("import java.util.List import java.io.Serializable interface Foo<T> extends test.GenericSuperTypeInterface<T> {  " +
									"override <T3> List<T3> getValue3(List<T3> t)" +
									"}");
		helper.assertNoErrors(xtendInterface);
	}
	
	@Test public void testInterfaceOverrideReturnType_3() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo extends test.GenericSuperTypeInterface<Comparable<String>> {  " +
									"override String getTypeParamValue() "+
									"}");
		helper.assertNoErrors(xtendInterface);
	}

	@Test public void testInterfaceOverrideReturnType_4() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo extends test.GenericSuperTypeInterface<Integer> {  " +
									"override String getComparable()" +
									"}");
		helper.assertNoErrors(xtendInterface);
	}

	@Test public void testInterfaceObsoleteOverride_0() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo { override bar() }");
		helper.assertError(xtendInterface.getMembers().get(0), XTEND_FUNCTION, OBSOLETE_OVERRIDE);
	}

	@Test public void testInterfaceObsoleteOverride_1() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo extends test.SuperInterface { override bar() }");
		helper.assertError(xtendInterface.getMembers().get(0), XTEND_FUNCTION, OBSOLETE_OVERRIDE);
	}

	@Test public void testInterfaceMissingOverride_0() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo extends test.SuperInterface { def string() }");
		helper.assertError(xtendInterface.getMembers().get(0), XTEND_FUNCTION, MISSING_OVERRIDE);
	}

	@Test public void testInterfaceMissingOverride_1() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo extends test.SuperInterface { def string(int i) }");
		helper.assertNoError(xtendInterface.getMembers().get(0), MISSING_OVERRIDE);
	}

	@Test public void testInterfaceMissingOverride_2() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo extends test.SuperInterface { def string(String s) }");
		helper.assertError(xtendInterface.getMembers().get(0), XTEND_FUNCTION, MISSING_OVERRIDE);
	}

	@Test public void testInterfaceMissingOverride_3() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo extends test.SuperInterface { def object(String s) }");
		helper.assertNoError(xtendInterface.getMembers().get(0), MISSING_OVERRIDE);
	}

	@Test public void testInterfaceMissingOverride_4() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo extends test.SomeInterface { def foo() }");
		helper.assertError(xtendInterface.getMembers().get(0), XTEND_FUNCTION, MISSING_OVERRIDE);
	}

	@Test public void testInterfaceMissingOverride_5() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo { def boolean equals(Object x) }");
		helper.assertError(xtendInterface.getMembers().get(0), XTEND_FUNCTION, MISSING_OVERRIDE);
	}

	@Test public void testInterfaceIncompatibleReturnType_0() throws Exception {
		var source = "interface Foo extends test.SuperInterface { override Boolean string() }";
		XtendInterface xtendInterface = interfaze(source);
		helper.assertError(xtendInterface.getMembers().get(0), XTEND_FUNCTION, INCOMPATIBLE_RETURN_TYPE,
				source.indexOf("Boolean"), "Boolean".length(),
				"return type is incompatible with", "string()");
	}

	@Test public void testInterfaceIncompatibleReturnType_1() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo extends test.SuperInterface { override String object() }");
		helper.assertNoError(xtendInterface.getMembers().get(0), INCOMPATIBLE_RETURN_TYPE);
	}

	@Test public void testInterfaceIncompatibleReturnType_2() throws Exception {
		var source = "interface Foo extends test.SomeInterface { override void foo() }";
		XtendInterface xtendInterface = interfaze(source);
		helper.assertError(xtendInterface.getMembers().get(0), XTEND_FUNCTION, INCOMPATIBLE_RETURN_TYPE,
				source.indexOf("void"), "void".length(),
				"return type is incompatible with", "foo()");
	}

	@Test public void testInterfaceIncompatibleGenericReturnType_0() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo extends test.SuperInterface { override java.util.ArrayList<String> returnsListString() }");
		helper.assertNoError(xtendInterface.getMembers().get(0), INCOMPATIBLE_RETURN_TYPE);
	}

	@Test public void testInterfaceIncompatibleGenericReturnType_1() throws Exception {
		var source = "interface Foo extends test.SuperInterface { override java.util.List<Object> returnsListString() }";
		XtendInterface xtendInterface = interfaze(source);
		helper.assertError(xtendInterface.getMembers().get(0), XTEND_FUNCTION, INCOMPATIBLE_RETURN_TYPE,
				source.indexOf("java.util.List<Object>"), "java.util.List<Object>".length());
	}

	@Test public void testInterfaceIncompatibleGenericReturnType_2() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo extends test.SuperInterface { override java.util.ArrayList<String> returnsListExtendsObject() }");
		helper.assertNoError(xtendInterface.getMembers().get(0), INCOMPATIBLE_RETURN_TYPE);
	}

	@Test public void testInterfaceOverrideVoidFunction() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo extends test.SuperInterface { override voidFunction() }");
		helper.assertNoError(xtendInterface.getMembers().get(0), INCOMPATIBLE_RETURN_TYPE);
	}

	@Test public void testInterfaceOverrideVoidFunction_1() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo extends test.SuperInterface { override String voidFunction() }");
		helper.assertError(xtendInterface.getMembers().get(0), XTEND_FUNCTION, INCOMPATIBLE_RETURN_TYPE);
	}

	@Test public void testInterfaceOverrideWithTypeParameter() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo extends test.GenericClass { override java.util.List<String> foo() }");
		helper.assertNoError(xtendInterface.getMembers().get(0), INCOMPATIBLE_RETURN_TYPE);
	}

	@Test public void testInterfaceOverrideWithTypeParameter_1() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo extends test.GenericClass<String> { override java.util.List<String> foo() }");
		helper.assertNoError(xtendInterface.getMembers().get(0), INCOMPATIBLE_RETURN_TYPE);
	}

	@Test public void testInterfaceOverrideWithTypeParameter_2() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo<S> extends test.GenericClass<S> { override java.util.List<S> foo() }");
		helper.assertNoError(xtendInterface.getMembers().get(0), INCOMPATIBLE_RETURN_TYPE);
	}

  	@Test public void testInterfaceIncompatibleThrowsClause() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo extends test.ExceptionThrowingInterface { override ioException() throws Exception }");
		helper.assertError(xtendInterface.getMembers().get(0), XTEND_FUNCTION, INCOMPATIBLE_THROWS_CLAUSE,
				"Exception", "not", "compatible", "throws", "clause");
	}
	
	@Test public void testInterfaceIncompatibleThrowsClause_01() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo extends test.ExceptionThrowingInterface { override runtimeException() throws Exception }");
		helper.assertError(xtendInterface.getMembers().get(0), XTEND_FUNCTION, INCOMPATIBLE_THROWS_CLAUSE,
				"Exception", "not", "compatible", "throws", "clause");
	}
	
	@Test public void testInterfaceIncompatibleThrowsClause_02() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo extends test.ExceptionThrowingInterface { override nullPointerException() throws Exception }");
		helper.assertError(xtendInterface.getMembers().get(0), XTEND_FUNCTION, INCOMPATIBLE_THROWS_CLAUSE,
				"Exception", "not", "compatible", "throws", "clause");
	}
	
	@Test public void testInterfaceIncompatibleThrowsClause_03() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo extends test.ExceptionThrowingInterface { override noException() throws Exception }");
		helper.assertError(xtendInterface.getMembers().get(0), XTEND_FUNCTION, INCOMPATIBLE_THROWS_CLAUSE,
				"Exception", "not", "compatible", "throws", "clause");
	}
	
	@Test public void testInterfaceIncompatibleThrowsClause_04() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo extends test.ExceptionThrowingInterface { override generifiedIoException() throws java.net.URISyntaxException }");
		helper.assertError(xtendInterface.getMembers().get(0), XTEND_FUNCTION, INCOMPATIBLE_THROWS_CLAUSE,
				"Exception", "URISyntaxException", "not", "compatible", "throws", "clause");
	}
	
	@Test public void testInterfaceIncompatibleThrowsClause_05() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo extends test.ExceptionThrowingInterface { override generifiedRuntimeException() throws java.io.FileNotFoundException }");
		helper.assertError(xtendInterface.getMembers().get(0), XTEND_FUNCTION, INCOMPATIBLE_THROWS_CLAUSE,
				"Exception", "FileNotFoundException", "not", "compatible", "throws", "clause");
	}
	
	@Test public void testInterfaceCompatibleThrowsClause() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo extends test.ExceptionThrowingInterface { override ioException() throws java.io.FileNotFoundException }");
		helper.assertNoError(xtendInterface.getMembers().get(0), INCOMPATIBLE_THROWS_CLAUSE);
	}

	@Test public void testInterfaceCompatibleThrowsClause_01() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo extends test.ExceptionThrowingInterface { override runtimeException() throws NullPointerException }");
		helper.assertNoError(xtendInterface.getMembers().get(0), INCOMPATIBLE_THROWS_CLAUSE);
	}

	@Test public void testInterfaceCompatibleThrowsClause_02() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo extends test.ExceptionThrowingInterface { override nullPointerException() throws RuntimeException }");
		helper.assertNoError(xtendInterface.getMembers().get(0), INCOMPATIBLE_THROWS_CLAUSE);
	}

	@Test public void testInterfaceCompatibleThrowsClause_03() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo extends test.ExceptionThrowingInterface { override noException() throws RuntimeException }");
		helper.assertNoError(xtendInterface.getMembers().get(0), INCOMPATIBLE_THROWS_CLAUSE);
	}

	@Test public void testInterfaceCompatibleThrowsClause_04() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo extends test.ExceptionThrowingInterface { override throwable() throws RuntimeException }");
		helper.assertNoError(xtendInterface.getMembers().get(0), INCOMPATIBLE_THROWS_CLAUSE);
	}

	@Test public void testInterfaceCompatibleThrowsClause_05() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo extends test.ExceptionThrowingInterface { override throwable() throws Exception }");
		helper.assertNoError(xtendInterface.getMembers().get(0), INCOMPATIBLE_THROWS_CLAUSE);
	}

	@Test public void testInterfaceCompatibleThrowsClause_06() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo extends test.ExceptionThrowingInterface { override throwable() throws Throwable }");
		helper.assertNoError(xtendInterface.getMembers().get(0), INCOMPATIBLE_THROWS_CLAUSE);
	}

	@Test public void testInterfaceCompatibleThrowsClause_07() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo extends test.ExceptionThrowingInterface { override ioException() }");
		helper.assertNoError(xtendInterface.getMembers().get(0), INCOMPATIBLE_THROWS_CLAUSE);
	}
	
	@Test public void testInterfaceCompatibleThrowsClause_08() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo extends test.ExceptionThrowingInterface { override <E extends java.io.IOException> generifiedIoException() throws E }");
		helper.assertNoError(xtendInterface.getMembers().get(0), INCOMPATIBLE_THROWS_CLAUSE);
	}
	
	@Test public void testInterfaceCompatibleThrowsClause_09() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo extends test.ExceptionThrowingInterface { override <E extends java.io.IOException> generifiedIoException() throws E }");
		helper.assertNoError(xtendInterface.getMembers().get(0), INCOMPATIBLE_THROWS_CLAUSE);
	}
	
	@Test public void testInterfaceCompatibleThrowsClause_10() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo extends test.ExceptionThrowingInterface { override <E extends java.io.IOException> generifiedIoException() throws E, NullPointerException, OutOfMemoryError }");
		helper.assertNoError(xtendInterface.getMembers().get(0), INCOMPATIBLE_THROWS_CLAUSE);
	}
	
	@Test public void testInterfaceCompatibleThrowsClause_11() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo extends test.ExceptionThrowingInterface { override generifiedIoException() throws java.io.IOException }");
		helper.assertNoError(xtendInterface.getMembers().get(0), INCOMPATIBLE_THROWS_CLAUSE);
	}
	
	@Test public void testInterfaceCompatibleThrowsClause_12() throws Exception {
		XtendInterface xtendInterface = interfaze("interface Foo extends test.ExceptionThrowingInterface { override generifiedRuntimeException() throws NullPointerException }");
		helper.assertNoError(xtendInterface.getMembers().get(0), INCOMPATIBLE_THROWS_CLAUSE);
	}
	
	@Test public void testInterfaceCompatibleThrowsClause_13() throws Exception {
		XtendClass xtendClass = clazz("class Foo extends test.ExceptionThrowingInterface { override <E extends RuntimeException> generifiedRuntimeException() throws E, NullPointerException }");
		helper.assertNoError(xtendClass.getMembers().get(0), INCOMPATIBLE_THROWS_CLAUSE);
	}

	@Test public void testAnonymousClassMustBeAbstract() throws Exception {
		var source = "class Foo { val foo = new Runnable() {} }";
		XtendClass xtendClass = clazz(source);
		helper.assertError(xtendClass, ANONYMOUS_CLASS, ANONYMOUS_CLASS_MISSING_MEMBERS,
				source.indexOf("new Runnable()"), "new Runnable()".length(),
				"The anonymous subclass of Runnable does not implement run()");
	}

	@Test public void testAnonymousClassMustBeAbstract_1() throws Exception {
		var source = "class Foo { val foo = new Runnable() { int i; } }";
		XtendClass xtendClass = clazz(source);
		helper.assertError(xtendClass, ANONYMOUS_CLASS, ANONYMOUS_CLASS_MISSING_MEMBERS,
				source.indexOf("new Runnable()"), "new Runnable()".length(),
				"The anonymous subclass of Runnable does not implement run()");
	}

	@Test public void testAnonymousClassIncompatibleSignature_0() throws Exception {
		XtendClass xtendClass = clazz("class Foo { val foo = new Runnable() { override run(int x) {} } }");
		helper.assertError(xtendClass, XTEND_FUNCTION, OBSOLETE_OVERRIDE);
	}
	
	@Test public void testAnonymousClassIncompatibleSignature_1() throws Exception {
		XtendClass xtendClass = clazz("class Foo { val foo = new Runnable() { override int run() { 1 } } }");
		helper.assertError(xtendClass, XTEND_FUNCTION, INCOMPATIBLE_RETURN_TYPE);
	}
	
	@Test public void testAnonymousClassIncompatibleSignature_2() throws Exception {
		XtendClass xtendClass = clazz("class Foo { val foo = new Runnable() { override run() throws Exception {} } }");
		helper.assertError(xtendClass, XTEND_FUNCTION, INCOMPATIBLE_THROWS_CLAUSE);
	}
	
	@Test public void testAnonymousClassMissingOverride() throws Exception {
		XtendClass xtendClass = clazz("class Foo { val foo = new Runnable() { def run() {} } }");
		helper.assertError(xtendClass, XTEND_FUNCTION, MISSING_OVERRIDE);
	}
	
	@Test public void testAnonymousClassIncompatibleTypeArg() throws Exception {
		XtendClass xtendClass = clazz("class Foo { val foo = new Bar<String>() { } } interface Bar<T extends Number> {}");
		helper.assertError(xtendClass, XCONSTRUCTOR_CALL, TYPE_BOUNDS_MISMATCH);
	}
	
	@Test public void testAnonymousClassIncompatibleConstructorArg_0() throws Exception {
		XtendClass xtendClass = clazz("class Foo { val foo = new Bar() { } } class Bar { new(String x) {} }");
		helper.assertError(xtendClass, XCONSTRUCTOR_CALL, INVALID_NUMBER_OF_ARGUMENTS);
	}
	
	@Test public void testAnonymousClassIncompatibleConstructorArg_1() throws Exception {
		XtendClass xtendClass = clazz("class Foo { val foo = new Bar(new Object()) { } } class Bar { new(int x) {} }");
		helper.assertError(xtendClass, XbasePackage.Literals.XCONSTRUCTOR_CALL, INCOMPATIBLE_TYPES);
	}
	
	@Test public void testAnonymousClassConstructorVisibility() throws Exception {
		XtendClass xtendClass = clazz("class Foo { val foo = new Bar() { } } class Bar { private new() {} }");
		helper.assertError(xtendClass, XCONSTRUCTOR_CALL, FEATURE_NOT_VISIBLE);
	}
	
	@Test public void testSynchronized_1() throws Exception{
		var source = "class Foo extends Bar { override myMethod() {1} } class Bar { def synchronized int myMethod() {0} }";
		XtendClass xtendClass = clazz(source);
		helper.assertWarning(xtendClass, XTEND_FUNCTION, MISSING_SYNCHRONIZED,
				source.indexOf("myMethod"), "myMethod".length(),
				"The overridden method is synchronized, the current one is not synchronized");
	}
	
	@Test public void testSynchronized_2() throws Exception{
		XtendClass xtendClass = clazz("class Foo extends Bar { override synchronized myMethod() {1} } class Bar { def synchronized int myMethod() {0} }");
		helper.assertNoWarnings(xtendClass, XTEND_FUNCTION, MISSING_SYNCHRONIZED);
	}

	@Test public void testOverrideJavaRecord() throws Exception {
		var source = "class Foo extends test.SomeRecord { }";
		XtendClass xtendClass = clazz(source);
		helper.assertError(xtendClass, XTEND_CLASS, OVERRIDDEN_FINAL,
				source.indexOf("test.SomeRecord"), "test.SomeRecord".length(),
				"override", "final");
	}

}
