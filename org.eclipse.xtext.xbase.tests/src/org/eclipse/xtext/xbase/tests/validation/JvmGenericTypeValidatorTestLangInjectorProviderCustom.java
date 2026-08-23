/*******************************************************************************
 * Copyright (c) 2024 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.xbase.tests.validation;

import org.eclipse.xtext.xbase.testlanguages.JvmGenericTypeValidatorTestLangStandaloneSetup;
import org.eclipse.xtext.xbase.testlanguages.tests.JvmGenericTypeValidatorTestLangInjectorProvider;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * To let the validator/scoping/linking find the Java classes of "testdata" referred in some tests also in an OSGI and Maven/Tycho
 * environment. This is required because the original {@link JvmGenericTypeValidatorTestLangInjectorProvider} is not in this bundle.
 *
 * @author Lorenzo Bettini - Initial contribution and API
 */
public class JvmGenericTypeValidatorTestLangInjectorProviderCustom extends JvmGenericTypeValidatorTestLangInjectorProvider {

	@Override
	protected Injector internalCreateInjector() {
		return new JvmGenericTypeValidatorTestLangStandaloneSetup() {
			@Override
			public Injector createInjector() {
				return Guice.createInjector(createRuntimeModule());
			}
		}.createInjectorAndDoEMFRegistration();
	}

}
