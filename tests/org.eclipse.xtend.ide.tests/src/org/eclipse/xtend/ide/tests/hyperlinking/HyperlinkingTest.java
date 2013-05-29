/*******************************************************************************
 * Copyright (c) 2011 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtend.ide.tests.hyperlinking;

import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.xtend.ide.tests.AbstractXtendUITestCase;
import org.eclipse.xtend.ide.tests.WorkbenchTestHelper;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.hyperlinking.IHyperlinkHelper;
import org.eclipse.xtext.ui.editor.hyperlinking.XtextHyperlink;
import org.junit.Test;

import com.google.inject.Inject;

/**
 * @author Jan Koehnlein - Initial contribution and API
 */
public class HyperlinkingTest extends AbstractXtendUITestCase {

	@Inject
	private IHyperlinkHelper hyperlinkHelper;

	@Inject
	private WorkbenchTestHelper testHelper;
	
	@Test public void testDispatchMethod() throws Exception {
		String modelAsString = "class Foo {\n"
				+ "  def bar() { foo(new Object()) }\n"
				+ "  def dispatch foo(Object o) { null }\n"
				+ "  def dispatch foo(String s) { null }\n"
				+ "  def dispatch foo(Number n) { null }\n"
				+ "}";
		XtextResource resource = (XtextResource) testHelper.xtendFile("Foo", modelAsString).eResource();
		IHyperlink[] hyperlinks = hyperlinkHelper.createHyperlinksByOffset(resource, modelAsString.indexOf("foo"), true);
		assertEquals(3, hyperlinks.length);
		assertEquals("foo(Number) : Object", hyperlinks[0].getHyperlinkText());
		assertTrue(((XtextHyperlink)hyperlinks[0]).getURI().isPlatformResource());
		assertEquals("foo(String) : Object", hyperlinks[1].getHyperlinkText());
		assertTrue(((XtextHyperlink)hyperlinks[1]).getURI().isPlatformResource());
		assertEquals("foo(Object) : Object", hyperlinks[2].getHyperlinkText());
		assertTrue(((XtextHyperlink)hyperlinks[2]).getURI().isPlatformResource());
	}
	
	@Test public void testPlainMethod() throws Exception {
		String modelAsString = "class Foo {\n"
				+ "  def bar() { foo() }\n"
				+ "  def foo() { null }\n"
				+ "}";
		XtextResource resource = (XtextResource) testHelper.xtendFile("Foo", modelAsString).eResource();
		IHyperlink[] hyperlinks = hyperlinkHelper.createHyperlinksByOffset(resource, modelAsString.indexOf("foo"), true);
		assertEquals(1, hyperlinks.length);
		assertEquals("foo() : Object", hyperlinks[0].getHyperlinkText());
		assertTrue(((XtextHyperlink)hyperlinks[0]).getURI().isPlatformResource());
	}
	
	@Test public void testConstructor() throws Exception {
		String modelAsString =
			"class Foo {\n"
			+ "  def bar() { new Foo() }\n"
			+ "}";
		XtextResource resource = (XtextResource) testHelper.xtendFile("Foo", modelAsString).eResource();
		IHyperlink[] hyperlinks = hyperlinkHelper.createHyperlinksByOffset(resource, modelAsString.indexOf("Foo", 10), true);
		assertEquals(1, hyperlinks.length);
		assertEquals("Foo", hyperlinks[0].getHyperlinkText());
		assertTrue(((XtextHyperlink)hyperlinks[0]).getURI().isPlatformResource());
	}

	@Test public void testSuperClass() throws Exception {
		String modelAsString =
			"class Foo extends Object {\n"
				+ "}";
		XtextResource resource = (XtextResource) testHelper.xtendFile("Foo", modelAsString).eResource();
		IHyperlink[] hyperlinks = hyperlinkHelper.createHyperlinksByOffset(resource, modelAsString.indexOf("Object"), true);
		assertEquals(1, hyperlinks.length);
		assertEquals("Object", hyperlinks[0].getHyperlinkText());
	}
	
	@Test public void testPackageFragment() throws Exception {
		String modelAsString = "class C { val x = java.lang.String }";
		XtextResource resource = (XtextResource) testHelper.xtendFile("C", modelAsString).eResource();
		IHyperlink[] hyperlinks = hyperlinkHelper.createHyperlinksByOffset(resource, modelAsString.indexOf("lang"), true);
		assertEquals(1, hyperlinks.length);
		assertEquals("String", hyperlinks[0].getHyperlinkText());
		assertEquals(modelAsString.indexOf("java"), hyperlinks[0].getHyperlinkRegion().getOffset());
		assertEquals("java.lang.String".length(), hyperlinks[0].getHyperlinkRegion().getLength());
	}
	
	@Test public void testTypeLiteral() throws Exception {
		String modelAsString = "class C { val x = java.lang.String }";
		XtextResource resource = (XtextResource) testHelper.xtendFile("C", modelAsString).eResource();
		IHyperlink[] hyperlinks = hyperlinkHelper.createHyperlinksByOffset(resource, modelAsString.indexOf("String"), true);
		assertEquals(1, hyperlinks.length);
		assertEquals("String", hyperlinks[0].getHyperlinkText());
		assertEquals(modelAsString.indexOf("java"), hyperlinks[0].getHyperlinkRegion().getOffset());
		assertEquals("java.lang.String".length(), hyperlinks[0].getHyperlinkRegion().getLength());
	}
	
	@Test public void testNestedTypePackageFragment() throws Exception {
		String modelAsString = "class C { val x = java.util.Map.Entry }";
		XtextResource resource = (XtextResource) testHelper.xtendFile("C", modelAsString).eResource();
		IHyperlink[] hyperlinks = hyperlinkHelper.createHyperlinksByOffset(resource, modelAsString.indexOf("util"), true);
		assertEquals(1, hyperlinks.length);
		assertEquals("Map<K, V>", hyperlinks[0].getHyperlinkText());
		assertEquals(modelAsString.indexOf("java"), hyperlinks[0].getHyperlinkRegion().getOffset());
		assertEquals("java.util.Map".length(), hyperlinks[0].getHyperlinkRegion().getLength());
	}
	
	@Test public void testOuterTypeLiteral() throws Exception {
		String modelAsString = "class C { val x = java.util.Map.Entry }";
		XtextResource resource = (XtextResource) testHelper.xtendFile("C", modelAsString).eResource();
		IHyperlink[] hyperlinks = hyperlinkHelper.createHyperlinksByOffset(resource, modelAsString.indexOf("Map"), true);
		assertEquals(1, hyperlinks.length);
		assertEquals("Map<K, V>", hyperlinks[0].getHyperlinkText());
		assertEquals(modelAsString.indexOf("java"), hyperlinks[0].getHyperlinkRegion().getOffset());
		assertEquals("java.util.Map".length(), hyperlinks[0].getHyperlinkRegion().getLength());
	}
	
	@Test public void testOuterTypeLiteralWithDollar() throws Exception {
		String modelAsString = "class C { val x = java.util.Map$Entry }";
		XtextResource resource = (XtextResource) testHelper.xtendFile("C", modelAsString).eResource();
		IHyperlink[] hyperlinks = hyperlinkHelper.createHyperlinksByOffset(resource, modelAsString.indexOf("Map"), true);
		assertEquals(1, hyperlinks.length);
		assertEquals("Entry<K, V>", hyperlinks[0].getHyperlinkText());
		assertEquals(modelAsString.indexOf("java"), hyperlinks[0].getHyperlinkRegion().getOffset());
		assertEquals("java.util.Map$Entry".length(), hyperlinks[0].getHyperlinkRegion().getLength());
	}
	
	@Test public void testNestedTypeLiteral() throws Exception {
		String modelAsString = "class C { val x = java.util.Map.Entry }";
		XtextResource resource = (XtextResource) testHelper.xtendFile("C", modelAsString).eResource();
		IHyperlink[] hyperlinks = hyperlinkHelper.createHyperlinksByOffset(resource, modelAsString.indexOf("Entry"), true);
		assertEquals(1, hyperlinks.length);
		assertEquals("Entry<K, V>", hyperlinks[0].getHyperlinkText());
		assertEquals(modelAsString.indexOf("Entry"), hyperlinks[0].getHyperlinkRegion().getOffset());
		assertEquals("Entry".length(), hyperlinks[0].getHyperlinkRegion().getLength());
	}
	
	@Test public void testNestedTypeLiteralWithDollar() throws Exception {
		String modelAsString = "class C { val x = java.util.Map$Entry }";
		XtextResource resource = (XtextResource) testHelper.xtendFile("C", modelAsString).eResource();
		IHyperlink[] hyperlinks = hyperlinkHelper.createHyperlinksByOffset(resource, modelAsString.indexOf("Entry"), true);
		assertEquals(1, hyperlinks.length);
		assertEquals("Entry<K, V>", hyperlinks[0].getHyperlinkText());
		assertEquals(modelAsString.indexOf("java"), hyperlinks[0].getHyperlinkRegion().getOffset());
		assertEquals("java.util.Map$Entry".length(), hyperlinks[0].getHyperlinkRegion().getLength());
	}

	@Test public void testStaticFeatureCall() throws Exception {
		String modelAsString = "class C { val x = java.lang.String.valueOf('') }";
		XtextResource resource = (XtextResource) testHelper.xtendFile("C", modelAsString).eResource();
		IHyperlink[] hyperlinks = hyperlinkHelper.createHyperlinksByOffset(resource, modelAsString.indexOf("lang"), true);
		assertEquals(1, hyperlinks.length);
		assertEquals("String", hyperlinks[0].getHyperlinkText());
		assertEquals(modelAsString.indexOf("java"), hyperlinks[0].getHyperlinkRegion().getOffset());
		assertEquals("java.lang.String".length(), hyperlinks[0].getHyperlinkRegion().getLength());
	}
	
	@Test public void testStaticFeatureCallWithColon() throws Exception {
		String modelAsString = "class C { val x = java.lang.String::valueOf('') }";
		XtextResource resource = (XtextResource) testHelper.xtendFile("C", modelAsString).eResource();
		IHyperlink[] hyperlinks = hyperlinkHelper.createHyperlinksByOffset(resource, modelAsString.indexOf("lang"), true);
		assertEquals(1, hyperlinks.length);
		assertEquals("String", hyperlinks[0].getHyperlinkText());
		assertEquals(modelAsString.indexOf("java"), hyperlinks[0].getHyperlinkRegion().getOffset());
		assertEquals("java.lang.String".length(), hyperlinks[0].getHyperlinkRegion().getLength());
	}
	
	@Test public void testStaticFeatureCallWithAllColon() throws Exception {
		String modelAsString = "class C { val x = java::lang::String::valueOf('') }";
		XtextResource resource = (XtextResource) testHelper.xtendFile("C", modelAsString).eResource();
		IHyperlink[] hyperlinks = hyperlinkHelper.createHyperlinksByOffset(resource, modelAsString.indexOf("lang"), true);
		assertEquals(1, hyperlinks.length);
		assertEquals("String", hyperlinks[0].getHyperlinkText());
		assertEquals(modelAsString.indexOf("java"), hyperlinks[0].getHyperlinkRegion().getOffset());
		assertEquals("java::lang::String".length(), hyperlinks[0].getHyperlinkRegion().getLength());
	}
}
