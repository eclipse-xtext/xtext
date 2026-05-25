/**
 * Copyright (c) 2018, 2020 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.xtext.ide.tests.testlanguage.ide;

import org.eclipse.xtend2.lib.StringConcatenation;
import org.eclipse.xtext.RuleCall;
import org.eclipse.xtext.ide.editor.contentassist.ContentAssistContext;
import org.eclipse.xtext.ide.editor.contentassist.IIdeContentProposalAcceptor;
import org.eclipse.xtext.ide.editor.contentassist.TemplateIdeContentProposalProvider;
import org.eclipse.xtext.ide.tests.testlanguage.services.TestLanguageGrammarAccess;

import com.google.inject.Inject;

/**
 * @author Christian Dietrich - Initial contribution and API
 */
public class TestLanguageIdeContentProposalProvider extends TemplateIdeContentProposalProvider {
	@Inject
	private TestLanguageGrammarAccess testLanguageGrammarAccess;

	@Override
	protected void _createProposals(RuleCall ruleCall, ContentAssistContext context,
			IIdeContentProposalAcceptor acceptor) {
		if (testLanguageGrammarAccess.getTypeDeclarationRule().equals(ruleCall.getRule())) {
			StringConcatenation builder = new StringConcatenation();
			builder.append("type ${1|A,B,C|} {");
			builder.newLine();
			builder.append("\t");
			builder.newLine();
			builder.append("}");
			acceptor.accept(getProposalCreator().createSnippet(builder.toString(), "Sample Snippet", context), 0);
		}
		super._createProposals(ruleCall, context, acceptor);
	}

	@Override
	protected String getTemplateFilePlugin() {
		return "org.eclipse.xtext.ide.tests";
	}
}
