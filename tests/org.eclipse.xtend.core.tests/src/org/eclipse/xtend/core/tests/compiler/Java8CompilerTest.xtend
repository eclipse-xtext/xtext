/*******************************************************************************
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtend.core.tests.compiler

import org.eclipse.xtend.core.tests.Java8RuntimeInjectorProvider
import org.eclipse.xtext.junit4.InjectWith
import org.junit.Test

/**
 * @author Miro Spoenemann - Initial contribution and API
 */
@InjectWith(Java8RuntimeInjectorProvider)
class Java8CompilerTest extends AbstractXtendCompilerTest {
	
	@Test def void testStaticMethodInInterface() {
		'''
		interface Foo {
			static def bar() {
				"bar!"
			}
		}
		'''
		.assertCompilesTo('''
		@SuppressWarnings("all")
		public interface Foo {
		  public static String bar() {
		    return "bar!";
		  }
		}
		''')
	}
	
}