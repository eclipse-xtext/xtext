/*******************************************************************************
 * Copyright (c) 2026 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.ui.tests.editor.folding;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.XtextRunner;
import org.eclipse.xtext.ui.editor.folding.BracePairFoldingRegionProvider;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.ui.testing.AbstractFoldingTest;
import org.eclipse.xtext.ui.tests.editor.outline.ui.tests.OutlineTestLanguageUiInjectorProvider;
import org.eclipse.xtext.util.CancelIndicator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;

/**
 * @author Arne Deutsch - Initial contribution and API
 */
@RunWith(XtextRunner.class)
@InjectWith(OutlineTestLanguageUiInjectorProvider.class)
public class BracePairFoldingRegionProviderTest extends AbstractFoldingTest {

	public static class CanceledBracePairFoldingRegionProvider extends BracePairFoldingRegionProvider {
		@Override
		protected CancelIndicator getCancelIndicator() {
			return () -> true;
		}
	}

	@Inject
	private BracePairFoldingRegionProvider braceProvider;

	@Inject
	private CanceledBracePairFoldingRegionProvider canceledBraceProvider;

	@Before
	public void useBraceProvider() {
		foldingRegionProvider = braceProvider;
	}

	@Test
	public void singleLineBraceIsNotFolded() throws Exception {
		testFoldingRegions("root {}");
	}

	@Test
	public void singleLineParensAndBraceIsNotFolded() throws Exception {
		testFoldingRegions("root() {}");
	}

	@Test
	public void emptyRoot() throws Exception {
		testFoldingRegions("""
				[>root() {
				}<]
				""");
	}

	@Test
	public void emptyRootWithNewline() throws Exception {
		testFoldingRegions("""
				[>root {
				
				}<]
				""");
	}

	@Test
	public void emptyRootWithManyNewlines() throws Exception {
		testFoldingRegions("""
				[>root {
				
				
				
				
				}<]
				""");
	}

	@Test
	public void nestedBracesAreFolded() throws Exception {
		testFoldingRegions("""
				[>root {
				[> child {
				   }<]
				}<]
				""");
	}

	@Test
	public void multiLineCommentIsFolded() throws Exception {
		testFoldingRegions("""
				[>/*
				   */<]
				root {}
				""");
	}

	@Test
	public void multiLineCommentInsideNodeIsFolded() throws Exception {
		testFoldingRegions("""
				[>root {
				[>/*
				   * multi-line-comment
				   */<]
				}<]
				""");
	}

	@Test
	public void javadocCommentInsideNodeIsFolded() throws Exception {
		testFoldingRegions("""
				[>root {
				[>/**
				   * java-doc-comment
				   */<]
				}<]
				""");
	}

	@Test
	public void rootNodeWithSingleLineCommentsIsFolded() throws Exception {
		testFoldingRegions("""
				[>root {
				  // first single line comment
				  
				  // second single line comment
				}<]
				""");
	}

	@Test
	public void rootNodeWithMultiAndSingleSingleLineCommentsIsFolded() throws Exception {
		testFoldingRegions("""
				[>root {
				  // first single line comment
				  
				  // second single line comment
				[>/*
				   * multi-line-comment
				   */<]
				}<]
				""");
	}

	@Test
	public void multiLineParensAreFolded() throws Exception {
		testFoldingRegions("""
				root
				[>  (
				       child1
				       child2
				    )<]
				[>{
				    child 1 {}
				    child 2 {}
				}<]
				""");
	}

	@Test
	public void multiLevelsOfNestingAreFolded() throws Exception {
		testFoldingRegions("""
				root
				[>  (
				       child1
				       child2
				    )<]
				[>{
				[>    child1 {
				[>    	subchild1 (
				    	  subchild2
				    	)<]
				    	{}
				    }<]
				[>    child2 {
				[>    	subchild2(
				    	  subchild1
				    	)<]
				    	{}
				    }<]
				}<]
				""");
	}

	@Test
	public void bracesInMultiLineCommentAreNotFolded() throws Exception {
		testFoldingRegions("""
				[>/*
				   * {
				   *
				   * }
				   *
				   */<]
				root {}
				""");
	}

	@Test
	public void brokenInputFoldsBestEffort() throws Exception {
		testFoldingRegions("""
				root {
				[>  child {
				    }<]
				""");
	}

	@Test
	public void mismatchedClosingBraceClosesEnclosingOpening() throws Exception {
		// recovers by matching '}' as closing brace of root, while '(' is still open
		testFoldingRegions("""
				[>root {
				  child ( other
				  }<]
				[>other {
				}<]
				""");
	}

	@Test
	public void strayClosingParenDoesNotBreakEnclosingFolding() throws Exception {
		testFoldingRegions("""
				[>root {
				[>  child ) {
				    }<]
				}<]
				""");
	}

	@Test
	public void strayClosingBraceAtEndDoesNotBreakFolding() throws Exception {
		testFoldingRegions("""
				[>root {
				}<]
				}
				""");
	}

	@Test
	public void bracesSkippedBySyntaxErrorRecoveryAreFolded() throws Exception {
		testFoldingRegions("""
				!INVALID
				[>root() {
				}<]
				""");
	}

	@Test
	public void braceFoldingIsCancelable() throws Exception {
		IXtextDocument document = openInEditor(dslFile(getProjectName(), getFileName(), getFileExtension(), """
				root {
				}
				"""));
		assertThrows(OperationCanceledException.class, () -> canceledBraceProvider.getFoldingRegions(document));
	}

}
