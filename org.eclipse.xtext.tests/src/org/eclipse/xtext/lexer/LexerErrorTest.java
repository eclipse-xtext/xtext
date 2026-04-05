/*******************************************************************************
 * Copyright (c) 2008 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/
package org.eclipse.xtext.lexer;

import java.util.List;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.Token;
import org.eclipse.xtext.testlanguages.parser.antlr.internal.InternalSimpleExpressionsTestLanguageLexer;
import org.eclipse.xtext.testlanguages.parser.antlr.internal.InternalSimpleExpressionsTestLanguageParser;
import org.junit.Assert;
import org.junit.Test;

public class LexerErrorTest extends Assert {

	@Test public void testLexerError_01() throws Exception {
        String model = "a /* incomplete comment *";
        InternalSimpleExpressionsTestLanguageLexer lexer = new InternalSimpleExpressionsTestLanguageLexer();
        lexer.setCharStream(new ANTLRStringStream(model));
        CommonTokenStream stream = new CommonTokenStream(lexer);
		List<? extends Token> tokens = stream.getTokens();
        assertEquals(tokens.toString(), 3, tokens.size());
        assertEquals("a", tokens.get(0).getText());
        assertEquals(" ", tokens.get(1).getText());
        assertEquals("/* incomplete comment *", tokens.get(2).getText());
        assertEquals(0, tokens.get(2).getType());
    }

	@Test public void testLexerError_02() throws Exception {
		String model = "a 'incomplete string";
		InternalSimpleExpressionsTestLanguageLexer lexer = new InternalSimpleExpressionsTestLanguageLexer();
		lexer.setCharStream(new ANTLRStringStream(model));
		CommonTokenStream stream = new CommonTokenStream(lexer);
		List<? extends Token> tokens = stream.getTokens();
        assertEquals(tokens.toString(), 3, tokens.size());
        assertEquals("a", tokens.get(0).getText());
        assertEquals(" ", tokens.get(1).getText());
        assertEquals("'incomplete string", tokens.get(2).getText());
        assertEquals(0, tokens.get(2).getType());
	}

	@Test public void testLexerError_03() throws Exception {
		String model = "a '\\ incomplete string with bad escape sequence";
		InternalSimpleExpressionsTestLanguageLexer lexer = new InternalSimpleExpressionsTestLanguageLexer();
		lexer.setCharStream(new ANTLRStringStream(model));
		CommonTokenStream stream = new CommonTokenStream(lexer);
		List<? extends Token> tokens = stream.getTokens();
        assertEquals(tokens.toString(), 3, tokens.size());
        assertEquals("a", tokens.get(0).getText());
        assertEquals(" ", tokens.get(1).getText());
        assertEquals("'\\ incomplete string with bad escape sequence", tokens.get(2).getText());
        assertEquals(0, tokens.get(2).getType());
	}

	@Test public void testLexerError_04() throws Exception {
		String model = "a 'incomplete string with bad escape sequence \\";
		InternalSimpleExpressionsTestLanguageLexer lexer = new InternalSimpleExpressionsTestLanguageLexer();
		lexer.setCharStream(new ANTLRStringStream(model));
		CommonTokenStream stream = new CommonTokenStream(lexer);
		List<? extends Token> tokens = stream.getTokens();
		assertEquals(tokens.toString(), 3, tokens.size());
		assertEquals("a", tokens.get(0).getText());
		assertEquals(" ", tokens.get(1).getText());
		assertEquals("'incomplete string with bad escape sequence \\", tokens.get(2).getText());
		assertEquals(0, tokens.get(2).getType());
	}

	@Test public void testLexerError_05() throws Exception {
		String model = "a 'incomplete string \\'";
		InternalSimpleExpressionsTestLanguageLexer lexer = new InternalSimpleExpressionsTestLanguageLexer();
		lexer.setCharStream(new ANTLRStringStream(model));
		CommonTokenStream stream = new CommonTokenStream(lexer);
		List<? extends Token> tokens = stream.getTokens();
		assertEquals(tokens.toString(), 3, tokens.size());
		assertEquals("a", tokens.get(0).getText());
		assertEquals(" ", tokens.get(1).getText());
		assertEquals("'incomplete string \\'", tokens.get(2).getText());
		assertEquals(0, tokens.get(2).getType());
	}

	@Test public void testLexerError_06() throws Exception {
		String model = "a '";
		InternalSimpleExpressionsTestLanguageLexer lexer = new InternalSimpleExpressionsTestLanguageLexer();
		lexer.setCharStream(new ANTLRStringStream(model));
		CommonTokenStream stream = new CommonTokenStream(lexer);
		List<? extends Token> tokens = stream.getTokens();
		assertEquals(tokens.toString(), 3, tokens.size());
		assertEquals("a", tokens.get(0).getText());
		assertEquals(" ", tokens.get(1).getText());
		assertEquals("'", tokens.get(2).getText());
		assertEquals(InternalSimpleExpressionsTestLanguageParser.RULE_ANY_OTHER, tokens.get(2).getType());
	}
}
