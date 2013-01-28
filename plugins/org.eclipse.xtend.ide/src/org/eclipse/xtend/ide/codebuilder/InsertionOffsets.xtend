/*******************************************************************************
 * Copyright (c) 2013 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtend.ide.codebuilder

import org.eclipse.emf.ecore.EObject
import org.eclipse.jdt.annotation.NonNullByDefault
import org.eclipse.jdt.annotation.Nullable
import org.eclipse.xtend.core.xtend.XtendConstructor
import org.eclipse.xtend.core.xtend.XtendField
import org.eclipse.xtend.core.xtend.XtendMember
import org.eclipse.xtend.core.xtend.XtendTypeDeclaration
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.nodemodel.util.NodeModelUtils

/**
 * Calculates where to insert new members into exisitng Xtend code.
 * 
 * @author Jan Koehnlein
 */
@NonNullByDefault
class InsertionOffsets {

	def getNewTypeInsertOffset(@Nullable EObject call, XtendTypeDeclaration ownerType) {
		after(ownerType)
	}

	def getNewFieldInsertOffset(@Nullable EObject call, XtendTypeDeclaration ownerType) {
		if (ownerType.members.empty)
			return inEmpty(ownerType)
		val lastDefinedField = ownerType.members.filter(typeof(XtendField)).last
		if (lastDefinedField == null)
			return before(ownerType.members.head)
		else
			return after(lastDefinedField)
	}

	def getNewMethodInsertOffset(@Nullable EObject call, XtendTypeDeclaration ownerType) {
		val callingMember = EcoreUtil2::getContainerOfType(call, typeof(XtendMember))
		if (callingMember != null && ownerType.members.contains(callingMember))
			return after(callingMember)
		else if (ownerType.members.empty)
			return inEmpty(ownerType)
		else
			return after(ownerType.members.last)
	}

	def getNewConstructorInsertOffset(@Nullable EObject call, XtendTypeDeclaration ownerType) {
		val lastDefinedConstructor = ownerType.members.filter(typeof(XtendConstructor)).last
		if(lastDefinedConstructor == null)
			return getNewFieldInsertOffset(call, ownerType)		
		else	
			return after(lastDefinedConstructor)
	}

	def protected before(EObject element) {
		NodeModelUtils::findActualNodeFor(element).offset
	}

	def protected after(EObject element) {
		val node = NodeModelUtils::findActualNodeFor(element)
		node.offset + node.length
	}
	
	def protected inEmpty(XtendTypeDeclaration ownerType) {
		val classNode = NodeModelUtils::findActualNodeFor(ownerType)
		val openingBraceNode = classNode.leafNodes.findFirst[text == "{"]
		if(openingBraceNode != null)
			openingBraceNode.offset + 1
		else 
			classNode.offset + classNode.length
	}
}
