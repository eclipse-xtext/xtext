/*******************************************************************************
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.xbase.idea.completion

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.completion.JavaClassNameCompletionContributor
import com.intellij.codeInsight.completion.JavaCompletionSorting
import com.intellij.codeInsight.completion.JavaPsiClassReferenceElement
import com.intellij.psi.PsiModifier
import org.eclipse.emf.ecore.EReference
import org.eclipse.xtext.common.types.TypesPackage
import org.eclipse.xtext.idea.lang.AbstractXtextLanguage
import org.eclipse.xtext.xbase.XbasePackage
import org.eclipse.xtext.xtype.XtypePackage

import static com.intellij.patterns.PlatformPatterns.*

class XbaseCompletionContributor extends XtypeCompletionContributor {

	new(AbstractXtextLanguage lang) {
		super(lang)
		completeJvmParameterizedTypeReference_Type
		completeXImportDeclaration_ImportedType
		completeXConstructorCall_Constructor
		completeXTypeLiteral_Type
	}

	protected def completeJvmParameterizedTypeReference_Type() {
		completeJavaTypes(TypesPackage.Literals.JVM_PARAMETERIZED_TYPE_REFERENCE__TYPE)
	}

	protected def completeXImportDeclaration_ImportedType() {
		completeJavaTypes(XtypePackage.Literals.XIMPORT_DECLARATION__IMPORTED_TYPE)
	}

	protected def completeXConstructorCall_Constructor() {
		completeJavaTypes(XbasePackage.Literals.XCONSTRUCTOR_CALL__CONSTRUCTOR) [
			val type = object
			!type.hasModifierProperty(PsiModifier.ABSTRACT) && !type.interface
		]
	}

	protected def completeXTypeLiteral_Type() {
		completeJavaTypes(XbasePackage.Literals.XTYPE_LITERAL__TYPE)
	}

	protected def completeJavaTypes(EReference reference) {
		completeJavaTypes(reference)[true]
	}

	protected def completeJavaTypes(EReference reference, (JavaPsiClassReferenceElement)=>boolean filter) {
		extend(CompletionType.BASIC, psiElement.withEReference(reference)) [
			completeJavaTypes($0, $2, filter)
		]
	}

	protected def completeJavaTypes(
		CompletionParameters completionParameters,
		CompletionResultSet completionResultSet,
		(JavaPsiClassReferenceElement)=>boolean filter
	) {
		JavaClassNameCompletionContributor.addAllClasses(
			completionParameters,
			completionParameters.invocationCount <= 2,
			JavaCompletionSorting.addJavaSorting(completionParameters, completionResultSet).prefixMatcher
		) [
			if (it instanceof JavaPsiClassReferenceElement) {
				if (filter.apply(it)) {
					completionResultSet.addElement(it)
				}
			}
		]
	}

}


