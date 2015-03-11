package org.eclipse.xtext.idea.lang.psi.impl;

import org.eclipse.xtext.psi.impl.BaseXtextFile;
import org.eclipse.xtext.idea.lang.XtextGrammarTestLanguageFileType;
import org.eclipse.xtext.idea.lang.XtextGrammarTestLanguageLanguage;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;

public final class XtextGrammarTestLanguageFileImpl extends BaseXtextFile {

	public XtextGrammarTestLanguageFileImpl(FileViewProvider viewProvider) {
		super(viewProvider, XtextGrammarTestLanguageLanguage.INSTANCE);
	}

	public FileType getFileType() {
		return XtextGrammarTestLanguageFileType.INSTANCE;
	}

}
