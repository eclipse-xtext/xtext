/*******************************************************************************
 * Copyright (c) 2014, 2017 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.ide.editor.contentassist.antlr.internal;

import org.antlr.runtime.IntStream;
import org.antlr.runtime.NoViableAltException;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.eclipse.xtext.parser.antlr.IUnorderedGroupHelper;
import org.eclipse.xtext.parser.antlr.XtextTokenStream;

/**
 * @since 2.9
 */
public class DFA extends org.antlr.runtime.DFA {
	
	@Override
	public int predict(IntStream input) throws RecognitionException {
		try {
			getRecognizer().beginDFAPrediction();
			return super.predict(input);
		} finally {
			getRecognizer().endDFAPrediction();
		}
	}
	
	protected BaseInternalContentAssistParser<?, ?> getRecognizer() {
		return (BaseInternalContentAssistParser<?, ?>) recognizer;
	}
	
	protected IUnorderedGroupHelper getUnorderedGroupHelper() {
		return getRecognizer().getUnorderedGroupHelper();
	}
	
	@Override
	protected void error(NoViableAltException nvae) {
		if (nvae.token.getType() == Token.EOF) {
			int lookAheadAddOn = getRecognizer().lookAheadAddOn;
			int lookAhead = ((XtextTokenStream)nvae.input).getCurrentLookAhead();
			if ((lookAhead >= lookAheadAddOn && lookAheadAddOn > 0) || (lookAhead == 0 && lookAheadAddOn > 0) || lookAhead == -1)
				getRecognizer().failedPredicateAtEOF = true;
		}
		super.error(nvae);
	}

}
