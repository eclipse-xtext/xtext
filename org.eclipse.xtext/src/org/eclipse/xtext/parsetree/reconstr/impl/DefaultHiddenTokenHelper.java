/*******************************************************************************
 * Copyright (c) 2010, 2016 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.parsetree.reconstr.impl;

import org.eclipse.xtext.AbstractRule;
import org.eclipse.xtext.ParserRule;
import org.eclipse.xtext.xtext.RuleNames;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Moritz Eysholdt - Initial contribution and API
 */
@Singleton
public class DefaultHiddenTokenHelper extends AbstractHiddenTokenHelper {

	private AbstractRule wsRule;

	@Override
	public boolean isWhitespace(AbstractRule rule) {
		return rule != null && "WS".equals(rule.getName());
	}

	@Override
	public boolean isComment(AbstractRule rule) {
		return rule != null && ("ML_COMMENT".equals(rule.getName()) || "SL_COMMENT".equals(rule.getName()));
	}

	@Override
	public AbstractRule getWhitespaceRuleFor(String whitespace) {
		return wsRule;
	}

	@Override
	public AbstractRule getWhitespaceRuleFor(ParserRule context, String whitespace) {
		if (context == null || !context.isDefinesHiddenTokens())
			return wsRule;
		if (context.getHiddenTokens().contains(wsRule))
			return wsRule;
		return null;
	}

	@Inject
	private void setGrammar(RuleNames names) {
		wsRule = Iterables.getFirst(names.getRulesBySimpleName("WS"), null);
	}

}
