/*******************************************************************************
 * Copyright (c) 2012 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtend.core.tests.parsing;

import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Lists.*;

import java.io.StringReader;
import java.util.List;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.eclipse.xtend.core.parser.antlr.internal.FlexTokenSource;
import org.eclipse.xtend.core.parser.antlr.internal.FlexerFactory;
import org.eclipse.xtend.core.tests.AbstractXtendTestCase;
import org.eclipse.xtext.parser.impl.TokenRegionProvider;
import org.eclipse.xtext.util.ITextRegion;
import org.eclipse.xtext.util.TextRegion;
import org.junit.Test;

import com.google.common.base.Predicate;
import com.google.inject.Inject;

/**
 * @author Jan Koehnlein - Initial contribution and API
 */
public class FlexTokenRegionProviderTest extends AbstractXtendTestCase {

	@Inject
	private TokenRegionProvider tokenRegionProvider;
	
	@Inject
	private FlexerFactory flexerFactory;
	
	@Test 
	public void testTokenRegionContainsRegion() throws Exception {
		String modelAsString = "a1 / /* comment */ b2";
		List<CommonToken> tokens = getTokens(modelAsString);
		for(int length=0; length < modelAsString.length(); ++length) {
			for(int offset = 0; offset + length < modelAsString.length(); ++offset) {
				ITextRegion tokenRegion = tokenRegionProvider.getTokenRegion(modelAsString, new TextRegion(offset, length));
//				System.out.println(offset + ":" + length + " -> " + tokenRegion);
				CommonToken firstToken = findTokenStartingAt(tokenRegion.getOffset(), tokens);
				assertTrue(firstToken.getStartIndex() <= offset);
				if(tokenRegion.getLength() != 0) {
					CommonToken lastToken = findTokenStopingAt(tokenRegion.getOffset() + tokenRegion.getLength()-1, tokens);
					assertTrue(lastToken.getStopIndex() >= offset + length -1);
				}
			}
		}
	}
	
	@Test 
	public void testTokenMerge() throws Exception {
		String model = "  ";
		ITextRegion tokenRegion = tokenRegionProvider.getTokenRegion(model, new TextRegion(1, 0));
		assertEquals(0, tokenRegion.getOffset());
		assertEquals(2, tokenRegion.getLength());
	}
	
	@Test 
	public void testTokenMerge_1() throws Exception {
		String model = " ab ";
		ITextRegion tokenRegion = tokenRegionProvider.getTokenRegion(model, new TextRegion(2, 0));
		assertEquals(1, tokenRegion.getOffset());
		assertEquals(2, tokenRegion.getLength());
	}
	
	@Test 
	public void testTokenSplit() throws Exception {
		String model = " a b ";
		ITextRegion tokenRegion = tokenRegionProvider.getTokenRegion(model, new TextRegion(2, 1));
		assertEquals(2, tokenRegion.getOffset());
		assertEquals(1, tokenRegion.getLength());
	}
	
	@Test 
	public void testTokenSplit_1() throws Exception {
		String model = " axb ";
		ITextRegion tokenRegion = tokenRegionProvider.getTokenRegion(model, new TextRegion(2, 1));
		assertEquals(1, tokenRegion.getOffset());
		assertEquals(3, tokenRegion.getLength());
	}
	
	protected CommonToken findTokenStartingAt(final int offset, List<CommonToken> tokens) {
		return find(tokens, new Predicate<CommonToken>() {
			@Override
			public boolean apply(CommonToken token) {
				return token.getStartIndex() == offset;
			}
		});
	}

	protected CommonToken findTokenStopingAt(final int offset, List<CommonToken> tokens) throws Exception {
		try {
			return find(tokens, new Predicate<CommonToken>() {
				@Override
				public boolean apply(CommonToken token) {
					return token.getStopIndex() == offset;
				}
			});
		} catch(Exception exc) {
			fail("No token ending at " + offset);
			throw exc;
		}
	}

	protected List<CommonToken> getTokens(String model) {
		FlexTokenSource tokenSource = flexerFactory.createTokenSource(new StringReader(model));
		List<CommonToken> tokens = newArrayList();
		CommonToken currentToken;
		do {
			currentToken = (CommonToken) tokenSource.nextToken();
			tokens.add(currentToken);
		} while (currentToken.getType() != Token.EOF);
		return tokens;
	}
}
