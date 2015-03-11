package org.eclipse.xtext.testlanguages.backtracking.idea.lang.psi;

import com.intellij.psi.util.PsiModificationTracker;
import org.eclipse.xtext.psi.BaseXtextCodeBlockModificationListener;
import org.eclipse.xtext.testlanguages.backtracking.idea.lang.BeeLangTestLanguageLanguage;

public class BeeLangTestLanguageCodeBlockModificationListener extends BaseXtextCodeBlockModificationListener {

	public BeeLangTestLanguageCodeBlockModificationListener(PsiModificationTracker psiModificationTracker) {
		super(BeeLangTestLanguageLanguage.INSTANCE, psiModificationTracker);
	}

}
