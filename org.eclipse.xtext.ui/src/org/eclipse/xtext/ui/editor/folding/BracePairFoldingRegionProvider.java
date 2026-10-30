/*******************************************************************************
 * Copyright (c) 2026 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.ui.editor.folding;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;

import org.eclipse.xtext.Keyword;
import org.eclipse.xtext.ide.editor.bracketmatching.BracePair;
import org.eclipse.xtext.ide.editor.bracketmatching.IBracePairProvider;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.parser.IParseResult;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.ITextRegion;

import com.google.inject.Inject;

/**
 * Calculate the folding regions from the brace pairs and the comments. The default regions from the EObjects are skipped.
 * 
 * @author Arne Deutsch - Initial contribution and API
 * @since 2.45
 */
public class BracePairFoldingRegionProvider extends DefaultFoldingRegionProvider {

	@Inject
	private IBracePairProvider bracePairProvider;

	@Override
	protected void computeObjectFolding(XtextResource resource, IFoldingRegionAcceptor<ITextRegion> acceptor) {
		IParseResult parseResult = resource.getParseResult();
		if (parseResult == null) {
			return;
		}
		ICompositeNode root = parseResult.getRootNode();
		if (root == null) {
			return;
		}
		computeBracePairFolding(root, acceptor);
	}

	/**
	 * The folding regions are calculated by iterating the tokens once and checking for matching opening/closing brace pairs. The
	 * IBracePairProvider can be used to configure the actual relevant tokens for folding.
	 */
	protected void computeBracePairFolding(ICompositeNode root, IFoldingRegionAcceptor<ITextRegion> acceptor) {
		HashMap<String, BracePair> openingPairs = new HashMap<>();
		HashMap<String, BracePair> closingPairs = new HashMap<>();
		for (BracePair pair : bracePairProvider.getPairs()) {
			openingPairs.put(pair.getLeftBrace(), pair);
			closingPairs.put(pair.getRightBrace(), pair);
		}
		Deque<ILeafNode> openings = new ArrayDeque<>();
		for (ILeafNode leaf : root.getLeafNodes()) {
			if (leaf.isHidden() || !(leaf.getGrammarElement() instanceof Keyword)) {
				continue;
			}
			String text = leaf.getText();
			BracePair openingPair = openingPairs.get(text);
			if (openingPair != null) {
				openings.push(leaf);
				continue;
			}
			BracePair closingPair = closingPairs.get(text);
			if (closingPair == null) {
				continue;
			}
			if (openings.isEmpty()) {
				continue;
			}
			ILeafNode opening = openings.peek();
			BracePair expectedPair = openingPairs.get(opening.getText());
			if (!closingPair.equals(expectedPair)) {
				openings.clear();
				continue;
			}
			openings.pop();

			int offset = opening.getOffset();
			int length = leaf.getEndOffset() - offset;
			acceptor.accept(offset, length);
		}
	}

}
