package org.eclipse.xtext.grammarinheritance.idea.lang.parser;

import org.eclipse.xtext.grammarinheritance.idea.lang.BaseInheritanceTestLanguageElementTypeProvider;
import org.eclipse.xtext.grammarinheritance.idea.lang.psi.impl.BaseInheritanceTestLanguageFileImpl;
import org.eclipse.xtext.idea.parser.AbstractXtextParserDefinition;

import com.google.inject.Inject;
import com.intellij.lang.ASTNode;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;

public class BaseInheritanceTestLanguageParserDefinition extends AbstractXtextParserDefinition {

	@Inject 
	private BaseInheritanceTestLanguageElementTypeProvider elementTypeProvider;

	@Override
	public PsiFile createFile(FileViewProvider viewProvider) {
		return new BaseInheritanceTestLanguageFileImpl(viewProvider);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public PsiElement createElement(ASTNode node) {
		return super.createElement(node);
	}

}
