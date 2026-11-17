/**
 * Copyright (c) 2019, 2020 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.ui.tests.xmleditor;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.Lexer;
import org.antlr.runtime.Token;
import org.eclipse.xtext.parser.antlr.IAntlrTokenFileProvider;
import org.junit.Assert;

import com.google.common.base.Joiner;
import com.google.common.io.CharStreams;
import com.google.inject.Inject;

/**
 * The implementation of the following helper methods is mainly taken from the org.eclipse.xpect.tests.LexerTest class of the Eclipse Xpect
 * project.
 */
public abstract class AbstractLexerTest {
	@Inject
	private IAntlrTokenFileProvider antlrTokenFileProvider;

	protected void assertLexing(CharSequence modelAsText, CharSequence expected) throws Exception {
		String actual = lex(lexer(), antlrTokenFileProvider.getAntlrTokenFile(), modelAsText.toString().trim());
		Assert.assertEquals(expected.toString().trim(), actual.toString().trim());
	}

	protected abstract Lexer lexer();

	protected String lex(Lexer lexer, InputStream tokensStream, CharSequence text) throws Exception {
		InputStreamReader _inputStreamReader = new InputStreamReader(tokensStream);
		LinkedHashMap<Integer, String> tokenNames = tokenNames(_inputStreamReader);
		return lexerResult(lexer, tokenNames, text);
	}

	private String lexerResult(Lexer lexer, Map<Integer, String> tokenNames, CharSequence text) {
		lexer.setCharStream(new ANTLRStringStream(text.toString()));
		List<String> result = new ArrayList<>();
		while (true) {
			Token token = lexer.nextToken();
			if (token.getType() == Token.EOF) {
				return Joiner.on(System.lineSeparator()).join(result);
			}
			Object nameOrType = null;
			String tokenName = tokenNames.get(token.getType());
			if (tokenName != null) {
				nameOrType = tokenName;
			} else {
				nameOrType = Integer.valueOf(token.getType());
			}
			result.add(nameOrType + " \'" + token.getText() + "\'");
		}
	}

	private LinkedHashMap<Integer, String> tokenNames(Reader tokensReader) throws Exception {
		LinkedHashMap<Integer, String> result = new LinkedHashMap<>();
		List<String> lines = CharStreams.readLines(tokensReader);
		for (String line : lines) {
			String[] s = line.split("=");
			String name = s[0];
			int index = Integer.parseInt(s[1]);
			String nameOrKEYWORD = null;
			if (name.startsWith("KEYWORD")) {
				nameOrKEYWORD = "KEYWORD";
			} else {
				nameOrKEYWORD = name;
			}
			result.put(index, nameOrKEYWORD);
		}
		return result;
	}
}
