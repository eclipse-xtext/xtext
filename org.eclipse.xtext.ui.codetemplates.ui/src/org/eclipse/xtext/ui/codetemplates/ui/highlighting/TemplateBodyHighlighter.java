/*******************************************************************************
 * Copyright (c) 2010 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.ui.codetemplates.ui.highlighting;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.Token;
import org.eclipse.xtext.ide.LexerIdeBindings;
import org.eclipse.xtext.parser.antlr.Lexer;
import org.eclipse.xtext.parser.antlr.TokenTool;
import org.eclipse.xtext.ui.editor.syntaxcoloring.AbstractAntlrTokenToAttributeIdMapper;
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightedPositionAcceptor;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 */
@SuppressWarnings("deprecation")
public class TemplateBodyHighlighter {

	@Inject
	@Named(LexerIdeBindings.HIGHLIGHTING)
	private Lexer lexer;

	@Inject
	private AbstractAntlrTokenToAttributeIdMapper tokenIdMapper;
	
	@Deprecated
	public void provideHighlightingFor(String body, IHighlightedPositionAcceptor acceptor) {
		doProvideHighlightingFor(body, IHighlightedPositionAcceptor.DeprecationHelper.upcast(acceptor));
	}
	
	public void provideHighlightingFor(String body, org.eclipse.xtext.ide.editor.syntaxcoloring.IHighlightedPositionAcceptor acceptor) {
		provideHighlightingFor(body, IHighlightedPositionAcceptor.DeprecationHelper.cast(acceptor));
	}
	
	protected void doProvideHighlightingFor(String body, org.eclipse.xtext.ide.editor.syntaxcoloring.IHighlightedPositionAcceptor acceptor) {
		lexer.setCharStream(new ANTLRStringStream(body));
		Token token = lexer.nextToken();
		while(token.getType() != Token.EOF) {
			String id = tokenIdMapper.getId(token.getType());
			int offset = TokenTool.getOffset(token);
			int length = TokenTool.getLength(token);
			acceptor.addPosition(offset, length, id);
			token = lexer.nextToken();
		}
	}
	
}
