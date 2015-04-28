package org.eclipse.xtext.generator.ecore.idea.lang.parser;

import org.eclipse.xtext.generator.ecore.idea.lang.SuperTestLanguageElementTypeProvider;
import org.eclipse.xtext.generator.ecore.idea.lang.psi.impl.SuperTestLanguageFileImpl;
import org.eclipse.xtext.idea.parser.AbstractXtextParserDefinition;

import com.google.inject.Inject;
import com.intellij.lang.ASTNode;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;

public class SuperTestLanguageParserDefinition extends AbstractXtextParserDefinition {

	@Inject 
	private SuperTestLanguageElementTypeProvider elementTypeProvider;

	@Override
	public PsiFile createFile(FileViewProvider viewProvider) {
		return new SuperTestLanguageFileImpl(viewProvider);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public PsiElement createElement(ASTNode node) {
		return super.createElement(node);
	}

}
