/*******************************************************************************
 * Copyright (c) 2011 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtend.core.tests.smoke;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.emf.ecore.util.EObjectValidator;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtend.core.parser.antlr.internal.InternalXtendLexer;
import org.eclipse.xtext.common.types.JvmTypeReference;
import org.eclipse.xtext.common.types.JvmWildcardTypeReference;
import org.eclipse.xtext.diagnostics.ExceptionDiagnostic;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.linking.lazy.LazyLinkingResource;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.impl.InvariantChecker;
import org.eclipse.xtext.parser.antlr.Lexer;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.EmfFormatter;
import org.eclipse.xtext.util.IAcceptor;
import org.eclipse.xtext.util.IResourceScopeCache;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IDiagnosticConverter;
import org.eclipse.xtext.validation.Issue;
import org.eclipse.xtext.validation.ResourceValidatorImpl;
import org.eclipse.xtext.xbase.typesystem.IBatchTypeResolver;
import org.junit.ComparisonFailure;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * A brute-force test class to ensure that parsing, linking and validation 
 * don't throw exceptions on invalid models.
 * 
 * @author Sven Efftinge - Initial contribution and API
 * @author Sebastian Zarnekow
 */
@Ignore("Resource update smoke scenarios have to be migrated to new smoke test infrastructure")
public class SmokeTest extends AbstractSmokeTest {
	
	private static final Logger logger = Logger.getLogger(SmokeTest.class);

	@Inject
	private InvariantChecker invariantChecker;
	
	@Inject
	private IResourceScopeCache cache;
	
	@Inject
	private IBatchTypeResolver typeResolver;
	
	@Inject
	private Provider<ResourceValidatorImpl> resourceValidatorProvider;
	
	private Provider<Lexer> lexerProvider = new Provider<Lexer>() {
		@Override
		public Lexer get() {
			return new InternalXtendLexer(null);
		}
	};
	
	@Test public void testResourceUpdateSkipLastCharacters() throws Exception {
		for(String string: smokeTestModels) {
			LazyLinkingResource resource = createResource("");
			for (int i = 0; i < string.length(); i++) {
				logProgress(i);
				compareWithNewResource(resource, i, 0, String.valueOf(string.charAt(i)));
			}
		}
	}

	@Test public void testResourceUpdateSkipFirstCharacters() throws Exception {
		for(String string: smokeTestModels) {
			LazyLinkingResource resource = createResource("");
			for (int i = string.length() - 1; i >= 0; i--) {
				logProgress(i);
				compareWithNewResource(resource, 0, 0, String.valueOf(string.charAt(i)));
			}
		}
	}

	@Test public void testResourceUpdateSkipCharacterInBetween() throws Exception {
		for(String string: smokeTestModels) {
			LazyLinkingResource resource = createResource(string.substring(1));
			for (int i = 0; i < string.length() - 1; i++) {
				logProgress(i);
				compareWithNewResource(resource, i, 1, String.valueOf(string.charAt(i)));
			}
		}
	}
	
	@Test public void testResourceUpdateSkipTokensInBetween() throws Exception {
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
			LazyLinkingResource resource = createResource(string);
			CommonToken prev = null;
			for(CommonToken token: tokenList) {
				logProgress(token);
				if (prev == null) {
					compareWithNewResource(resource, 0, token.getText().length(), "");	
				} else {
					int offset = prev.getStartIndex();
					int length = token.getText().length();
					if (offset + length < string.length())
						compareWithNewResource(resource, offset, length, prev.getText());
				}
				prev = token;
			}
		}
	}
	
	protected void compareWithNewResource(LazyLinkingResource resource, int offset, int length, String newText) throws IOException {
		String model = resource.getParseResult().getRootNode().getText();
		try {
			long start = System.currentTimeMillis();
			long time = System.currentTimeMillis();
			if (logger.isDebugEnabled()) {
				logger.debug("Partial parsing ...");
				logger.trace("Input is >>>" + resource.getParseResult().getRootNode().getText() + "<<<");
				logger.trace("Offset: " + offset);
				logger.trace("Length: " + length);
				logger.trace("New Text: >>>" + newText + "<<<");
			}
			resource.update(offset, length, newText);
			resource.resolveLazyCrossReferences(CancelIndicator.NullImpl);
			String text = resource.getParseResult().getRootNode().getText();
			if (logger.isDebugEnabled()) {
				logger.debug("... took " + (System.currentTimeMillis() - time));
				logger.debug("Loading second resource ...");
				time = System.currentTimeMillis();
			}
			LazyLinkingResource newResource = createResource(text);
			if (logger.isDebugEnabled()) {
				logger.debug("... took " + (System.currentTimeMillis() - time));
				logger.debug("Semantic equality check ...");
				time = System.currentTimeMillis();
			}
			assertEquals(text, resource.getContents().size(), newResource.getContents().size());
			EcoreUtil.resolveAll(resource);
			EcoreUtil.resolveAll(newResource);
			for(int i = 0; i < resource.getContents().size(); i++) {
				assertEquals(text, EmfFormatter.objToStr(newResource.getContents().get(i)), EmfFormatter.objToStr(resource.getContents().get(i)));
			}
			if (logger.isDebugEnabled()) {
				logger.debug("... took " + (System.currentTimeMillis() - time));
				logger.debug("Node equality check ...");
				time = System.currentTimeMillis();
			}
			ICompositeNode rootNode = resource.getParseResult().getRootNode();
			ICompositeNode newRootNode = newResource.getParseResult().getRootNode();
			Iterator<INode> iterator = rootNode.getAsTreeIterable().iterator();
			Iterator<INode> newIterator = newRootNode.getAsTreeIterable().iterator();
			while(iterator.hasNext()) {
				assertTrue(newIterator.hasNext());
				assertEqualNodes(text, iterator.next(), newIterator.next());
			}
			assertFalse(iterator.hasNext());
			assertFalse(newIterator.hasNext());
			if (logger.isDebugEnabled()) {
				logger.debug("... took " + (System.currentTimeMillis() - time));
				logger.debug("... done - total time: " + (System.currentTimeMillis() - start));
			}
		} catch(Throwable e) {
			e.printStackTrace();
			String failureMessage = e.getMessage()+" : Model was >>>\n"	+ 
			model + 
			"\n<<<\n" +
			"offset: " + offset + "\n" +
			"length:" + length + "\n" +
			"newText: >>>" + newText + "<<<";
			assertEquals(failureMessage, model, "");
			// just to make sure we fail for empty model, too
			fail(failureMessage);
		}
	}

	protected void assertEqualNodes(String text, INode node, INode other) {
		assertEquals(text, node.getClass(), other.getClass());
		assertEquals(text, node.getTotalOffset(), other.getTotalOffset());
		assertEquals(text, node.getTotalLength(), other.getTotalLength());
		assertEquals(text, node.getGrammarElement(), other.getGrammarElement());
		assertEquals(text, node.hasDirectSemanticElement(), other.hasDirectSemanticElement());
		assertEquals(text, node.getSyntaxErrorMessage(), other.getSyntaxErrorMessage());
		if (node instanceof ICompositeNode) {
			assertEquals(text, ((ICompositeNode)node).getLookAhead(), ((ICompositeNode) other).getLookAhead());
		}
	}

	@Override
	protected void checkForSmoke(final String model, LazyLinkingResource resource) {
		checkNodeModelInvariant(resource);
		cache.clear(resource);
		checkNoErrorsInTypeProvider(resource);
		cache.clear(resource);
		checkNoErrorsInValidator(model, resource);
	}

	protected void checkNoErrorsInTypeProvider(LazyLinkingResource resource) {
		typeResolver.resolveTypes(resource);
		
		Iterator<Object> contents = EcoreUtil.getAllContents(resource, true);
		while(contents.hasNext()) {
			Object object = contents.next();
			if (object instanceof JvmWildcardTypeReference) {
				assertTrue(((JvmWildcardTypeReference) object).eContainer() instanceof JvmTypeReference);
			}
		}
	}

	protected void checkNoErrorsInValidator(final String model, LazyLinkingResource resource) {
		ResourceValidatorImpl validator = resourceValidatorProvider.get();
		assertNotSame(validator, resource.getResourceServiceProvider().getResourceValidator());
		validator.setDiagnosticConverter(new IDiagnosticConverter() {
			@Override
			public void convertValidatorDiagnostic(org.eclipse.emf.common.util.Diagnostic diagnostic, IAcceptor<Issue> acceptor) {
				if (diagnostic instanceof BasicDiagnostic) {
					List<?> data = diagnostic.getData();
					if (!data.isEmpty() && data.get(0) instanceof Throwable) {
						Throwable t = (Throwable) data.get(0);
						throwError(t);
					}
					if (EObjectValidator.DIAGNOSTIC_SOURCE.equals(diagnostic.getSource()) && diagnostic.getCode() == EObjectValidator.EOBJECT__EVERY_REFERENCE_IS_CONTAINED) {
						throwError(new RuntimeException("Dangling reference found."));
					}
				}
			}
			
			private void throwError(Throwable e) {
				ComparisonFailure result = new ComparisonFailure(e.getMessage(), model, "");
				result.setStackTrace(e.getStackTrace());
				throw result;
			}
			
			@Override
			public void convertResourceDiagnostic(Diagnostic diagnostic, Severity severity, IAcceptor<Issue> acceptor) {
				if (diagnostic instanceof ExceptionDiagnostic) {
					Exception e = ((ExceptionDiagnostic) diagnostic).getException();
					throwError(e);
				}
			}
		});
		validator.validate(resource, CheckMode.ALL, CancelIndicator.NullImpl);
	}

	protected void checkNodeModelInvariant(LazyLinkingResource resource) {
		invariantChecker.checkInvariant(resource.getParseResult().getRootNode());
	}

}
