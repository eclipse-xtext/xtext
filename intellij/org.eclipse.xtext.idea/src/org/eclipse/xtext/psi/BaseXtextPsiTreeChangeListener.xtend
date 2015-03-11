/*******************************************************************************
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.psi

import com.intellij.psi.impl.PsiModificationTrackerImpl
import com.intellij.psi.impl.PsiTreeChangeEventImpl
import com.intellij.psi.impl.PsiTreeChangePreprocessor
import com.intellij.psi.util.PsiModificationTracker
import org.eclipse.xtend.lib.annotations.Accessors
import org.eclipse.xtext.idea.lang.IXtextLanguage

class BaseXtextCodeBlockModificationListener implements PsiTreeChangePreprocessor {

	@Accessors(PROTECTED_GETTER)
	val IXtextLanguage language

	@Accessors(PROTECTED_GETTER)
	val PsiModificationTrackerImpl psiModificationTracker

	new(IXtextLanguage language, PsiModificationTracker psiModificationTracker) {
		this.language = language
		language.injectMembers(this)

		this.psiModificationTracker = psiModificationTracker as PsiModificationTrackerImpl
	}

	override treeChanged(PsiTreeChangeEventImpl event) {
		if (event.file?.language == language && event.hasStructuralChanges) {
			if (event.hasJavaStructuralChanges) {
				psiModificationTracker.incCounter
			} else {
				psiModificationTracker.incOutOfCodeBlockModificationCounter
			}
		}
	}

	protected def boolean hasStructuralChanges(PsiTreeChangeEventImpl it) {
		switch code {
			case CHILD_ADDED,
			case CHILD_REMOVED,
			case CHILD_REPLACED,
			case CHILD_MOVED,
			case CHILDREN_CHANGED,
			case PROPERTY_CHANGED: genericChange
			default: false
		}
	}

	protected def boolean hasJavaStructuralChanges(PsiTreeChangeEventImpl event) {
		false
	}

}
