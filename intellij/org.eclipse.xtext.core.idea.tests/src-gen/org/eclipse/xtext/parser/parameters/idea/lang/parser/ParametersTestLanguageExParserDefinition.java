package org.eclipse.xtext.parser.parameters.idea.lang.parser;

import org.eclipse.xtext.parser.parameters.idea.lang.ParametersTestLanguageExElementTypeProvider;
import org.eclipse.xtext.parser.parameters.idea.lang.psi.impl.ParametersTestLanguageExFileImpl;
import org.eclipse.xtext.idea.parser.AbstractXtextParserDefinition;

import com.google.inject.Inject;
import com.intellij.lang.ASTNode;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;

public class ParametersTestLanguageExParserDefinition extends AbstractXtextParserDefinition {

	@Inject 
	private ParametersTestLanguageExElementTypeProvider elementTypeProvider;

	@Override
	public PsiFile createFile(FileViewProvider viewProvider) {
		return new ParametersTestLanguageExFileImpl(viewProvider);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public PsiElement createElement(ASTNode node) {
		IElementType elementType = node.getElementType();
		return super.createElement(node);
	}

}
