/*******************************************************************************
 * Copyright (c) 2026 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.ui.tests.editor.folding;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.XtextRunner;
import org.eclipse.xtext.ui.editor.folding.BracePairFoldingRegionProvider;
import org.eclipse.xtext.ui.testing.AbstractFoldingTest;
import org.eclipse.xtext.ui.tests.editor.bracketmatching.ui.tests.BmTestLanguageUiInjectorProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;

/**
 * Test BracePairFoldingRegionProvider in relation to string literals. 
 *
 * @author Arne Deutsch - Initial contribution and API
 */
@RunWith(XtextRunner.class)
@InjectWith(BmTestLanguageUiInjectorProvider.class)
public class BracePairFoldingRegionProviderStringLiteralTest extends AbstractFoldingTest {

	@Inject
	private BracePairFoldingRegionProvider braceProvider;

	@Before
	public void useBraceProvider() {
		foldingRegionProvider = braceProvider;
	}

	@Test
	public void bracesInMultiLineStringAreNotFolded() throws Exception {
		testFoldingRegions("""
				"(
				  [
				  ]
				)"
				""");
	}

	@Test
	public void unbalancedBracesInStringsDoNotAffectFolding() throws Exception {
		testFoldingRegions("""
				[>(
				  "(("
				  "]"
				[>  [
				    x
				  ]<]
				  ")"
				)<]
				""");
	}

}
