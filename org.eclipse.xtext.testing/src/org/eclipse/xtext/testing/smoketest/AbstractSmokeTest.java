/*******************************************************************************
 * Copyright (c) 2011 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.testing.smoketest;

import java.util.List;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.eclipse.xtext.linking.lazy.LazyLinkingResource;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.parser.antlr.Lexer;
import org.eclipse.xtext.util.ReplaceRegion;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Base class for smoke tests.
 *
 * Clients may want to use the {@link XtextSmokeTestRunner} instead.
 *
 * @author Sebastian Zarnekow - Initial contribution and API
 * @author Sven Efftinge
 */
public abstract class AbstractSmokeTest {

	@Inject
	Provider<Lexer> lexerProvider;

	protected abstract Iterable<String> getSmokeTestModels();

	protected abstract void processModel(String model) throws Exception;

	protected abstract void processModelWithoutResourceSet(String model) throws Exception;

	protected abstract LazyLinkingResource createResource(String string);

	@Test
	public void testSkipLastCharacters() throws Exception {
		for (String string : getSmokeTestModels()) {
			for (int i = 0; i < string.length(); i++) {
				logProgress(i);
				processModel(string.substring(0, i));
			}
		}
	}

	@Test
	public void testSkipFirstCharacters() throws Exception {
		for (String string : getSmokeTestModels()) {
			for (int i = 0; i < string.length(); i++) {
				logProgress(i);
				processModel(string.substring(i));
			}
		}
	}

	@Test
	public void testSkipCharacterInBetween() throws Exception {
		for (String string : getSmokeTestModels()) {
			for (int i = 0; i < string.length() - 1; i++) {
				logProgress(i);
				processModel(string.substring(0, i) + string.substring(i + 1));
			}
		}
	}

	@Test
	public void testSkipTokensInBetween() throws Exception {
		for (String string : getSmokeTestModels()) {
			List<CommonToken> tokenList = Lists.newArrayList();
			{
				Lexer lexer = lexerProvider.get();
				lexer.setCharStream(new ANTLRStringStream(string));
				Token token = lexer.nextToken();
				while (token.getType() != Token.EOF) {
					tokenList.add((CommonToken) token);
					token = lexer.nextToken();
				}
			}
			for (CommonToken token : tokenList) {
				int start = token.getStartIndex();
				int length = token.getText().length();
				logProgress(token);
				processModel(string.substring(0, start) + string.substring(start + length));
			}
		}
	}

	@Test
	public void testSkipNodesInBetween() throws Exception {
		for (String string : getSmokeTestModels()) {
			LazyLinkingResource resource = createResource(string);
			if (resource != null) {
				ICompositeNode rootNode = resource.getParseResult().getRootNode();
				ReplaceRegion replaceRegion = null;
				for (INode node : rootNode.getAsTreeIterable()) {
					int offset = node.getTotalOffset();
					int length = node.getTotalLength();
					if (replaceRegion == null || replaceRegion.getOffset() != offset
							|| replaceRegion.getLength() != length) {
						replaceRegion = new ReplaceRegion(offset, length, "");
						StringBuilder builder = new StringBuilder(string);
						replaceRegion.applyTo(builder);
						processModel(builder.toString());
					}
				}
			}
		}
	}

	@Test
	public void testSkipLastCharactersWithoutResourceSet() throws Exception {
		for (String string : getSmokeTestModels()) {
			for (int i = 0; i < string.length(); i++) {
				logProgress(i);
				processModelWithoutResourceSet(string.substring(0, i));
			}
		}
	}

	@Test
	public void testSkipFirstCharactersWithoutResourceSet() throws Exception {
		for (String string : getSmokeTestModels()) {
			for (int i = 0; i < string.length(); i++) {
				logProgress(i);
				processModelWithoutResourceSet(string.substring(i));
			}
		}
	}

	@Test
	public void testSkipCharacterInBetweenWithoutResourceSet() throws Exception {
		for (String string : getSmokeTestModels()) {
			for (int i = 0; i < string.length() - 1; i++) {
				logProgress(i);
				processModelWithoutResourceSet(string.substring(0, i) + string.substring(i + 1));
			}
		}
	}

	@Test
	public void testSkipTokensInBetweenWithoutResourceSet() throws Exception {
		for (String string : getSmokeTestModels()) {
			List<CommonToken> tokenList = Lists.newArrayList();
			{
				Lexer lexer = lexerProvider.get();
				lexer.setCharStream(new ANTLRStringStream(string));
				Token token = lexer.nextToken();
				while (token.getType() != Token.EOF) {
					tokenList.add((CommonToken) token);
					token = lexer.nextToken();
				}
			}
			for (CommonToken token : tokenList) {
				int start = token.getStartIndex();
				int length = token.getText().length();
				logProgress(token);
				processModelWithoutResourceSet(string.substring(0, start) + string.substring(start + length));
			}
		}
	}

	@Test
	public void testSkipNodesInBetweenWithoutResourceSet() throws Exception {
		for (String string : getSmokeTestModels()) {
			LazyLinkingResource resource = createResource(string);
			if (resource != null) {
				ICompositeNode rootNode = resource.getParseResult().getRootNode();
				ReplaceRegion replaceRegion = null;
				for (INode node : rootNode.getAsTreeIterable()) {
					int offset = node.getTotalOffset();
					int length = node.getTotalLength();
					if (replaceRegion == null || replaceRegion.getOffset() != offset
							|| replaceRegion.getLength() != length) {
						replaceRegion = new ReplaceRegion(offset, length, "");
						StringBuilder builder = new StringBuilder(string);
						replaceRegion.applyTo(builder);
						processModelWithoutResourceSet(builder.toString());
					}
				}
			}
		}
	}

	protected void logProgress(Object info) {
	}
}
