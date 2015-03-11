package org.eclipse.xtext.parser.antlr.idea.lang.parser;

import org.eclipse.xtext.parser.antlr.idea.lang.Bug443221TestLanguageElementTypeProvider;
import org.eclipse.xtext.parser.antlr.idea.lang.psi.impl.Bug443221TestLanguageFileImpl;
import org.eclipse.xtext.common.idea.lang.parser.TerminalsParserDefinition;

import com.google.inject.Inject;
import com.intellij.lang.ASTNode;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;

public class Bug443221TestLanguageParserDefinition extends TerminalsParserDefinition {

	@Inject 
	private Bug443221TestLanguageElementTypeProvider elementTypeProvider;

	public PsiFile createFile(FileViewProvider viewProvider) {
		return new Bug443221TestLanguageFileImpl(viewProvider);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public PsiElement createElement(ASTNode node) {
		IElementType elementType = node.getElementType();
		return super.createElement(node);
	}

}
