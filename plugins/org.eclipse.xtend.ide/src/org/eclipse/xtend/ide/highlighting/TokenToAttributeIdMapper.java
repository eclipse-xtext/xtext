/*******************************************************************************
 * Copyright (c) 2011 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtend.ide.highlighting;

import org.eclipse.xtend.ide.editor.model.XtendDocumentTokenSource;
import org.eclipse.xtext.ide.editor.syntaxcoloring.HighlightingStyles;
import org.eclipse.xtext.ui.editor.syntaxcoloring.DefaultAntlrTokenToAttributeIdMapper;

import com.google.inject.Singleton;

/**
 * @author Sebastian Zarnekow - Initial contribution and API
 * @author Holger Schill
 */
@Singleton
public class TokenToAttributeIdMapper extends DefaultAntlrTokenToAttributeIdMapper {

	@Override
	protected String getMappedValue(int tokenType) {
		if(tokenType == XtendDocumentTokenSource.JAVA_DOC_TOKEN_TYPE){
			return HighlightingStyles.COMMENT_ID;
		}
		return super.getMappedValue(tokenType);
	}

	@Override
	protected String calculateId(String tokenName, int tokenType) {
		if(
			"RULE_RICH_TEXT".equals(tokenName) || 
			"RULE_RICH_TEXT_START".equals(tokenName) ||
			"RULE_RICH_TEXT_END".equals(tokenName) ||
			"RULE_RICH_TEXT_INBETWEEN".equals(tokenName) ||
			"RULE_COMMENT_RICH_TEXT_END".equals(tokenName) ||
			"RULE_COMMENT_RICH_TEXT_INBETWEEN".equals(tokenName)) {
			return XtendHighlightingConfiguration.RICH_TEXT_ID;
		}
		return super.calculateId(tokenName, tokenType);
	}
}
