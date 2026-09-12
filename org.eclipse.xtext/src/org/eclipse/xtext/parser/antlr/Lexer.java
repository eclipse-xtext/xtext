/*******************************************************************************
 * Copyright (c) 2008 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/
package org.eclipse.xtext.parser.antlr;

import java.util.HashMap;
import java.util.Map;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.FailedPredicateException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.Token;
import org.apache.log4j.Logger;

/**
 * Hack: As AntLR does not allow to define the superclass of the generated
 * lexer, we call this class Lexer and import it to be used instead of the
 * original lexer class.
 * 
 * @author Jan Köhnlein - Initial contribution and API
 * 
 */
public abstract class Lexer extends org.antlr.runtime.Lexer {

	private static final Logger logger = Logger.getLogger(Lexer.class);
	
	public Lexer() {
		super();
	}

	public Lexer(CharStream input) {
		super(input);
	}
	
	public Lexer(CharStream input, RecognizerSharedState state) {
        super(input,state);
    }

	private final Map<Token, String> tokenErrorMap = new HashMap<Token, String>();

	@Override
	public Token nextToken() {
		while (true) {
			this.state.token = null;
			this.state.channel = Token.DEFAULT_CHANNEL;
			this.state.tokenStartCharIndex = input.index();
			this.state.tokenStartCharPositionInLine = input.getCharPositionInLine();
			this.state.tokenStartLine = input.getLine();
			this.state.text = null;
			if (input.LA(1) == CharStream.EOF) {
				return new CommonToken(Token.EOF);
			}
			try {
				mTokens();
				if (this.state.token == null) {
					emit();
				}
				else if (this.state.token == Token.SKIP_TOKEN) {
					continue;
				}
				return this.state.token;
			}
			catch (RecognitionException re) {
				reportError(re);
				if (re instanceof NoViableAltException ||
					re instanceof FailedPredicateException) {
					recover(re);
				}
				// create token that holds mismatched char
				Token t = new CommonToken(input, Token.INVALID_TOKEN_TYPE, Token.HIDDEN_CHANNEL,
						this.state.tokenStartCharIndex, getCharIndex() - 1);
				t.setLine(this.state.tokenStartLine);
				t.setCharPositionInLine(this.state.tokenStartCharPositionInLine);
				tokenErrorMap.put(t, getErrorMessage(re, this.getTokenNames()));
				emit(t);
				return this.state.token;
			}
		}
	}

	public String getErrorMessage(Token t) {
		return tokenErrorMap.get(t);
	}
	
	@Override
	public void emitErrorMessage(String msg) {
		// don't call super, since it would do a plain vanilla
		// System.err.println(msg);
		if (logger.isTraceEnabled())
			logger.trace(msg);
	}
	
}
