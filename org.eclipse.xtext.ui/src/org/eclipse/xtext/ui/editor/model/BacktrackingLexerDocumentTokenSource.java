/*******************************************************************************
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.ui.editor.model;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenSource;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.xtext.parser.antlr.Lexer;

/**
 * The old version of the {@link DocumentTokenSource}. It assumes that all tokens before the damaged region can change
 * with a {@link DocumentEvent} therefore the entire document is being lexed.
 *
 * @author koehnlein - Initial contribution and API
 * @since 2.4
 */
public class BacktrackingLexerDocumentTokenSource extends DocumentTokenSource {

	/**
	 * @since 2.4
	 */
	@Override
	protected RepairEntryData getRepairEntryData(DocumentEvent e) throws Exception {
		int tokenStartsAt = 0;
		int tokenInfoIdx = 0;
		TokenSource source = createTokenSource(e.fDocument.get());
		CommonToken token = (CommonToken) source.nextToken();
		// find start idx
		while (true) {
			if (token.getType() == Token.EOF) {
				break;
			}
			if (tokenInfoIdx >= getInternalModifyableTokenInfos().size())
				break;
			TokenInfo tokenInfo = getInternalModifyableTokenInfos().get(tokenInfoIdx);
			if (tokenInfo.getAntlrTokenType() != token.getType()
					|| token.getStopIndex() - token.getStartIndex() + 1 != tokenInfo.getLength())
				break;
			if (tokenStartsAt + tokenInfo.getLength() > e.fOffset)
				break;
			tokenStartsAt += tokenInfo.getLength();
			tokenInfoIdx++;
			token = (CommonToken) source.nextToken();
		}
		return new RepairEntryData(tokenStartsAt, tokenInfoIdx, token, source);
	}

}
