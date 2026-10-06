/**
 * Copyright (c) 2015, 2020 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.xbase.tests.compiler;

import org.eclipse.xtext.xbase.compiler.output.FakeTreeAppendable;
import org.junit.Test;

/**
 * @author Miro Spoenemann - Initial contribution and API
 */
public class NewLanguageFeaturesCompilerTest extends AbstractOutputComparingCompilerTests {

	@Override
	protected FakeTreeAppendable createAppendable() {
		return new FakeTreeAppendable();
	}
	
	@Test
	public void testOverride() throws Exception {
		String xbaseCode = "{ val com.google.common.base.AbstractIterator<String> x = [ self.endOfData ] }";
		String result =
				"final com.google.common.base.AbstractIterator<String> _function = new com.google.common.base.AbstractIterator<String>() {\n" + 
				"  @Override\n" + 
				"  protected String computeNext() {\n" + 
				"    return this.endOfData();\n" + 
				"  }\n" + 
				"};\n" + 
				"final com.google.common.base.AbstractIterator<String> x = _function;";
		compilesTo(xbaseCode, result);
	}

	@Test
	public void testLamdba() throws Exception {
		String xbaseCode = "{ val x = #['a', '', 'c'].filter[!empty] }";
		String result =
				"final org.eclipse.xtext.xbase.lib.Functions.Function1<String, Boolean> _function = (String it) -> {\n" + 
				"  boolean _isEmpty = it.isEmpty();\n" + 
				"  return Boolean.valueOf((!_isEmpty));\n" + 
				"};\n" + 
				"final Iterable<String> x = org.eclipse.xtext.xbase.lib.IterableExtensions.<String>filter(java.util.Collections.<String>unmodifiableList(org.eclipse.xtext.xbase.lib.CollectionLiterals.<String>newArrayList(\"a\", \"\", \"c\")), _function);";
		compilesTo(xbaseCode, result);
	}

	@Test
	public void testSwitchOverString() throws Exception {
		String xbaseCode =
				"{\n" +
				"	val x = switch('foo') {\n" +
				"		case 'a': 1\n" +
				"		case 'b' + 'c': 2\n" +
				"		default: 3\n" +
				"	}\n" +
				"}\n";
		String result =
				"int _switchResult = (int) 0;\n" +
				"final String _switchValue = \"foo\";\n" +
				"if (_switchValue != null) {\n" +
				"  switch (_switchValue) {\n" +
				"    case \"a\":\n" +
				"      _switchResult = 1;\n" +
				"      break;\n" +
				"    case (\"b\" + \"c\"):\n" +
				"      _switchResult = 2;\n" +
				"      break;\n" +
				"    default:\n" +
				"      _switchResult = 3;\n" +
				"      break;\n" +
				"  }\n" +
				"} else {\n" +
				"  _switchResult = 3;\n" +
				"}\n" +
				"final int x = _switchResult;\n";
		compilesTo(xbaseCode, result);
	}

	@Test
	public void testUnderscoreInNumericLiteral() throws Exception {
		String xbaseCode =
				"{\n" +
				"	val x = 123_456_789\n" +
				"}\n";
		String result = "final int x = 123_456_789;\n";
		compilesTo(xbaseCode, result);
	}

	@Test
	public void testNewThread() throws Exception {
		String xbaseCode = "new Thread [| ]";
		String result =
				"final Runnable _function = () -> {\n" +
				"};\n" +
				"Thread _thread = new Thread(_function);\n" +
				"return _thread;\n";
		compilesTo(xbaseCode, result);
	}

	@Test
	public void testBasicForLoop_8() throws Exception {
		String xbaseCode =
				"{\n" +
				"	[| for (;;) {} ]\n" +
				"}\n";
		String result =
				"final org.eclipse.xtext.xbase.lib.Procedures.Procedure0 _function = () -> {\n" +
				"  for (;;) {\n" +
				"  }\n" +
				"};\n" +
				"return _function;\n";
		compilesTo(xbaseCode, result);
	}

	@Test
	public void testBasicForLoop_10() throws Exception {
		String xbaseCode =
				"{\n" +
				"	val i = 0\n" +
				"	for ([| i ].apply, [| i ].apply; i < 10;) {\n" +
				"		\n" +
				"	}\n" +
				"}\n";
		String result =
				"final int i = 0;\n" +
				"for (((org.eclipse.xtext.xbase.lib.Functions.Function0<Integer>) () -> {\n" +
				"  return Integer.valueOf(i);\n" +
				"}).apply(), ((org.eclipse.xtext.xbase.lib.Functions.Function0<Integer>) () -> {\n" +
				"  return Integer.valueOf(i);\n" +
				"}).apply(); (i < 10);) {\n" +
				"}\n";
		compilesTo(xbaseCode, result);
	}

	@Test
	public void testBug410797_01() throws Exception {
		String xbaseCode =
				"{ val boolean bug = #[true, false, true].reduce[a,b|a && b] }";
		String result =
				"final org.eclipse.xtext.xbase.lib.Functions.Function2<Boolean, Boolean, Boolean> _function = (Boolean a, Boolean b) -> {\n" +
				"  return Boolean.valueOf(((a).booleanValue() && (b).booleanValue()));\n" +
				"};\n" +
				"final boolean bug = (boolean) org.eclipse.xtext.xbase.lib.IterableExtensions.<Boolean>reduce(java.util.Collections.<Boolean>unmodifiableList(org.eclipse.xtext.xbase.lib.CollectionLiterals.<Boolean>newArrayList(Boolean.valueOf(true), Boolean.valueOf(false), Boolean.valueOf(true))), _function);\n" +
				"";
		compilesTo(xbaseCode, result);
	}

	@Test
	public void testImplicitReferenceToMultitype() throws Exception {
		String xbaseCode = "((null as Iterable<java.util.List<Object>>) + (null as Iterable<java.util.Set<Object>>)).forEach[ size ]";
		String result = "final java.util.function.Consumer<java.util.Collection<Object>> _function = (java.util.Collection<Object> it) -> {\n" +
				"  it.size();\n" +
				"};\n" +
				"com.google.common.collect.Iterables.<java.util.Collection<Object>>concat(((Iterable<java.util.List<Object>>) null), ((Iterable<java.util.Set<Object>>) null)).forEach(_function);\n";
		compilesTo(xbaseCode, result);
	}

	@Test
	public void testImplicitReferenceToSynonym_01() throws Exception {
		String xbaseCode = "(null as Iterable<String[]>).forEach[ reverse ]";
		String result =
				"final java.util.function.Consumer<String[]> _function = (String[] it) -> {\n" +
				"  org.eclipse.xtext.xbase.lib.ListExtensions.<String>reverse(((java.util.List<String>)org.eclipse.xtext.xbase.lib.Conversions.doWrapArray(it)));\n" +
				"};\n" +
				"((Iterable<String[]>) null).forEach(_function);\n";
		compilesTo(xbaseCode, result);
	}

	@Test
	public void testBlockHasNoSuperfluousBraces_03() throws Exception {
		String xbaseCode = "{ var (int)=>void fun = [ int i | new Object() new Object() ] }";
		String result =
				"final org.eclipse.xtext.xbase.lib.Procedures.Procedure1<Integer> _function = (Integer i) -> {\n" +
				"  new Object();\n" +
				"  new Object();\n" +
				"};\n" +
				"org.eclipse.xtext.xbase.lib.Procedures.Procedure1<? super Integer> fun = _function;\n";
		compilesTo(xbaseCode, result);
	}

	@Test
	public void testExceptionOnClosure() throws Exception {
		String xbaseCode = "{val java.beans.VetoableChangeListener x = []}";
		String result =
				"final java.beans.VetoableChangeListener _function = (java.beans.PropertyChangeEvent it) -> {\n" +
				"};\n" +
				"final java.beans.VetoableChangeListener x = _function;\n";
		compilesTo(xbaseCode, result);
	}
}
