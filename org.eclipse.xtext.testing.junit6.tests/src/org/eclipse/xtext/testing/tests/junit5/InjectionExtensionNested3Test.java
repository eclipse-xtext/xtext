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
@InjectWith(InjectionExtensionNested3Test.MyInjectorProvider.class)
public class InjectionExtensionNested3Test {
	
	public static class MyInjectorProvider implements IInjectorProvider {
		@Override
		public Injector getInjector() {
			return Guice.createInjector(binder -> binder.bind(String.class).toInstance("OUTER"));
		}
	}
	
	public static class MyNestedInjectorProvider implements IInjectorProvider {
		@Override
		public Injector getInjector() {
			return Guice.createInjector(binder -> binder.bind(String.class).toInstance("NESTED"));
		}
	}

	@Inject 
	String testValue1;
	
	@BeforeEach
	public void setUp () {
		assertEquals("OUTER", testValue1);
	}
	
	@Test
	void outerTest() {
		assertEquals("OUTER", testValue1);
	}
	
	@Nested
	@InjectWith(InjectionExtensionNested3Test.MyNestedInjectorProvider.class)
	class NestedClass {
		
		@Inject 
		String testValue2 = "";

		@BeforeEach
		public void setUp () {
			assertEquals("OUTER", testValue1);
			assertEquals("NESTED", testValue2);
		}
		
		@Test
		void innerTest() {
			assertEquals("OUTER", testValue1);
			assertEquals("NESTED", testValue2);
		}
	}
}

