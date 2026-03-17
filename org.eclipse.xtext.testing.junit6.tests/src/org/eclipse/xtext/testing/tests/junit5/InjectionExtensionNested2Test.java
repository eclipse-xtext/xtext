/*******************************************************************************
 * Copyright (c) 2020 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.testing.tests.junit5;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.xtext.testing.IInjectorProvider;
import org.eclipse.xtext.testing.IRegistryConfigurator;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * Test for {@link InjectionExtension}.
 * Injection working for nested test classes.
 * Here the @InjectWith is attached to the inner class.
 * 
 * @author Frank Benoit - Initial contribution and API
 */
@ExtendWith(InjectionExtension.class)
public class InjectionExtensionNested2Test {
	
	public static class MyInjectorProvider implements IRegistryConfigurator, IInjectorProvider {

		@Override
		public Injector getInjector() {
			return Guice.createInjector(binder -> binder.bind(String.class).toInstance(INJECTED));
		}

		@Override
		public void setupRegistry() {
		}

		@Override
		public void restoreRegistry() {
		}
	}

	private static final String NOT_INJECTED = "not-injected";
	private static final String INJECTED = "injected";
	
	@Inject 
	String testValue1 = NOT_INJECTED;
	
	@Test
	void outerTest() {
		assertEquals(NOT_INJECTED, testValue1);
	}
	
	@Nested
	@InjectWith(InjectionExtensionNested2Test.MyInjectorProvider.class)
	class NestedClass {
		
		@Inject 
		String testValue2 = "";

		@BeforeEach
		public void setUp () {
			assertEquals(NOT_INJECTED, testValue1);
			assertEquals(INJECTED, testValue2);
		}
		
		@Test
		void innerTest() {
			assertEquals(NOT_INJECTED, testValue1);
			assertEquals(INJECTED, testValue2);
		}
	}
}

