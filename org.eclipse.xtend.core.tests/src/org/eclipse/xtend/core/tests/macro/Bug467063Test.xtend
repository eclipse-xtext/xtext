/*******************************************************************************
 * Copyright (c) 2017 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtend.core.tests.macro

import org.eclipse.xtend.lib.macro.AbstractClassProcessor
import org.eclipse.xtend.lib.macro.Active
import org.eclipse.xtend.lib.macro.RegisterGlobalsContext
import org.eclipse.xtend.lib.macro.declaration.ClassDeclaration
import org.junit.Test
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration
import org.eclipse.xtend.lib.macro.TransformationContext
import org.eclipse.xtend.lib.macro.declaration.MutableInterfaceDeclaration

/**
 * @author Christian Dietrich - Initial contribution and API
 */
class Bug467063Test extends AbstractActiveAnnotationTest {
	
	@Test def void testAddInterfaceWithDefaultMethod() {
		'''
			@org.eclipse.xtend.core.tests.macro.AddInterfaceWithDefault class Foo {}
		'''.assertCompilesTo('''
			MULTIPLE FILES WERE GENERATED
			
			File 1 : /myProject/xtend-gen/Foo.java
			
			import org.eclipse.xtend.core.tests.macro.AddInterfaceWithDefault;
			
			@AddInterfaceWithDefault
			@SuppressWarnings("all")
			public class Foo {
			}
			
			File 2 : /myProject/xtend-gen/de/test/Test.java
			
			package de.test;
			
			@SuppressWarnings("all")
			public interface Test {
			  default void sayHello() {
			    System.out.println("Hello World");
			  }
			}

		''')
	}
	
}

@Active(typeof(AddInterfaceWithDefaultProcessor))
annotation AddInterfaceWithDefault {}

class AddInterfaceWithDefaultProcessor extends AbstractClassProcessor {
	
	override doRegisterGlobals(ClassDeclaration annotatedClass, RegisterGlobalsContext context) {
		context.registerInterface('de.test.Test')
	}
	
	override doTransform(MutableClassDeclaration annotatedClass, extension TransformationContext context) {
		super.doTransform(annotatedClass, context)
		val ifType = 'de.test.Test'.findTypeGlobally as MutableInterfaceDeclaration
		ifType.addMethod("sayHello") [
			body = '''System.out.println("Hello World");'''
			^default = true
		]
	}
	
}