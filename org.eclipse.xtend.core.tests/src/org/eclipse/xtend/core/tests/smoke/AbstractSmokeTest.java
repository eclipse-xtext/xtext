/*******************************************************************************
 * Copyright (c) 2011, 2016 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtend.core.tests.smoke;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtend.core.parser.antlr.internal.InternalXtendLexer;
import org.eclipse.xtend.core.tests.AbstractXtendTestCase;
import org.eclipse.xtend.core.tests.RuntimeInjectorProviderWithResourceUpdates;
import org.eclipse.xtext.common.types.access.IJvmTypeProvider;
import org.eclipse.xtext.linking.lazy.LazyLinkingResource;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.parser.antlr.Lexer;
import org.eclipse.xtext.resource.IResourceFactory;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.Files;
import org.eclipse.xtext.util.ReplaceRegion;
import org.eclipse.xtext.util.StringInputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
@InjectWith(RuntimeInjectorProviderWithResourceUpdates.class)
public abstract class AbstractSmokeTest extends AbstractXtendTestCase {

	private static final Logger logger = Logger.getLogger(AbstractSmokeTest.class);
	
	protected List<String> smokeTestModels;
	
	@Inject
	protected IJvmTypeProvider.Factory typeProviderFactory;
	
	@Inject
	protected IResourceFactory resourceFactory;

	private Provider<Lexer> lexerProvider = new Provider<Lexer>() {
		@Override
		public Lexer get() {
			return new InternalXtendLexer(null);
		}
	};
	
	@Before
	public void setUp() throws Exception {
		int i = 0; // TODO sz: investigate how to speed things up here (parallelism)?
		smokeTestModels = Lists.newArrayList();
		while (true) {
			String location = SmokeTest.class.getPackage().getName().replace('.', '/') + "/Case_" + i + ".xtend.smoke";
			URL resource = getClass().getClassLoader().getResource(location);
			if (resource == null)
				resource = getClass().getClassLoader().getResource("/" + location);
			if (resource == null)
				break;
			try (InputStream resourceAsStream = resource.openStream()) {
				String string = Files.readStreamIntoString(resourceAsStream);
				smokeTestModels.add(string);
				i++;
			}
		}
	}
	
	@After
	public void tearDown() throws Exception {
		smokeTestModels = null;
	}
	
	protected void doParseAndCheckForSmoke(final String model) throws Exception {
		try {
			LazyLinkingResource resource = createResource(model);
			checkForSmoke(model, resource);
		} catch (Throwable e) {
			e.printStackTrace();
			assertEquals(e.getMessage()+" : Model was : \n\n"+model, model, "");
			// just to make sure we fail for empty model, too
			fail(e.getMessage()+" : Model was : \n\n"+model);
		}
	}
	
	protected void doParseAndCheckForSmokeWithoutResourceSet(final String model) throws Exception {
		try {
			LazyLinkingResource resource = createResource(model);
			// simulate closed editor
			ResourceSet resourceSet = resource.getResourceSet();
			resourceSet.eSetDeliver(false);
			resourceSet.getResources().clear();
			resourceSet.eAdapters().clear();
			checkForSmoke(model, resource);
		} catch (Throwable e) {
			e.printStackTrace();
			assertEquals(e.getMessage()+" : Model was : \n\n"+model, model, "");
			// just to make sure we fail for empty model, too
			fail(e.getMessage()+" : Model was : \n\n"+model);
		}
	}
	
	protected abstract void checkForSmoke(final String model, LazyLinkingResource resource);
	
	protected LazyLinkingResource createResource(final String model) throws IOException {
		if (logger.isTraceEnabled()) {
			logger.trace("createResource: " + model);
		}
		XtextResourceSet set = getResourceSet();
		typeProviderFactory.findOrCreateTypeProvider(set);
		LazyLinkingResource resource = (LazyLinkingResource) resourceFactory.createResource(URI.createURI("Test.xtend"));
		set.getResources().add(resource);
		resource.load(new StringInputStream(model), null);
		resource.resolveLazyCrossReferences(CancelIndicator.NullImpl);
		return resource;
	}
	
	@Test public void testSkipLastCharacters() throws Exception {
		for(String string: smokeTestModels) {
			for (int i = 0; i < string.length(); i++) {
				logProgress(i);
				doParseAndCheckForSmoke(string.substring(0, i));
			}
		}
	}

	@Test public void testSkipFirstCharacters() throws Exception {
		for(String string: smokeTestModels) {
			for (int i = 0; i < string.length(); i++) {
				logProgress(i);
				doParseAndCheckForSmoke(string.substring(i));
			}
		}
	}

	@Test public void testSkipCharacterInBetween() throws Exception {
		for(String string: smokeTestModels) {
			for (int i = 0; i < string.length() - 1; i++) {
				logProgress(i);
				doParseAndCheckForSmoke(string.substring(0, i) + string.substring(i+1));
			}
		}
	}
	
	@Test public void testSkipTokensInBetween() throws Exception {
		for(String string: smokeTestModels) {
			List<CommonToken> tokenList = Lists.newArrayList();
			{
				Lexer lexer = lexerProvider.get();
				lexer.setCharStream(new ANTLRStringStream(string));
				Token token = lexer.nextToken();
				while(token.getType() != Token.EOF) {
					tokenList.add((CommonToken) token);
					token = lexer.nextToken();
				}
			}
			for(CommonToken token: tokenList) {
				int start = token.getStartIndex();
				int length = token.getText().length();
				logProgress(token);
				doParseAndCheckForSmoke(string.substring(0, start) + string.substring(start + length));
			}
		}
	}
	
	@Test public void testSkipNodesInBetween() throws Exception {
		for(String string: smokeTestModels) {
			LazyLinkingResource resource = createResource(string);
			ICompositeNode rootNode = resource.getParseResult().getRootNode();
			ReplaceRegion replaceRegion = null;
			for(INode node: rootNode.getAsTreeIterable()) {
				int offset = node.getTotalOffset();
				int length = node.getTotalLength();
				if (replaceRegion == null || replaceRegion.getOffset() != offset || replaceRegion.getLength() != length) {
					replaceRegion = new ReplaceRegion(offset, length, "");
					StringBuilder builder = new StringBuilder(string);
					replaceRegion.applyTo(builder);
					doParseAndCheckForSmoke(builder.toString());
				}
			}
		}
	}
	
	@Test public void testSkipLastCharactersWithoutResourceSet() throws Exception {
		for(String string: smokeTestModels) {
			for (int i = 0; i < string.length(); i++) {
				logProgress(i);
				doParseAndCheckForSmokeWithoutResourceSet(string.substring(0, i));
			}
		}
	}

	@Test public void testSkipFirstCharactersWithoutResourceSet() throws Exception {
		for(String string: smokeTestModels) {
			for (int i = 0; i < string.length(); i++) {
				logProgress(i);
				doParseAndCheckForSmokeWithoutResourceSet(string.substring(i));
			}
		}
	}

	@Test public void testSkipCharacterInBetweenWithoutResourceSet() throws Exception {
		for(String string: smokeTestModels) {
			for (int i = 0; i < string.length() - 1; i++) {
				logProgress(i);
				doParseAndCheckForSmokeWithoutResourceSet(string.substring(0, i) + string.substring(i+1));
			}
		}
	}
	
	@Test public void testSkipTokensInBetweenWithoutResourceSet() throws Exception {
		for(String string: smokeTestModels) {
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
			for(CommonToken token: tokenList) {
				int start = token.getStartIndex();
				int length = token.getText().length();
				logProgress(token);
				doParseAndCheckForSmokeWithoutResourceSet(string.substring(0, start) + string.substring(start + length));
			}
		}
	}
	
	@Test public void testSkipNodesInBetweenWithoutResourceSet() throws Exception {
		for(String string: smokeTestModels) {
			LazyLinkingResource resource = createResource(string);
			ICompositeNode rootNode = resource.getParseResult().getRootNode();
			ReplaceRegion replaceRegion = null;
			for(INode node: rootNode.getAsTreeIterable()) {
				int offset = node.getTotalOffset();
				int length = node.getTotalLength();
				if (replaceRegion == null || replaceRegion.getOffset() != offset || replaceRegion.getLength() != length) {
					replaceRegion = new ReplaceRegion(offset, length, "");
					StringBuilder builder = new StringBuilder(string);
					replaceRegion.applyTo(builder);
					doParseAndCheckForSmokeWithoutResourceSet(builder.toString());
				}
			}
		}
	}
	
	protected void logProgress(Object info) {
		if (logger.isDebugEnabled()) {
			Runtime runtime = Runtime.getRuntime();
			logger.debug("Step: " + info + " memory: free / total / max MB " + runtime.freeMemory() / (1000 * 1000) + " / " + runtime.totalMemory() / (1000 * 1000) + " / " + runtime.maxMemory() / (1000 * 1000));
		}
	}
}
