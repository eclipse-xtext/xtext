/**
 * Copyright (c) 2012, 2020 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.xbase.tests.compiler;

import org.eclipse.xtext.util.JavaVersion;
import org.eclipse.xtext.xbase.compiler.GeneratorConfig;
import org.eclipse.xtext.xbase.compiler.output.FakeTreeAppendable;
import org.eclipse.xtext.xbase.lib.ObjectExtensions;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.Test;

public class Compiler2Tests extends AbstractOutputComparingCompilerTests {

	@Override
	protected FakeTreeAppendable createAppendable() {
		return new FakeTreeAppendable();
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=424279
	 */
	@Test
	public void testBug_424279() throws Exception {
		compilesTo("""
				{
					val treeIt = <CharSequence>newArrayList.iterator;
					while (treeIt.hasNext) {
						val o = treeIt.next;
						if (o instanceof java.util.List<?>) {
							return o;
						} else {
							return o;
						}
					}
					return null;
				}
				""",
				"""
						final java.util.Iterator<CharSequence> treeIt = org.eclipse.xtext.xbase.lib.CollectionLiterals.<CharSequence>newArrayList().iterator();
						while (treeIt.hasNext()) {
						  {
						    final CharSequence o = treeIt.next();
						    if ((o instanceof java.util.List<?>)) {
						      return o;
						    } else {
						      return o;
						    }
						  }
						}
						return null;
						""");
	}

	@Test
	public void testAbstractIterator() throws Exception {
		compilesTo("""
				{
					var com.google.common.collect.AbstractIterator<String> iter = [| return self.endOfData ]
					return iter
				}
				""",
				"""
						final com.google.common.collect.AbstractIterator<String> _function = new com.google.common.collect.AbstractIterator<String>() {
						  @Override
						  protected String computeNext() {
						    return this.endOfData();
						  }
						};
						com.google.common.collect.AbstractIterator<String> iter = _function;
						return iter;
						""");
	}

	@Test
	public void testVariableDeclaration() throws Exception {
		compilesTo("""
				{
					val x = 42
				}
				""", "final int x = 42;");
	}

	@Test
	public void testVariableDeclaration1() throws Exception {
		compilesTo("""
				{
					val a = 'foo'
					val x = a
				}
				""", """
				final String a = "foo";
				final String x = a;
				""");
	}

	@Test
	public void testVariableDeclaration2() throws Exception {
		compilesTo("""
				{
					val x = 'foo'.toUpperCase
				}
				""", """
				final String x = "foo".toUpperCase();
				""");
	}

	@Test
	public void testBug367144() throws Exception {
		compilesTo("foo::bar::SubOfClassWithStatics::MY_STATIC_FIELD", "return foo.bar.SubOfClassWithStatics.MY_STATIC_FIELD;");
	}

	@Test
	public void testBug367144_1() throws Exception {
		compilesTo("foo::bar::SubOfClassWithStatics::MY_STATIC_METHOD", """
				String _MY_STATIC_METHOD = foo.bar.SubOfClassWithStatics.MY_STATIC_METHOD();
				return _MY_STATIC_METHOD;
				""");
	}

	@Test
	public void testBug371321_1() throws Exception {
		compilesTo("""
				try {
					'foo'.length
				} catch (RuntimeException assert) {
					assert.printStackTrace
				}
				""", """
				int _xtrycatchfinallyexpression = (int) 0;
				try {
				  _xtrycatchfinallyexpression = "foo".length();
				} catch (final Throwable _t) {
				  if (_t instanceof RuntimeException) {
				    final RuntimeException assert_ = (RuntimeException)_t;
				    assert_.printStackTrace();
				  } else {
				    throw org.eclipse.xtext.xbase.lib.Exceptions.sneakyThrow(_t);
				  }
				}
				return Integer.valueOf(_xtrycatchfinallyexpression);
				""");
	}

	@Test
	public void testBug420984_EmptyCatch() throws Exception {
		compilesTo("""
				try {
					println("")
				} catch (RuntimeException e) {
				}
				""", """
				String _xtrycatchfinallyexpression = null;
				try {
				  _xtrycatchfinallyexpression = org.eclipse.xtext.xbase.lib.InputOutput.<String>println("");
				} catch (final Throwable _t) {
				  if (_t instanceof RuntimeException) {
				    _xtrycatchfinallyexpression = null;
				  } else {
				    throw org.eclipse.xtext.xbase.lib.Exceptions.sneakyThrow(_t);
				  }
				}
				return _xtrycatchfinallyexpression;
				""");
	}

	@Test
	public void testBug420984_EmptyCatchWithoutReturnType() throws Exception {
		compilesToStatement("""
				try {
				  println("")
				} catch (RuntimeException e) {
				}
				""", """
				try {
				  org.eclipse.xtext.xbase.lib.InputOutput.<String>println("");
				} catch (final Throwable _t) {
				  if (_t instanceof RuntimeException) {
				  } else {
				    throw org.eclipse.xtext.xbase.lib.Exceptions.sneakyThrow(_t);
				  }
				}""");
	}

	@Test
	public void testBug420984_CatchWithoutReference() throws Exception {
		compilesTo("""
				try {
					println("")
				} catch (RuntimeException e) {
					println("")
				}
				""", """
				String _xtrycatchfinallyexpression = null;
				try {
				  _xtrycatchfinallyexpression = org.eclipse.xtext.xbase.lib.InputOutput.<String>println("");
				} catch (final Throwable _t) {
				  if (_t instanceof RuntimeException) {
				    _xtrycatchfinallyexpression = org.eclipse.xtext.xbase.lib.InputOutput.<String>println("");
				  } else {
				    throw org.eclipse.xtext.xbase.lib.Exceptions.sneakyThrow(_t);
				  }
				}
				return _xtrycatchfinallyexpression;
				""");
	}

	@Test
	public void testBug420984_CatchWithReference() throws Exception {
		compilesTo("""
				try {
					println("")
				} catch (RuntimeException e) {
					println(e)
				}
				""", """
				java.io.Serializable _xtrycatchfinallyexpression = null;
				try {
				  _xtrycatchfinallyexpression = org.eclipse.xtext.xbase.lib.InputOutput.<String>println("");
				} catch (final Throwable _t) {
				  if (_t instanceof RuntimeException) {
				    final RuntimeException e = (RuntimeException)_t;
				    _xtrycatchfinallyexpression = org.eclipse.xtext.xbase.lib.InputOutput.<RuntimeException>println(e);
				  } else {
				    throw org.eclipse.xtext.xbase.lib.Exceptions.sneakyThrow(_t);
				  }
				}
				return _xtrycatchfinallyexpression;
				""");
	}

	@Test
	public void testBug371321_2() throws Exception {
		compilesTo("""
				for (assert : 'foo'.toCharArray) {
					println(assert)
				}
				""", """
				char[] _charArray = "foo".toCharArray();
				for (final char assert_ : _charArray) {
				  org.eclipse.xtext.xbase.lib.InputOutput.<Character>println(Character.valueOf(assert_));
				}
				""");
	}

	@Test
	public void testNewThread() throws Exception {
		compilesTo("new Thread [| ]\n", """
				final Runnable _function = () -> {
				};
				Thread _thread = new Thread(_function);
				return _thread;""");
	}

	@Test
	public void testForLoop_01() throws Exception {
		compilesTo("""
				for(int i: 1..2) { i.toString }
				""", """
				org.eclipse.xtext.xbase.lib.IntegerRange _upTo = new org.eclipse.xtext.xbase.lib.IntegerRange(1, 2);
				for (final int i : _upTo) {
				  Integer.valueOf(i).toString();
				}
				""");
	}

	@Test
	public void testForLoop_02() throws Exception {
		compilesTo("""
				for(Integer i: 1..2) { i.toString }
				""", """
				org.eclipse.xtext.xbase.lib.IntegerRange _upTo = new org.eclipse.xtext.xbase.lib.IntegerRange(1, 2);
				for (final Integer i : _upTo) {
				  i.toString();
				}
				""");
	}

	@Test
	public void testForLoop_03() throws Exception {
		compilesTo("""
				for(i: 1..2) { i.toString }
				""", """
				org.eclipse.xtext.xbase.lib.IntegerRange _upTo = new org.eclipse.xtext.xbase.lib.IntegerRange(1, 2);
				for (final Integer i : _upTo) {
				  i.toString();
				}
				""");
	}

	@Test
	public void testForLoop_04() throws Exception {
		compilesTo("""
				for(i: null as int[]) { i.toString }
				""", """
				for (final int i : ((int[]) null)) {
				  Integer.valueOf(i).toString();
				}
				""");
	}

	@Test
	public void testForLoop_05() throws Exception {
		compilesTo("""
				for(i: null as Integer[]) { i.toString }
				""", """
				for (final Integer i : ((Integer[]) null)) {
				  i.toString();
				}
				""");
	}

	@Test
	public void testForLoop_06() throws Exception {
		compilesTo("""
				for(Integer i: null as int[]) { i.toString }
				""", """
				for (final Integer i : ((int[]) null)) {
				  i.toString();
				}
				""");
	}

	@Test
	public void testForLoop_07() throws Exception {
		compilesTo("""
				for(int i: null as Integer[]) { i.toString }
				""", """
				for (final int i : ((Integer[]) null)) {
				  Integer.valueOf(i).toString();
				}
				""");
	}

	@Test
	public void testForLoop_08() throws Exception {
		compilesTo("""
				for(Integer i: null as int[]) { i.toString }
				""", """
				for (final Integer i : ((int[]) null)) {
				  i.toString();
				}
				""");
	}

	@Test
	public void testForLoop_09() throws Exception {
		compilesTo("""
				for(int i: null as Integer[]) { i.toString }
				""", """
				for (final int i : ((Integer[]) null)) {
				  Integer.valueOf(i).toString();
				}
				""");
	}

	@Test
	public void testForLoop_10() throws Exception {
		compilesTo("""
				for(int i: newArrayList) { i.toString }
				""", """
				java.util.ArrayList<Integer> _newArrayList = org.eclipse.xtext.xbase.lib.CollectionLiterals.<Integer>newArrayList();
				for (final int i : _newArrayList) {
				  Integer.valueOf(i).toString();
				}
				""");
	}

	@Test
	public void testForLoop_11() throws Exception {
		compilesTo("""
				for(Integer i: newArrayList) { i.toString }
				""", """
				java.util.ArrayList<Integer> _newArrayList = org.eclipse.xtext.xbase.lib.CollectionLiterals.<Integer>newArrayList();
				for (final Integer i : _newArrayList) {
				  i.toString();
				}
				""");
	}

	@Test
	public void testForLoop_12() throws Exception {
		compilesTo("""
				for(double d: <Integer>newArrayList) { d.toString }
				""", """
				java.util.ArrayList<Integer> _newArrayList = org.eclipse.xtext.xbase.lib.CollectionLiterals.<Integer>newArrayList();
				for (final double d : _newArrayList) {
				  Double.valueOf(d).toString();
				}
				""");
	}

	@Test
	public void testForLoop_13() throws Exception {
		compilesTo("""
				for(double d: null as int[]) { d.toString }
				""", """
				for (final double d : ((int[]) null)) {
				  Double.valueOf(d).toString();
				}
				""");
	}

	@Test
	public void testForLoop_14() throws Exception {
		compilesTo("""
				for(double d: null as Integer[]) { d.toString }
				""", """
				for (final double d : ((Integer[]) null)) {
				  Double.valueOf(d).toString();
				}
				""");
	}

	@Test
	public void testNullSafe_01() throws Exception {
		compilesTo("""
				('foo'.toUpperCase as CharSequence)?.subSequence(0,2)
				""", """
				String _upperCase = "foo".toUpperCase();
				CharSequence _subSequence = null;
				if (((CharSequence) _upperCase)!=null) {
				  _subSequence=((CharSequence) _upperCase).subSequence(0, 2);
				}
				return _subSequence;
				""");
	}

	@Test
	public void testSwitch() throws Exception {
		compilesTo("""
				{
					val it = String
					switch name {
						CharSequence : name
						default : 'noname'
					}
				}
				""", """
				String _xblockexpression = null;
				{
				  final Class<String> it = String.class;
				  String _switchResult = null;
				  String _name = it.getName();
				  boolean _matched = false;
				  if (_name instanceof CharSequence) {
				    _matched=true;
				    _switchResult = it.getName();
				  }
				  if (!_matched) {
				    _switchResult = "noname";
				  }
				  _xblockexpression = _switchResult;
				}
				return _xblockexpression;
				""");
	}

	@Test
	public void testSwitchScopes_Bug470586_01() throws Exception {
		compilesTo("""
				{
					val list = #["1", "2"]
					list.forEach[
						val res = switch it {
							CharSequence : it
						}
					]
					list.forEach[
						println(it)
					]
				}
				""",
				"""
						final java.util.List<String> list = java.util.Collections.<String>unmodifiableList(org.eclipse.xtext.xbase.lib.CollectionLiterals.<String>newArrayList("1", "2"));
						final java.util.function.Consumer<String> _function = (String it) -> {
						  String _switchResult = null;
						  boolean _matched = false;
						  if (it instanceof CharSequence) {
						    _matched=true;
						    _switchResult = it;
						  }
						  final String res = _switchResult;
						};
						list.forEach(_function);
						final java.util.function.Consumer<String> _function_1 = (String it) -> {
						  org.eclipse.xtext.xbase.lib.InputOutput.<String>println(it);
						};
						list.forEach(_function_1);
						""");
	}

	@Test
	public void testSwitchScopes_Bug470586_02() throws Exception {
		compilesTo("""
				{
					val list = #["1", "2"]
					list.forEach[
						val res = switch it {
							CharSequence : it
							default : ''
						}
					]
					list.forEach[
						println(it)
					]
				}
				""",
				"""
						final java.util.List<String> list = java.util.Collections.<String>unmodifiableList(org.eclipse.xtext.xbase.lib.CollectionLiterals.<String>newArrayList("1", "2"));
						final java.util.function.Consumer<String> _function = (String it) -> {
						  String _switchResult = null;
						  boolean _matched = false;
						  if (it instanceof CharSequence) {
						    _matched=true;
						    _switchResult = it;
						  }
						  if (!_matched) {
						    _switchResult = "";
						  }
						  final String res = _switchResult;
						};
						list.forEach(_function);
						final java.util.function.Consumer<String> _function_1 = (String it) -> {
						  org.eclipse.xtext.xbase.lib.InputOutput.<String>println(it);
						};
						list.forEach(_function_1);""");
	}

	@Test
	public void testSwitchScopes_Bug470586_03() throws Exception {
		compilesTo("""
				{
					val list = #["1", "2"]
					list.forEach[
						val res = switch it {
							String, CharSequence : it
							default : ''
						}
					]
					list.forEach[
						println(it)
					]
				}
				""",
				"""
						final java.util.List<String> list = java.util.Collections.<String>unmodifiableList(org.eclipse.xtext.xbase.lib.CollectionLiterals.<String>newArrayList("1", "2"));
						final java.util.function.Consumer<String> _function = (String it) -> {
						  String _switchResult = null;
						  boolean _matched = false;
						  if (it instanceof String) {
						    _matched=true;
						  }
						  if (!_matched) {
						    if (it instanceof CharSequence) {
						      _matched=true;
						    }
						  }
						  if (_matched) {
						    _switchResult = it;
						  }
						  if (!_matched) {
						    _switchResult = "";
						  }
						  final String res = _switchResult;
						};
						list.forEach(_function);
						final java.util.function.Consumer<String> _function_1 = (String it) -> {
						  org.eclipse.xtext.xbase.lib.InputOutput.<String>println(it);
						};
						list.forEach(_function_1);""");
	}

	@Test
	public void testFallThroughSwitch() throws Exception {
		compilesTo("""
				{
					switch x : 1 {
						case 1,
						case 2:
							'lalala'
					}
				}
				""", """
				String _switchResult = null;
				final int x = 1;
				switch (x) {
				  case 1:
				  case 2:
				    _switchResult = "lalala";
				    break;
				}
				return _switchResult;
				""");
	}

	@Test
	public void testFallThroughSwitch_2() throws Exception {
		compilesTo("""
				{
					switch x : 'lalala' {
						case 'a',
						case 'b',
						case 'c':
							'lalala'
					}
				}
				""", """
				String _switchResult = null;
				final String x = "lalala";
				if (x != null) {
				  switch (x) {
				    case "a":
				    case "b":
				    case "c":
				      _switchResult = "lalala";
				      break;
				  }
				}
				return _switchResult;""");
	}

	@Test
	public void testFallThroughSwitch_3() throws Exception {
		compilesTo("""
				{
					switch x : 1 {
						case 1,
						case 2,
						default:
							'lalala'
					}
				}
				""", """
				String _switchResult = null;
				final int x = 1;
				switch (x) {
				  case 1:
				  case 2:
				  default:
				    _switchResult = "lalala";
				    break;
				}
				return _switchResult;
				""");
	}

	@Test
	public void testFallThroughSwitch_4() throws Exception {
		compilesTo("""
				{
					switch x : 'lalala' {
						case 'a',
						case 'b',
						default:
							'lalala'
					}
				}
				""", """
				String _switchResult = null;
				final String x = "lalala";
				if (x != null) {
				  switch (x) {
				    case "a":
				    case "b":
				    default:
				      _switchResult = "lalala";
				      break;
				  }
				} else {
				  _switchResult = "lalala";
				}
				return _switchResult;""");
	}

	@Test
	public void testFallThroughSwitch_5() throws Exception {
		compilesTo("""
				{
					switch x : 1 {
						case 1:
							'blabla'
						case 2,
						default:
							'lalala'
					}
				}
				""", """
				String _switchResult = null;
				final int x = 1;
				switch (x) {
				  case 1:
				    _switchResult = "blabla";
				    break;
				  case 2:
				  default:
				    _switchResult = "lalala";
				    break;
				}
				return _switchResult;
				""");
	}

	@Test
	public void testFallThroughSwitch_6() throws Exception {
		compilesTo("""
				{
					switch x : new Object {
						String case 'a':
							'blabla'
						Integer case 1,
						case 2,
						default:
							'lalala'
					}
				}
				""", """
				String _switchResult = null;
				Object _object = new Object();
				final Object x = _object;
				boolean _matched = false;
				if (x instanceof String) {
				  if (java.util.Objects.equals(x, "a")) {
				    _matched=true;
				    _switchResult = "blabla";
				  }
				}
				if (!_matched) {
				  if (x instanceof Integer) {
				    if (java.util.Objects.equals(x, 1)) {
				      _matched=true;
				    }
				  }
				  if (!_matched) {
				    if (java.util.Objects.equals(x, 2)) {
				      _matched=true;
				    }
				  }
				  if (_matched) {
				    _switchResult = "lalala";
				  }
				}
				if (!_matched) {
				  _switchResult = "lalala";
				}
				return _switchResult;
				""");
	}

	@Test
	public void testFallThroughSwitch_7() throws Exception {
		compilesTo("""
				{
					switch x : 'lalala' as Object {
						String, Integer case 1: 0
						Integer, default: 1
					}
				}
				""", """
				int _switchResult = (int) 0;
				final Object x = ((Object) "lalala");
				boolean _matched = false;
				if (x instanceof String) {
				  _matched=true;
				}
				if (!_matched) {
				  if (x instanceof Integer) {
				    if (java.util.Objects.equals(x, 1)) {
				      _matched=true;
				    }
				  }
				}
				if (_matched) {
				  _switchResult = 0;
				}
				if (!_matched) {
				  if (x instanceof Integer) {
				    _matched=true;
				    _switchResult = 1;
				  }
				}
				if (!_matched) {
				  _switchResult = 1;
				}
				return _switchResult;
				""");
	}

	@Test
	public void testFallThroughSwitch_8() throws Exception {
		compilesTo("""
				{
					switch x : 1 as Object {
						Integer,
						Double: x
					}
				}
				""", """
				Number _switchResult = null;
				final Object x = ((Object) Integer.valueOf(1));
				boolean _matched = false;
				if (x instanceof Integer) {
				  _matched=true;
				}
				if (!_matched) {
				  if (x instanceof Double) {
				    _matched=true;
				  }
				}
				if (_matched) {
				  _switchResult = ((Number)x);
				}
				return ((Number)_switchResult);
				""");
	}

	@Test
	public void testEmptySwitch() throws Exception {
		compilesTo("""
				{
					switch 1 {
					}
				}
				""", """
				Object _switchResult = null;
				final int _switchValue = 1;
				switch (_switchValue) {
				}
				return _switchResult;
				""");
	}

	@Test
	public void testEmptySwitch_2() throws Exception {
		compilesTo("""
				{
					switch 1 {
						default: {
						}
					}
				}
				""", """
				Object _switchResult = null;
				final int _switchValue = 1;
				switch (_switchValue) {
				  default:
				    _switchResult = null;
				    break;
				}
				return _switchResult;
				""");
	}

	@Test
	public void testEmptySwitch_3() throws Exception {
		compilesTo("""
				{
					switch Object {
					}
				}
				""", """
				Object _switchResult = null;
				final Class<Object> Object = Object.class;
				boolean _matched = false;
				return _switchResult;
				""");
	}

	@Test
	public void testEmptySwitch_4() throws Exception {
		compilesTo("""
				{
					switch Object {
						default: {
						}
					}
				}
				""", """
				Object _switchResult = null;
				final Class<Object> Object = Object.class;
				boolean _matched = false;
				if (!_matched) {
				  _switchResult = null;
				}
				return _switchResult;
				""");
	}

	@Test
	public void testEmptySwitch_5() throws Exception {
		compilesTo("""
				{
					switch Thread.State.NEW {
					}
				}
				""", """
				Object _switchResult = null;
				final Thread.State _switchValue = Thread.State.NEW;
				if (_switchValue != null) {
				  switch (_switchValue) {
				    default:
				      break;
				  }
				}
				return _switchResult;
				""");
	}

	@Test
	public void testEmptySwitch_6() throws Exception {
		compilesTo("""
				{
					switch Thread.State.NEW {
						default: {
						}
					}
				}
				""", """
				Object _switchResult = null;
				final Thread.State _switchValue = Thread.State.NEW;
				if (_switchValue != null) {
				  switch (_switchValue) {
				    default:
				      _switchResult = null;
				      break;
				  }
				} else {
				  _switchResult = null;
				}
				return _switchResult;
				""");
	}

	@Test
	public void testEmptySwitch_7() throws Exception {
		compilesTo("""
				{
					switch Thread.State x : Thread.State.NEW {
						default: {
						}
					}
				}
				""", """
				Object _switchResult = null;
				final Thread.State x = Thread.State.NEW;
				if (x != null) {
				  switch (x) {
				    default:
				      _switchResult = null;
				      break;
				  }
				} else {
				  _switchResult = null;
				}
				return _switchResult;
				""");
	}

	@Test
	public void testEmptySwitch_8() throws Exception {
		compilesTo("""
				{
					switch (Thread.State x : Thread.State.NEW) {
						default: {
						}
					}
				}
				""", """
				Object _switchResult = null;
				final Thread.State x = Thread.State.NEW;
				if (x != null) {
				  switch (x) {
				    default:
				      _switchResult = null;
				      break;
				  }
				} else {
				  _switchResult = null;
				}
				return _switchResult;
				""");
	}

	@Test
	public void testEmptySwitch_9() throws Exception {
		compilesTo("""
				{
					switch Object x : Thread.State.NEW {
						default: {
						}
					}
				}
				""", """
				Object _switchResult = null;
				final Object x = Thread.State.NEW;
				boolean _matched = false;
				if (!_matched) {
				  _switchResult = null;
				}
				return _switchResult;
				""");
	}

	@Test
	public void testEmptySwitch_10() throws Exception {
		compilesTo("""
				{
					switch (Object x : Thread.State.NEW) {
						default: {
						}
					}
				}
				""", """
				Object _switchResult = null;
				final Object x = Thread.State.NEW;
				boolean _matched = false;
				if (!_matched) {
				  _switchResult = null;
				}
				return _switchResult;
				""");
	}

	@Test
	public void testEmptySwitch_11() throws Exception {
		compilesTo("""
				{
					val String x = null
					switch (x) {
					}
				}
				""", """
				Object _xblockexpression = null;
				{
				  final String x = null;
				  Object _switchResult = null;
				  if (x != null) {
				    switch (x) {
				    }
				  }
				  _xblockexpression = _switchResult;
				}
				return _xblockexpression;
				""");
	}

	@Test
	public void testBasicForLoop_0() throws Exception {
		compilesTo("""
				{
					for (var i = 0; i < 10; i = i + 1) {
					}
				}
				""", """
				for (int i = 0; (i < 10); i = (i + 1)) {
				}
				""");
	}

	@Test
	public void testBasicForLoop_toJavaWhile_0() throws Exception {
		compilesTo("""
				{
					for (var i = if (true) { 1 }; i < 10; i = i + 1) {
					}
				}
				""", """
				int _xifexpression = (int) 0;
				if (true) {
				  _xifexpression = 1;
				}
				int i = _xifexpression;
				boolean _while = (i < 10);
				while (_while) {
				  i = (i + 1);
				  _while = (i < 10);
				}
				""");
	}

	@Test
	public void testBasicForLoop_1() throws Exception {
		compilesTo("""
				{
					for (;;) {
					}
				}
				""", """
				for (;;) {
				}
				""");
	}

	@Test
	public void testBasicForLoop_toJavaWhile_1() throws Exception {
		compilesTo("""
				{
					for (for (;;) {};;) {
					}
				}
				""", """
				for (;;) {
				}
				boolean _while = true;
				while (_while) {
				  _while = true;
				}
				""");
	}

	@Test
	public void testBasicForLoop_2() throws Exception {
		compilesTo("""
				{
					for (val i = 0; i < 10;) {
						if (i == 2) {
							return true;
						}
					}
				}
				""", """
				for (final int i = 0; (i < 10);) {
				  if ((i == 2)) {
				    return Boolean.valueOf(true);
				  }
				}
				""");
	}

	@Test
	public void testBasicForLoop_toJavaWhile_2() throws Exception {
		compilesTo("""
				{
					for (val i = #[if(true) {1} else {2}]; i.head < 10;) {
						if (i.head == 2) {
							return true;
						}
					}
				}
				""",
				"""
						int _xifexpression = (int) 0;
						if (true) {
						  _xifexpression = 1;
						} else {
						  _xifexpression = 2;
						}
						final java.util.List<Integer> i = java.util.Collections.<Integer>unmodifiableList(org.eclipse.xtext.xbase.lib.CollectionLiterals.<Integer>newArrayList(Integer.valueOf(_xifexpression)));
						Integer _head = org.eclipse.xtext.xbase.lib.IterableExtensions.<Integer>head(i);
						boolean _lessThan = ((_head).intValue() < 10);
						boolean _while = _lessThan;
						while (_while) {
						  Integer _head_1 = org.eclipse.xtext.xbase.lib.IterableExtensions.<Integer>head(i);
						  boolean _equals = ((_head_1).intValue() == 2);
						  if (_equals) {
						    return Boolean.valueOf(true);
						  }
						  Integer _head_2 = org.eclipse.xtext.xbase.lib.IterableExtensions.<Integer>head(i);
						  boolean _lessThan_1 = ((_head_2).intValue() < 10);
						  _while = _lessThan_1;
						}
						return null;
						""");
	}

	@Test
	public void testBasicForLoop_3() throws Exception {
		compilesTo("""
				{
					for (val i = 0; i < 10;) {
						return true
					}
				}
				""", """
				for (final int i = 0; (i < 10);) {
				  return Boolean.valueOf(true);
				}
				""");
	}

	@Test
	public void testBasicForLoop_4() throws Exception {
		compilesTo("""
				{
					for (var i = 1; i < 10;) {}
					for (val i = 1;;) {}
				}
				""", """
				for (int i = 1; (i < 10);) {
				}
				for (final int i = 1;;) {
				}
				""");
	}

	@Test
	public void testBasicForLoop_5() throws Exception {
		compilesTo("""
				{
					{
						for (var i = 1;i < 10;) {}
					}
					{
						for (val i = 1;;) {}
					}
				}
				""", """
				for (int i = 1; (i < 10);) {
				}
				for (final int i = 1;;) {
				}
				""");
	}

	@Test
	public void testBasicForLoop_6() throws Exception {
		compilesTo("""
				{
					if (true) {
						for (;;) {}
					}
				}
				""", """
				if (true) {
				  for (;;) {
				  }
				}
				""");
	}

	@Test
	public void testBasicForLoop_7() throws Exception {
		compilesTo("""
				{
					try {
						for (;;) {}
					} finally {
					}
				}
				""", """
				try {
				  for (;;) {
				  }
				} finally {
				}
				""");
	}

	@Test
	public void testBasicForLoop_8() throws Exception {
		compilesTo("""
				{
					[| for (;;) {} ]
				}
				""", """
				final org.eclipse.xtext.xbase.lib.Procedures.Procedure0 _function = () -> {
				  for (;;) {
				  }
				};
				return _function;""");
	}

	@Test
	public void testBasicForLoop_9() throws Exception {
		compilesTo("""
				{
					{
						{
							{
								for (;;) {}
							}
						}
					}
				}
				""", """
				for (;;) {
				}
				""");
	}

	@Test
	public void testBasicForLoop_10() throws Exception {
		compilesTo("""
				{
					val i = 0
					for ([| i ].apply, [| i ].apply; i < 10;) {
					}
				}
				""", """
				final int i = 0;
				for (((org.eclipse.xtext.xbase.lib.Functions.Function0<Integer>) () -> {
				  return Integer.valueOf(i);
				}).apply(), ((org.eclipse.xtext.xbase.lib.Functions.Function0<Integer>) () -> {
				  return Integer.valueOf(i);
				}).apply(); (i < 10);) {
				}""");
	}

	@Test
	public void testBasicForLoop_11() throws Exception {
		compilesTo("""
				{
					for (val i = 0, val j = 0; i < 10;) {
					}
				}
				""", """
				final int i = 0;
				final int j = 0;
				boolean _while = (i < 10);
				while (_while) {
				  _while = (i < 10);
				}
				""");
	}

	@Test
	public void testBasicForLoop_12() throws Exception {
		compilesTo("""
				{
					for (var i = 0; i != 10; i += 1) {
					}
				}
				""", """
				int i = 0;
				boolean _while = (i != 10);
				while (_while) {
				  int _i = i;
				  i = (_i + 1);
				  _while = (i != 10);
				}
				""");
	}

	@Test
	public void testWhileLoopWithConstantCondition_01() throws Exception {
		compilesTo("""
				while (newArrayList('').empty || 2 == (1 << 1)) {
					return ''
				}
				""", """
				while ((org.eclipse.xtext.xbase.lib.CollectionLiterals.<String>newArrayList("").isEmpty() || (2 == (1 << 1)))) {
				  return "";
				}
				return null;
				""");
	}

	@Test
	public void testWhileLoopWithConstantCondition_02() throws Exception {
		compilesTo("""
				while (2 == (1 << 1)) {
					return ''
				}
				""", """
				while ((2 == (1 << 1))) {
				  return "";
				}
				""");
	}

	@Test
	public void testSynchronizedBlock_1() throws Exception {
		compilesTo("""
				{
					val x = new Integer(1)
					synchronized(x) {}
				}
				""", """
				Object _xblockexpression = null;
				{
				  final Integer x = new Integer(1);
				  Object _xsynchronizedexpression = null;
				  synchronized (x) {
				    _xsynchronizedexpression = null;
				  }
				  _xblockexpression = _xsynchronizedexpression;
				}
				return _xblockexpression;
				""");
	}

	@Test
	public void testSynchronizedBlock_2() throws Exception {
		compilesTo("""
				{
					val x = new Integer(1)
					val y = synchronized(x) 1
				}
				""", """
				final Integer x = new Integer(1);
				int _xsynchronizedexpression = (int) 0;
				synchronized (x) {
				  _xsynchronizedexpression = 1;
				}
				final int y = _xsynchronizedexpression;
				""");
	}

	@Test
	public void testSynchronizedBlock_3() throws Exception {
		compilesTo("""
				{
					val x = new Integer(1)
					val y = synchronized(x) {
						{
							{
								val i = 1
								val j = 2
								i + j
							}
						}
					}
				}
				""", """
				final Integer x = new Integer(1);
				int _xsynchronizedexpression = (int) 0;
				synchronized (x) {
				  int _xblockexpression = (int) 0;
				  {
				    final int i = 1;
				    final int j = 2;
				    _xblockexpression = (i + j);
				  }
				  _xsynchronizedexpression = _xblockexpression;
				}
				final int y = _xsynchronizedexpression;
				""");
	}

	@Test
	public void testBug410797_01() throws Exception {
		compilesTo("{ val boolean bug = #[true, false, true].reduce[a,b|a && b] }\n",
				"""
						final org.eclipse.xtext.xbase.lib.Functions.Function2<Boolean, Boolean, Boolean> _function = (Boolean a, Boolean b) -> {
						  return Boolean.valueOf(((a).booleanValue() && (b).booleanValue()));
						};
						final boolean bug = (boolean) org.eclipse.xtext.xbase.lib.IterableExtensions.<Boolean>reduce(java.util.Collections.<Boolean>unmodifiableList(org.eclipse.xtext.xbase.lib.CollectionLiterals.<Boolean>newArrayList(Boolean.valueOf(true), Boolean.valueOf(false), Boolean.valueOf(true))), _function);""");
	}

	@Test
	public void testBug434224_01() throws Exception {
		compilesTo("""
				{
					val bar = new Object
					if (bar instanceof Byte) {
						bar as char
					}
				}
				""", """
				char _xblockexpression = (char) 0;
				{
				  final Object bar = new Object();
				  char _xifexpression = (char) 0;
				  if ((bar instanceof Byte)) {
				    _xifexpression = ((char) ((Byte) bar).byteValue());
				  }
				  _xblockexpression = _xifexpression;
				}
				return _xblockexpression;
				""");
	}

	@Test
	public void testBug434224_02() throws Exception {
		compilesTo("""
				{
					switch bar : new Object {
						Byte: bar as char
					}
				}
				""", """
				char _switchResult = (char) 0;
				Object _object = new Object();
				final Object bar = _object;
				boolean _matched = false;
				if (bar instanceof Byte) {
				  _matched=true;
				  _switchResult = ((char) ((Byte) bar).byteValue());
				}
				return _switchResult;
				""");
	}

	@Test
	public void testBug433573_01() throws Exception {
		compilesTo("""
				{
					val Number element = null
					if(element instanceof Double) {
						val Iterable<? extends Number> i = #[element]
					}
				}
				""",
				"""
						final Number element = null;
						if ((element instanceof Double)) {
						  final Iterable<? extends Number> i = java.util.Collections.<Number>unmodifiableList(org.eclipse.xtext.xbase.lib.CollectionLiterals.<Number>newArrayList(element));
						}
						""");
	}

	@Test
	public void testBug433573_02() throws Exception {
		compilesTo("""
				{
					val Number element = null
					switch element {
						Double: {
							val Iterable<? extends Number> i = #[element]
						}
					}
				}
				""",
				"""
						final Number element = null;
						boolean _matched = false;
						if (element instanceof Double) {
						  _matched=true;
						  final Iterable<? extends Number> i = java.util.Collections.<Number>unmodifiableList(org.eclipse.xtext.xbase.lib.CollectionLiterals.<Number>newArrayList(element));
						}
						""");
	}

	@Test
	public void testBug433573_03() throws Exception {
		compilesTo("""
				{
					val Number element = null
					if(element instanceof Double) {
						val Iterable<? extends Number> i = #{element}
					}
				}
				""",
				"""
						final Number element = null;
						if ((element instanceof Double)) {
						  final Iterable<? extends Number> i = java.util.Collections.<Number>unmodifiableSet(org.eclipse.xtext.xbase.lib.CollectionLiterals.<Number>newHashSet(element));
						}
						""");
	}

	@Test
	public void testBug433573_04() throws Exception {
		compilesTo("""
				{
					val Number element = null
					switch element {
						Double: {
							val Iterable<? extends Number> i = #{element}
						}
					}
				}
				""",
				"""
						final Number element = null;
						boolean _matched = false;
						if (element instanceof Double) {
						  _matched=true;
						  final Iterable<? extends Number> i = java.util.Collections.<Number>unmodifiableSet(org.eclipse.xtext.xbase.lib.CollectionLiterals.<Number>newHashSet(element));
						}
						""");
	}

	@Test
	public void testBug433573_05() throws Exception {
		compilesTo("""
				{
					val Object element = null
					if(element instanceof Double) {
						val Iterable<? extends Number> i = #[element]
					}
				}
				""",
				"""
						final Object element = null;
						if ((element instanceof Double)) {
						  final Iterable<? extends Number> i = java.util.Collections.<Number>unmodifiableList(org.eclipse.xtext.xbase.lib.CollectionLiterals.<Number>newArrayList(((Number)element)));
						}
						""");
	}

	@Test
	public void testBug433573_06() throws Exception {
		compilesTo("""
				{
					val Object element = null
					switch element {
						Double: {
							val Iterable<? extends Number> i = #[element]
						}
					}
				}
				""",
				"""
						final Object element = null;
						boolean _matched = false;
						if (element instanceof Double) {
						  _matched=true;
						  final Iterable<? extends Number> i = java.util.Collections.<Number>unmodifiableList(org.eclipse.xtext.xbase.lib.CollectionLiterals.<Number>newArrayList(((Number)element)));
						}
						""");
	}

	@Test
	public void testBug433573_07() throws Exception {
		compilesTo("""
				{
					val Object element = null
					if(element instanceof Double) {
						val Iterable<? extends Number> i = #{element}
					}
				}
				""",
				"""
						final Object element = null;
						if ((element instanceof Double)) {
						  final Iterable<? extends Number> i = java.util.Collections.<Number>unmodifiableSet(org.eclipse.xtext.xbase.lib.CollectionLiterals.<Number>newHashSet(((Number)element)));
						}
						""");
	}

	@Test
	public void testBug433573_08() throws Exception {
		compilesTo("""
				{
					val Object element = null
					switch element {
						Double: {
							val Iterable<? extends Number> i = #{element}
						}
					}
				}
				""",
				"""
						final Object element = null;
						boolean _matched = false;
						if (element instanceof Double) {
						  _matched=true;
						  final Iterable<? extends Number> i = java.util.Collections.<Number>unmodifiableSet(org.eclipse.xtext.xbase.lib.CollectionLiterals.<Number>newHashSet(((Number)element)));
						}
						""");
	}

	@Test
	public void testBug466974_01() throws Exception {
		compilesTo("""
				{
					var i = 0
					Math.max( i = i + 1, if (i == 1) { 1 } else { 2 })
				}
				""", """
				int _xblockexpression = (int) 0;
				{
				  int i = 0;
				  int _i = i = (i + 1);
				  int _xifexpression = (int) 0;
				  if ((i == 1)) {
				    _xifexpression = 1;
				  } else {
				    _xifexpression = 2;
				  }
				  _xblockexpression = Math.max(_i, _xifexpression);
				}
				return _xblockexpression;
				""");
	}

	@Test
	public void testBug466974_02() throws Exception {
		compilesTo("""
				{
					var i = 0
					Math.max(if (i == 1) { 1 } else { 2 }, i = i + 1)
				}
				""", """
				int _xblockexpression = (int) 0;
				{
				  int i = 0;
				  int _xifexpression = (int) 0;
				  if ((i == 1)) {
				    _xifexpression = 1;
				  } else {
				    _xifexpression = 2;
				  }
				  _xblockexpression = Math.max(_xifexpression, i = (i + 1));
				}
				return _xblockexpression;
				""");
	}

	@Test
	public void testBug466974_03() throws Exception {
		compilesTo("""
				{
					var i = 0
					Math.max({ i = i + 1 }, if (i == 1) { 1 } else { 2 })
				}
				""", """
				int _xblockexpression = (int) 0;
				{
				  int i = 0;
				  int _i = i = (i + 1);
				  int _xifexpression = (int) 0;
				  if ((i == 1)) {
				    _xifexpression = 1;
				  } else {
				    _xifexpression = 2;
				  }
				  _xblockexpression = Math.max(_i, _xifexpression);
				}
				return _xblockexpression;
				""");
	}

	@Test
	public void testBug466974_04() throws Exception {
		compilesTo("""
				{
					var i = 0
					Math.max(if (i == 1) { 1 } else { 2 }, { i = i + 1 })
				}
				""", """
				int _xblockexpression = (int) 0;
				{
				  int i = 0;
				  int _xifexpression = (int) 0;
				  if ((i == 1)) {
				    _xifexpression = 1;
				  } else {
				    _xifexpression = 2;
				  }
				  _xblockexpression = Math.max(_xifexpression, i = (i + 1));
				}
				return _xblockexpression;
				""");
	}

	@Test
	public void testBug466974_05() throws Exception {
		compilesTo("""
				{
					var i = 0
					{i = i + 1}.compareTo(if (i == 1) { 1 } else { 2 })
				}
				""", """
				int _xblockexpression = (int) 0;
				{
				  int i = 0;
				  int _i = i = (i + 1);
				  int _xifexpression = (int) 0;
				  if ((i == 1)) {
				    _xifexpression = 1;
				  } else {
				    _xifexpression = 2;
				  }
				  _xblockexpression = Integer.valueOf(_i).compareTo(Integer.valueOf(_xifexpression));
				}
				return _xblockexpression;
				""");
	}

	@Test
	public void testBug466974_06() throws Exception {
		compilesTo("""
				{
					var i = 0
					{if (i == 1) { 1 } else { 2 }}.compareTo(i = i + 1)
				}
				""", """
				int _xblockexpression = (int) 0;
				{
				  int i = 0;
				  int _xifexpression = (int) 0;
				  if ((i == 1)) {
				    _xifexpression = 1;
				  } else {
				    _xifexpression = 2;
				  }
				  _xblockexpression = Integer.valueOf(_xifexpression).compareTo(Integer.valueOf(i = (i + 1)));
				}
				return _xblockexpression;
				""");
	}

	@Test
	public void testBug466974_07() throws Exception {
		compilesTo("""
				{
					var i = 0
					{{i = i + 1}}.compareTo(if (i == 1) { 1 } else { 2 })
				}
				""", """
				int _xblockexpression = (int) 0;
				{
				  int i = 0;
				  int _i = i = (i + 1);
				  int _xifexpression = (int) 0;
				  if ((i == 1)) {
				    _xifexpression = 1;
				  } else {
				    _xifexpression = 2;
				  }
				  _xblockexpression = Integer.valueOf(_i).compareTo(Integer.valueOf(_xifexpression));
				}
				return _xblockexpression;
				""");
	}

	@Test
	public void testBug466974_08() throws Exception {
		compilesTo("""
				{
					var i = 0
					{if (i == 1) { 1 } else { 2 }}.compareTo({i = i + 1})
				}
				""", """
				int _xblockexpression = (int) 0;
				{
				  int i = 0;
				  int _xifexpression = (int) 0;
				  if ((i == 1)) {
				    _xifexpression = 1;
				  } else {
				    _xifexpression = 2;
				  }
				  _xblockexpression = Integer.valueOf(_xifexpression).compareTo(Integer.valueOf(i = (i + 1)));
				}
				return _xblockexpression;
				""");
	}

	@Test
	public void testBug466974_09() throws Exception {
		compilesTo("""
				{
					var i = 0
					Math.max(i = i + 1, 2)
				}
				""", """
				int _xblockexpression = (int) 0;
				{
				  int i = 0;
				  _xblockexpression = Math.max(i = (i + 1), 2);
				}
				return _xblockexpression;
				""");
	}

	@Test
	public void testBug466974_10() throws Exception {
		compilesTo("""
				{
					var i = 0
					{i = i + 1}.compareTo(2)
				}
				""", """
				int _xblockexpression = (int) 0;
				{
				  int i = 0;
				  _xblockexpression = Integer.valueOf(i = (i + 1)).compareTo(Integer.valueOf(2));
				}
				return _xblockexpression;
				""");
	}

	@Test
	public void testBug489037_NestedAssignment_1() throws Exception {
		compilesTo("""
				{
					var i = 0
					var j = 0
					j=i=0;
				}
				""", """
				int _xblockexpression = (int) 0;
				{
				  int i = 0;
				  int j = 0;
				  _xblockexpression = j = (i = 0);
				}
				return _xblockexpression;
				""");
	}

	@Test
	public void testBug489037_NestedAssignment_2() throws Exception {
		compilesTo("""
				{
					var i = 0
					var n = 1
					n=i=(if (i==0) 1 else 2)
				}
				""", """
				int _xblockexpression = (int) 0;
				{
				  int i = 0;
				  int n = 1;
				  int _xifexpression = (int) 0;
				  if ((i == 0)) {
				    _xifexpression = 1;
				  } else {
				    _xifexpression = 2;
				  }
				  int _i = (i = _xifexpression);
				  _xblockexpression = n = _i;
				}
				return _xblockexpression;
				""");
	}

	@Test
	public void testBug489037_NestedAssignment_3() throws Exception {
		compilesTo("""
				{
					var i = 0
					var n = 1
					n=i=switch(i) {
						default: 2
					}
				}
				""", """
				int _xblockexpression = (int) 0;
				{
				  int i = 0;
				  int n = 1;
				  int _switchResult = (int) 0;
				  switch (i) {
				    default:
				      _switchResult = 2;
				      break;
				  }
				  int _i = (i = _switchResult);
				  _xblockexpression = n = _i;
				}
				return _xblockexpression;
				""");
	}

	@Test
	public void test406762_NestedReturn() throws Exception {
		compilesTo("""
				{
					return return 0
				}
				""", """
				return 0;
				return /* error - couldn't compile nested return */;
				""", false);
	}

	@Test
	public void test406762_NestedReturn2() throws Exception {
		compilesTo("""
				{
					return {
						return 0
					}
				}
				""", """
				return 0;
				return /* error - couldn't compile nested return */;
				""", false);
	}

	@Test
	public void test406762_NestedReturn3() throws Exception {
		compilesTo("""
				{
					return {
						if (true) return 0 else return 1
					}
				}
				""", """
				int _xifexpression = (int) 0;
				if (true) {
				  return 0;
				} else {
				  return 1;
				}
				return _xifexpression;
				""", false);
	}

	@Test
	public void test406762_NestedReturn4() throws Exception {
		compilesTo("""
				{
					return
						if (true) return 0 else return 1
				}
				""", """
				int _xifexpression = (int) 0;
				if (true) {
				  return 0;
				} else {
				  return 1;
				}
				return _xifexpression;
				""", false);
	}

	@Test
	public void test406762_ReturnInThrow() throws Exception {
		compilesTo("""
				{
					throw return
				}
				""", """
				try {
				  return;
				  throw /* error - couldn't compile nested return */;
				} catch (Throwable _e) {
				  throw org.eclipse.xtext.xbase.lib.Exceptions.sneakyThrow(_e);
				}
				""", false);
	}

	@Test
	public void test406762_ReturnInThrow2() throws Exception {
		compilesTo("""
				{
					throw {
						return
					}
				}
				""", """
				try {
				  return;
				  throw /* error - couldn't compile nested return */;
				} catch (Throwable _e) {
				  throw org.eclipse.xtext.xbase.lib.Exceptions.sneakyThrow(_e);
				}
				""", false);
	}

	@Test
	public void test406762_ReturnThrow() throws Exception {
		compilesTo("""
				{
					return throw new Exception()
				}
				""", """
				try {
				  throw new Exception();
				  return /* error - couldn't compile invalid throw */;
				} catch (Throwable _e) {
				  throw org.eclipse.xtext.xbase.lib.Exceptions.sneakyThrow(_e);
				}
				""", false);
	}

	@Test
	public void test406762_ValidThrowInSingleIfBranch() throws Exception {
		compilesTo("""
				{
					val b = true
					return if (b) throw new RuntimeException() else 42
				}
				""", """
				final boolean b = true;
				int _xifexpression = (int) 0;
				if (b) {
				  throw new RuntimeException();
				} else {
				  _xifexpression = 42;
				}
				return _xifexpression;
				""");
	}

	@Test
	public void test406762_ValidThrowInSingleIfBranch_1() throws Exception {
		compilesTo("""
				{
					val b = true
					return if (b) 42 else throw new RuntimeException()
				}
				""", """
				final boolean b = true;
				int _xifexpression = (int) 0;
				if (b) {
				  _xifexpression = 42;
				} else {
				  throw new RuntimeException();
				}
				return _xifexpression;
				""");
	}

	@Test
	public void test406762_InvalidThrowInBothIfBranches() throws Exception {
		compilesTo("""
				{
					val b = true
					return if (b) throw new RuntimeException() else throw new RuntimeException()
				}
				""", """
				final boolean b = true;
				void _xifexpression = null;
				if (b) {
				  throw new RuntimeException();
				} else {
				  throw new RuntimeException();
				}
				return _xifexpression;
				""", false);
	}

	@Test
	public void test406762_ValidReturnInLambdaContainedInThrow() throws Exception {
		compilesTo("""
				throw {
					val foo = [|return "foo"]
					new Exception(foo.apply)
				}
				""", """
				try {
				  Exception _xblockexpression = null;
				  {
				    final org.eclipse.xtext.xbase.lib.Functions.Function0<String> _function = () -> {
				      return "foo";
				    };
				    final org.eclipse.xtext.xbase.lib.Functions.Function0<String> foo = _function;
				    String _apply = foo.apply();
				    _xblockexpression = new Exception(_apply);
				  }
				  throw _xblockexpression;
				} catch (Throwable _e) {
				  throw org.eclipse.xtext.xbase.lib.Exceptions.sneakyThrow(_e);
				}""");
	}

	@Test
	public void test406762_ValidReturnInLambdaContainedInThrow_1() throws Exception {
		compilesTo("""
				throw {
					val ()=>Exception foo = [|return new Exception]
					foo.apply
				}
				""", """

				try {
				  Exception _xblockexpression = null;
				  {
				    final org.eclipse.xtext.xbase.lib.Functions.Function0<Exception> _function = () -> {
				      return new Exception();
				    };
				    final org.eclipse.xtext.xbase.lib.Functions.Function0<? extends Exception> foo = _function;
				    _xblockexpression = foo.apply();
				  }
				  throw _xblockexpression;
				} catch (Throwable _e) {
				  throw org.eclipse.xtext.xbase.lib.Exceptions.sneakyThrow(_e);
				}""");
	}

	@Test
	public void testObjectEqualNull() throws Exception {
		compilesTo("""
				"Foo" == null
				""", """
				boolean _equals = java.util.Objects.equals("Foo", null);
				return _equals;
				""");
	}

	@Test
	public void testObjectNotEqualNull() throws Exception {
		compilesTo("""
				"Foo" != null
				""", """
				boolean _notEquals = (!java.util.Objects.equals("Foo", null));
				return _notEquals;
				""");
	}

	@Test
	public void testObjectIdenticalNull() throws Exception {
		compilesTo("""
				"Foo" === null
				""", """
				return ("Foo" == null);
				""");
	}

	@Test
	public void testObjectNotIdenticalNull() throws Exception {
		compilesTo("""
				"Foo" !== null
				""", """
				return ("Foo" != null);
				""");
	}

	@Test
	public void testBigIntegerLiteral01() throws Exception {
		compilesTo("1bi", "return java.math.BigInteger.ONE;");
	}

	@Test
	public void testBigIntegerLiteral02() throws Exception {
		compilesTo("1.0bi", "return java.math.BigInteger.ONE;");
	}

	@Test
	public void testBigIntegerLiteral03() throws Exception {
		compilesTo("1e23bi", """
				return new java.math.BigInteger("1").multiply(java.math.BigInteger.TEN.pow(23));
				""");
	}

	@Test
	public void testBigIntegerLiteral04() throws Exception {
		compilesTo("1.23e45bi", """
				return new java.math.BigDecimal("1.23").multiply(java.math.BigDecimal.TEN.pow(45)).toBigInteger();
				""");
	}

	@Test
	public void testExtrasIssue394() throws Exception {
		compilesTo("newArrayOfSize(1).get(0)", """
				Object _get = (new Object[1])[0];
				return _get;
				""");
	}

	@Test
	public void testExtrasIssue394_2() throws Exception {
		compilesTo("newArrayOfSize(1)", """
				Object[] _newArrayOfSize = new Object[1];
				return _newArrayOfSize;
				""");
	}

	@Test
	public void testLibIssue60() throws Exception {
		compilesTo("""
				{
					val a = <String>newArrayOfSize(1)
					a.set(0, "").length
					a.set(0,"hello")
					1
				}
				""", """
				int _xblockexpression = (int) 0;
				{
				  final String[] a = new String[1];
				  (a[0] = "").length();
				  a[0] = "hello";
				  _xblockexpression = 1;
				}
				return _xblockexpression;
				""");
	}

	@Test
	public void testStringLiteralWithUnixEOL_Issue2293() throws Exception {
		compilesTo(
				"{var s = \"a multiline\nstring\"}",
				"String s = \"a multiline\\nstring\";");
	}

	@Test
	public void testStringLiteralWithWindowsEOL_Issue2293() throws Exception {
		compilesTo(
				"{var s = \"a multiline\r\nstring\r\nstring\"}",
				"String s = \"a multiline\\nstring\\nstring\";");
	}

	@Test
	public void testStringLiteralWithCarriageReturn_Issue2293() throws Exception {
		compilesTo(
				"{\"astring\".toString.replace(\"\\r\", \"\");}",
				"return \"astring\".toString().replace(\"\\r\", \"\");");
	}

	@Test
	public void testStringLiteralWithCarriageReturn2_Issue2293() throws Exception {
		compilesTo(
				"{\"astring\".toString.replaceAll(\"\\r?\\n\", \"\\n\");}",
				"return \"astring\".toString().replaceAll(\"\\r?\\n\", \"\\n\");");
	}

	@Test
	public void testStringLiteralWithCarriageReturn3_Issue2293() throws Exception {
		compilesTo(
				"{\"astring\".toString.replaceAll(\"\\r\\n\", \"\\n\");}",
				"return \"astring\".toString().replaceAll(\"\\r\\n\", \"\\n\");");
	}
}
