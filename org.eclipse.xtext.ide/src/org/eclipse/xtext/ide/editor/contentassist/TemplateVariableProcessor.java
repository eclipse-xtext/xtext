/*******************************************************************************
 * Copyright (c) 2023 Avaloq Evolution AG (http://www.avaloq.com).
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.ide.editor.contentassist;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.annotations.Beta;

/**
 * Simple framework for providing server-side variable values for turning templates into snippets. Variables take one of
 * the following forms in a template: $var, ${var}, or ${var:default}.
 * 
 * @author Andrew Lamb - Initial contribution and API
 * 
 * @since 2.31
 */
@Beta
public class TemplateVariableProcessor {
	private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$([_a-zA-Z]\\w*|\\{[_a-zA-Z][^\\}]*\\})"); //$NON-NLS-1$
	private static final String SELECTION_VARIABLE = "selection"; //$NON-NLS-1$

	/**
	 * Replace all server-side variable references in the given pattern with their current values.
	 *
	 * @param pattern
	 *            the pattern containing variable references to resolve.
	 * @param context
	 *            the content assist context that may be used during the evaluation of any variables.
	 * @return the pattern with the server-side variable references replaced by their values.
	 */
	public String resolveAll(final String pattern, final ContentAssistContext context) {
		Matcher matcher = VARIABLE_PATTERN.matcher(pattern);
		StringBuffer buffer = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(buffer, Matcher.quoteReplacement(getVariableValue(matcher.group(), context)));
		}
		matcher.appendTail(buffer);
		return buffer.toString();
	}

	/**
	 * Get the value of the matched variable reference.
	 *
	 * @param matchedVariable
	 *            the text that matches the variable reference pattern.
	 * @param context
	 *            the content assist context that may be used during the evaluation of the variable.
	 * @return the server-side value of the matched variable or it's default value if the variable is undefined.
	 */
	protected String getVariableValue(final String matchedVariable, final ContentAssistContext context) {
		String variableName = getVariableName(matchedVariable);

		// built-in 'selection' variable
		if (SELECTION_VARIABLE.equals(variableName)) {
			String selection = context.getSelectedText();
			if (selection != null) {
				return selection;
			}
			return getVariableDefault(matchedVariable);
		}

		// TODO: would be nice to see something like the jface variable resolvers go here.

		// For now, assume all other variables are evaluated on the client, so leave them untouched.
		return matchedVariable;
	}

	/**
	 * Extract the variable name from the matched variable reference.
	 *
	 * @param matchedVariable
	 *            the text that matches the variable reference pattern.
	 * @return the extracted variable name.
	 */
	protected String getVariableName(final String matchedVariable) {
		if (matchedVariable.charAt(1) == '{') {
			int nameEnd = matchedVariable.indexOf(':');
			if (nameEnd < 0) {
				nameEnd = matchedVariable.length() - 1;
			}
			return matchedVariable.substring(2, nameEnd);
		}
		return matchedVariable.substring(1);
	}

	/**
	 * Extract the variable default value from the matched variable reference.
	 *
	 * @param matchedVariable
	 *            the text that matches the variable reference pattern.
	 * @return the extracted default value.
	 */
	protected String getVariableDefault(final String matchedVariable) {
		if (matchedVariable.charAt(1) == '{') {
			int defaultStart = matchedVariable.indexOf(':') + 1;
			if (defaultStart > 0) {
				return matchedVariable.substring(defaultStart, matchedVariable.length() - 1);
			}
		}
		return ""; //$NON-NLS-1$
	}
}
