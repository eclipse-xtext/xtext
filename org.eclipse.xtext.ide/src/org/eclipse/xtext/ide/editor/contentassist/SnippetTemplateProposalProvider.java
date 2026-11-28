/*******************************************************************************
 * Copyright (c) 2023 Avaloq Evolution AG (http://www.avaloq.com).
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.ide.editor.contentassist;

import org.eclipse.xtend2.lib.StringConcatenationClient;

import org.eclipse.xtext.ide.editor.contentassist.TemplateIdeContentProposalProvider.TemplateData;

import com.google.common.annotations.Beta;
import com.google.inject.Inject;

/**
 * IdeTemplateProposalProvider that converts templates to snippet content assist proposals.
 * 
 * @author Andrew Lamb - Initial contribution and API
 * 
 * @since 2.31
 */
@Beta
public class SnippetTemplateProposalProvider extends AbstractIdeTemplateProposalProvider {
	@Inject
	private TemplateVariableProcessor variableResolver;

	/**
	 * Create and accept a context assist proposal from the given template data.
	 *
	 * @param templateData
	 *            the template data to create the proposal from.
	 * @param context
	 *            the content assist context.
	 * @param acceptor
	 *            the proposal acceptor.
	 */
	public void acceptProposal(final TemplateData templateData, final ContentAssistContext context,
			final IIdeContentProposalAcceptor acceptor) {
		StringConcatenationClient template = new StringConcatenationClient() {
			@Override
			protected void appendTo(final StringConcatenationClient.TargetStringConcatenation target) {
				target.append(variableResolver.resolveAll(templateData.getPattern(), context));
			}
		};
		acceptProposal(templateData.getName(), templateData.getDescription(), template, context, acceptor);
	}

	@Override
	protected ContentAssistEntry createProposal(final StringConcatenationClient template,
			final ContentAssistContext context, final boolean adaptIndentation) {
		ContentAssistEntry entry = super.createProposal(template, context, adaptIndentation);
		entry.setKind(ContentAssistEntry.KIND_SNIPPET);
		return entry;
	}

}
