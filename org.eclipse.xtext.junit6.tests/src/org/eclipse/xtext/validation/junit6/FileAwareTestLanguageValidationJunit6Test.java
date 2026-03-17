/*******************************************************************************
 * Copyright (c) 2020 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.validation.junit6;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.eclipse.xtext.testing.util.ParseHelper;
import org.eclipse.xtext.testing.validation.ValidationTestHelper;
import org.eclipse.xtext.testlanguages.fileAware.fileAware.FileAwarePackage;
import org.eclipse.xtext.testlanguages.fileAware.fileAware.PackageDeclaration;
import org.eclipse.xtext.testlanguages.fileAware.tests.FileAwareTestLanguageInjectorProvider;
import org.eclipse.xtext.testlanguages.fileAware.validation.FileAwareTestLanguageValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Inject;

/**
 * @author cdietrich - Initial contribution and API
 */
@ExtendWith(InjectionExtension.class)
@InjectWith(FileAwareTestLanguageInjectorProvider.class)
public class FileAwareTestLanguageValidationJunit6Test {

	@Inject
	private ParseHelper<PackageDeclaration> parseHelper;

	@Inject
	private ValidationTestHelper validationTestHelper;

	@Test
	public void testWithJunit6_01() throws Exception {
		PackageDeclaration model = parseHelper.parse("package x element Y {}");
		validationTestHelper.assertNoIssues(model);
	}

	@Test
	public void testWithJunit6_02() throws Exception {
		PackageDeclaration model = parseHelper.parse("package x element LetItFail {}");
		validationTestHelper.assertWarning(model.eResource(), FileAwarePackage.Literals.ELEMENT,
				FileAwareTestLanguageValidator.INVALID_NAME);
	}

}
