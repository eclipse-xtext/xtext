/*******************************************************************************
 * Copyright (c) 2008-2020 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/
package org.eclipse.xtext.parser.antlr;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenSource;

/**
 * A token stream that is aware of the current lookahead.
 * 
 * @author Jan Köhnlein - Initial contribution and API
 * @author Sebastian Zarnekow - Support for dynamic hidden tokens, reworked lookahead algorithm
 */
public class XtextTokenStream extends CommonTokenStream {

	/**
	 * The lookAhead marks a position relative to the current position
	 * in the token list. The lookahead is sensitive to hidden tokens
	 * e.g. a lookahead of 1 may point 10 tokens into the future if there are 9 hidden tokens before the next 
	 * non-hidden token.
	 */ 
	private int currentLookAhead;
	
	/** The maximum, absolute position p that was considered by the parser so far. */
	private int indexOfLookAhead;
	
	/**
	 * markerCount indicates the number of markers that are currently set on this stream.
	 * If it is greater than 0, the parser is predicting, otherwise it is consuming.
	 */ 
	private int markerCount = 0;
	
	/**
	 * The position of the firts marker that was set, e.g. where the parser started its prediction.
	 * {@code -1} indicates that no marker is set right now. 
	 */
	@Deprecated
	private int firstMarker = -1;
	
	private Map<String, Integer> rulenameToTokenType;
	
	private BitSet hiddenTokens;

	public XtextTokenStream() {
		super();
		tokens = new TokenList(500);
	}

	public XtextTokenStream(TokenSource tokenSource, int channel) {
		super(tokenSource, channel);
		tokens = new TokenList(500);
	}

	public XtextTokenStream(TokenSource tokenSource, ITokenDefProvider tokenDefProvider) {
		super(tokenSource);
		tokens = new TokenList(500);
		rulenameToTokenType = new HashMap<String, Integer>(tokenDefProvider.getTokenDefMap().size());
		for(Map.Entry<Integer, String> entry: tokenDefProvider.getTokenDefMap().entrySet()) {
			rulenameToTokenType.put(entry.getValue(), entry.getKey());
		}
	}
	
	@Override
	public String toString(int start, int stop) {
		if ( start<0 || stop<0 ) {
			return null;
		}
		if ( p == -1 ) {
			fill();
		}
		if ( stop>=tokens.size() ) {
			stop = tokens.size()-1;
		}
		if (tokenSource instanceof Lexer) {
			Token startToken = (Token) tokens.get(start);
			Token stopToken = (Token) tokens.get(stop);
			if (startToken instanceof CommonToken && stopToken instanceof CommonToken) {
				CommonToken commonStart = (CommonToken) startToken;
				CommonToken commonStop = (CommonToken) stopToken;
				CharStream charStream = ((Lexer) tokenSource).getCharStream();
				String result = charStream.substring(commonStart.getStartIndex(), commonStop.getStopIndex());
				return result;
			}
		}
		// fall back to super implementation but use StringBuilder instead of StringBuffer
		// and use reasonable initialization size
 		StringBuilder result = new StringBuilder(Math.max(1024, tokens.size() * 6));
		for (int i = start; i <= stop; i++) {
			Token t = (Token)tokens.get(i);
			result.append(t.getText());
		}
		return result.toString();
	}
	
	@SuppressWarnings({ "serial" })
	private final class TokenList extends ArrayList<Token> {
		private TokenList(int initialCapacity) {
			super(initialCapacity);
		}

		@Override
		public Token get(int index) {
			Token tok = (Token) super.get(index);
			// adjust only tokens in the 'future', as we wont change the channel of previously parsed
			// tokens
			int current = p;
			if (firstMarker != -1)
				current = firstMarker;
			if (hiddenTokens != null && tok.getType() >= Token.MIN_TOKEN_TYPE && index >= current)
				tok.setChannel(hiddenTokens.get(tok.getType()) ? Token.HIDDEN_CHANNEL : Token.DEFAULT_CHANNEL);
			return tok;
		}
		
		@Override
		public void clear() {
			if (isEmpty())
				return;
			super.clear();
		}
	}

	public interface HiddenTokens {
		void restore();
	}
	
	public HiddenTokens setHiddenTokens(String... lexerRules) {
		skipHiddenTokens();
		HiddenTokens result = new MyHiddenTokens(hiddenTokens);
		doSetHiddenTokens(lexerRules);
		return result;
	}
	
	public void setInitialHiddenTokens(String... lexerRules) {
		doSetHiddenTokens(lexerRules);
	}

	protected void doSetHiddenTokens(String... lexerRules) {
		BitSet newHiddens = new BitSet();
		for(String lexerRule: lexerRules) {
			Integer idxObj = rulenameToTokenType.get(lexerRule);
			if (idxObj != null) {
				newHiddens.set(idxObj.intValue());
			} else {
				throw new IllegalStateException("unknown rule: " + lexerRule + 
						" - the rule seems to be hidden by a keyword.");
			}
		}
		hiddenTokens = newHiddens;
	}

	protected void skipHiddenTokens() {
		if (hiddenTokens == null || hiddenTokens.isEmpty())
			return;
		Token token = LT(1);
		while(token.getChannel() == Token.HIDDEN_CHANNEL) {
			p++;
			token = LT(1);
		}
	}
	
	private class MyHiddenTokens implements HiddenTokens {

		private final BitSet prev;
		
		private MyHiddenTokens(BitSet prev) {
			this. prev = prev;
		}
		@Override
		public void restore() {
			skipHiddenTokens();
			XtextTokenStream.this.hiddenTokens = prev;
		}
		
	}
	
	@Override
	public int LA(int i) {
		Token lookaheadToken = LT(i);
		if (markerCount > 0) { // predicting with a marker
			int laTokenIndex = getTokenIndex(lookaheadToken);
			if (indexOfLookAhead < laTokenIndex) {
				indexOfLookAhead = laTokenIndex;
				currentLookAhead++;
			}
		} else if (i > currentLookAhead) {
			currentLookAhead = i;
		}
		return lookaheadToken.getType();
	}
	
	/**
	 * @since 2.22
	 */
	protected int getTokenIndex(Token tok) {
		if (tok.getType() == Token.EOF) {
			return size();
		}
		return tok.getTokenIndex();
	}
	
	@Override
	public void consume() {
		if (markerCount > 0) { // predicting
			basicConsume();
		} else { // producing
			basicConsume();
			currentLookAhead--;
		}
	}

	/**
	 * Make super-impl of {@link #consume()} accessible to subtypes.
	 * 
	 * @since 2.22
	 */
	protected void basicConsume() {
		super.consume();
	}
	
	/**
	 * @since 2.1
	 */
	protected int getFirstMarker() {
		return firstMarker;
	}
	
	@Override
	public int mark() {
		int result = basicMark();
		if (markerCount == 0) {
			if (indexOfLookAhead < p) {
				indexOfLookAhead = p;
			}
			firstMarker = result;
		}
		markerCount++;
		return result;
	}

	/**
	 * Make super-impl of {@link #mark()} accessible to subtypes.
	 * 
	 * @since 2.22
	 */
	protected int basicMark() {
		return super.mark();
	}

	@Override
	public void rewind(int marker) {
		markerCount--;
		if (markerCount == 0) {
			firstMarker = -1;
		}
		basicRewind(marker);
	}

	/**
	 * Make super-impl of {@link #rewind()} accessible to subtypes.
	 * 
	 * @since 2.22
	 */
	protected void basicRewind(int marker) {
		super.rewind(marker);
	}

	/**
	 * Same as {@link CommonTokenStream#LT(int)} except that we skip
	 * hidden tokens even for <code>k == 1<code>.
	 */
	@Override
	public Token LT(int k) {
		Token result = super.LT(k);
        if (k == 1 && result.getChannel()!=channel) {
        	// copied from super.LT(k) except from the last assignment to p
        	int k_ = k + 1;
        	if ( (p+k_-1) >= tokens.size() ) {
    			return new CommonToken(Token.EOF);
    		}
    		int i = p;
    		int n = 1;
    		// find k good tokens
    		while ( n<k_ ) {
    			// skip off-channel tokens
    			i = skipOffTokenChannels(i+1); // leave p to a valid pointer
    										   // SZ: prev. comment from superclass
    										   // is irritating because p is not set
    										   // in skipOffTokenChannels(...)
    			n++;
    		}
    		if ( i>=tokens.size() ) {
    			return new CommonToken(Token.EOF);
    		}
    		p = i; // adjust p to the valid pointer
            result = (Token)tokens.get(i);
        }
        return result;
	}

	public int getCurrentLookAhead() {
		return currentLookAhead;  
	}
	
	public void initCurrentLookAhead(int currentLookAhead) {
		this.currentLookAhead = currentLookAhead;
	}

	public String getLexerErrorMessage(Token invalidToken) {
		if (tokenSource instanceof org.eclipse.xtext.parser.antlr.Lexer) {
			return ((org.eclipse.xtext.parser.antlr.Lexer) tokenSource).getErrorMessage(invalidToken);
		}
		return (invalidToken.getType() == Token.INVALID_TOKEN_TYPE) ? "Invalid token " + invalidToken.getText() : null;
	}

}
