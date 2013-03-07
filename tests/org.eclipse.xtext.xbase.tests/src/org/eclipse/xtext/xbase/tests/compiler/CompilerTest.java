/*******************************************************************************
 * Copyright (c) 2010 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.xbase.tests.compiler;

import org.junit.Test;

/**
 * @author Sven Efftinge - Initial contribution and API
 */
public class CompilerTest extends AbstractOutputComparingCompilerTests {
	
	@Test public void testSimple() throws Exception {
		assertCompilesTo("\nint _length = \"foo\".length();\n" + 
				"return _length;", "'foo'.length");
	}
	
	@Test public void testBug377855() throws Exception {
		assertCompilesToStatement(
				"\n" + 
				"try {\n" + 
				"  Class<? extends Object> clazz = Class.forName(\"java.lang.String\");\n" + 
				"  Class<? extends Object> _superclass = clazz.getSuperclass();\n" + 
				"  org.eclipse.xtext.xbase.lib.InputOutput.<Class<? extends Object>>println(_superclass);\n" + 
				"} catch (Throwable _e) {\n" + 
				"  throw org.eclipse.xtext.xbase.lib.Exceptions.sneakyThrow(_e);\n" + 
				"}", 
				"{" +
				"  var clazz = Class::forName('java.lang.String')\n" + 
				"  println(clazz.superclass)\n" +
				"}");
	}
	
	@Test public void testBug383551() throws Exception {
		assertCompilesToStatement(
				"\n" + 
				"final java.util.List<String> list = null;\n" + 
				"Object _switchResult = null;\n" + 
				"boolean _matched = false;\n" + 
				"if (!_matched) {\n" + 
				"  if (list instanceof java.util.ArrayList) {\n" + 
				"    final java.util.ArrayList _arrayList = (java.util.ArrayList)list;\n" + 
				"    _matched=true;\n" + 
				"    Object _get = _arrayList.get(1);\n" + 
				"    _switchResult = _get;\n" + 
				"  }\n" + 
				"}\n" + 
				"final Object it = _switchResult;\n" + 
				"it.toString();", 
				"{" +
				"  val java.util.List<String> list = null\n" +
				"  val Object it = switch list {\n" +
				"    java.util.ArrayList: list.get(1)\n" +
				"  }\n" +
				"  toString" + 
				"}");
	}
	
	@Test public void testImplicitReferenceToMultitype() throws Exception {
		assertCompilesTo(
				"Iterable<Object> _plus = com.google.common.collect.Iterables.<Object>concat(((Iterable<StringBuilder>) null), ((Iterable<StringBuffer>) null));\n" + 
				"Iterable<Object> _plus_1 = com.google.common.collect.Iterables.<Object>concat(_plus, ((Iterable<String>) null));\n" + 
				"final org.eclipse.xtext.xbase.lib.Procedures.Procedure1<Object> _function = new org.eclipse.xtext.xbase.lib.Procedures.Procedure1<Object>() {\n" + 
				"    public void apply(final Object it) {\n" + 
				"      ((CharSequence)it).length();\n" + 
				"    }\n" + 
				"  };\n" + 
				"org.eclipse.xtext.xbase.lib.IterableExtensions.<Object>forEach(_plus_1, _function);", 
				"((null as Iterable<StringBuilder>) + (null as Iterable<StringBuffer>) + (null as Iterable<String>)).forEach[ length ]");
	}
	
	@Test public void testImplicitReferenceToSynonym() throws Exception {
		assertCompilesTo(
				"final org.eclipse.xtext.xbase.lib.Procedures.Procedure1<String[]> _function = new org.eclipse.xtext.xbase.lib.Procedures.Procedure1<String[]>() {\n" + 
				"    public void apply(final String[] it) {\n" + 
				"      org.eclipse.xtext.xbase.lib.IterableExtensions.size(((Iterable<? extends Object>)org.eclipse.xtext.xbase.lib.Conversions.doWrapArray(it)));\n" + 
				"    }\n" + 
				"  };\n" + 
				"org.eclipse.xtext.xbase.lib.IterableExtensions.<String[]>forEach(((Iterable<String[]>) null), _function);", 
				"(null as Iterable<String[]>).forEach[ size ]");
	}
	
	@Test public void testImplicitReferenceToArray() throws Exception {
		assertCompilesTo(
				"final org.eclipse.xtext.xbase.lib.Procedures.Procedure1<String[]> _function = new org.eclipse.xtext.xbase.lib.Procedures.Procedure1<String[]>() {\n" + 
				"    public void apply(final String[] it) {\n" + 
				"      int _length = it.length;\n" +
				"      org.eclipse.xtext.xbase.lib.InputOutput.<Integer>println(Integer.valueOf(_length));\n" + 
				"    }\n" + 
				"  };\n" + 
				"org.eclipse.xtext.xbase.lib.IterableExtensions.<String[]>forEach(((Iterable<String[]>) null), _function);", 
				"(null as Iterable<String[]>).forEach[ println(length) ]");
	}
	
	@Test public void testFieldAccessDontGetAVariableDeclaration() throws Exception {
		assertCompilesTo(
				"\ntestdata.Properties1 _properties1 = new testdata.Properties1();" + 
				"\nreturn _properties1.prop1;", 
				"new testdata.Properties1().prop1");
	}

	@Test public void testNull() throws Exception {
		assertCompilesToStatement(
				"\n/* null */", 
				"null");
	}
	
	@Test public void testStringLiteralInComment() throws Exception {
		assertCompilesToStatement(
				"\n/* \"* /\" */", 
				"'*/'");
	}
	
	@Test public void testBlockHasNoSuperfluousBraces_01() throws Exception {
		assertCompilesToStatement(
				"\n" +
				"try {\n" +
				"  new Object();\n" +
				"  new Object();\n" +
				"} finally {\n" +
				"  new Object();\n" +
				"  new Object();\n" +
				"}", 
				"try { new Object() new Object() } finally { new Object() new Object() }");
	}
	
	@Test public void testBlockHasNoSuperfluousBraces_02() throws Exception {
		assertCompilesToStatement(
				"\n" +
				"if (true) {\n" +
				"  new Object();\n" +
				"  new Object();\n" +
				"} else {\n" +
				"  new Object();\n" +
				"  new Object();\n" +
				"}", 
				"if (true) { new Object() new Object() } else { new Object() new Object() }");
	}
	
	@Test public void testBlockHasNoSuperfluousBraces_03() throws Exception {
		assertCompilesTo(
				"\n" + 
				"final org.eclipse.xtext.xbase.lib.Procedures.Procedure1<Integer> _function = new org.eclipse.xtext.xbase.lib.Procedures.Procedure1<Integer>() {\n" + 
				"    public void apply(final Integer i) {\n" + 
				"      new Object();\n" + 
				"      new Object();\n" + 
				"    }\n" + 
				"  };\n" + 
				"org.eclipse.xtext.xbase.lib.Procedures.Procedure1<? super Integer> fun = _function;", 
				"{ var (int)=>void fun = [ int i | new Object() new Object() ] }");
	}
	
	@Test public void testBlockHasNoSuperfluousBraces_04() throws Exception {
		assertCompilesTo(
				"\n" + 
				"Object _object = new Object();\n" +
				"Object it = _object;\n" +
				"it.notify();",
				"{ var it = new Object() notify }");
	}
	
	@Test public void testBlockHasNoSuperfluousBraces_05() throws Exception {
		assertCompilesTo(
				"\n" + 
				"{\n" +
				"  Object _object = new Object();\n" +
				"  Object it = _object;\n" +
				"  it.notify();\n" +
				"}\n" +
				"{\n" +
				"  Object _object = new Object();\n" +
				"  Object it = _object;\n" +
				"  it.notify();\n" +
				"}",
				"{ { var it = new Object() notify } { var it = new Object() notify } }");
	}
	
	@Test public void testBlockHasNoSuperfluousBraces_06() throws Exception {
		assertCompilesTo(
				"\n" + 
				"Object _object = new Object();\n" + 
				"Object it = _object;\n" + 
				"{\n" + 
				"  Object _object_1 = new Object();\n" + 
				"  Object it_1 = _object_1;\n" + 
				"  it_1.notify();\n" + 
				"}\n" + 
				"it.notify();",
				"{ var it = new Object() { var it = new Object() it.notify() } notify }");
	}
	
	@Test public void testBlockHasNoSuperfluousBraces_07() throws Exception {
		assertCompilesTo(
				"\n" + 
				"Object _object = new Object();\n" + 
				"Object it = _object;\n" + 
				"Object _object_1 = new Object();\n" + 
				"_object_1.notify();\n" + 
				"it.notify();",
				"{ var it = new Object() { new Object().notify() } notify }");
	}
	
	@Test
	public void testNoUnneccessaryConversionStatement() throws Exception {
		assertCompilesTo(
				"String _xblockexpression = null;\n" + 
				"{\n" + 
				"  testdata.Properties1 _properties1 = new testdata.Properties1();\n" + 
				"  final testdata.Properties1 x = _properties1;\n" + // this one is not necessary 
				"  String _string = x.toString();\n" + 
				"  _xblockexpression = (_string);\n" + 
				"}\n" + 
				"return _xblockexpression;",
				"{ val x = new testdata.Properties1() x.toString()}");
	}
	
	@Test public void testBlock() throws Exception {
		assertCompilesTo(
				"\nint _xblockexpression = (int) 0;\n" +
				"{\n" +
				"  java.util.ArrayList<String> _arrayList = new java.util.ArrayList<String>();\n" + 
				"  final java.util.ArrayList<String> it = _arrayList;\n" +
				"  int _size = it.size();\n" + 
				"  _xblockexpression = (_size);\n" +
				"}\n" +
				"return _xblockexpression;"
				, "{ val it = new java.util.ArrayList<String>(); size;}");
	}
	
	@Test public void testIf() throws Exception {
		assertCompilesTo(
				"\nint _xifexpression = (int) 0;\n" +
				"if (true) {\n" +
				"  _xifexpression = 42;\n" +
				"} else {\n" +
				"  _xifexpression = 21;\n}\n" +
				"return _xifexpression;"
				, "if (true) 42 else 21");
	}

	@Test public void testForEach_01() throws Exception {
		assertCompilesTo(
				"\njava.util.ArrayList<String> _arrayList = new java.util.ArrayList<String>();\n" + 
				"for (final String s : _arrayList) {\n" +
				"  s.length();\n" +
				"}"
				, "for (String s : new java.util.ArrayList<String>())" +
						"s.length");
	}
	
	@Test public void testForEach_02() throws Exception {
		assertCompilesTo(
				"\njava.util.ArrayList<String> _arrayList = new java.util.ArrayList<String>();\n" + 
				"for (final String s : _arrayList) {\n" +
				"  s.length();\n" +
				"}"
				, "for (s : new java.util.ArrayList<String>())" +
						"s.length");
	}
	
	@Test
	public void testForEach_03() throws Exception {	
		assertCompilesTo(
				"\njava.util.ArrayList<String> _arrayList = new java.util.ArrayList<String>();\n" + 
				"for (final String s : _arrayList) {\n" +
				"  s.length();\n" +
				"}"
				, "for (String s : new java.util.ArrayList())" +
						"s.length");
	}
	
	@Test public void testFeatureCall() throws Exception {
		assertCompilesTo(
				"\n" + 
				"java.util.ArrayList<String> _xblockexpression = null;\n" + 
				"{\n" + 
				"  final java.util.ArrayList<String> x = org.eclipse.xtext.xbase.lib.CollectionLiterals.<String>newArrayList(\"foo\");\n" + 
				"  _xblockexpression = (x);\n" + 
				"}\n" + 
				"return _xblockexpression;",
				"{val x = newArrayList('foo')\n" +
				"x}");
	}
	
	@Test public void testNullSafeFeatureCall_01() throws Exception {
		assertCompilesTo(
				"\n" + 
				"Object _object = new Object();\n" +
				"if (_object!=null) _object.notify();",
				"new Object()?.notify");
	}
	
	@Test public void testNullSafeFeatureCall_02() throws Exception {
		assertCompilesTo(
				"\n" + 
				"Object _object = new Object();\n" +
				"String _string = _object.toString();\n" +
				"if (_string!=null) _string.notify();",
				"new Object().toString?.notify");
	}
	
	@Test public void testNullSafeFeatureCall_03() throws Exception {
		assertCompilesTo(
				"\n" + 
				"Object _object = new Object();\n" +
				"String _string = _object==null?(String)null:_object.toString();\n" +
				"if (_string!=null) _string.notify();",
				"new Object()?.toString?.notify");
	}
	
	@Test public void testInline_01() throws Exception {
		assertCompilesTo(
				"\n" + 
				"String _plus = (\"a\" + \"b\");\n" + 
				"String _string = _plus.toString();\n" + 
				"return _string;",
				"('a'+'b').toString");
	}
	
	@Test public void testInline_02() throws Exception {
		assertCompilesTo(
				"\n" + 
				"boolean _xifexpression = false;\n" + 
				"boolean _not = (!true);\n" + 
				"if (_not) {\n" + 
				"  boolean _equals = (true == false);\n" + 
				"  _xifexpression = _equals;\n" + 
				"} else {\n" + 
				"  boolean _notEquals = (true != false);\n" + 
				"  _xifexpression = _notEquals;\n" + 
				"}\n" + 
				"return _xifexpression;",
				"if(!true) true==false else true!=false");
	}
	
	@Test public void testSwitch_() throws Exception {
		assertCompilesTo("String _xblockexpression = null;\n" + 
				"{\n" + 
				"  final Object o = \"foo\";\n" + 
				"  boolean _matched = false;\n" + 
				"  if (!_matched) {\n" + 
				"    if (o instanceof String) {\n" + 
				"      final String _string = (String)o;\n" + 
				"      _matched=true;\n" + 
				"      \"\".toString();\n" + 
				"    }\n" + 
				"  }\n" + 
				"  String _switchResult_1 = null;\n" + 
				"  boolean _matched_1 = false;\n" + 
				"  if (!_matched_1) {\n" + 
				"    if (o instanceof String) {\n" + 
				"      final String _string = (String)o;\n" + 
				"      _matched_1=true;\n" + 
				"      _switchResult_1 = \"\";\n" + 
				"    }\n" + 
				"  }\n" + 
				"  _xblockexpression = (_switchResult_1);\n" + 
				"}\n" + 
				"return _xblockexpression;",
				"{" +
				"  val Object o = 'foo'" +
				"    switch(o) {\n" + 
				"        String: \"\".toString\n" + 
				"    }\n" + 
				"    switch(o) {\n" + 
				"        String: \"\"\n" + 
				"    }" +
				"}");
	}
	
	@Test
	public void testSwitchTypeGuards_01() throws Exception {
		assertCompilesTo(
				"String _switchResult = null;\n" + 
				"final CharSequence x = ((CharSequence) \"foo\");\n" + 
				"boolean _matched = false;\n" + 
				"if (!_matched) {\n" + 
				"  if (x instanceof String) {\n" + 
				"    final String _string = (String)x;\n" + 
				"    _matched=true;\n" + 
				"    String _substring = _string.substring(3);\n" + 
				"    String _plus = (_substring + _string);\n" + 
				"    _switchResult = _plus;\n" + 
				"  }\n" + 
				"}\n" + 
				"if (!_matched) {\n" + 
				"  if (x instanceof Comparable) {\n" + 
				"    final Comparable _comparable = (Comparable)x;\n" + 
				"    _matched=true;\n" + 
				"    int _compareTo = ((Comparable)_comparable).compareTo(\"jho\");\n" + 
				"    String _plus = (\"\" + Integer.valueOf(_compareTo));\n" + 
				"    String _string = ((CharSequence)_comparable).toString();\n" + 
				"    String _plus_1 = (_plus + _string);\n" + 
				"    _switchResult = _plus_1;\n" + 
				"  }\n" + 
				"}\n" + 
				"return _switchResult;"
				, 
				"switch x : 'foo' as CharSequence {" +
				"  String : x.substring(3) + x " +
				"  Comparable : '' + x.compareTo('jho') + x.toString" +
				"}");
	}
	
	/*
	 * TODO output should be more straight forward, e.g. the cast of _comparable to CharSequence is unnecessary
	 * it should more look like
	 * int _compareTo = ..
	 * int _plus = ..
	 * int _length = x.length();
	 * ..
	 */
	@Test public void testSwitchTypeGuards_02() throws Exception {
		assertCompilesTo(
				"String _switchResult = null;\n" + 
				"final CharSequence x = ((CharSequence) \"foo\");\n" + 
				"boolean _matched = false;\n" + 
				"if (!_matched) {\n" + 
				"  if (x instanceof String) {\n" + 
				"    final String _string = (String)x;\n" + 
				"    _matched=true;\n" + 
				"    String _substring = _string.substring(3);\n" + 
				"    String _plus = (_substring + _string);\n" + 
				"    _switchResult = _plus;\n" + 
				"  }\n" + 
				"}\n" + 
				"if (!_matched) {\n" + 
				"  if (x instanceof Comparable) {\n" + 
				"    final Comparable<String> _comparable = (Comparable<String>)x;\n" + 
				"    _matched=true;\n" + 
				"    int _compareTo = ((Comparable<String>)_comparable).compareTo(\"jho\");\n" + 
				"    String _plus = (\"\" + Integer.valueOf(_compareTo));\n" + 
				"    int _length = ((CharSequence)_comparable).length();\n" + 
				"    String _plus_1 = (_plus + Integer.valueOf(_length));\n" + 
				"    _switchResult = _plus_1;\n" + 
				"  }\n" + 
				"}\n" + 
				"return _switchResult;"
				, 
				"switch x : 'foo' as CharSequence {" +
				"  String : x.substring(3) + x " +
				"  Comparable<String> : '' + x.compareTo('jho') + x.length" +
				"}");
	}
	
	/*
	 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=371306
	 */
	@Test public void testSwitchEarlyExits() throws Exception {
		assertCompilesTo(
				"final boolean _switchValue = true;\n" + 
				"boolean _matched = false;\n" + 
				"if (!_matched) {\n" + 
				"  if (true) {\n" + 
				"    _matched=true;\n" + 
				"    return 1;\n" + 
				"  }\n" + 
				"}\n" + 
				"if (!_matched) {\n" + 
				"  if (false) {\n" + 
				"    _matched=true;\n" + 
				"    return (-1);\n" + 
				"  }\n" + 
				"}\n" +
				"return 0;" 
				, 
				"switch true {" +
				"  case true: return 1" +
				"  case false: return -1" +
				"  default: return 0" +
				"}");
	}
	
	/*
	 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=371306
	 */
	@Test public void testSwitchEarlyExits_1() throws Exception {
		assertCompilesTo(
				"int _switchResult = (int) 0;\n" + 
				"final boolean _switchValue = true;\n" + 
				"boolean _matched = false;\n" + 
				"if (!_matched) {\n" + 
				"  if (true) {\n" + 
				"    _matched=true;\n" + 
				"    _switchResult = 1;\n" + 
				"  }\n" + 
				"}\n" + 
				"if (!_matched) {\n" + 
				"  if (false) {\n" + 
				"    _matched=true;\n" + 
				"    return (-1);\n" + 
				"  }\n" + 
				"}\n" +
				"if (!_matched) {\n" + 
				"  return 0;\n" + 
				"}\n" + 
				"return _switchResult;" 
				, 
				"switch true {" +
				"  case true: 1" +
				"  case false: return -1" +
				"  default: return 0" +
				"}");
	}
	
	@Test public void testNewLines_withinline() throws Exception {
		assertCompilesTo(
				"java.util.ArrayList<String> _newArrayList = org.eclipse.xtext.xbase.lib.CollectionLiterals.<String>newArrayList(\"foo\");\n" + 
				"boolean _add = _newArrayList.add(\n" + 
				"  \"bar\");\n" + 
				"return _add;", 
				"newArrayList('foo') += \n'bar'");
	}
	
	@Test public void testNewLines_withinline1() throws Exception {
		assertCompilesTo(
				"String _plus = (\"foo\" + \n" + 
				"  \"bar\");\n" + 
				"java.util.ArrayList<String> _newArrayList = org.eclipse.xtext.xbase.lib.CollectionLiterals.<String>newArrayList(_plus);\n" + 
				"return _newArrayList;", 
				"newArrayList(\n'foo' + \n'bar')");
	}
	
	@Test public void testNewLines_forExtensionMethod() throws Exception {
		assertCompilesTo(
				"String _firstUpper = org.eclipse.xtext.xbase.lib.StringExtensions.toFirstUpper(\"foo\");\n" + 
				"return _firstUpper;", 
				"\n'foo'.toFirstUpper");
	}
	
	@Test public void testListLiteralAsList() throws Exception {
		assertCompilesTo(
				"java.util.List<String> _xlistliteral = null;\n" + 
				"com.google.common.collect.ImmutableList.Builder<String> _builder = com.google.common.collect.ImmutableList.builder();\n" + 
				"_builder.add(\"foo\");\n" + 
				"_xlistliteral = _builder.build();\n" + 
				"return _xlistliteral;",
				"#['foo']");
	}

	@Test public void testListLiteralAsArray() throws Exception {
		assertCompilesTo(
				"final String[] x = { \"foo\" };",
				"{val String[] x = #['foo']}");
	}

	@Test public void testExceptionOnClosure() throws Exception {
		assertCompilesTo(
				"final java.beans.VetoableChangeListener _function = new java.beans.VetoableChangeListener() {\n" + 
				"    public void vetoableChange(final java.beans.PropertyChangeEvent it) throws java.beans.PropertyVetoException {\n" + 
				"    }\n" + 
				"  };\n" + 
				"final java.beans.VetoableChangeListener x = _function;",
				"{val java.beans.VetoableChangeListener x = []}");
	}
}
