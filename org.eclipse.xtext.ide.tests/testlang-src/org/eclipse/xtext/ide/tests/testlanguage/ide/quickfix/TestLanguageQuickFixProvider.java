/*******************************************************************************
 * Copyright (c) 2016, 2022 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.ide.tests.testlanguage.ide.quickfix;

import org.eclipse.xtext.ide.editor.quickfix.AbstractDeclarativeIdeQuickfixProvider;
import org.eclipse.xtext.ide.editor.quickfix.DiagnosticResolutionAcceptor;
import org.eclipse.xtext.ide.editor.quickfix.QuickFix;
import org.eclipse.xtext.ide.tests.testlanguage.testLanguage.TypeDeclaration;
import org.eclipse.xtext.ide.tests.testlanguage.validation.TestLanguageValidator;
import org.eclipse.xtext.xbase.lib.StringExtensions;

import jakarta.inject.Singleton;

@Singleton
public class TestLanguageQuickFixProvider extends AbstractDeclarativeIdeQuickfixProvider {

	public static String EMF_QF_LABEL = "Change element name to first upper using object modification";
	public static String TEXT_QF_LABEL = "Change element name to first upper using text replacement";

	private String fixedName(TypeDeclaration declaration) {
		return StringExtensions.toFirstUpper(declaration.getName());
	}

	@QuickFix(TestLanguageValidator.INVALID_NAME)
	public void fixLowerCaseName(DiagnosticResolutionAcceptor acceptor) {
		acceptor.accept(EMF_QF_LABEL, obj -> {
			TypeDeclaration declaration = (TypeDeclaration) obj;
			declaration.setName(fixedName(declaration));
		});
	}
	
	@QuickFix(TestLanguageValidator.INVALID_NAME)
	public void textFixLowerCaseName(DiagnosticResolutionAcceptor acceptor) {
		acceptor.accept(TEXT_QF_LABEL,  (diagnostic, obj, document) -> 
			createTextEdit(diagnostic, "type " + fixedName((TypeDeclaration) obj) + " {\n}")
		);
	}
}
