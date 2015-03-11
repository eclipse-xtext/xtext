package org.eclipse.xtext.xtext.ecoreInference.idea.lang.parser;

import org.eclipse.xtext.xtext.ecoreInference.idea.lang.MultiValueFeatureTestLanguageElementTypeProvider;
import org.eclipse.xtext.xtext.ecoreInference.idea.lang.psi.impl.MultiValueFeatureTestLanguageFileImpl;
import org.eclipse.xtext.common.idea.lang.parser.TerminalsParserDefinition;

import com.google.inject.Inject;
import com.intellij.lang.ASTNode;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;

public class MultiValueFeatureTestLanguageParserDefinition extends TerminalsParserDefinition {

	@Inject 
	private MultiValueFeatureTestLanguageElementTypeProvider elementTypeProvider;

	public PsiFile createFile(FileViewProvider viewProvider) {
		return new MultiValueFeatureTestLanguageFileImpl(viewProvider);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public PsiElement createElement(ASTNode node) {
		IElementType elementType = node.getElementType();
		return super.createElement(node);
	}

}
