/*******************************************************************************
 * Copyright (c) 2015 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.idea.findusages;

import org.eclipse.xtext.psi.PsiNamedEObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.lang.HelpID;
import com.intellij.lang.cacheBuilder.WordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;

public class BaseXtextFindUsageProvider implements FindUsagesProvider {

	@Nullable
	public WordsScanner getWordsScanner() {
		return null;
	}

	public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
		return psiElement instanceof PsiNamedEObject;
	}

	@Nullable
	public String getHelpId(@NotNull PsiElement psiElement) {
		return HelpID.FIND_OTHER_USAGES;
	}

	@NotNull
	public String getType(@NotNull PsiElement element) {
		return "";
	}

	@NotNull
	public String getDescriptiveName(@NotNull PsiElement element) {
		if (element instanceof PsiNamedElement) {
			return ((PsiNamedElement) element).getName();
		}
		return null;
	}

	@NotNull
	public String getNodeText(@NotNull PsiElement element, boolean useFullName) {
		if (element instanceof PsiNamedElement) {
			return ((PsiNamedElement) element).getName();
		} else {
			return "";
		}
	}

}