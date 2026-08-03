/*******************************************************************************
 * Copyright (c) 2009 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.ui.editor.contentassist.antlr;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenSource;
import org.eclipse.xtext.parser.antlr.Lexer;
import org.eclipse.xtext.parser.antlr.LexerBindings;
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext;
import org.eclipse.xtext.ui.editor.contentassist.ProposalConflictHelper;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * <p>An implementation that relies on the lexer to detect proposals that conflict
 * with the input in the document.</p>
 * <p>A proposal is considered to be conflicting if the lexer would not produce
 * two distinct tokens for the previous sibling and the proposal itself but consume
 * parts of the proposal as part of the first token.
 * Example:</p>
 * <code>
 * === Grammar:<br/>
 * 	MyParserRule: name=ID other=[MyParserRule];<br/>
 * === Input:<br/>
 *  MyId|<br/>
 * </code>
 * <p>where <code>|</code> denotes the cursor position. A valid follow
 * token for the parser is the ID of the cross reference. However, since
 * <code>MyIdSomethingElse</code> would be consumed as a single ID by the lexer,
 * the proposal <code>SomethingElse</code> is invalid.</p>
 *
 * @author Sebastian Zarnekow - Initial contribution and API
 */
public class AntlrProposalConflictHelper extends ProposalConflictHelper {

	@Inject
	@Named(LexerBindings.RUNTIME)
	private Lexer proposalLexer;

	@Inject
	@Named(LexerBindings.RUNTIME)
	private Lexer lastCompleteLexer;

	@Inject
	@Named(LexerBindings.RUNTIME)
	private Lexer combinedLexer;

	@Override
	public boolean existsConflict(String lastCompleteText, String proposal, ContentAssistContext context) {
		initTokenSources(lastCompleteText, proposal, context);
		if (!equalTokenSequence(getLastCompleteLexer(), getCombinedLexer()))
			return true;
		if (!equalTokenSequence(getProposalLexer(), getCombinedLexer()))
			return true;
		Token lastToken = getProposalLexer().nextToken();
		return lastToken.getType() != Token.EOF;
	}

	protected boolean equalTokenSequence(TokenSource first, TokenSource second) {
		Token token = null;
		while ((token = first.nextToken()).getType() != Token.EOF) {
			Token otherToken = second.nextToken();
			if (otherToken.getType() == Token.EOF) {
				return false;
			}
			if (!token.getText().equals(otherToken.getText())) {
				return false;
			}
		}
		return true;
	}

	protected void initTokenSources(String lastCompleteText, String proposal, ContentAssistContext context) {
		String combinedText = lastCompleteText.concat(proposal);
		initTokenSource(combinedText, getCombinedLexer(), context);
		initTokenSource(lastCompleteText, getLastCompleteLexer(), context);
		initTokenSource(proposal, getProposalLexer(), context);
	}

	protected void initTokenSource(String text, TokenSource tokenSource, ContentAssistContext context) {
		Lexer lexer = (Lexer) tokenSource;
		CharStream stream = new ANTLRStringStream(text);
		lexer.setCharStream(stream);
	}

	public TokenSource getProposalLexer() {
		return proposalLexer;
	}

	public void setProposalLexer(Lexer proposalLexer) {
		this.proposalLexer = proposalLexer;
	}

	public TokenSource getCombinedLexer() {
		return combinedLexer;
	}

	public void setCombinedLexer(Lexer combinedLexer) {
		this.combinedLexer = combinedLexer;
	}

	public void setLastCompleteLexer(Lexer lastCompleteLexer) {
		this.lastCompleteLexer = lastCompleteLexer;
	}

	public TokenSource getLastCompleteLexer() {
		return lastCompleteLexer;
	}

}
