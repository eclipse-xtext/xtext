package org.eclipse.xtext.parser.keywords.idea.lang.parser;

import org.eclipse.xtext.parser.keywords.idea.lang.KeywordsTestLanguageElementTypeProvider;
import org.eclipse.xtext.parser.keywords.idea.lang.psi.impl.KeywordsTestLanguageFileImpl;
import org.eclipse.xtext.idea.parser.AbstractXtextParserDefinition;

import com.google.inject.Inject;
import com.intellij.lang.ASTNode;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;

public class KeywordsTestLanguageParserDefinition extends AbstractXtextParserDefinition {

	@Inject 
	private KeywordsTestLanguageElementTypeProvider elementTypeProvider;

	@Override
	public PsiFile createFile(FileViewProvider viewProvider) {
		return new KeywordsTestLanguageFileImpl(viewProvider);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public PsiElement createElement(ASTNode node) {
		return super.createElement(node);
	}

}
