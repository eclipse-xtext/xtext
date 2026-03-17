/*******************************************************************************
 * Copyright (c) 2018, 2020 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.testing.tests.junit5;

import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * Test for {@link InjectionExtension}.
 * Copied from {@link XtextRunnerTest}.
 * 
 * @author Karsten Thoms - Initial contribution and API
 */
@InjectWith(InjectionExtensionTest.MyInjectorProvider.class)
@ExtendWith(InjectionExtension.class)
public class InjectionExtensionTest extends AbstractJUnitIntegrationTest {
	@Inject Injector injector;
	
	static class SomeExtension implements BeforeEachCallback {
		@Inject Injector injector;
		boolean didCallBeforeEach;

		@Override
		public void beforeEach(ExtensionContext context) throws Exception {
			Assertions.assertNotNull(injector);
			didCallBeforeEach = true;
		}
	}
	
	@Inject
	@RegisterExtension
	SomeExtension registeredExtension;
	
	@BeforeEach
	public void setUp () {
		Assertions.assertNotNull(injector);
	}
	
	/**
	 * Need to override to add proper {@code @Test}, otherwise no tests are found in Gradle build
	 */
	@Test
	@Override
	public void shouldSaveRegistriesBeforeCreatingAnInjector() {
		// tests are performed in MyInjectorProvider
	}
	
	@Test
	public void didUseRegisteredExtension() {
		Assertions.assertNotNull(registeredExtension);
		Assertions.assertTrue(registeredExtension.didCallBeforeEach);
	}
	
}
