/*******************************************************************************
 * Copyright (c) 2014, 2019 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.ide.editor.contentassist.antlr.internal;

import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.FailedPredicateException;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.RecognizerSharedState;
import org.antlr.runtime.Token;
import org.apache.log4j.Logger;

/**
 * @since 2.9
 */
public abstract class Lexer extends org.antlr.runtime.Lexer {

	private static final Logger logger = Logger.getLogger(Lexer.class);

	protected Lexer(CharStream input) {
		super(input);
	}

	protected Lexer() {
		super();
	}

	protected Lexer(CharStream input, RecognizerSharedState state) {
		super(input, state);
	}

	@Override
	public Token nextToken() {
		while (true) {
			state.token = null;
			state.channel = Token.DEFAULT_CHANNEL;
			state.tokenStartCharIndex = input.index();
			state.tokenStartCharPositionInLine = input.getCharPositionInLine();
			state.tokenStartLine = input.getLine();
			state.text = null;
			if ( input.LA(1)==CharStream.EOF ) {
				return new CommonToken(Token.EOF);
			}
			try {
				mTokens();
				if ( state.token==null ) {
					emit();
				}
				else if ( state.token==Token.SKIP_TOKEN ) {
					continue;
				}
				return state.token;
			}
			/*
			 * Avoid infinite loop (editor freeze) on {@link FailedPredicateException}
			 */
			catch (NoViableAltException | FailedPredicateException e) {
				reportError(e);
				recover(e); // throw out current char and try again
			}
			catch (RecognitionException re) {
				reportError(re);
				// match() routine has already called recover()
			}
		}
	}

	@Override
	public void emitErrorMessage(String msg) {
		// don't call super, since it would do a plain vanilla
		// System.err.println(msg);
		if (logger.isTraceEnabled())
			logger.trace(msg);
	}

}
