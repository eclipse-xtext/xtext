/*******************************************************************************
 * Copyright (c) 2011 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtend.ide.tests.outline;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.xtend.core.xtend.XtendFile;
import org.eclipse.xtend.ide.tests.AbstractXtendUITestCase;
import org.eclipse.xtend.ide.tests.WorkbenchTestHelper;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.model.XtextDocument;
import org.eclipse.xtext.ui.editor.outline.IOutlineNode;
import org.eclipse.xtext.ui.editor.outline.impl.OutlineFilterAndSorter;
import org.eclipse.xtend.ide.outline.XtendOutlineTreeProvider;
import org.junit.Test;

import com.google.common.base.Joiner;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * @author Jan Koehnlein - Initial contribution and API
 */
public abstract class AbstractOutlineTests extends AbstractXtendUITestCase {

	@Inject
	private XtendOutlineTreeProvider treeProvider;

	@Inject
	private Provider<XtextDocument> documentProvider;

	@Inject
	private WorkbenchTestHelper workbenchTestHelper;

	@Inject
	private OutlineFilterAndSorter.IComparator comparator;
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
		getSorter().setComparator(comparator);
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		workbenchTestHelper.tearDown();
	}
	
	protected XtendOutlineTreeProvider getTreeProvider() {
		return treeProvider;
	}

	@Test public void testSimpleClass() throws Exception {
		AssertBuilder assertBuilder = newAssertBuilder("class Foo {}");
		assertBuilder.numChildren(1).child(0, "Foo").numChildren(0);
	}

	@Test public void testPackage() throws Exception {
		AssertBuilder assertBuilder = newAssertBuilder("package test class Foo {}");
		assertBuilder.numChildren(2).child(0, "test").numChildren(0);
		assertBuilder.child(1, "Foo").numChildren(0);
	}
	
	@Test public void testTypeParameter() throws Exception {
		AssertBuilder assertBuilder = newAssertBuilder("class Foo <T extends Object> {}");
		assertBuilder.numChildren(1).child(0, "Foo<T>").numChildren(0);
	}
	
	@Test public void testField() throws Exception {
		AssertBuilder assertBuilder = newAssertBuilder("class Foo { String bar }");
		assertBuilder.numChildren(1).child(0, "Foo").numChildren(1).child(0, "bar : String").numChildren(0);
	}
	
	@Test public void testConstructor() throws Exception  {
		AssertBuilder assertBuilder = newAssertBuilder("class Foo { new(int foo) {} }");
		assertBuilder.numChildren(1).child(0, "Foo").numChildren(1).child(0, "new(int)").numChildren(0);
	}

	@Test public void testSimpleMethod() throws Exception {
		AssertBuilder assertBuilder = newAssertBuilder("class Foo { def foo() {null} }");
		assertBuilder.numChildren(1).child(0, "Foo").numChildren(1).child(0, "foo() : Object").numChildren(0);
	}
	
	@Test public void testMethodWithParameter() throws Exception {
		AssertBuilder assertBuilder = newAssertBuilder("class Foo { def foo(int bar) {null} }");
		assertBuilder.numChildren(1).child(0, "Foo").numChildren(1).child(0, "foo(int) : Object").numChildren(0);
	}
	
	@Test public void testMethodWithParameters() throws Exception {
		AssertBuilder assertBuilder = newAssertBuilder("class Foo { def foo(int bar, java.lang.Object x) {null} }");
		assertBuilder.numChildren(1).child(0, "Foo").numChildren(1).child(0, "foo(int, Object) : Object").numChildren(0);
	}
	
	@Test public void testMethodWithReturnType() throws Exception {
		AssertBuilder assertBuilder = newAssertBuilder("class Foo { def java.lang.Object foo() {null} }");
		assertBuilder.numChildren(1).child(0, "Foo").numChildren(1).child(0, "foo() : Object").numChildren(0);
	}
	
	@Test public void testMethodWithTypeParameter() throws Exception {
		AssertBuilder assertBuilder = newAssertBuilder("class Foo { def <T> foo() {null} }");
		assertBuilder.numChildren(1).child(0, "Foo").numChildren(1).child(0, "foo() : Object").numChildren(0);
	}
	
	@Test public void testDispatchMethod() throws Exception {
		AssertBuilder assertBuilder = newAssertBuilder("class Foo { def dispatch foo(Object x) {''} def dispatch foo(String y) {''} }");
		AssertBuilder dispatcher = assertBuilder.numChildren(1).child(0, "Foo").numChildren(1).child(0, "foo(Object) : String").numChildren(2);
		dispatcher.child(0, "foo(Object) : String").numChildren(0);
		dispatcher.child(1, "foo(String) : String").numChildren(0);
	}

	@Test public void testInterface() throws Exception {
		AssertBuilder assertBuilder = newAssertBuilder("interface Foo { int bar def String foo() }");
		AssertBuilder interfaze = assertBuilder.numChildren(1).child(0, "Foo").numChildren(2);
		interfaze.child(0, "bar : int").numChildren(0);
		interfaze.child(1, "foo() : String").numChildren(0);
	}

	// enum literals are displayed without their type since that one is implicit
	@Test public void testEnum() throws Exception {
		AssertBuilder assertBuilder = newAssertBuilder("enum Foo { BAR, BAZ }");
		AssertBuilder interfaze = assertBuilder.numChildren(1).child(0, "Foo").numChildren(2);
		interfaze.child(0, "BAR").numChildren(0);
		interfaze.child(1, "BAZ").numChildren(0);
	}

	@Test public void testAnnotationType() throws Exception {
		AssertBuilder assertBuilder = newAssertBuilder("annotation Foo { int bar String foo = '' }");
		AssertBuilder annotationType = assertBuilder.numChildren(1).child(0, "Foo").numChildren(2);
		annotationType.child(0, "bar : int").numChildren(0);
		annotationType.child(1, "foo : String").numChildren(0);
	}

	protected AssertBuilder newAssertBuilder(String model) throws Exception, CoreException {
		XtendFile file = workbenchTestHelper.xtendFile("Foo", model);
		XtextDocument document = documentProvider.get();
		document.setInput((XtextResource) file.eResource());
		IOutlineNode root = treeProvider.createRoot(document);
		AssertBuilder assertBuilder = new AssertBuilder(root);
		return assertBuilder;
	}

	protected abstract OutlineFilterAndSorter getSorter();

	public class AssertBuilder {

		private final IOutlineNode node;

		AssertBuilder(IOutlineNode node) {
			this.node = node;
		}

		AssertBuilder(IOutlineNode node, String text) {
			this(node);
			assertEquals(text, node.getText().toString());
		}

		AssertBuilder numChildren(int num) {
			IOutlineNode[] filteredAndSortedChildren = getSorter().filterAndSort(node.getChildren());
			assertEquals("Wrong number of children\n" + Joiner.on("\n").join(filteredAndSortedChildren), num, filteredAndSortedChildren.length);
			return this;
		}
		
		AssertBuilder hasTextRegion(boolean hasTextRegion) {
			if(hasTextRegion)
				assertNotNull(node.getSignificantTextRegion());
			else 
				assertNull(node.getSignificantTextRegion());
			return this;
		}

		AssertBuilder child(int index, String text) {
			IOutlineNode[] sortedChildren = getSorter().filterAndSort(node.getChildren());
			if (sortedChildren.length <= index)
				fail("Missing child node " + index);
			return new AssertBuilder(sortedChildren[index], text);
		}

	}

}
