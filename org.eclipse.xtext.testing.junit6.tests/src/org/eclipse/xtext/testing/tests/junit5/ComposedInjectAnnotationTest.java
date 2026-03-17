/*******************************************************************************
 * Copyright (c) 2021 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtext.testing.tests.junit5;

import org.eclipse.xtext.testing.extensions.InjectionExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.inject.Inject;

/**
 * Test for {@link InjectionExtension} meta annotation/composed annotation
 * compatibility. It uses a composed annotation instead of the original
 * {@code InjectWith} annotation.
 *
 * @author Michael Keppler - Initial contribution and API
 */
@ComposedInject
public class ComposedInjectAnnotationTest {
	@Inject
	String toBeInjected = "";

	@Test
	public void didUseComposedInjectAnnotation() {
		Assertions.assertEquals(ComposedInject.INJECTED, toBeInjected);
	}

}
