/*******************************************************************************
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.idea.tests

import org.eclipse.xtend.lib.macro.AbstractClassProcessor
import org.eclipse.xtend.lib.macro.Active
import org.eclipse.xtend.lib.macro.TransformationContext
import org.eclipse.xtend.lib.macro.declaration.MutableClassDeclaration

@Active(TestDecoratorProcessor)
annotation TestDecorator {
}

class TestDecoratorProcessor extends AbstractClassProcessor {

	override doTransform(MutableClassDeclaration it, extension TransformationContext context) {
		val delegate = findDeclaredField('delegate')
		if (delegate == null) {
			addWarning("Delegate is not declared")
		}
		delegate.markAsRead
		for (declaredMethod : delegate.type.allResolvedMethods.map[declaration].filter [
			simpleName.startsWith('test')
		]) {
			addMethod(declaredMethod.simpleName) [
				body = '''delegate.�declaredMethod.simpleName�();'''
				exceptions = declaredMethod.exceptions
			]
		}
	}

}
