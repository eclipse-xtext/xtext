/*******************************************************************************
 * Copyright (c) 2010 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.parser.antlr;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenSource;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
public class XtextTokenStreamTest extends Assert implements TokenSource {

	private int tokenCount = 0;
	
	@Test public void testConsumeUntilEOF_large() {
		tokenCount = 25000;
		XtextTokenStream stream = new XtextTokenStream(this, Token.DEFAULT_CHANNEL);
		while(stream.LA(1) != Token.EOF) {
			stream.consume();
			stream.LA(1);
		}
	}
	
	@Test public void testConsumeUntilEOF_small() {
		tokenCount = 25;
		XtextTokenStream stream = new XtextTokenStream(this, Token.DEFAULT_CHANNEL);
		while(stream.LA(1) != Token.EOF) {
			stream.consume();
			stream.LA(1);
		}
	}
	
	@Test
	public void test_smallFile_hugeLA() {
		doTestLookahead(500, 2500);
	}
	
	@Test
	public void test_avgFile_largeLA() {
		doTestLookahead(50, 25000);
	}

	@Test
	public void test_largeFile_largeLA() {
		doTestLookahead(50, 2500000);
	}
	
	@Test
	public void test_largeFile_tinyLA() {
		doTestLookahead(2, 2500000);
	}
	
	@Test
	public void test_largeFile_noLA() {
		doTestLookahead(1, 2500000);
	}
	
	protected void doTestLookahead(int lookAhead, int tokenCount) {
		this.tokenCount = tokenCount;
		XtextTokenStream stream = new XtextTokenStream(this, Token.DEFAULT_CHANNEL);
		while(stream.LA(1) != Token.EOF) {
			for(int i = 0; i < lookAhead; i++) {
				for(int j = i + 1; j < lookAhead; j++) {
					stream.LA(j - i);					
				}
				stream.consume();
			}
		}
	}

	@Override
	public Token nextToken() {
		if (tokenCount == 0)
			return new CommonToken(Token.EOF);
		return new CommonToken(tokenCount--, "Text");
	}
	
	@Override
	public String getSourceName() {
		return null;
	}
	
}
