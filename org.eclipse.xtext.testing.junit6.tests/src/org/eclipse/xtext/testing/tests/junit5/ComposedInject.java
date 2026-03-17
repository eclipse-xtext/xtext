/*******************************************************************************
 * Copyright (c) 2021 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.testing.tests.junit5;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.eclipse.xtext.testing.IInjectorProvider;
import org.eclipse.xtext.testing.InjectWith;
import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * Test for {@link InjectionExtension} meta annotation/composed annotation
 * compatibility.
 *
 * @author Michael Keppler - Initial contribution and API
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE})
@InjectWith(ComposedInject.MyInjectorProvider.class)
@ExtendWith(InjectionExtension.class)
public @interface ComposedInject {

	public static final String INJECTED = "injected";

	public static class MyInjectorProvider implements IInjectorProvider {

		@Override
		public Injector getInjector() {
			return Guice.createInjector(binder -> binder.bind(String.class).toInstance(INJECTED));
		}

	}

}
