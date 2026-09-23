/*******************************************************************************
 * Copyright (c) 2012 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.parser.impl;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.eclipse.xtext.parser.antlr.Lexer;
import org.eclipse.xtext.parser.antlr.LexerBindings;
import org.eclipse.xtext.util.ITextRegion;
import org.eclipse.xtext.util.ReplaceRegion;
import org.eclipse.xtext.util.TextRegion;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

/**
 * Expands a region such that it contains only complete lexer tokens.
 * 
 * @author Jan Koehnlein - Initial contribution and API
 * @since 2.3
 */
public class TokenRegionProvider {

	@Inject
	@Named(LexerBindings.RUNTIME)
	private Provider<Lexer> lexerProvider;

	public ITextRegion getTokenRegion(String text, ITextRegion region) {
		Lexer lexer = lexerProvider.get();
		lexer.setCharStream(new ANTLRStringStream(text));
		int currentStart = 0;
		int currentEnd = 0;
		CommonToken nextToken = (CommonToken) lexer.nextToken();
		int regionStart = region.getOffset();
		int regionEnd = region.getEndOffset();
		while (nextToken.getType() != Token.EOF && currentEnd <= regionStart) {
			currentStart = nextToken.getStartIndex();
			currentEnd = nextToken.getStopIndex() + 1;
			nextToken = (CommonToken) lexer.nextToken();
		}
		// nextToken is either EOF or the first token that follows the start of the given region
		while (nextToken.getType() != Token.EOF && currentEnd < regionEnd) {
			currentEnd = nextToken.getStopIndex() + 1;
			nextToken = (CommonToken) lexer.nextToken();
		}
		if (currentStart != regionStart || currentEnd != regionEnd) 
			return new TextRegion(currentStart, currentEnd - currentStart);
		else 
			return region;
	}

	public ReplaceRegion getTokenReplaceRegion(String changedText, ReplaceRegion replaceRegion) {
		int lengthDelta = replaceRegion.getText().length() - replaceRegion.getLength();
		ITextRegion tokenRegion = getTokenRegion(changedText, new TextRegion(replaceRegion.getOffset(), replaceRegion
				.getText().length()));
		if (tokenRegion.getOffset() == replaceRegion.getOffset()
				&& tokenRegion.getLength() == replaceRegion.getText().length())
			return replaceRegion;
		else 
			return new ReplaceRegion(tokenRegion.getOffset(), tokenRegion.getLength() - lengthDelta,
					changedText.substring(tokenRegion.getOffset(), tokenRegion.getOffset() + tokenRegion.getLength()));
	}

}
