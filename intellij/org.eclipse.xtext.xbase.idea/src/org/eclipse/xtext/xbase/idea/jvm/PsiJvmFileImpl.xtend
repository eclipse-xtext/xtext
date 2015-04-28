/*******************************************************************************
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.xbase.idea.jvm

import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.PsiJavaFileBaseImpl
import com.intellij.psi.tree.IElementType
import java.util.Map
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtend.lib.annotations.Accessors

/**
 * @author kosyakov - Initial contribution and API
 */
class PsiJvmFileImpl extends PsiJavaFileBaseImpl {

	@Accessors
	Map<EObject, PsiElement> mapping

	new(IElementType elementType, IElementType contentElementType, FileViewProvider viewProvider) {
		super(elementType, contentElementType, viewProvider)
	}

	override isPhysical() {
		// Generated Java code has to be marked as physical in order to make Java services work
		// downside: IntelliJ tries to modify these
		true
	}

	override getFileType() {
		JvmFileType.INSTANCE
	}

	override getLanguage() {
		JvmLanguage.INSTANCE
	}

	override toString() {
		'PsiJvmFile:' + name
	}

}