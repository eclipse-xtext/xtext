/*******************************************************************************
 * Copyright (c) 2023 itemis AG (http://www.itemis.eu) and others.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.xtend.core.tests.macro

import java.io.ByteArrayOutputStream
import java.lang.annotation.Target
import java.util.List
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtend.lib.macro.AbstractClassProcessor
import org.eclipse.xtend.lib.macro.Active
import org.eclipse.xtend.lib.macro.RegisterGlobalsContext
import org.eclipse.xtend.lib.macro.TransformationContext
import org.eclipse.xtend.lib.macro.declaration.ClassDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration
import org.eclipse.xtend.lib.macro.declaration.MutableFieldDeclaration
import org.eclipse.xtext.resource.persistence.StorageAwareResource
import org.eclipse.xtext.xbase.resource.BatchLinkableResourceStorageWritable
import org.junit.Test

/**
 * @author Didier Vojtisek - Initial contribution and API
 */
class AnnotationPropertyRemoveTest extends AbstractActiveAnnotationTest {

	@Test def void testAnnotationPropertyRemove_01() {
		'''
			@org.eclipse.xtend.core.tests.macro.RemoveUnderscoreAnnotation()
			class TestClass {
				public String foo
				
				public String _ba 
			}
		'''.assertCompilesTo('''
			import org.eclipse.xtend.core.tests.macro.RemoveUnderscoreAnnotation;
			
			@RemoveUnderscoreAnnotation
			@SuppressWarnings("all")
			public class TestClass {
			  public String foo;
			}
		''')
	}

	@Test def void testresourceStorageWrite() {
		val contents = '''
			@org.eclipse.xtend.core.tests.macro.RemoveUnderscoreAnnotation()
			class AnnotationPropertyRemoveTestClass {
				public String foo
				
				public String _ba 
			}
		'''

		val file = file(contents)
		new BatchLinkableResourceStorageWritable(new ByteArrayOutputStream, false) {
			override String getFragment(EObject obj) {
				assertTrue(obj.toString + " is not contained in any resource ", obj.eResource() !== null)
				return super.getFragment(obj)
			}
		}.writeResource(file.eResource as StorageAwareResource)

	}
}

@Active(RemoveUnderscoreAnnotationProcessor)
@Target(TYPE)
annotation RemoveUnderscoreAnnotation {
}

class RemoveUnderscoreAnnotationProcessor extends AbstractClassProcessor {

	override doRegisterGlobals(ClassDeclaration annotatedClass, extension RegisterGlobalsContext context) {
		super.doRegisterGlobals(annotatedClass, context)
	}

	override doTransform(MutableClassDeclaration annotatedClass, extension TransformationContext context) {
		super.doTransform(annotatedClass, context)
		val List<MutableFieldDeclaration> toRemove = newArrayList
		// remove fields starting with "_"
		for (f : annotatedClass.declaredFields) {
			if (f.simpleName.startsWith("_")) {
				toRemove.add(f)
			}
		}
		toRemove.forEach[f|f.remove]
	}

}
