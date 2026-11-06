/*******************************************************************************
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtend.core.parser.antlr.internal;

import java.io.IOException;
import java.io.Reader;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenSource;
import org.eclipse.xtend.core.parser.InternalFlexer;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
public class FlexTokenSource implements TokenSource {

	private final InternalFlexer flexer;
	private int offset;

	protected FlexTokenSource(InternalFlexer flexer) {
		this.flexer = flexer;
		offset = 0;
	}
	
	public void reset(Reader reader) {
		flexer.yyreset(reader);
		offset = 0;
	}

	@Override
	public Token nextToken() {
		try {
			int type = flexer.advance();
			if (type == Token.EOF) {
				return new CommonToken(Token.EOF);
			}
			int length = flexer.getTokenLength();
			final String tokenText = flexer.getTokenText();
			CommonToken result = new CommonTokenWithText(tokenText, type, Token.DEFAULT_CHANNEL, offset);
			offset += length;
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getSourceName() {
		return "FlexTokenSource";
	}
	
	public static class CommonTokenWithText extends CommonToken {

		private static final long serialVersionUID = 1L;

		public CommonTokenWithText(String tokenText, int type, int defaultChannel, int offset) {
			super(null, type, defaultChannel, offset, offset + tokenText.length() - 1);
			this.text = tokenText;
		}
		
	}

}
