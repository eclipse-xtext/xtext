/*******************************************************************************
 * Copyright (c) 2010, 2026 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.common.types.xtext.ui;

import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.xtext.ISetup;
import org.eclipse.xtext.common.types.access.jdt.IJavaProjectProvider;
import org.eclipse.xtext.common.types.access.jdt.MockJavaProjectProvider;
import org.eclipse.xtext.common.types.eclipse.tests.internal.TestsActivator;
import org.eclipse.xtext.common.types.xtext.ui.ui.ContentAssistTestLanguageUiModule;
import org.eclipse.xtext.testing.AbstractXtextTests;
import org.eclipse.xtext.ui.shared.SharedStateModule;
import org.eclipse.xtext.ui.testing.ContentAssistProcessorTestBuilder;
import org.eclipse.xtext.ui.testing.util.ResourceLoadHelper;
import org.eclipse.xtext.util.Modules2;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
/**
 * @author dhuebner - Initial contribution and API
 */
public class ContentAssistTest_65 extends AbstractXtextTests implements ResourceLoadHelper {

	@BeforeClass public static void createMockJavaProject() throws Exception {
		MockJavaProjectProvider.setUp();
	}
	
	protected ContentAssistProcessorTestBuilder newBuilder() throws Exception {
		with(getSetup());
		return new ContentAssistProcessorTestBuilder(getInjector(), this);
	}
	
	protected ISetup getSetup() {
		return doGetSetup();
	}
	public ISetup doGetSetup() {
		return new ContentAssistTestLanguageStandaloneSetup() {
			@Override
			public Injector createInjector() {
				return Guice.createInjector(Modules2.mixin(
						new ContentAssistTestLanguageRuntimeModule(),
						new ContentAssistTestLanguageUiModule(TestsActivator.getInstance()) {
							@Override
							public Class<? extends IJavaProjectProvider> bindIJavaProjectProvider() {
								return MockJavaProjectProvider.class;
							}
						}, 
						new SharedStateModule()));
			}
		};
	}
	
	@Test
	public void testDefaultArrayList_01() throws Exception {
		//FIXME this does not consider JDT type filter, i.e. TypeFilter#isFiltered(char[],char[]) is not called.
		//      in test testSubtypeArrayList_01 this is respected.
		ContentAssistProcessorTestBuilder builder = newBuilder().append("default Appendable");
		builder.assertText("java.lang.Appendable", "com.google.common.io.AppendableWriter");
	}

	@Test
	public void testDefaultArrayList_02() throws Exception {
		//FIXME this does not consider JDT type filter, i.e. TypeFilter#isFiltered(char[],char[]) is not called.
		//      in test testSubtypeArrayList_01 this is respected.
		ContentAssistProcessorTestBuilder builder = newBuilder().append("import java.lang.* default Appendable");
		builder.assertText("Appendable", "com.google.common.io.AppendableWriter");
	}

	@Test public void testCustomArrayList_01() throws Exception {
		newBuilder().append("custom ArrayLis").assertText(
				"java.util.ArrayList",
				"com.google.common.collect.MultimapBuilder.ArrayListSupplier",
				"java.util.ArrayList.ArrayListSpliterator",
				"java.util.Arrays.ArrayList",
				"com.google.common.collect.ArrayListMultimap");
	}
	
	@Test public void testCustomArrayList_02() throws Exception {
		newBuilder().append("import java.util.* custom ArrayLis").assertText(
			"ArrayList",
			"ArrayList.ArrayListSpliterator",
			"Arrays.ArrayList",
			"com.google.common.collect.MultimapBuilder.ArrayListSupplier",
			"com.google.common.collect.ArrayListMultimap");
	}
	
	@Test public void testDefaultBlockingQueue_01() throws Exception {
		newBuilder().append("import java.util.* default BlockingQ").assertText("concurrent.BlockingQueue");
	}
	
	@Test public void testDefaultBlockingQueue_02() throws Exception {
		newBuilder().append("import java.* default BlockingQ").assertText("util.concurrent.BlockingQueue");
	}
	
	@Test public void testCustomArrayList_03() throws Exception {
		newBuilder().append("import java.util.* custom java.util.ArrayLis").assertText("ArrayList","ArrayList.ArrayListSpliterator");
	}

	@Test public void testCustomBlockingQueue_01() throws Exception {
		newBuilder().append("import java.util.* custom BlockingQ").assertText("concurrent.BlockingQueue");
	}
	
	@Test public void testCustomBlockingQueue_02() throws Exception {
		newBuilder().append("import java.* custom BlockingQ").assertText("util.concurrent.BlockingQueue");
	}
	
	@Test public void testCustomBlockingQueue_03() throws Exception {
		newBuilder().append("import java.* custom concurrent.BlockingQ").assertText("util.concurrent.BlockingQueue");
	}
	
	@Test public void testSubtypeArrayList_01() throws Exception {
		newBuilder().append("subtype ArrayLis").assertText("java.util.ArrayList", "java.util.Arrays.ArrayList");
	}
	
	@Test public void testSubtypeArrayList_02() throws Exception {
		newBuilder().append("import java.util.* subtype ArrayLis").assertText("ArrayList", "Arrays.ArrayList");
	}
	
	@Test public void testSubtypeHashSet_01() throws Exception {
		newBuilder().append("subtype HashSe").assertText("java.util.HashSet");
	}
	
	@Test public void testSubtypeHashSet_02() throws Exception {
		newBuilder().append("import java.util.* subtype HashSe").assertText("HashSet");
	}

	@Test public void testSubtypeBlockingQueue_01() throws Exception {
		newBuilder().append("import java.util.* subtype BlockingQ").assertText("concurrent.BlockingQueue");
	}
	
	@Test public void testSubtypeBlockingQueue_02() throws Exception {
		newBuilder().append("import java.* subtype BlockingQ").assertText("util.concurrent.BlockingQueue");
	}
	
	@Test public void testSubtypeBlockingQueue_03() throws Exception {
		newBuilder().append("import java.* subtype concurrent.BlockingQ").assertText("util.concurrent.BlockingQueue");
	}
	
	/**
	 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=438191
	 */
	@Test public void testSubtypeProposals() throws Exception {
		ICompletionProposal[] proposals = newBuilder().append("import java.util.* subtype I").computeCompletionProposals();
		for (ICompletionProposal iCompletionProposal : proposals) {
			String displayString = iCompletionProposal.getDisplayString();
			assertFalse(displayString, displayString.contains(Iterable.class.getSimpleName()));
		}
	}
	
	@Test public void testMap_01() throws Exception {
		newBuilder().append("import java.util.* import javax.swing.text.html.* custom java.util.Map").assertText(
				"java.util.Map");
	}
	
	@Test public void testMap_02() throws Exception {
		newBuilder().append("import javax.swing.text.html.* import java.util.* custom java.util.Map").assertText(
				"java.util.Map");
	}
	
	@Test public void testMap_03() throws Exception {
		newBuilder().append("import java.util.* custom java.util.Map").assertText("Map");
	}

}
