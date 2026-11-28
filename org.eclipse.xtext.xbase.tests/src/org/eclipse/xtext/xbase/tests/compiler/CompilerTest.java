/*******************************************************************************
 * Copyright (c) 2010, 2017 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.xbase.tests.compiler;

import org.eclipse.xtext.util.JavaVersion;
import org.eclipse.xtext.xbase.compiler.GeneratorConfig;
import org.eclipse.xtext.xbase.compiler.IGeneratorConfigProvider;
import org.eclipse.xtext.xbase.compiler.output.FakeTreeAppendable;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Sven Efftinge - Initial contribution and API
 */
public class CompilerTest extends AbstractOutputComparingCompilerTests {
	
	@Override
	protected FakeTreeAppendable createAppendable() {
		return new FakeTreeAppendable();
	}
	
	@Test public void testSimple() throws Exception {
		assertCompilesTo("""
				int _length = "foo".length();
				return _length;
				""", "'foo'.length");
	}
	
	@Test public void testExtensionOnTypeLiteral() throws Exception {
		assertCompilesTo("""
				boolean _identityEquals = (org.eclipse.xtext.xbase.lib.StringExtensions.class == null);
				return _identityEquals;
				""", "org.eclipse.xtext.xbase.lib.StringExtensions.identityEquals(null)");
	}
	
	@Test public void testBug377855() throws Exception {
		assertCompilesToStatement("""

				try {
				  Class<?> clazz = Class.forName("java.lang.String");
				  org.eclipse.xtext.xbase.lib.InputOutput.<Class<?>>println(clazz.getSuperclass());
				} catch (Throwable _e) {
				  throw org.eclipse.xtext.xbase.lib.Exceptions.sneakyThrow(_e);
				}""", """
				{
				  var clazz = Class::forName('java.lang.String')
				  println(clazz.superclass)
				}
				""");
	}
	
	@Test public void testBug383551_01() throws Exception {
		assertCompilesToStatement("""

				final java.util.List<String> list = null;
				Object _switchResult = null;
				boolean _matched = false;
				if (list instanceof java.util.ArrayList) {
				  _matched=true;
				  _switchResult = ((java.util.ArrayList)list).get(1);
				}
				final Object it = _switchResult;
				it.toString();""", """
				{
				  val java.util.List<String> list = null
				  val Object it = switch list {
				    java.util.ArrayList: list.get(1)
				  }
				  toString
				}
				""");
	}
	
	@Ignore("TODO automatically insert type arguments after instanceof expressions")
	@Test public void testBug383551_02() throws Exception {
		assertCompilesToStatement("""
				final java.util.List<String> list = null;
				Object _switchResult = null;
				boolean _matched = false;
				if (!_matched) {
				  if (list instanceof java.util.ArrayList) {
				    _matched=true;
				    Object _get = ((java.util.ArrayList)list).get(1);
				    _switchResult = _get;
				  }
				}
				final String it = _switchResult;
				it.substring(1);
				""", """
				{
				  val java.util.List<String> list = null
				  val it = switch list {
				    java.util.ArrayList: list.get(1)
				  }
				  substring(1)
				}
				""");
	}
	
	@Test public void testBug383551_03() throws Exception {
		assertCompilesToStatement("""

				final java.util.List<String> list = null;
				String _switchResult = null;
				boolean _matched = false;
				if (list instanceof java.util.ArrayList) {
				  _matched=true;
				  _switchResult = ((java.util.ArrayList<String>)list).get(1);
				}
				final String it = _switchResult;
				it.substring(1);""", """
				{
				  val java.util.List<String> list = null
				  val it = switch list {
				    java.util.ArrayList<String>: list.get(1)
				  }
				  substring(1)
				}
				""");
	}
	
	@Test public void testBug383551_04() throws Exception {
		assertCompilesToStatement("""

				final Object o = null;
				if ((o instanceof String)) {
				  ((String) o).length();
				}""", """
				{
				  val Object o = null
				  if (o instanceof String) (o as String).length()
				}
				""");
	}
	
	@Test public void testImplicitReferenceToMultitype() throws Exception {
		assertCompilesTo("""
				Iterable<AbstractStringBuilder> _plus = com.google.common.collect.Iterables.<AbstractStringBuilder>\
				concat(((Iterable<StringBuilder>) null), ((Iterable<StringBuffer>) null));
				final java.util.function.Consumer<Object> _function = (Object it) -> {
				  ((CharSequence)it).length();
				};
				com.google.common.collect.Iterables.<Object>concat(_plus, ((Iterable<String>) null)).forEach(_function);
				""",
				"((null as Iterable<StringBuilder>) + (null as Iterable<StringBuffer>) + (null as Iterable<String>)).forEach[ length ]");
	}
	
	@Test public void testReferenceToSynonym_01() throws Exception {
		assertCompilesTo("""
				int _size = ((java.util.List<String>)org.eclipse.xtext.xbase.lib.Conversions.doWrapArray(((String[]) null))).size();
				return _size;
				""", "(null as String[]).size");
	}
	
	@Test public void testImplicitReferenceToSynonym_01() throws Exception {
		assertCompilesTo("""
				final java.util.function.Consumer<String[]> _function = (String[] it) -> {
				  ((java.util.List<String>)org.eclipse.xtext.xbase.lib.Conversions.doWrapArray(it)).subList(1, 1);
				};
				((Iterable<String[]>) null).forEach(_function);
				""", "(null as Iterable<String[]>).forEach[ subList(1,1) ]");
	}
	
	@Test public void testImplicitReferenceToSynonym_02() throws Exception {
		assertCompilesTo("""
				final java.util.function.Consumer<String[]> _function = (String[] it) -> {
				  ((java.util.List<String>)org.eclipse.xtext.xbase.lib.Conversions.doWrapArray(it)).size();
				};
				((Iterable<String[]>) null).forEach(_function);
				""", "(null as Iterable<String[]>).forEach[ size() ]");
	}
	
	@Test public void testImplicitReferenceToSynonymWithPrimitives() throws Exception {
		assertCompilesTo("""
				final java.util.function.Consumer<int[]> _function = (int[] it) -> {
				  ((java.util.List<Integer>)org.eclipse.xtext.xbase.lib.Conversions.doWrapArray(it)).subList(1, 1);
				};
				((Iterable<int[]>) null).forEach(_function);
				""", "(null as Iterable<int[]>).forEach[ subList(1,1) ]");
	}
	
	@Test public void testImplicitReferenceToArray() throws Exception {
		assertCompilesTo("""
				final java.util.function.Consumer<String[]> _function = (String[] it) -> {
				  org.eclipse.xtext.xbase.lib.InputOutput.<Integer>println(Integer.valueOf(it.length));
				};
				((Iterable<String[]>) null).forEach(_function);
				""", "(null as Iterable<String[]>).forEach[ println(length) ]");
	}
	
	@Test public void testExplicitReferenceToSynonym_01() throws Exception {
		assertCompilesTo("""
				final java.util.function.Consumer<String[]> _function = (String[] it) -> {
				  ((java.util.List<String>)org.eclipse.xtext.xbase.lib.Conversions.doWrapArray(it)).subList(1, 1);
				};
				((Iterable<String[]>) null).forEach(_function);
				""", "(null as Iterable<String[]>).forEach[ it.subList(1,1) ]");
	}
	
	@Test public void testExplicitReferenceToSynonym_02() throws Exception {
		assertCompilesTo("""
				final java.util.function.Consumer<String[]> _function = (String[] it) -> {
				  ((java.util.List<String>)org.eclipse.xtext.xbase.lib.Conversions.doWrapArray(it)).size();
				};
				((Iterable<String[]>) null).forEach(_function);
				""", "(null as Iterable<String[]>).forEach[ it.size ]");
	}
	
	@Test public void testExplicitReferenceToSynonymWithPrimitives() throws Exception {
		assertCompilesTo("""
				final java.util.function.Consumer<int[]> _function = (int[] it) -> {
				  ((java.util.List<Integer>)org.eclipse.xtext.xbase.lib.Conversions.doWrapArray(it)).subList(1, 1);
				};
				((Iterable<int[]>) null).forEach(_function);
				""", "(null as Iterable<int[]>).forEach[ it.subList(1,1) ]");
	}
	
	@Test public void testExplicitReferenceToArray() throws Exception {
		assertCompilesTo("""
				final java.util.function.Consumer<String[]> _function = (String[] it) -> {
				  org.eclipse.xtext.xbase.lib.InputOutput.<Integer>println(Integer.valueOf(it.length));
				};
				((Iterable<String[]>) null).forEach(_function);
				""", "(null as Iterable<String[]>).forEach[ println(it.length) ]");
	}
	
	@Test public void testFieldAccessDontGetAVariableDeclaration() throws Exception {
		assertCompilesTo("return new testdata.Properties1().prop1;", "new testdata.Properties1().prop1");
	}

	@Test public void testNull() throws Exception {
		assertCompilesToStatement("""

				/* null */""", "null");
	}
	
	@Test public void testStringLiteralInComment() throws Exception {
		assertCompilesToStatement("""

				/* "* /" */""", "'*/'");
	}
	
	@Test public void testBlockHasNoSuperfluousBraces_01() throws Exception {
		assertCompilesToStatement("""

				try {
				  new Object();
				  new Object();
				} finally {
				  new Object();
				  new Object();
				}""", "try { new Object() new Object() } finally { new Object() new Object() }");
	}
	
	@Test public void testBlockHasNoSuperfluousBraces_02() throws Exception {
		assertCompilesToStatement("""

				if (true) {
				  new Object();
				  new Object();
				} else {
				  new Object();
				  new Object();
				}""", "if (true) { new Object() new Object() } else { new Object() new Object() }");
	}
	
	@Test public void testBlockHasNoSuperfluousBraces_03() throws Exception {
		assertCompilesTo("""
				final org.eclipse.xtext.xbase.lib.Procedures.Procedure1<Integer> _function = (Integer i) -> {
				  new Object();
				  new Object();
				};
				org.eclipse.xtext.xbase.lib.Procedures.Procedure1<? super Integer> fun = _function;
				""", "{ var (int)=>void fun = [ int i | new Object() new Object() ] }");
	}
	
	@Test public void testBlockHasNoSuperfluousBraces_04() throws Exception {
		assertCompilesTo("""
				Object it = new Object();
				it.notify();
				""", "{ var it = new Object() notify }");
	}
	
	@Test public void testBlockHasNoSuperfluousBraces_05() throws Exception {
		assertCompilesTo("""
				{
				  Object it = new Object();
				  it.notify();
				}
				{
				  Object it = new Object();
				  it.notify();
				}
				""", "{ { var it = new Object() notify } { var it = new Object() notify } }");
	}
	
	@Test public void testBlockHasNoSuperfluousBraces_06() throws Exception {
		assertCompilesTo("""
				Object it = new Object();
				{
				  Object it_1 = new Object();
				  it_1.notify();
				}
				it.notify();
				""", "{ var it = new Object() { var it = new Object() it.notify() } notify }");
	}
	
	@Test public void testBlockHasNoSuperfluousBraces_07() throws Exception {
		assertCompilesTo("""
				Object it = new Object();
				new Object().notify();
				it.notify();
				""", "{ var it = new Object() { new Object().notify() } notify }");
	}
	
	@Test
	public void testNoUnneccessaryConversionStatement() throws Exception {
		assertCompilesTo("""
				String _xblockexpression = null;
				{
				  final testdata.Properties1 x = new testdata.Properties1();
				  _xblockexpression = x.toString();
				}
				return _xblockexpression;
				""", "{ val x = new testdata.Properties1() x.toString()}");
	}
	
	@Test public void testBlock() throws Exception {
		assertCompilesTo("""
				int _xblockexpression = (int) 0;
				{
				  final java.util.ArrayList<String> it = new java.util.ArrayList<String>();
				  _xblockexpression = it.size();
				}
				return _xblockexpression;
				""", "{ val it = new java.util.ArrayList<String>(); size;}");
	}
	
	@Test public void testIf() throws Exception {
		assertCompilesTo("""
				int _xifexpression = (int) 0;
				if (true) {
				  _xifexpression = 42;
				} else {
				  _xifexpression = 21;
				}
				return _xifexpression;
				""", "if (true) 42 else 21");
	}

	@Test public void testForEach_01() throws Exception {
		assertCompilesTo("""
				java.util.ArrayList<String> _arrayList = new java.util.ArrayList<String>();
				for (final String s : _arrayList) {
				  s.length();
				}
				""", "for (String s : new java.util.ArrayList<String>())" + "s.length");
	}
	
	@Test public void testForEach_02() throws Exception {
		assertCompilesTo("""
				java.util.ArrayList<String> _arrayList = new java.util.ArrayList<String>();
				for (final String s : _arrayList) {
				  s.length();
				}
				""", "for (s : new java.util.ArrayList<String>())" + "s.length");
	}
	
	@Test public void testForEach_03() throws Exception {
		assertCompilesTo("""
				java.util.ArrayList<String> _arrayList = new java.util.ArrayList<String>();
				for (final String s : _arrayList) {
				  s.length();
				}
				""", "for (String s : new java.util.ArrayList())" + "s.length");
	}
	
	@Test public void testFeatureCall() throws Exception {
		assertCompilesTo("""
				java.util.ArrayList<String> _xblockexpression = null;
				{
				  final java.util.ArrayList<String> x = org.eclipse.xtext.xbase.lib.CollectionLiterals.<String>newArrayList("foo");
				  _xblockexpression = x;
				}
				return _xblockexpression;
				""", """
				{val x = newArrayList('foo')
				x}
				""");
	}
	
	@Test public void testNullSafeFeatureCall_01() throws Exception {
		assertCompilesTo("""
				Object _object = new Object();
				if (_object!=null) {
				  _object.notify();
				}
				""", "new Object()?.notify");
	}
	
	@Test public void testNullSafeFeatureCall_02() throws Exception {
		assertCompilesTo("""
				String _string = new Object().toString();
				if (_string!=null) {
				  _string.notify();
				}
				""", "new Object().toString?.notify");
	}
	
	@Test public void testNullSafeFeatureCall_03() throws Exception {
		assertCompilesTo("""
				Object _object = new Object();
				String _string = null;
				if (_object!=null) {
				  _string=_object.toString();
				}
				if (_string!=null) {
				  _string.notify();
				}
				""", "new Object()?.toString?.notify");
	}
	
	@Test public void testNullSafeFeatureCall_04() throws Exception {
		assertCompilesTo("""
				String _string = new String();
				String _substring = null;
				if (_string!=null) {
				  _substring=_string.substring(1);
				}
				int _length = 0;
				if (_substring!=null) {
				  _length=_substring.length();
				}
				return _length;
				""", "new String()?.substring(1)?.length");
	}
	
	@Test public void testNullSafeFeatureCall_05() throws Exception {
		assertCompilesTo("""
				int _xblockexpression = (int) 0;
				{
				  int x = 0;
				  String _string = new String();
				  if (_string!=null) {
				    _string.substring(x = 2);
				  }
				  _xblockexpression = x;
				}
				return _xblockexpression;
				""", "{var x = 0; new String()?.substring(x=2); x}");
	}
	
	@Test public void testNullSafeFeatureCall_06() throws Exception {
		assertCompilesTo("""
				String s = new String();
				String _string = null;
				if (s!=null) {
				  _string=s.toString();
				}
				return _string;
				""", "{ var s = new String; return s?.toString() }");
	}
	
	@Test public void testInline_01() throws Exception {
		assertCompilesTo("""
				String _string = ("a" + "b").toString();
				return _string;
				""", "('a'+'b').toString");
	}
	
	@Test public void testInline_02() throws Exception {
		assertCompilesTo("""
				boolean _xifexpression = false;
				if ((!true)) {
				  _xifexpression = (true == false);
				} else {
				  _xifexpression = (true != false);
				}
				return _xifexpression;
				""", "if(!true) true==false else true!=false");
	}
	
	@Test public void testSwitch_01() throws Exception {
		assertCompilesTo("""
				String _xblockexpression = null;
				{
				  final Object o = "foo";
				  boolean _matched = false;
				  if (o instanceof String) {
				    _matched=true;
				    "".toString();
				  }
				  String _switchResult_1 = null;
				  boolean _matched_1 = false;
				  if (o instanceof String) {
				    _matched_1=true;
				    _switchResult_1 = "";
				  }
				  _xblockexpression = _switchResult_1;
				}
				return _xblockexpression;
				""", """
				{
				  val Object o = 'foo'
				    switch(o) {
				        String: "".toString
				    }
				    switch(o) {
				        String: ""
				    }
				}
				""");
	}
	
	@Test
	public void testSwitchTypeGuards_01() throws Exception {
		assertCompilesTo("""
				String _switchResult = null;
				final CharSequence x = ((CharSequence) "foo");
				boolean _matched = false;
				if (x instanceof String) {
				  _matched=true;
				  String _substring = ((String)x).substring(3);
				  _switchResult = (_substring + ((String)x));
				}
				if (!_matched) {
				  if (x instanceof Comparable) {
				    _matched=true;
				    int _compareTo = ((Comparable)x).compareTo("jho");
				    String _plus = ("" + Integer.valueOf(_compareTo));
				    String _string = x.toString();
				    _switchResult = (_plus + _string);
				  }
				}
				return _switchResult;
				""", """
				switch x : 'foo' as CharSequence {
				  String : x.substring(3) + x
				  Comparable : '' + x.compareTo('jho') + x.toString
				}
				""");
	}
	
	@Test public void testSwitchTypeGuards_02() throws Exception {
		assertCompilesTo("""
				String _switchResult = null;
				final CharSequence x = ((CharSequence) "foo");
				boolean _matched = false;
				if (x instanceof String) {
				  _matched=true;
				  String _substring = ((String)x).substring(3);
				  _switchResult = (_substring + ((String)x));
				}
				if (!_matched) {
				  if (x instanceof Comparable) {
				    _matched=true;
				    int _compareTo = ((Comparable<String>)x).compareTo("jho");
				    String _plus = ("" + Integer.valueOf(_compareTo));
				    int _length = x.length();
				    _switchResult = (_plus + Integer.valueOf(_length));
				  }
				}
				return _switchResult;
				""", """
				switch x : 'foo' as CharSequence {
				  String : x.substring(3) + x
				  Comparable<String> : '' + x.compareTo('jho') + x.length
				}
				""");
	}
	
	@Test public void testNoExceptionWithUnresolveableTypes() throws Exception {
		assertCompilesTo("""
				java.util.List<Unresolveable> _switchResult = null;
				Object _instance = new Object(void.class, _Unresolvable);
				final Object x = _instance;
				boolean _matched = false;
				if (x instanceof void) {
				  _matched=true;
				  _switchResult = java.util.Arrays.<void>asList(x);
				}
				return _switchResult;
				""", """
				switch x : new Unresolveable<Unresolveable>(typeof(Unresolvable), Unresolvable) {
				  Unresolvable : java.util.Arrays.<Unresolveable>asList(x)
				}
				""", false);
	}
	
	/*
	 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=371306
	 */
	@Test public void testSwitchEarlyExits() throws Exception {
		assertCompilesTo("""
				final boolean _switchValue = true;
				boolean _matched = false;
				if (true) {
				  _matched=true;
				  return 1;
				}
				if (!_matched) {
				  if (false) {
				    _matched=true;
				    return (-1);
				  }
				}
				return 0;
				""", """
				switch true {
				  case true: return 1
				  case false: return -1
				  default: return 0
				}
				""");
	}
	
	/*
	 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=371306
	 */
	@Test public void testSwitchEarlyExits_1() throws Exception {
		assertCompilesTo("""
				int _switchResult = (int) 0;
				final boolean _switchValue = true;
				boolean _matched = false;
				if (true) {
				  _matched=true;
				  _switchResult = 1;
				}
				if (!_matched) {
				  if (false) {
				    _matched=true;
				    return (-1);
				  }
				}
				if (!_matched) {
				  return 0;
				}
				return _switchResult;
				""", """
				switch true {
				  case true: 1
				  case false: return -1
				  default: return 0
				}
				""");
	}
	
	@Test public void testNewLines_withinline() throws Exception {
		assertCompilesTo("""
				java.util.ArrayList<String> _newArrayList = org.eclipse.xtext.xbase.lib.CollectionLiterals.<String>newArrayList("foo");
				boolean _add = _newArrayList.add(
				  "bar");
				return _add;
				""", """
				newArrayList('foo') +=
				'bar'
				""");
	}
	
	@Test public void testNewLines_withinline1() throws Exception {
		assertCompilesTo("""
				java.util.ArrayList<String> _newArrayList = org.eclipse.xtext.xbase.lib.CollectionLiterals.<String>newArrayList(
				  ("foo" +\s
				    "bar"));
				return _newArrayList;
				""", """
				newArrayList(
				'foo' +
				'bar')
				""");
	}
	
	@Test public void testNewLines_forExtensionMethod() throws Exception {
		assertCompilesTo("""
				String _firstUpper = org.eclipse.xtext.xbase.lib.StringExtensions.toFirstUpper("foo");
				return _firstUpper;
				""", "'foo'.toFirstUpper");
	}
	
	@Test public void testSetLiteral() throws Exception {
		assertCompilesTo("""
				return java.util.Collections.<String>unmodifiableSet(\
				org.eclipse.xtext.xbase.lib.CollectionLiterals.<String>newHashSet("foo"));
				""", "#{'foo'}");
	}
	
	@Test public void testMapLiteral() throws Exception {
		assertCompilesTo("""
				org.eclipse.xtext.xbase.lib.Pair<String, Integer> _mappedTo = \
				org.eclipse.xtext.xbase.lib.Pair.<String, Integer>of("foo", Integer.valueOf(42));
				return java.util.Collections.<String, Integer>unmodifiableMap(\
				org.eclipse.xtext.xbase.lib.CollectionLiterals.<String, Integer>newHashMap(_mappedTo));
				""", "#{'foo'->42}");
	}
	
	@Test public void testListLiteralAsList() throws Exception {
		assertCompilesTo("""
				return java.util.Collections.<String>unmodifiableList(\
				org.eclipse.xtext.xbase.lib.CollectionLiterals.<String>newArrayList("foo"));
				""", "#['foo']");
	}

	@Test public void testListLiteralAsArray() throws Exception {
		assertCompilesTo("""
				final String[] x = { "foo" };
				""", "{val String[] x = #['foo']}");
	}

	@Test public void testExceptionOnClosure() throws Exception {
		assertCompilesTo("""
				final java.beans.VetoableChangeListener _function = (java.beans.PropertyChangeEvent it) -> {
				};
				final java.beans.VetoableChangeListener x = _function;
				""", "{val java.beans.VetoableChangeListener x = []}");
	}

	@Test public void testArrayLiteralInForLoop() throws Exception {
		assertCompilesTo("""
				for (final String i : new String[] { "a", "b", "c" }) {
				  org.eclipse.xtext.xbase.lib.InputOutput.<String>println(i);
				}
				""", "for (String i : #['a','b','c']) { println(i) }");
	}

	@Test public void testArrayLiteralInForLoop2() throws Exception {
		assertCompilesTo("""
				for (final String i : java.util.Collections.<String>unmodifiableList(\
				org.eclipse.xtext.xbase.lib.CollectionLiterals.<String>newArrayList("a", "b", "c"))) {
				  org.eclipse.xtext.xbase.lib.InputOutput.<String>println(i);
				}
				""", "for (i : #['a','b','c']) { println(i) }");
	}

	@Test
	public void testArrayLiteralInForLoop3() throws Exception {
		assertCompilesTo("""
				org.eclipse.xtext.xbase.lib.IntegerRange _upTo = new org.eclipse.xtext.xbase.lib.IntegerRange(1, 2);
				for (final org.eclipse.xtext.xbase.lib.IntegerRange i : \
				java.util.Collections.<org.eclipse.xtext.xbase.lib.IntegerRange>unmodifiableList(\
				org.eclipse.xtext.xbase.lib.CollectionLiterals.<org.eclipse.xtext.xbase.lib.IntegerRange>newArrayList(_upTo))) {
				}
				""", "for (i : #[1..2]) {}");
	}

	@Test public void testArrayLiteralInForLoop4() throws Exception {
		assertCompilesTo("""
				org.eclipse.xtext.xbase.lib.IntegerRange _upTo = new org.eclipse.xtext.xbase.lib.IntegerRange(1, 2);
				for (final org.eclipse.xtext.xbase.lib.IntegerRange i : new org.eclipse.xtext.xbase.lib.IntegerRange[] { _upTo }) {
				}
				""", "for (IntegerRange i : #[1..2]) {}");
	}

	@Test
	public void testArrayLiteralInForLoop5() throws Exception {
		assertCompilesTo("""
				org.eclipse.xtext.xbase.lib.IntegerRange _upTo = new org.eclipse.xtext.xbase.lib.IntegerRange(1, 2);
				for (final Iterable<Integer> i : new Iterable[] { _upTo }) {
				}
				""", "for (Iterable<Integer> i : #[1..2]) {}");
	}

	@Test
	public void testSetLiteralInForLoop1() throws Exception {
		assertCompilesTo("""
				org.eclipse.xtext.xbase.lib.IntegerRange _upTo = new org.eclipse.xtext.xbase.lib.IntegerRange(1, 2);
				for (final org.eclipse.xtext.xbase.lib.IntegerRange i : \
				java.util.Collections.<org.eclipse.xtext.xbase.lib.IntegerRange>unmodifiableSet(\
				org.eclipse.xtext.xbase.lib.CollectionLiterals.<org.eclipse.xtext.xbase.lib.IntegerRange>newHashSet(_upTo))) {
				}
				""", "for (i : #{1..2}) {}");
	}

	@Test
	public void testSetLiteralInForLoop2() throws Exception {
		assertCompilesTo("""
				org.eclipse.xtext.xbase.lib.IntegerRange _upTo = new org.eclipse.xtext.xbase.lib.IntegerRange(1, 2);
				for (final org.eclipse.xtext.xbase.lib.IntegerRange i : \
				java.util.Collections.<org.eclipse.xtext.xbase.lib.IntegerRange>unmodifiableSet(\
				org.eclipse.xtext.xbase.lib.CollectionLiterals.<org.eclipse.xtext.xbase.lib.IntegerRange>newHashSet(_upTo))) {
				}
				""", "for (IntegerRange i : #{1..2}) {}");
	}

	@Test
	public void testSetLiteralInForLoop3() throws Exception {
		assertCompilesTo("""
				org.eclipse.xtext.xbase.lib.IntegerRange _upTo = new org.eclipse.xtext.xbase.lib.IntegerRange(1, 2);
				for (final Iterable<Integer> i : java.util.Collections.<Iterable<Integer>>unmodifiableSet(\
				org.eclipse.xtext.xbase.lib.CollectionLiterals.<Iterable<Integer>>newHashSet(_upTo))) {
				}
				""", "for (Iterable<Integer> i : #{1..2}) {}");
	}

	@Test
	public void testBug472265_01() throws Exception {
		assertCompilesTo("""
				final closures.IAcceptors.IAcceptor _function = (String x) -> {
				};
				closures.IAcceptors.IAcceptor a = _function;
				""", "{var closures.IAcceptors.IAcceptor a = [x|]}");
	}
	
	@Test
	public void testBug472265_02() throws Exception {
		assertCompilesTo("""
				final closures.IAcceptors.IAcceptor2 _function = (String[] x) -> {
				};
				closures.IAcceptors.IAcceptor2 a = _function;
				""", "{var closures.IAcceptors.IAcceptor2 a = [x|]}");
	}
	
	@Test
	public void testBug472265_03() throws Exception {
		assertCompilesTo("""
				final closures.IAcceptors.IAcceptor3 _function = (String... x) -> {
				};
				closures.IAcceptors.IAcceptor3 a = _function;
				""", "{var closures.IAcceptors.IAcceptor3 a = [x|]}");
	}
	
	@Test
	public void testBug472265_04() throws Exception {
		assertCompilesTo("""
				final closures.IAcceptors.IAcceptor4 _function = (String x, String[] y) -> {
				};
				closures.IAcceptors.IAcceptor4 a = _function;
				""", "{var closures.IAcceptors.IAcceptor4 a = [x,y|]}");
	}
	
	@Test
	public void testBug472265_05() throws Exception {
		assertCompilesTo("""
				final closures.IAcceptors.IAcceptor5 _function = (String x, String... y) -> {
				};
				closures.IAcceptors.IAcceptor5 a = _function;
				""", "{var closures.IAcceptors.IAcceptor5 a = [x,y|]}");
	}
	
	@Test
	public void testBug472265_06() throws Exception {
		assertCompilesTo("""
				final closures.IAcceptors.IAcceptor6 _function = (String[] x, String[] y) -> {
				};
				closures.IAcceptors.IAcceptor6 a = _function;
				""", "{var closures.IAcceptors.IAcceptor6 a = [x,y|]}");
	}
	
	@Test
	public void testBug472265_07() throws Exception {
		assertCompilesTo("""
				final closures.IAcceptors.IAcceptor7 _function = (String[] x, String... y) -> {
				};
				closures.IAcceptors.IAcceptor7 a = _function;
				""", "{var closures.IAcceptors.IAcceptor7 a = [x,y|]}");
	}
	
}
