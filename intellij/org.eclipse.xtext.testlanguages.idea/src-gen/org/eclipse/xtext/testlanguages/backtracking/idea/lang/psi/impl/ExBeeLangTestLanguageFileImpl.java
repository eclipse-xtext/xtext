package org.eclipse.xtext.testlanguages.backtracking.idea.lang.psi.impl;

import org.eclipse.xtext.psi.impl.BaseXtextFile;
import org.eclipse.xtext.testlanguages.backtracking.idea.lang.ExBeeLangTestLanguageFileType;
import org.eclipse.xtext.testlanguages.backtracking.idea.lang.ExBeeLangTestLanguageLanguage;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;

public final class ExBeeLangTestLanguageFileImpl extends BaseXtextFile {

	public ExBeeLangTestLanguageFileImpl(FileViewProvider viewProvider) {
		super(viewProvider, ExBeeLangTestLanguageLanguage.INSTANCE);
	}

	@Override
	public FileType getFileType() {
		return ExBeeLangTestLanguageFileType.INSTANCE;
	}

}
