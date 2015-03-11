package org.eclipse.xtext.testlanguages.backtracking.idea.lang.psi;

import com.intellij.psi.util.PsiModificationTracker;
import org.eclipse.xtext.psi.BaseXtextCodeBlockModificationListener;
import org.eclipse.xtext.testlanguages.backtracking.idea.lang.ExBeeLangTestLanguageLanguage;

public class ExBeeLangTestLanguageCodeBlockModificationListener extends BaseXtextCodeBlockModificationListener {

	public ExBeeLangTestLanguageCodeBlockModificationListener(PsiModificationTracker psiModificationTracker) {
		super(ExBeeLangTestLanguageLanguage.INSTANCE, psiModificationTracker);
	}

}
