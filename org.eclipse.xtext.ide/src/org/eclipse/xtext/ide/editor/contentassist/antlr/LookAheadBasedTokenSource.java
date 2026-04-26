/*******************************************************************************
 * Copyright (c) 2017 itemis AG (http://www.itemis.de) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.ide.editor.contentassist.antlr;

import java.util.Iterator;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenSource;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 * @since 2.14
 */
public class LookAheadBasedTokenSource implements TokenSource {
	private final Iterator<? extends ILookAheadTerminal> iter;

	protected LookAheadBasedTokenSource(Iterator<? extends ILookAheadTerminal> iter) {
		this.iter = iter;
	}

	@Override
	public Token nextToken() {
		if (iter.hasNext()) {
			ILookAheadTerminal lookAhead = iter.next();
			return lookAhead.getToken();
		}
		return new CommonToken(Token.EOF);
	}

	@Override
	public String getSourceName() {
		return "LookAheadTerminalTokenSource";
	}
}