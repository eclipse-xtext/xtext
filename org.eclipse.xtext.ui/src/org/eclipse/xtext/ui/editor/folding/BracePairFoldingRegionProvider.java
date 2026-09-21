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
import java.util.Map;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.xtext.ide.editor.bracketmatching.BracePair;
import org.eclipse.xtext.ide.editor.bracketmatching.IBracePairProvider;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.ILeafNode;
import org.eclipse.xtext.parser.IParseResult;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.CancelIndicator;
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
		Map<String, BracePair> openingPairs = new HashMap<>();
		Map<String, BracePair> closingPairs = new HashMap<>();
		for (BracePair pair : bracePairProvider.getPairs()) {
			openingPairs.put(pair.getLeftBrace(), pair);
			closingPairs.put(pair.getRightBrace(), pair);
		}
		CancelIndicator cancelIndicator = getCancelIndicator();
		Deque<ILeafNode> openings = new ArrayDeque<>();
		for (ILeafNode leaf : root.getLeafNodes()) {
			if (cancelIndicator.isCanceled()) {
				throw new OperationCanceledException();
			}
			if (leaf.isHidden()) {
				continue;
			}
			String text = leaf.getText();
			if (openingPairs.containsKey(text)) {
				openings.push(leaf);
				continue;
			}
			BracePair closingPair = closingPairs.get(text);
			if (closingPair == null) {
				continue;
			}
			ILeafNode opening = findMatchingOpening(openings, closingPair, openingPairs);
			if (opening == null) {
				// stray closing brace: ignore it
				continue;
			}
			// discard unmatched openings nested inside the matching one
			while (openings.peek() != opening) {
				openings.pop();
			}
			openings.pop();

			int offset = opening.getOffset();
			int length = leaf.getEndOffset() - offset;
			acceptor.accept(offset, length);
		}
	}

	/**
	 * @return the innermost opening on the stack that is closed by the given pair. 'null' if there is none.
	 */
	private ILeafNode findMatchingOpening(Deque<ILeafNode> openings, BracePair closingPair, Map<String, BracePair> openingPairs) {
		for (ILeafNode opening : openings) {
			if (closingPair.equals(openingPairs.get(opening.getText()))) {
				return opening;
			}
		}
		return null;
	}

}
