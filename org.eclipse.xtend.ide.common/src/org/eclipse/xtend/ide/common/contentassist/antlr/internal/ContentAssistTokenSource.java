/*******************************************************************************
 * Copyright (c) 2014, 2016 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtend.ide.common.contentassist.antlr.internal;

import java.io.IOException;
import java.io.Reader;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenSource;
import org.eclipse.xtend.core.parser.InternalFlexer;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
public class ContentAssistTokenSource implements TokenSource {

	private final InternalFlexer flexer;
	private int offset;

	protected ContentAssistTokenSource(InternalFlexer flexer) {
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
			CommonToken result = new CommonTokenWithoutText(type, Token.DEFAULT_CHANNEL, offset, length);
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
	
	public static class CommonTokenWithoutText extends CommonToken {

		private static final long serialVersionUID = 1L;

		public CommonTokenWithoutText(int type, int defaultChannel, int offset, int length) {
			super(null, type, defaultChannel, offset, offset + length - 1);
		}
		
	}

}
